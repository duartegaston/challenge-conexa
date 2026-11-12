package com.conexa.challenge.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conexa.challenge.support.AbstractIntegrationTest;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

class AuthIntegrationTest extends AbstractIntegrationTest {

    @Test
    void loginSucceedsWithSeedUser() throws Exception {
        login("user", "user123")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.roles[0]").value("USER"));
    }

    @Test
    void adminHasBothRoles() throws Exception {
        login("admin", "admin123")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("ADMIN"))
                .andExpect(jsonPath("$.roles[1]").value("USER"));
    }

    @Test
    void usernameIsCaseInsensitiveOnLogin() throws Exception {
        login("USER", "user123").andExpect(status().isOk());
    }

    @Test
    void wrongPasswordAndUnknownUserGetTheSameProblem() throws Exception {
        for (ResultActions attempt : new ResultActions[] {login("user", "wrong"), login("ghost", "whatever")}) {
            attempt.andExpect(status().isUnauthorized())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.detail").value("Invalid username or password"));
        }
    }

    @Test
    void blankCredentialsAreRejectedWithFieldErrors() throws Exception {
        login("", "")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Validation failed"))
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void oversizedPasswordIsRejected() throws Exception {
        login("user", "x".repeat(73))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void malformedJsonIsBadRequestProblem() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void meReturnsUsernameAndRoles() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me").header(HttpHeaders.AUTHORIZATION, adminBearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.roles[0]").value("ADMIN"))
                .andExpect(jsonPath("$.roles[1]").value("USER"));
    }

    @Test
    void meWithoutTokenIsUnauthorizedProblem() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void invalidBearerTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerCreatesUserThatCanLoginAndCallTheApi() throws Exception {
        String username = uniqueUsername();

        register(username, "s3cret-pass")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.roles[0]").value("USER"))
                .andExpect(jsonPath("$.roles.length()").value(1));

        mockMvc.perform(get("/api/v1/auth/me").header(HttpHeaders.AUTHORIZATION, bearer(username, "s3cret-pass")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username));
    }

    @Test
    void registerNormalizesUsernameAndRejectsDuplicatesIgnoringCase() throws Exception {
        String username = uniqueUsername();
        register(username, "s3cret-pass").andExpect(status().isCreated());

        register(username.toUpperCase(), "another-pass")
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value(containsString("already taken")));
    }

    @Test
    void registerCannotEscalateToAdmin() throws Exception {
        String username = uniqueUsername();
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", username, "password", "s3cret-pass", "roles", new String[] {"ADMIN"}))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roles.length()").value(1))
                .andExpect(jsonPath("$.roles[0]").value("USER"));
    }

    @Test
    void registerValidatesUsernameAndPassword() throws Exception {
        register("a b", "short")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    private ResultActions login(String username, String password) throws Exception {
        return postCredentials("/api/v1/auth/login", username, password);
    }

    private ResultActions register(String username, String password) throws Exception {
        return postCredentials("/api/v1/auth/register", username, password);
    }

    private ResultActions postCredentials(String path, String username, String password) throws Exception {
        return mockMvc.perform(post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("username", username, "password", password))));
    }

    private static String uniqueUsername() {
        return "new-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
