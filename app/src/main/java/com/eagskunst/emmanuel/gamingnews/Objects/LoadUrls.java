package com.eagskunst.emmanuel.gamingnews.Objects;

import com.eagskunst.emmanuel.gamingnews.Models.Categories;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;

public class LoadUrls {
    private String language;
    private InputStream jsonFile;
    private String[] allUrls;
    private String[] ps4Urls;
    private String[] xboxOUrls;
    private String[] switchUrls;
    private String[] pcUrls;

    public LoadUrls(String language, InputStream jsonFile){
        this.language = language;
        this.jsonFile = jsonFile;
    }
    private Categories[] loadCategories(){
        Gson gson = new Gson();
        return gson.fromJson(loadJSONFromAsset(),Categories[].class);
    }
    private Categories getLocalCategory(){
        Categories[] categories = loadCategories();
        Categories category = null;
        for(Categories cat:categories){
            if(cat.getLanguage().equals(language)){
                category = cat;
                break;
            }
            else if(cat.getLanguage().equals("en") && category == null){
                category = cat;
            }
        }
        return category;
    }

    public void setUrls(){
        Categories category = getLocalCategory();
        allUrls = category.getAll_urls();
        ps4Urls = category.getPs4_urls();
        xboxOUrls = category.getXboxO_urls();
        switchUrls = category.getSwitch_urls();
        pcUrls = category.getPc_urls();
    }

    private String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = jsonFile;
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
            return json;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return json;
    }


    public String[] getAllUrls() {
        return allUrls;
    }

    public String[] getPs4Urls() {
        return ps4Urls;
    }

    public String[] getXboxOUrls() {
        return xboxOUrls;
    }

    public String[] getSwitchUrls() {
        return switchUrls;
    }

    public String[] getPcUrls() {
        return pcUrls;
    }

}
