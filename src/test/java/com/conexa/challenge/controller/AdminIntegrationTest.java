package com.conexa.challenge.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conexa.challenge.config.CacheConfig;
import com.conexa.challenge.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * Autorización por rol: el endpoint de admin solo lo puede usar ADMIN.
 */
class AdminIntegrationTest extends AbstractIntegrationTest {

    private static final String EVICT_CACHE = "/api/v1/admin/cache";

    @Test
    void anonymousIsUnauthorized() throws Exception {
        mockMvc.perform(delete(EVICT_CACHE)).andExpect(status().isUnauthorized());
    }

    @Test
    void userWithoutAdminRoleIsForbiddenProblem() throws Exception {
        mockMvc.perform(delete(EVICT_CACHE).header(HttpHeaders.AUTHORIZATION, userBearer()))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void adminEvictsAllCaches() throws Exception {
        cacheManager.getCache(CacheConfig.SWAPI_LISTS).put("k", "v");
        cacheManager.getCache(CacheConfig.SWAPI_DETAILS).put("k", "v");

        mockMvc.perform(delete(EVICT_CACHE).header(HttpHeaders.AUTHORIZATION, adminBearer()))
                .andExpect(status().isNoContent());

        assertThat(cacheManager.getCache(CacheConfig.SWAPI_LISTS).get("k")).isNull();
        assertThat(cacheManager.getCache(CacheConfig.SWAPI_DETAILS).get("k")).isNull();
    }
}
