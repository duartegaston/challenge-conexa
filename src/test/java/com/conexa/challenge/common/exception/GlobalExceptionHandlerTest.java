package com.conexa.challenge.common.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ProbeController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void swapiUnavailableMapsTo502() throws Exception {
        mockMvc.perform(get("/probe/swapi-down").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.message").value("SWAPI is unavailable"));
    }

    @Test
    void resourceNotFoundMapsTo404() throws Exception {
        mockMvc.perform(get("/probe/missing").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @RestController
    static class ProbeController {

        @GetMapping("/probe/swapi-down")
        void swapiDown() {
            throw new SwapiUnavailableException("SWAPI is unavailable");
        }

        @GetMapping("/probe/missing")
        void missing() {
            throw new ResourceNotFoundException("people with id 0 was not found");
        }
    }
}
