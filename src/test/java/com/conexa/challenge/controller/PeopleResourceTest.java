package com.conexa.challenge.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conexa.challenge.client.SwapiClient;
import com.conexa.challenge.client.SwapiResource;
import com.conexa.challenge.exception.ResourceNotFoundException;
import com.conexa.challenge.exception.SwapiUnavailableException;
import com.conexa.challenge.model.dto.PageResponse;
import com.conexa.challenge.model.dto.SwapiItem;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PeopleResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SwapiClient swapiClient;

    @Test
    void listRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/people")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void listReturnsNormalizedPage() throws Exception {
        when(swapiClient.getList(eq(SwapiResource.PEOPLE), eq(1), isNull()))
                .thenReturn(new PageResponse<>(
                        List.of(new SwapiItem("1", "Luke Skywalker", Map.of("height", "172"))),
                        1,
                        10,
                        82,
                        true));

        mockMvc.perform(get("/api/v1/people").param("page", "1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.limit").value(10))
                .andExpect(jsonPath("$.total").value(82))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.content[0].id").value("1"))
                .andExpect(jsonPath("$.content[0].name").value("Luke Skywalker"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void listFiltersByNameViaSwapiSearch() throws Exception {
        when(swapiClient.getList(SwapiResource.PEOPLE, 1, "leia"))
                .thenReturn(new PageResponse<>(
                        List.of(new SwapiItem("5", "Leia Organa", Map.of())), 1, 10, 1, false));

        mockMvc.perform(get("/api/v1/people").param("name", "leia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Leia Organa"));

        verify(swapiClient).getList(SwapiResource.PEOPLE, 1, "leia");
    }

    @Test
    @WithMockUser(roles = "USER")
    void getByIdReturnsItem() throws Exception {
        when(swapiClient.getById(SwapiResource.PEOPLE, "1"))
                .thenReturn(new SwapiItem("1", "Luke Skywalker", Map.of("gender", "male")));

        mockMvc.perform(get("/api/v1/people/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Luke Skywalker"))
                .andExpect(jsonPath("$.fields.gender").value("male"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getByIdMapsNotFound() throws Exception {
        when(swapiClient.getById(SwapiResource.PEOPLE, "999"))
                .thenThrow(new ResourceNotFoundException("people with id 999 was not found"));

        mockMvc.perform(get("/api/v1/people/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(roles = "USER")
    void listMapsSwapiFailureTo502() throws Exception {
        when(swapiClient.getList(eq(SwapiResource.PEOPLE), eq(1), isNull()))
                .thenThrow(new SwapiUnavailableException("SWAPI is unavailable"));

        mockMvc.perform(get("/api/v1/people"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502));
    }
}
