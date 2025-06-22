package com.example.estacion_meteorologica.models;

public class ItemSlide {
    private final String title;
    private final String description;


    public ItemSlide(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

}
