package com.conexa.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.conexa.challenge.client.SwapiResource;
import com.conexa.challenge.model.dto.PageResponse;
import com.conexa.challenge.model.dto.SwapiItem;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pruebas unitarias del servicio de people.
 */
@ExtendWith(MockitoExtension.class)
class PeopleServiceTest {

    @Mock
    private CatalogService catalogService;

    @InjectMocks
    private PeopleService peopleService;

    @Test
    void shouldDelegateListToCatalog() {
        PageResponse<SwapiItem> page = new PageResponse<>(
                List.of(new SwapiItem("1", "Luke Skywalker", Map.of())), 1, 10, 1, false);
        when(catalogService.list(SwapiResource.PEOPLE, 1, 5, "luke")).thenReturn(page);

        assertThat(peopleService.list(1, 5, "luke")).isEqualTo(page);
        verify(catalogService).list(SwapiResource.PEOPLE, 1, 5, "luke");
    }

    @Test
    void shouldDelegateGetByIdToCatalog() {
        SwapiItem item = new SwapiItem("1", "Luke Skywalker", Map.of());
        when(catalogService.getById(SwapiResource.PEOPLE, "1")).thenReturn(item);

        assertThat(peopleService.getById("1")).isEqualTo(item);
    }
}
