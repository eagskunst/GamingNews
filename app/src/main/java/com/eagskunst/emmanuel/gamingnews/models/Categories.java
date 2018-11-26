package com.eagskunst.emmanuel.gamingnews.models;

import java.util.Arrays;

public class Categories {
    private String language;
    private String[] all_urls;
    private String[] ps4_urls;
    private String[] xboxO_urls;
    private String[] switch_urls;
    private String[] pc_urls;

    public Categories(String language, String[] all_urls) {
        this.language = language;
        this.all_urls = all_urls;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String[] getAll_urls() {
        return all_urls;
    }

    public void setAll_urls(String[] all_urls) {
        this.all_urls = all_urls;
    }

    public String[] getPs4_urls() {
        return ps4_urls;
    }

    public void setPs4_urls(String[] ps4_urls) {
        this.ps4_urls = ps4_urls;
    }

    public String[] getXboxO_urls() {
        return xboxO_urls;
    }

    public void setXboxO_urls(String[] xboxO_urls) {
        this.xboxO_urls = xboxO_urls;
    }

    public String[] getSwitch_urls() {
        return switch_urls;
    }

    public void setSwitch_urls(String[] switch_urls) {
        this.switch_urls = switch_urls;
    }

    public String[] getPc_urls() {
        return pc_urls;
    }

    public void setPc_urls(String[] pc_urls) {
        this.pc_urls = pc_urls;
    }

    @Override
    public String toString() {
        return "Categories{" +
                "language='" + language + '\'' +
                ", all_urls=" + Arrays.toString(all_urls) +
                '}';
    }
}
