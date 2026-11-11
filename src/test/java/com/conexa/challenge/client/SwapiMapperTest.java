package com.conexa.challenge.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.conexa.challenge.model.dto.PageResponse;
import com.conexa.challenge.model.dto.SwapiItem;
import com.conexa.challenge.model.dto.SwapiRawListResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SwapiMapperTest {

    private final SwapiMapper mapper = new SwapiMapper();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void extractsIdAndNameFromPersonRow() {
        SwapiItem item = mapper.toItem(
                Map.of(
                        "uid", "1",
                        "name", "Luke Skywalker",
                        "url", "https://www.swapi.tech/api/people/1"),
                SwapiResource.PEOPLE);

        assertThat(item.id()).isEqualTo("1");
        assertThat(item.name()).isEqualTo("Luke Skywalker");
    }

    @Test
    void usesTitleForFilms() {
        SwapiItem item = mapper.toItem(
                Map.of("title", "A New Hope", "url", "https://www.swapi.tech/api/films/1", "uid", "1"),
                SwapiResource.FILMS);

        assertThat(item.id()).isEqualTo("1");
        assertThat(item.name()).isEqualTo("A New Hope");
    }

    @Test
    void mapsListToPageResponse() {
        ArrayNode results = objectMapper.createArrayNode();
        ObjectNode row = results.addObject();
        row.put("uid", "1");
        row.put("name", "Luke Skywalker");
        row.put("url", "https://www.swapi.tech/api/people/1");

        SwapiRawListResponse raw = new SwapiRawListResponse(
                null, 82, "https://www.swapi.tech/api/people?page=2&limit=10", null, results, null);

        PageResponse<SwapiItem> page = mapper.toPage(raw, SwapiResource.PEOPLE, 1, 10);

        assertThat(page.page()).isEqualTo(1);
        assertThat(page.limit()).isEqualTo(10);
        assertThat(page.total()).isEqualTo(82);
        assertThat(page.hasNext()).isTrue();
        assertThat(page.content()).hasSize(1);
        assertThat(page.content().getFirst().id()).isEqualTo("1");
    }

    @Test
    void mapsDetailEnvelopeWithProperties() {
        SwapiItem item = mapper.toItemFromDetail(
                Map.of(
                        "message", "ok",
                        "result",
                                Map.of(
                                        "uid",
                                        "1",
                                        "properties",
                                        Map.of(
                                                "name",
                                                "Luke Skywalker",
                                                "height",
                                                "172",
                                                "url",
                                                "https://www.swapi.tech/api/people/1"))),
                SwapiResource.PEOPLE);

        assertThat(item.id()).isEqualTo("1");
        assertThat(item.name()).isEqualTo("Luke Skywalker");
        assertThat(item.fields()).containsEntry("height", "172");
    }
}
