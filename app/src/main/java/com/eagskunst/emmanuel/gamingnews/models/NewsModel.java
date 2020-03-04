package com.eagskunst.emmanuel.gamingnews.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.eagskunst.emmanuel.gamingnews.utility.SimpleDateSingleton;

import java.text.ParseException;
import java.util.Date;

public class NewsModel implements Parcelable {
    private String newsImage;
    private String title;
    private String subtext;
    private String link;
    private Date pubDate;
    private String channelName;

    public NewsModel(String newsImage, String title, String subtext, String link, Date pubDate, String channelName) {
        this.newsImage = newsImage;
        this.title = title;
        this.subtext = subtext;
        this.link = link;
        this.pubDate = pubDate;
        this.channelName = channelName;
    }


    protected NewsModel(Parcel in) {
        newsImage = in.readString();
        title = in.readString();
        subtext = in.readString();
        link = in.readString();
        channelName = in.readString();
        final String pubDateString = in.readString();
        this.pubDate = parseDate(pubDateString);
    }

    private Date parseDate(final String dateString){
        try {
            return SimpleDateSingleton.getInstance().getInputSdf().parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
        }
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

    public String formattedDate(){
        return  SimpleDateSingleton.getInstance().getToSdf().format(pubDate);
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(newsImage);
        dest.writeString(title);
        dest.writeString(subtext);
        dest.writeString(link);
        dest.writeString(channelName);
        dest.writeString(SimpleDateSingleton.getInstance().getInputSdf().format(pubDate));
    }
}
