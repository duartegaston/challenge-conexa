package com.conexa.challenge.model.dto;

import java.util.Map;

/**
 * Ítem del catálogo (people, films, starships o vehicles) normalizado: id, nombre visible y campos originales.
 */
public record CatalogItem(String id, String name, Map<String, Object> fields) {}
