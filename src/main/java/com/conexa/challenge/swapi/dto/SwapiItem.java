package com.conexa.challenge.swapi.dto;

import java.util.Map;

/**
 * Normalized SWAPI item used by services (id extracted from URL; display name from name/title).
 */
public record SwapiItem(String id, String name, Map<String, Object> fields) {
}
