package com.conexa.challenge.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conexa.challenge.client.SwapiClient;
import com.conexa.challenge.client.SwapiResource;
import com.conexa.challenge.model.dto.PageResponse;
import com.conexa.challenge.model.dto.SwapiItem;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StarshipsResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SwapiClient swapiClient;

    @Test
    @WithMockUser(roles = "USER")
    void listAndGetById() throws Exception {
        when(swapiClient.getList(SwapiResource.STARSHIPS, 1, 10, null))
                .thenReturn(new PageResponse<>(
                        List.of(new SwapiItem("2", "CR90 corvette", Map.of())), 1, 10, 36, true));
        when(swapiClient.getById(SwapiResource.STARSHIPS, "2"))
                .thenReturn(new SwapiItem("2", "CR90 corvette", Map.of("model", "CR90 corvette")));

        mockMvc.perform(get("/api/v1/starships"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("2"));

        mockMvc.perform(get("/api/v1/starships/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("CR90 corvette"));
    }
}
