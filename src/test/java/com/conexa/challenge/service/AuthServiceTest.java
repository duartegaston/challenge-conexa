package com.conexa.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.conexa.challenge.exception.InvalidCredentialsException;
import com.conexa.challenge.exception.UsernameAlreadyExistsException;
import com.conexa.challenge.model.Role;
import com.conexa.challenge.model.UserEntity;
import com.conexa.challenge.model.dto.LoginRequest;
import com.conexa.challenge.model.dto.LoginResponse;
import com.conexa.challenge.model.dto.MeResponse;
import com.conexa.challenge.model.dto.RegisterRequest;
import com.conexa.challenge.repository.UserRepository;
import com.conexa.challenge.security.JwtService;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginIssuesTokenWithRolesFromTheAuthentication() {
        var authenticated = UsernamePasswordAuthenticationToken.authenticated(
                "admin",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(authenticationManager.authenticate(any())).thenReturn(authenticated);
        when(jwtService.generateToken("admin", Set.of(Role.USER, Role.ADMIN))).thenReturn("jwt-token");
        when(jwtService.expiresInSeconds()).thenReturn(3600L);

        LoginResponse response = authService.login(new LoginRequest("  Admin ", "admin123"));

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(3600L);
        assertThat(response.roles()).containsExactly("ADMIN", "USER");

        ArgumentCaptor<UsernamePasswordAuthenticationToken> request = forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(request.capture());
        assertThat(request.getValue().getName()).isEqualTo("admin");
    }

    @Test
    void loginFailureHidesTheCause() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("user", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage(InvalidCredentialsException.MESSAGE);
    }

    @Test
    void registerStoresHashedPasswordWithUserRoleOnly() {
        when(userRepository.existsByUsername("luke")).thenReturn(false);
        when(passwordEncoder.encode("s3cret-pass")).thenReturn("hashed");
        when(userRepository.saveAndFlush(any(UserEntity.class))).thenAnswer(call -> call.getArgument(0));

        MeResponse response = authService.register(new RegisterRequest("Luke", "s3cret-pass"));

        ArgumentCaptor<UserEntity> saved = forClass(UserEntity.class);
        verify(userRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getUsername()).isEqualTo("luke");
        assertThat(saved.getValue().getPasswordHash()).isEqualTo("hashed");
        assertThat(saved.getValue().getRoles()).containsExactly(Role.USER);
        assertThat(response).isEqualTo(new MeResponse("luke", List.of("USER")));
    }

    @Test
    void registerRejectsExistingUsername() {
        when(userRepository.existsByUsername("luke")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest("luke", "s3cret-pass")))
                .isInstanceOf(UsernameAlreadyExistsException.class);
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void registerTranslatesUniqueConstraintRaceIntoConflict() {
        when(userRepository.existsByUsername("luke")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> authService.register(new RegisterRequest("luke", "s3cret-pass")))
                .isInstanceOf(UsernameAlreadyExistsException.class);
    }

    @Test
    void meReturnsNameAndSortedRolesWithoutPrefix() {
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                "admin",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("ROLE_ADMIN")));

        assertThat(authService.me(authentication)).isEqualTo(new MeResponse("admin", List.of("ADMIN", "USER")));
    }
}
