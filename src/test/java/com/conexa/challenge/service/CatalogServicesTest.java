package com.conexa.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.conexa.challenge.client.SwapiClient;
import com.conexa.challenge.client.SwapiResource;
import com.conexa.challenge.exception.ResourceNotFoundException;
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

        assertThat(new PeopleService(swapiClient).list(1, null, null, null)).isEqualTo(PAGE);
    }

    @Test
    void forwardsPageAndLimit() {
        when(swapiClient.getList(SwapiResource.STARSHIPS, 3, 25, null)).thenReturn(PAGE);

        assertThat(new StarshipsService(swapiClient).list(3, 25, null, null)).isEqualTo(PAGE);
    }

    @Test
    void trimsSearchAndTreatsBlankAsNoFilter() {
        when(swapiClient.getList(SwapiResource.VEHICLES, 1, 10, "sand")).thenReturn(PAGE);
        when(swapiClient.getList(SwapiResource.VEHICLES, 1, 10, null)).thenReturn(PAGE);
        var service = new VehiclesService(swapiClient);

        service.list(1, 10, null, "  sand ");
        service.list(1, 10, null, "   ");

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

        new FilmsService(swapiClient).list(1, null, null, "ignored", "hope");

        verify(swapiClient).getList(SwapiResource.FILMS, 1, 10, "hope");
    }

    @Test
    void filmsFallBackToNameWhenTitleIsBlankOrMissing() {
        when(swapiClient.getList(SwapiResource.FILMS, 1, 10, "empire")).thenReturn(PAGE);
        var service = new FilmsService(swapiClient);

        service.list(1, null, null, "empire", " ");
        service.list(1, null, null, "empire", null);

        verify(swapiClient, org.mockito.Mockito.times(2)).getList(SwapiResource.FILMS, 1, 10, "empire");
    }

    @Test
    void filmsWithoutTitleNorNameHaveNoFilter() {
        when(swapiClient.getList(SwapiResource.FILMS, 1, 10, null)).thenReturn(PAGE);

        new FilmsService(swapiClient).list(1, null, null, null, null);

        verify(swapiClient).getList(SwapiResource.FILMS, 1, 10, null);
    }

    @Test
    void listByIdReturnsSingleItemPageWithoutSearchingSwapi() {
        CatalogItem luke = new CatalogItem("1", "Luke Skywalker", Map.of());
        when(swapiClient.getById(SwapiResource.PEOPLE, "1")).thenReturn(luke);

        PageResponse<CatalogItem> page = new PeopleService(swapiClient).list(1, null, " 1 ", null);

        assertThat(page).isEqualTo(new PageResponse<>(List.of(luke), 1, 10, 1, false));
        verify(swapiClient).getById(SwapiResource.PEOPLE, "1");
        verifyNoMoreInteractions(swapiClient);
    }

    @Test
    void listByUnknownIdIsAnEmptyPageNotAnError() {
        when(swapiClient.getById(SwapiResource.PEOPLE, "999")).thenThrow(new ResourceNotFoundException("nope"));

        PageResponse<CatalogItem> page = new PeopleService(swapiClient).list(1, 10, "999", null);

        assertThat(page.content()).isEmpty();
        assertThat(page.total()).isZero();
        assertThat(page.hasNext()).isFalse();
    }

    @Test
    void listByIdAndNameRequiresBothToMatchIgnoringCase() {
        CatalogItem luke = new CatalogItem("1", "Luke Skywalker", Map.of());
        when(swapiClient.getById(SwapiResource.PEOPLE, "1")).thenReturn(luke);
        var service = new PeopleService(swapiClient);

        assertThat(service.list(1, 10, "1", " SKY ").content()).containsExactly(luke);
        assertThat(service.list(1, 10, "1", "vader").content()).isEmpty();
    }

    @Test
    void listByIdWithNameDoesNotMatchItemsWithoutName() {
        when(swapiClient.getById(SwapiResource.PEOPLE, "1")).thenReturn(new CatalogItem("1", null, Map.of()));

        assertThat(new PeopleService(swapiClient).list(1, 10, "1", "luke").content()).isEmpty();
    }

    @Test
    void listByIdBeyondFirstPageIsEmptyButKeepsTotal() {
        CatalogItem luke = new CatalogItem("1", "Luke Skywalker", Map.of());
        when(swapiClient.getById(SwapiResource.PEOPLE, "1")).thenReturn(luke);

        PageResponse<CatalogItem> page = new PeopleService(swapiClient).list(2, 10, "1", null);

        assertThat(page.content()).isEmpty();
        assertThat(page.total()).isEqualTo(1);
        assertThat(page.page()).isEqualTo(2);
    }

    @Test
    void blankIdIsTreatedAsNoFilter() {
        when(swapiClient.getList(SwapiResource.PEOPLE, 1, 10, null)).thenReturn(PAGE);

        assertThat(new PeopleService(swapiClient).list(1, null, "  ", null)).isEqualTo(PAGE);
        verify(swapiClient).getList(SwapiResource.PEOPLE, 1, 10, null);
    }

    @Test
    void filmsListByIdUsesTitleAsTheNameFilter() {
        CatalogItem hope = new CatalogItem("1", "A New Hope", Map.of());
        when(swapiClient.getById(SwapiResource.FILMS, "1")).thenReturn(hope);
        var service = new FilmsService(swapiClient);

        assertThat(service.list(1, null, "1", "ignored", "hope").content()).containsExactly(hope);
        assertThat(service.list(1, null, "1", "hope", "empire").content()).isEmpty();
    }
}
