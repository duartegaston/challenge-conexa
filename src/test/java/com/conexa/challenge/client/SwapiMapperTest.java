package com.conexa.challenge.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.conexa.challenge.model.dto.PageResponse;
import com.conexa.challenge.model.dto.SwapiItem;
import com.conexa.challenge.model.dto.SwapiRawListResponse;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SwapiMapperTest {

    private final SwapiMapper mapper = new SwapiMapper();

    @Test
    void extractsIdAndNameFromPersonRow() {
        SwapiItem item = mapper.toItem(
                Map.of(
                        "name", "Luke Skywalker",
                        "url", "https://swapi.dev/api/people/1/",
                        "height", "172"),
                SwapiResource.PEOPLE);

        assertThat(item.id()).isEqualTo("1");
        assertThat(item.name()).isEqualTo("Luke Skywalker");
        assertThat(item.fields()).containsEntry("height", "172");
    }

    @Test
    void usesTitleForFilms() {
        SwapiItem item = mapper.toItem(
                Map.of("title", "A New Hope", "url", "https://swapi.dev/api/films/1/"),
                SwapiResource.FILMS);

        assertThat(item.id()).isEqualTo("1");
        assertThat(item.name()).isEqualTo("A New Hope");
    }

    @Test
    void mapsListToPageResponse() {
        SwapiRawListResponse raw = new SwapiRawListResponse(
                82,
                "https://swapi.dev/api/people/?page=2",
                null,
                List.of(Map.of("name", "Luke Skywalker", "url", "https://swapi.dev/api/people/1/")));

        PageResponse<SwapiItem> page = mapper.toPage(raw, SwapiResource.PEOPLE, 1);

        assertThat(page.page()).isEqualTo(1);
        assertThat(page.limit()).isEqualTo(10);
        assertThat(page.total()).isEqualTo(82);
        assertThat(page.hasNext()).isTrue();
        assertThat(page.content()).hasSize(1);
        assertThat(page.content().getFirst().id()).isEqualTo("1");
    }
}
