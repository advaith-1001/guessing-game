package com.adv.game.model;

public class Country {

    private String name;
    private String flagUrl;

    public Country(String name, String flagUrl) {
        this.name = name;
        this.flagUrl = flagUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFlagUrl() {
        return flagUrl;
    }

    public void setFlagUrl(String flagUrl) {
        this.flagUrl = flagUrl;
    }
}
