package com.conexa.challenge.swapi;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.conexa.challenge.common.dto.PageResponse;
import com.conexa.challenge.common.exception.ResourceNotFoundException;
import com.conexa.challenge.common.exception.SwapiUnavailableException;
import com.conexa.challenge.config.CacheConfig;
import com.conexa.challenge.swapi.dto.SwapiItem;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@ActiveProfiles("test")
class SwapiClientTest {

    @RegisterExtension
    static final WireMockExtension SWAPI = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void swapiBaseUrl(DynamicPropertyRegistry registry) {
        registry.add("swapi.base-url", SWAPI::baseUrl);
        registry.add("swapi.connect-timeout-ms", () -> "1000");
        registry.add("swapi.read-timeout-ms", () -> "2000");
    }

    @Autowired
    private SwapiClient swapiClient;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void clearCaches() {
        var lists = cacheManager.getCache(CacheConfig.SWAPI_LISTS);
        var details = cacheManager.getCache(CacheConfig.SWAPI_DETAILS);
        if (lists != null) {
            lists.clear();
        }
        if (details != null) {
            details.clear();
        }
        SWAPI.resetRequests();
    }

    @Test
    void getListReturnsMappedPageFromMockedSwapi() {
        SWAPI.stubFor(get(urlPathEqualTo("/people/"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "count": 1,
                                  "next": null,
                                  "previous": null,
                                  "results": [
                                    {
                                      "name": "Luke Skywalker",
                                      "height": "172",
                                      "url": "https://swapi.dev/api/people/1/"
                                    }
                                  ]
                                }
                                """)));

        PageResponse<SwapiItem> page = swapiClient.getList(SwapiResource.PEOPLE, 1, null);

        assertThat(page.total()).isEqualTo(1);
        assertThat(page.hasNext()).isFalse();
        assertThat(page.content()).hasSize(1);
        assertThat(page.content().getFirst().id()).isEqualTo("1");
        assertThat(page.content().getFirst().name()).isEqualTo("Luke Skywalker");
    }

    @Test
    void secondListCallUsesCacheWithoutHittingSwapiAgain() {
        SWAPI.stubFor(get(urlPathEqualTo("/people/"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "count": 1,
                                  "next": null,
                                  "previous": null,
                                  "results": [
                                    {"name":"Leia Organa","url":"https://swapi.dev/api/people/5/"}
                                  ]
                                }
                                """)));

        PageResponse<SwapiItem> first = swapiClient.getList(SwapiResource.PEOPLE, 1, "leia");
        PageResponse<SwapiItem> second = swapiClient.getList(SwapiResource.PEOPLE, 1, "leia");

        assertThat(first.content().getFirst().name()).isEqualTo("Leia Organa");
        assertThat(second.content().getFirst().id()).isEqualTo("5");
        SWAPI.verify(1, getRequestedFor(urlPathEqualTo("/people/")));
    }

    @Test
    void getListThrowsWhenSwapiReturnsServerError() {
        SWAPI.stubFor(get(urlPathEqualTo("/films/"))
                .willReturn(aResponse().withStatus(500).withBody("boom")));

        assertThatThrownBy(() -> swapiClient.getList(SwapiResource.FILMS, 1, null))
                .isInstanceOf(SwapiUnavailableException.class)
                .hasMessageContaining("SWAPI");
    }

    @Test
    void getByIdThrowsNotFoundOn404() {
        SWAPI.stubFor(get(urlPathEqualTo("/starships/999/"))
                .willReturn(aResponse().withStatus(404).withBody("Not found")));

        assertThatThrownBy(() -> swapiClient.getById(SwapiResource.STARSHIPS, "999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void getByIdCachesSuccessfulDetail() {
        SWAPI.stubFor(get(urlPathEqualTo("/vehicles/4/"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "name": "Sand Crawler",
                                  "url": "https://swapi.dev/api/vehicles/4/"
                                }
                                """)));

        SwapiItem first = swapiClient.getById(SwapiResource.VEHICLES, "4");
        SwapiItem second = swapiClient.getById(SwapiResource.VEHICLES, "4");

        assertThat(first.name()).isEqualTo("Sand Crawler");
        assertThat(second.id()).isEqualTo("4");
        SWAPI.verify(1, getRequestedFor(urlPathEqualTo("/vehicles/4/")));
    }
}
