package com.conexa.challenge.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.conexa.challenge.exception.ResourceNotFoundException;
import com.conexa.challenge.exception.SwapiUnavailableException;
import com.conexa.challenge.model.dto.CatalogItem;
import com.conexa.challenge.model.dto.PageResponse;
import com.conexa.challenge.support.AbstractIntegrationTest;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * El cliente contra un SWAPI simulado con WireMock (nunca red real). Las respuestas replican las formas reales
 * de swapi.tech: listado paginado por el origen, y búsqueda/films con todo junto.
 */
class SwapiClientTest extends AbstractIntegrationTest {

    @RegisterExtension
    static final WireMockExtension SWAPI = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void swapiProperties(DynamicPropertyRegistry registry) {
        registry.add("swapi.base-url", SWAPI::baseUrl);
        registry.add("swapi.connect-timeout", () -> "1s");
        registry.add("swapi.read-timeout", () -> "500ms");
    }

    @Autowired
    private SwapiClient swapiClient;

    @BeforeEach
    void resetWireMock() {
        SWAPI.resetAll();
    }

    @Test
    void listMapsPageThatSourcePaginated() {
        SWAPI.stubFor(json(get(urlPathEqualTo("/people"))
                .withQueryParam("page", equalTo("2"))
                .withQueryParam("limit", equalTo("5")), """
                {
                  "message": "ok",
                  "total_records": 82,
                  "total_pages": 17,
                  "next": "https://www.swapi.tech/api/people?page=3&limit=5",
                  "previous": "https://www.swapi.tech/api/people?page=1&limit=5",
                  "results": [
                    {"uid": "6", "name": "Owen Lars", "url": "https://www.swapi.tech/api/people/6"}
                  ]
                }
                """));

        PageResponse<CatalogItem> page = swapiClient.getList(SwapiResource.PEOPLE, 2, 5, null);

        assertThat(page.page()).isEqualTo(2);
        assertThat(page.limit()).isEqualTo(5);
        assertThat(page.total()).isEqualTo(82);
        assertThat(page.hasNext()).isTrue();
        assertThat(page.content()).singleElement().satisfies(item -> {
            assertThat(item.id()).isEqualTo("6");
            assertThat(item.name()).isEqualTo("Owen Lars");
        });
    }

    @Test
    void searchResultsAreAllReturnedBySourceSoTheyArePagedHere() {
        SWAPI.stubFor(json(get(urlPathEqualTo("/people")).withQueryParam("name", equalTo("a")), searchBody(25)));

        PageResponse<CatalogItem> second = swapiClient.getList(SwapiResource.PEOPLE, 2, 10, "a");
        PageResponse<CatalogItem> third = swapiClient.getList(SwapiResource.PEOPLE, 3, 10, "a");

        assertThat(second.total()).isEqualTo(25);
        assertThat(second.hasNext()).isTrue();
        assertThat(second.content()).extracting(CatalogItem::id).containsExactly(
                "11", "12", "13", "14", "15", "16", "17", "18", "19", "20");
        assertThat(third.hasNext()).isFalse();
        assertThat(third.content()).extracting(CatalogItem::id).containsExactly("21", "22", "23", "24", "25");
    }

    @Test
    void pageBeyondTheLastOneIsEmpty() {
        SWAPI.stubFor(json(get(urlPathEqualTo("/people")).withQueryParam("name", equalTo("a")), searchBody(3)));

        PageResponse<CatalogItem> page = swapiClient.getList(SwapiResource.PEOPLE, 5, 10, "a");

        assertThat(page.content()).isEmpty();
        assertThat(page.total()).isEqualTo(3);
        assertThat(page.hasNext()).isFalse();
    }

    @Test
    void filmsListIsNotPaginatedBySourceEitherSoItIsPagedHere() {
        SWAPI.stubFor(json(get(urlPathEqualTo("/films")), """
                {"message":"ok","result":[
                  {"uid":"1","properties":{"title":"A New Hope","url":"https://www.swapi.tech/api/films/1"}},
                  {"uid":"2","properties":{"title":"The Empire Strikes Back","url":"https://www.swapi.tech/api/films/2"}},
                  {"uid":"3","properties":{"title":"Return of the Jedi","url":"https://www.swapi.tech/api/films/3"}}
                ]}
                """));

        PageResponse<CatalogItem> page = swapiClient.getList(SwapiResource.FILMS, 1, 2, null);

        assertThat(page.total()).isEqualTo(3);
        assertThat(page.hasNext()).isTrue();
        assertThat(page.content()).extracting(CatalogItem::name).containsExactly("A New Hope", "The Empire Strikes Back");
    }

    @Test
    void filmsSearchUsesTitleQueryParameter() {
        SWAPI.stubFor(json(get(urlPathEqualTo("/films")).withQueryParam("title", equalTo("hope")), """
                {"message":"ok","result":[
                  {"uid":"1","properties":{"title":"A New Hope","url":"https://www.swapi.tech/api/films/1"}}
                ]}
                """));

        PageResponse<CatalogItem> page = swapiClient.getList(SwapiResource.FILMS, 1, 10, "hope");

        assertThat(page.content()).extracting(CatalogItem::name).containsExactly("A New Hope");
    }

    @Test
    void searchTextWithSpecialCharactersIsSentEncodedNotInterpretedAsTemplate() {
        SWAPI.stubFor(json(get(urlPathEqualTo("/people")).withQueryParam("name", equalTo("{x}&y=z")), searchBody(1)));

        PageResponse<CatalogItem> page = swapiClient.getList(SwapiResource.PEOPLE, 1, 10, "{x}&y=z");

        assertThat(page.content()).hasSize(1);
    }

    @Test
    void secondListCallIsServedFromCacheIgnoringSearchCase() {
        SWAPI.stubFor(json(get(urlPathEqualTo("/people")).withQueryParam("name", equalTo("leia")), searchBody(1)));

        swapiClient.getList(SwapiResource.PEOPLE, 1, 10, "leia");
        swapiClient.getList(SwapiResource.PEOPLE, 1, 10, "leia");
        swapiClient.getList(SwapiResource.PEOPLE, 1, 10, "LEIA");

        SWAPI.verify(1, getRequestedFor(urlPathEqualTo("/people")));
    }

    @Test
    void listOfDifferentPagesAreCachedSeparately() {
        SWAPI.stubFor(json(get(urlPathEqualTo("/people")), """
                {"message":"ok","total_records":2,"next":null,"results":[
                  {"uid":"1","name":"Luke Skywalker","url":"https://www.swapi.tech/api/people/1"}]}
                """));

        swapiClient.getList(SwapiResource.PEOPLE, 1, 10, null);
        swapiClient.getList(SwapiResource.PEOPLE, 2, 10, null);

        SWAPI.verify(2, getRequestedFor(urlPathEqualTo("/people")));
    }

    @Test
    void serverErrorBecomesSwapiUnavailableAndIsNotCached() {
        SWAPI.stubFor(get(urlPathEqualTo("/films")).willReturn(aResponse().withStatus(500).withBody("boom")));

        assertThatThrownBy(() -> swapiClient.getList(SwapiResource.FILMS, 1, 10, null))
                .isInstanceOf(SwapiUnavailableException.class)
                .hasMessage("SWAPI is unavailable");
        assertThatThrownBy(() -> swapiClient.getList(SwapiResource.FILMS, 1, 10, null))
                .isInstanceOf(SwapiUnavailableException.class);

        SWAPI.verify(2, getRequestedFor(urlPathEqualTo("/films")));
    }

    @Test
    void timeoutBecomesSwapiUnavailable() {
        SWAPI.stubFor(get(urlPathEqualTo("/people")).willReturn(
                aResponse().withFixedDelay(2_000).withHeader("Content-Type", "application/json").withBody("{}")));

        assertThatThrownBy(() -> swapiClient.getList(SwapiResource.PEOPLE, 1, 10, null))
                .isInstanceOf(SwapiUnavailableException.class);
    }

    @Test
    void unreadableBodyBecomesSwapiUnavailable() {
        SWAPI.stubFor(json(get(urlPathEqualTo("/people")), "this is not json"));

        assertThatThrownBy(() -> swapiClient.getList(SwapiResource.PEOPLE, 1, 10, null))
                .isInstanceOf(SwapiUnavailableException.class);
    }

    @Test
    void detailNotFoundOn404() {
        SWAPI.stubFor(get(urlPathEqualTo("/starships/999")).willReturn(aResponse().withStatus(404).withBody("Not found")));

        assertThatThrownBy(() -> swapiClient.getById(SwapiResource.STARSHIPS, "999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void detailOtherErrorsBecomeSwapiUnavailable() {
        SWAPI.stubFor(get(urlPathEqualTo("/starships/3")).willReturn(aResponse().withStatus(503)));

        assertThatThrownBy(() -> swapiClient.getById(SwapiResource.STARSHIPS, "3"))
                .isInstanceOf(SwapiUnavailableException.class);
    }

    @Test
    void detailIsMappedAndCached() {
        SWAPI.stubFor(json(get(urlPathEqualTo("/vehicles/4")), """
                {"message":"ok","result":{"uid":"4","properties":{
                  "name":"Sand Crawler","model":"Digger Crawler","url":"https://www.swapi.tech/api/vehicles/4"}}}
                """));

        CatalogItem first = swapiClient.getById(SwapiResource.VEHICLES, "4");
        CatalogItem second = swapiClient.getById(SwapiResource.VEHICLES, "4");

        assertThat(first.id()).isEqualTo("4");
        assertThat(first.name()).isEqualTo("Sand Crawler");
        assertThat(second.fields()).containsEntry("model", "Digger Crawler");
        SWAPI.verify(1, getRequestedFor(urlPathEqualTo("/vehicles/4")));
    }

    @Test
    void detailWithNullFieldsDoesNotFail() {
        SWAPI.stubFor(json(get(urlPathEqualTo("/people/1")), """
                {"message":"ok","result":{"uid":"1","properties":{"name":"Luke","homeworld":null}}}
                """));

        CatalogItem item = swapiClient.getById(SwapiResource.PEOPLE, "1");

        assertThat(item.name()).isEqualTo("Luke");
        assertThat(item.fields()).containsEntry("homeworld", null);
    }

    @Test
    void emptyDetailBecomesSwapiUnavailable() {
        SWAPI.stubFor(json(get(urlPathEqualTo("/people/7")), """
                {"message":"ok","result":null}
                """));

        assertThatThrownBy(() -> swapiClient.getById(SwapiResource.PEOPLE, "7"))
                .isInstanceOf(SwapiUnavailableException.class);
    }

    private static MappingBuilder json(MappingBuilder builder, String body) {
        return builder.willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(body));
    }

    /** Respuesta de búsqueda de swapi.tech: {@code result} con todos los elementos y sin metadatos de paginación. */
    private static String searchBody(int count) {
        String rows = IntStream.rangeClosed(1, count)
                .mapToObj(i -> """
                        {"uid":"%d","properties":{"name":"Person %d","url":"https://www.swapi.tech/api/people/%d"}}"""
                        .formatted(i, i, i))
                .collect(Collectors.joining(","));
        return "{\"message\":\"ok\",\"result\":[" + rows + "]}";
    }
}
