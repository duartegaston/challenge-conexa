package com.conexa.challenge.service;

import com.conexa.challenge.client.SwapiClient;
import com.conexa.challenge.client.SwapiResource;
import com.conexa.challenge.model.dto.CatalogItem;
import com.conexa.challenge.model.dto.PageResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Catálogo de películas (films) de SWAPI. Se busca por {@code title}; {@code name} se acepta como alias.
 */
@Service
public class FilmsService extends AbstractCatalogService {

    public FilmsService(SwapiClient swapiClient) {
        super(swapiClient, SwapiResource.FILMS);
    }

    /**
     * Igual que {@link #list(int, Integer, String)}, pero {@code title} tiene prioridad sobre {@code name}.
     */
    public PageResponse<CatalogItem> list(int page, Integer limit, String name, String title) {
        return list(page, limit, StringUtils.hasText(title) ? title : name);
    }
}
