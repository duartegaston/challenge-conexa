package com.conexa.challenge.swapi;

public enum SwapiResource {
    PEOPLE("people", "name"),
    FILMS("films", "title"),
    STARSHIPS("starships", "name"),
    VEHICLES("vehicles", "name");

    private final String path;
    private final String displayField;

    SwapiResource(String path, String displayField) {
        this.path = path;
        this.displayField = displayField;
    }

    public String path() {
        return path;
    }

    public String displayField() {
        return displayField;
    }
}
