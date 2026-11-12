package com.conexa.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.conexa.challenge.client.SwapiClient;
import com.conexa.challenge.client.SwapiResource;
import com.conexa.challenge.model.dto.CatalogItem;
import com.conexa.challenge.model.dto.PageResponse;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Lógica compartida de los servicios de catálogo ({@link AbstractCatalogService}) y la particularidad de films.
 */
@ExtendWith(MockitoExtension.class)
class CatalogServicesTest {

    private static final PageResponse<CatalogItem> PAGE =
            new PageResponse<>(List.of(new CatalogItem("1", "X", Map.of())), 1, 10, 1, false);

    @Mock
    private SwapiClient swapiClient;

    @Test
    void usesDefaultLimitWhenNoneGiven() {
        when(swapiClient.getList(SwapiResource.PEOPLE, 1, AbstractCatalogService.DEFAULT_LIMIT, null))
                .thenReturn(PAGE);

        assertThat(new PeopleService(swapiClient).list(1, null, null)).isEqualTo(PAGE);
    }

    @Test
    void forwardsPageAndLimit() {
        when(swapiClient.getList(SwapiResource.STARSHIPS, 3, 25, null)).thenReturn(PAGE);

        assertThat(new StarshipsService(swapiClient).list(3, 25, null)).isEqualTo(PAGE);
    }

    @Test
    void trimsSearchAndTreatsBlankAsNoFilter() {
        when(swapiClient.getList(SwapiResource.VEHICLES, 1, 10, "sand")).thenReturn(PAGE);
        when(swapiClient.getList(SwapiResource.VEHICLES, 1, 10, null)).thenReturn(PAGE);
        var service = new VehiclesService(swapiClient);

        service.list(1, 10, "  sand ");
        service.list(1, 10, "   ");

        verify(swapiClient).getList(SwapiResource.VEHICLES, 1, 10, "sand");
        verify(swapiClient).getList(SwapiResource.VEHICLES, 1, 10, null);
    }

    @Test
    void getByIdDelegatesWithTheServiceResource() {
        CatalogItem item = new CatalogItem("4", "Death Star", Map.of());
        when(swapiClient.getById(SwapiResource.STARSHIPS, "4")).thenReturn(item);

        assertThat(new StarshipsService(swapiClient).getById("4")).isEqualTo(item);
    }

    @Test
    void filmsPreferTitleOverName() {
        when(swapiClient.getList(SwapiResource.FILMS, 1, 10, "hope")).thenReturn(PAGE);

        new FilmsService(swapiClient).list(1, null, "ignored", "hope");

        verify(swapiClient).getList(SwapiResource.FILMS, 1, 10, "hope");
    }

    @Test
    void filmsFallBackToNameWhenTitleIsBlankOrMissing() {
        when(swapiClient.getList(SwapiResource.FILMS, 1, 10, "empire")).thenReturn(PAGE);
        var service = new FilmsService(swapiClient);

        service.list(1, null, "empire", " ");
        service.list(1, null, "empire", null);

        verify(swapiClient, org.mockito.Mockito.times(2)).getList(SwapiResource.FILMS, 1, 10, "empire");
    }

    @Test
    void filmsWithoutTitleNorNameHaveNoFilter() {
        when(swapiClient.getList(SwapiResource.FILMS, 1, 10, null)).thenReturn(PAGE);

        new FilmsService(swapiClient).list(1, null, null, null);

        verify(swapiClient).getList(SwapiResource.FILMS, 1, 10, null);
    }
}
