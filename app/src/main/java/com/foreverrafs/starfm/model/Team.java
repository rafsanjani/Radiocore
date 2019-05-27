package com.foreverrafs.starfm.model;

// Created by Emperor95 on 1/13/2019.

public class Team {
    private String name;
    private String porfolio;
    private int image;

    public Team() {
    }

    public Team(String name, String porfolio, int image) {
        this.name = name;
        this.porfolio = porfolio;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPorfolio() {
        return porfolio;
    }

    public void setPorfolio(String porfolio) {
        this.porfolio = porfolio;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }
}
