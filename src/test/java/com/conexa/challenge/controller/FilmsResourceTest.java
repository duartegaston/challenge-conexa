package com.conexa.challenge.controller;

import static org.mockito.Mockito.verify;
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
class FilmsResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SwapiClient swapiClient;

    @Test
    @WithMockUser(roles = "USER")
    void listPrefersTitleOverNameForSearch() throws Exception {
        when(swapiClient.getList(SwapiResource.FILMS, 1, 10, "hope"))
                .thenReturn(new PageResponse<>(
                        List.of(new SwapiItem("1", "A New Hope", Map.of())), 1, 10, 1, false));

        mockMvc.perform(get("/api/v1/films").param("title", "hope").param("name", "ignored"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("A New Hope"));

        verify(swapiClient).getList(SwapiResource.FILMS, 1, 10, "hope");
    }

    @Test
    @WithMockUser(roles = "USER")
    void getByIdReturnsFilm() throws Exception {
        when(swapiClient.getById(SwapiResource.FILMS, "1"))
                .thenReturn(new SwapiItem("1", "A New Hope", Map.of("episode_id", 4)));

        mockMvc.perform(get("/api/v1/films/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.fields.episode_id").value(4));
    }
}
