package com.conexa.challenge.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conexa.challenge.client.SwapiClient;
import com.conexa.challenge.client.SwapiResource;
import com.conexa.challenge.exception.ResourceNotFoundException;
import com.conexa.challenge.exception.SwapiUnavailableException;
import com.conexa.challenge.model.dto.CatalogItem;
import com.conexa.challenge.model.dto.PageResponse;
import com.conexa.challenge.support.AbstractIntegrationTest;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Contrato HTTP de los cuatro recursos del catálogo, con seguridad real (login → JWT) y SWAPI mockeado.
 */
class CatalogResourcesIntegrationTest extends AbstractIntegrationTest {

    @MockitoBean
    private SwapiClient swapiClient;

    static Stream<Arguments> resources() {
        return Stream.of(
                Arguments.of("/api/v1/people", SwapiResource.PEOPLE),
                Arguments.of("/api/v1/films", SwapiResource.FILMS),
                Arguments.of("/api/v1/starships", SwapiResource.STARSHIPS),
                Arguments.of("/api/v1/vehicles", SwapiResource.VEHICLES));
    }

    @ParameterizedTest
    @MethodSource("resources")
    void requireAuthentication(String path, SwapiResource resource) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401));
        mockMvc.perform(get(path + "/1")).andExpect(status().isUnauthorized());

        verifyNoInteractions(swapiClient);
    }

    @ParameterizedTest
    @MethodSource("resources")
    void listUsesDefaultPagingAndReturnsNormalizedPage(String path, SwapiResource resource) throws Exception {
        when(swapiClient.getList(resource, 1, 10, null))
                .thenReturn(new PageResponse<>(List.of(item("1", "First")), 1, 10, 82, true));

        mockMvc.perform(get(path).header(HttpHeaders.AUTHORIZATION, userBearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.limit").value(10))
                .andExpect(jsonPath("$.total").value(82))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.content[0].id").value("1"))
                .andExpect(jsonPath("$.content[0].name").value("First"));
    }

    @ParameterizedTest
    @MethodSource("resources")
    void listForwardsPageLimitAndTrimmedName(String path, SwapiResource resource) throws Exception {
        when(swapiClient.getList(resource, 2, 5, "luke"))
                .thenReturn(new PageResponse<>(List.of(item("9", "Luke")), 2, 5, 6, false));

        mockMvc.perform(get(path)
                        .header(HttpHeaders.AUTHORIZATION, userBearer())
                        .param("page", "2")
                        .param("limit", "5")
                        .param("name", "  luke  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Luke"));

        verify(swapiClient).getList(resource, 2, 5, "luke");
    }

    @ParameterizedTest
    @MethodSource("resources")
    void getByIdReturnsItem(String path, SwapiResource resource) throws Exception {
        when(swapiClient.getById(resource, "4")).thenReturn(new CatalogItem("4", "Item four", Map.of("k", "v")));

        mockMvc.perform(get(path + "/4").header(HttpHeaders.AUTHORIZATION, userBearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("4"))
                .andExpect(jsonPath("$.name").value("Item four"))
                .andExpect(jsonPath("$.fields.k").value("v"));
    }

    @ParameterizedTest
    @MethodSource("resources")
    void getByIdMapsUnknownIdTo404Problem(String path, SwapiResource resource) throws Exception {
        when(swapiClient.getById(resource, "999")).thenThrow(new ResourceNotFoundException("not found 999"));

        mockMvc.perform(get(path + "/999").header(HttpHeaders.AUTHORIZATION, userBearer()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("not found 999"));
    }

    @ParameterizedTest
    @MethodSource("resources")
    void swapiFailureIsMappedTo502Problem(String path, SwapiResource resource) throws Exception {
        when(swapiClient.getList(resource, 1, 10, null)).thenThrow(new SwapiUnavailableException("SWAPI is unavailable"));

        mockMvc.perform(get(path).header(HttpHeaders.AUTHORIZATION, userBearer()))
                .andExpect(status().isBadGateway())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(502));
    }

    @ParameterizedTest
    @ValueSource(strings = {"page=0", "page=-1", "page=abc", "limit=0", "limit=101", "limit=abc"})
    void invalidPagingIsBadRequest(String query) throws Exception {
        mockMvc.perform(get("/api/v1/people?" + query).header(HttpHeaders.AUTHORIZATION, userBearer()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(swapiClient);
    }

    @Test
    void invalidPagingReportsWhichParameterFailed() throws Exception {
        mockMvc.perform(get("/api/v1/people?limit=500").header(HttpHeaders.AUTHORIZATION, userBearer()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Validation failed"))
                .andExpect(jsonPath("$.errors.limit").exists());
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "1x", "-1"})
    void nonNumericIdIsBadRequestWithoutCallingSwapi(String id) throws Exception {
        mockMvc.perform(get("/api/v1/starships/" + id).header(HttpHeaders.AUTHORIZATION, userBearer()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.id").exists());

        verifyNoInteractions(swapiClient);
    }

    @Test
    void filmsListPrefersTitleOverName() throws Exception {
        when(swapiClient.getList(SwapiResource.FILMS, 1, 10, "hope"))
                .thenReturn(new PageResponse<>(List.of(item("1", "A New Hope")), 1, 10, 1, false));

        mockMvc.perform(get("/api/v1/films")
                        .header(HttpHeaders.AUTHORIZATION, userBearer())
                        .param("title", "hope")
                        .param("name", "ignored"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("A New Hope"));

        verify(swapiClient).getList(SwapiResource.FILMS, 1, 10, "hope");
    }

    @Test
    void filmsListFallsBackToNameWhenNoTitle() throws Exception {
        when(swapiClient.getList(SwapiResource.FILMS, 1, 10, "empire"))
                .thenReturn(new PageResponse<>(List.of(item("2", "The Empire Strikes Back")), 1, 10, 1, false));

        mockMvc.perform(get("/api/v1/films")
                        .header(HttpHeaders.AUTHORIZATION, userBearer())
                        .param("name", "empire"))
                .andExpect(status().isOk());

        verify(swapiClient).getList(SwapiResource.FILMS, 1, 10, "empire");
    }

    @ParameterizedTest
    @MethodSource("resources")
    void listFilteredByIdReturnsASingleItemPage(String path, SwapiResource resource) throws Exception {
        when(swapiClient.getById(resource, "4")).thenReturn(item("4", "Item four"));

        mockMvc.perform(get(path).header(HttpHeaders.AUTHORIZATION, userBearer()).param("id", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value("4"))
                .andExpect(jsonPath("$.content[0].name").value("Item four"));

        verify(swapiClient, never()).getList(any(), anyInt(), anyInt(), any());
    }

    @ParameterizedTest
    @MethodSource("resources")
    void listFilteredByUnknownIdIsAnEmptyPage(String path, SwapiResource resource) throws Exception {
        when(swapiClient.getById(resource, "999")).thenThrow(new ResourceNotFoundException("not found 999"));

        mockMvc.perform(get(path).header(HttpHeaders.AUTHORIZATION, userBearer()).param("id", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @ParameterizedTest
    @MethodSource("resources")
    void listFilteredByIdAndNameRequiresBothToMatch(String path, SwapiResource resource) throws Exception {
        when(swapiClient.getById(resource, "4")).thenReturn(item("4", "Item four"));

        mockMvc.perform(get(path)
                        .header(HttpHeaders.AUTHORIZATION, userBearer())
                        .param("id", "4")
                        .param("name", "FOUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("4"));

        mockMvc.perform(get(path)
                        .header(HttpHeaders.AUTHORIZATION, userBearer())
                        .param("id", "4")
                        .param("name", "other"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "1x", "-1"})
    void nonNumericIdFilterIsBadRequestWithoutCallingSwapi(String id) throws Exception {
        mockMvc.perform(get("/api/v1/people").header(HttpHeaders.AUTHORIZATION, userBearer()).param("id", id))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors.id").exists());

        verifyNoInteractions(swapiClient);
    }

    @Test
    void filmsListFilteredByIdMatchesAgainstTitle() throws Exception {
        when(swapiClient.getById(SwapiResource.FILMS, "1")).thenReturn(item("1", "A New Hope"));

        mockMvc.perform(get("/api/v1/films")
                        .header(HttpHeaders.AUTHORIZATION, userBearer())
                        .param("id", "1")
                        .param("title", "hope"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("A New Hope"));

        mockMvc.perform(get("/api/v1/films")
                        .header(HttpHeaders.AUTHORIZATION, userBearer())
                        .param("id", "1")
                        .param("title", "empire"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void unknownRouteIsNotFoundProblem() throws Exception {
        mockMvc.perform(get("/api/v1/nope").header(HttpHeaders.AUTHORIZATION, userBearer()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    private static CatalogItem item(String id, String name) {
        return new CatalogItem(id, name, Map.of());
    }
}
