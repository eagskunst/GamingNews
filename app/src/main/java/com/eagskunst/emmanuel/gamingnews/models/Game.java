package com.eagskunst.emmanuel.gamingnews.models;

import com.google.gson.annotations.SerializedName;


public class Game{

	@SerializedName("cover")
	private Cover cover;

	@SerializedName("name")
	private String name;

	@SerializedName("id")
	private int id;

	@SerializedName("url")
	private String gameUrl;

	public void setCover(Cover cover){
		this.cover = cover;
	}

	public Cover getCover(){
		return cover;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}

	public void setId(int id){
		this.id = id;
	}

	public int getId(){
		return id;
	}

	public String getGameUrl() {
		return gameUrl;
	}

	public void setGameUrl(String gameUrl) {
		this.gameUrl = gameUrl;
	}

    @Override
    public String toString() {
        return "Game{" +
                "cover=" + cover +
                ", name='" + name + '\'' +
                ", id=" + id +
                ", gameUrl='" + gameUrl + '\'' +
                '}';
    }
}