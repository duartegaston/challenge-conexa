package com.conexa.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.conexa.challenge.client.SwapiClient;
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

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    private SwapiClient swapiClient;

    @InjectMocks
    private CatalogService catalogService;

    @Test
    void listAppliesLimitAndForwardsNameAsSearch() {
        PageResponse<SwapiItem> fromClient = new PageResponse<>(
                List.of(
                        new SwapiItem("1", "Luke Skywalker", Map.of()),
                        new SwapiItem("2", "C-3PO", Map.of()),
                        new SwapiItem("3", "R2-D2", Map.of())),
                1,
                10,
                82,
                true);

        when(swapiClient.getList(SwapiResource.PEOPLE, 1, 2, "luke")).thenReturn(fromClient);

        PageResponse<SwapiItem> result = catalogService.list(SwapiResource.PEOPLE, 1, 2, "luke");

        assertThat(result.limit()).isEqualTo(2);
        assertThat(result.content()).hasSize(2);
        assertThat(result.content().getFirst().name()).isEqualTo("Luke Skywalker");
        assertThat(result.total()).isEqualTo(82);
        assertThat(result.hasNext()).isTrue();
        verify(swapiClient).getList(SwapiResource.PEOPLE, 1, 2, "luke");
    }

    @Test
    void getByIdDelegatesToClient() {
        SwapiItem item = new SwapiItem("4", "Death Star", Map.of("model", "DS-1"));
        when(swapiClient.getById(SwapiResource.STARSHIPS, "4")).thenReturn(item);

        assertThat(catalogService.getById(SwapiResource.STARSHIPS, "4")).isEqualTo(item);
    }
}
