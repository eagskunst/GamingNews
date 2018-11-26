package com.eagskunst.emmanuel.gamingnews.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class NewsModel implements Parcelable{
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

    protected NewsModel(Parcel in) {
        newsImage = in.readString();
        title = in.readString();
        subtext = in.readString();
        link = in.readString();
    }

    public static final Creator<NewsModel> CREATOR = new Creator<NewsModel>() {
        @Override
        public NewsModel createFromParcel(Parcel in) {
            return new NewsModel(in);
        }

        @Override
        public NewsModel[] newArray(int size) {
            return new NewsModel[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(newsImage);
        parcel.writeString(title);
        parcel.writeString(subtext);
        parcel.writeString(link);
    }
}
