package com.eagskunst.emmanuel.gamingnews.models;

import com.google.gson.annotations.SerializedName;

public class Cover{

	@SerializedName("id")
	private int id;

	@SerializedName("url")
	private String url;

	public void setId(int id){
		this.id = id;
	}

	public int getId(){
		return id;
	}

	public void setUrl(String url){
		this.url = url;
	}

	public String getUrl(){
		return url;
	}

	@Override
 	public String toString(){
		return 
			"Cover{" + 
			"id = '" + id + '\'' + 
			",url = '" + url + '\'' + 
			"}";
		}
}