package com.conexa.challenge.model.dto;

import java.util.Map;

/**
 * Ítem SWAPI normalizado (id, nombre visible y campos crudos).
 */
public record SwapiItem(String id, String name, Map<String, Object> fields) {
}
