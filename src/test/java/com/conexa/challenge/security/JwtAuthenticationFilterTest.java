package com.conexa.challenge.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.conexa.challenge.config.JwtProperties;
import com.conexa.challenge.model.Role;
import java.time.Duration;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthenticationFilterTest {

    private final JwtService jwtService = new JwtService(
            new JwtProperties("filter-test-secret-must-be-at-least-32-chars!!", Duration.ofMinutes(5)));
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validTokenAuthenticatesWithRoleAuthorities() throws Exception {
        String token = jwtService.generateToken("luke", Set.of(Role.USER, Role.ADMIN));

        MockFilterChain chain = run("Bearer " + token);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication.getName()).isEqualTo("luke");
        assertThat(authentication.getAuthorities()).extracting(Object::toString)
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void missingHeaderLeavesRequestAnonymousButContinuesTheChain() throws Exception {
        MockFilterChain chain = run(null);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void nonBearerSchemeIsIgnored() throws Exception {
        run("Basic dXNlcjp1c2Vy");

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void emptyAndInvalidTokensAreIgnored() throws Exception {
        MockFilterChain emptyChain = run("Bearer ");
        MockFilterChain invalidChain = run("Bearer garbage");

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(emptyChain.getRequest()).isNotNull();
        assertThat(invalidChain.getRequest()).isNotNull();
    }

    @Test
    void expiredTokenIsIgnored() throws Exception {
        var expiring = new JwtService(
                new JwtProperties("filter-test-secret-must-be-at-least-32-chars!!", Duration.ofSeconds(-5)));

        run("Bearer " + expiring.generateToken("luke", Set.of(Role.USER)));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    private MockFilterChain run(String authorization) throws Exception {
        SecurityContextHolder.clearContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (authorization != null) {
            request.addHeader(HttpHeaders.AUTHORIZATION, authorization);
        }
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, new MockHttpServletResponse(), chain);
        return chain;
    }
}
