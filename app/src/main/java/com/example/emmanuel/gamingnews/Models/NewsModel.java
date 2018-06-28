package com.example.emmanuel.gamingnews.Models;

import java.util.Date;

public class NewsModel {
    private String newsImage;
    private String title;
    private String subtext;
    private String link;
    private Date pubDate;

    public NewsModel(String newsImage, String title, String subtext,String link,Date pubDate) {
        this.newsImage = newsImage;
        this.title = title;
        this.subtext = subtext;
        this.link = link;
        this.pubDate = pubDate;
    }

    public String getNewsImage() {
        return newsImage;
    }

    public void setNewsImage(String newsImage) {
        this.newsImage = newsImage;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtext() {
        return subtext;
    }

    public void setSubtext(String subtext) {
        this.subtext = subtext;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Date getPubDate() {
        return pubDate;
    }

    public void setPubDate(Date pubDate) {
        this.pubDate = pubDate;
    }
}
