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
class VehiclesResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SwapiClient swapiClient;

    @Test
    @WithMockUser(roles = "USER")
    void listAndGetById() throws Exception {
        when(swapiClient.getList(SwapiResource.VEHICLES, 1, 10, "sand"))
                .thenReturn(new PageResponse<>(
                        List.of(new SwapiItem("4", "Sand Crawler", Map.of())), 1, 10, 1, false));
        when(swapiClient.getById(SwapiResource.VEHICLES, "4"))
                .thenReturn(new SwapiItem("4", "Sand Crawler", Map.of("vehicle_class", "wheeled")));

        mockMvc.perform(get("/api/v1/vehicles").param("name", "sand"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Sand Crawler"));

        mockMvc.perform(get("/api/v1/vehicles/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fields.vehicle_class").value("wheeled"));
    }
}
