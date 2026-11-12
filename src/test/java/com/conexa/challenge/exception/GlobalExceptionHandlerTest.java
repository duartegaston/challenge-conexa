package com.conexa.challenge.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Mapeo de las excepciones de dominio a problem+json. El mapeo de validación y de las excepciones de Spring MVC
 * se ejercita de punta a punta en los tests de integración.
 */
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ProbeController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void invalidCredentialsMapsTo401() throws Exception {
        expectProblem("/probe/credentials", 401, "Invalid username or password");
    }

    @Test
    void usernameTakenMapsTo409() throws Exception {
        expectProblem("/probe/taken", 409, "Username 'luke' is already taken");
    }

    @Test
    void resourceNotFoundMapsTo404() throws Exception {
        expectProblem("/probe/missing", 404, "people with id 0 was not found");
    }

    @Test
    void swapiUnavailableMapsTo502() throws Exception {
        expectProblem("/probe/swapi-down", 502, "SWAPI is unavailable");
    }

    @Test
    void unexpectedErrorMapsTo500WithoutLeakingTheCause() throws Exception {
        expectProblem("/probe/boom", 500, "Unexpected error");
    }

    private void expectProblem(String path, int status, String detail) throws Exception {
        mockMvc.perform(get(path).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(status))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(status))
                .andExpect(jsonPath("$.detail").value(detail));
    }

    @RestController
    static class ProbeController {

        @GetMapping("/probe/credentials")
        void credentials() {
            throw new InvalidCredentialsException();
        }

        @GetMapping("/probe/taken")
        void taken() {
            throw new UsernameAlreadyExistsException("luke");
        }

        @GetMapping("/probe/missing")
        void missing() {
            throw new ResourceNotFoundException("people with id 0 was not found");
        }

        @GetMapping("/probe/swapi-down")
        void swapiDown() {
            throw new SwapiUnavailableException("SWAPI is unavailable");
        }

        @GetMapping("/probe/boom")
        void boom() {
            throw new IllegalStateException("secret internal detail");
        }
    }
}
