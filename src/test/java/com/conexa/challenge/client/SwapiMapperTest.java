package com.conexa.challenge.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.conexa.challenge.model.dto.CatalogItem;
import com.conexa.challenge.model.dto.PageResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SwapiMapperTest {

    private final SwapiMapper mapper = new SwapiMapper();

    @Test
    void mapsListRowWithUidNameAndUrl() {
        CatalogItem item = mapper.toItem(
                Map.of("uid", "1", "name", "Luke Skywalker", "url", "https://www.swapi.tech/api/people/1"),
                SwapiResource.PEOPLE);

        assertThat(item.id()).isEqualTo("1");
        assertThat(item.name()).isEqualTo("Luke Skywalker");
    }

    @Test
    void usesTitleAsNameForFilms() {
        CatalogItem item = mapper.toItem(
                Map.of("properties", Map.of("title", "A New Hope"), "uid", "1"), SwapiResource.FILMS);

        assertThat(item.name()).isEqualTo("A New Hope");
    }

    @Test
    void unwrapsNestedProperties() {
        CatalogItem item = mapper.toItem(
                Map.of("uid", "1", "properties", Map.of("name", "Luke Skywalker", "height", "172")),
                SwapiResource.PEOPLE);

        assertThat(item.id()).isEqualTo("1");
        assertThat(item.name()).isEqualTo("Luke Skywalker");
        assertThat(item.fields()).containsEntry("height", "172").doesNotContainKey("properties");
    }

    @Test
    void fallsBackToTheIdInTheUrlWhenThereIsNoUid() {
        assertThat(mapper.toItem(
                                Map.of("name", "Death Star", "url", "https://www.swapi.tech/api/starships/9/"),
                                SwapiResource.STARSHIPS)
                        .id())
                .isEqualTo("9");
        assertThat(mapper.toItem(Map.of("name", "Nameless"), SwapiResource.STARSHIPS).id()).isEmpty();
    }

    @Test
    void keepsNullValuesInsteadOfFailing() {
        Map<String, Object> row = new HashMap<>();
        row.put("uid", "1");
        row.put("name", "Luke");
        row.put("homeworld", null);

        CatalogItem item = mapper.toItem(row, SwapiResource.PEOPLE);

        assertThat(item.fields()).containsEntry("homeworld", null);
    }

    @Test
    void returnedFieldsAreReadOnly() {
        CatalogItem item = mapper.toItem(Map.of("uid", "1", "name", "Luke"), SwapiResource.PEOPLE);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> item.fields().put("x", "y"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void respectsPaginationWhenTheSourcePaginated() {
        var raw = new SwapiListResponse(
                82,
                "https://www.swapi.tech/api/people?page=2&limit=10",
                List.of(Map.of("uid", "1", "name", "Luke Skywalker")),
                null);

        PageResponse<CatalogItem> page = mapper.toPage(raw, SwapiResource.PEOPLE, 1, 10);

        assertThat(page.page()).isEqualTo(1);
        assertThat(page.limit()).isEqualTo(10);
        assertThat(page.total()).isEqualTo(82);
        assertThat(page.hasNext()).isTrue();
        assertThat(page.content()).hasSize(1);
    }

    @Test
    void lastPageOfSourcePaginationHasNoNext() {
        var raw = new SwapiListResponse(1, null, List.of(Map.of("uid", "1", "name", "Luke")), null);

        assertThat(mapper.toPage(raw, SwapiResource.PEOPLE, 1, 10).hasNext()).isFalse();
    }

    @Test
    void pagesInMemoryWhenTheSourceReturnedEverything() {
        var rows = List.<Map<String, Object>>of(
                Map.of("uid", "1", "properties", Map.of("name", "A")),
                Map.of("uid", "2", "properties", Map.of("name", "B")),
                Map.of("uid", "3", "properties", Map.of("name", "C")));
        var raw = new SwapiListResponse(null, null, null, rows);

        PageResponse<CatalogItem> page = mapper.toPage(raw, SwapiResource.PEOPLE, 2, 2);

        assertThat(page.total()).isEqualTo(3);
        assertThat(page.hasNext()).isFalse();
        assertThat(page.content()).extracting(CatalogItem::name).containsExactly("C");
    }

    @Test
    void emptyResponseGivesEmptyPage() {
        PageResponse<CatalogItem> page =
                mapper.toPage(new SwapiListResponse(null, null, null, null), SwapiResource.PEOPLE, 1, 10);

        assertThat(page.content()).isEmpty();
        assertThat(page.total()).isZero();
        assertThat(page.hasNext()).isFalse();
    }
}
