package com.eagskunst.emmanuel.gamingnews.models;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;

public class Cover{

	@SerializedName("id")
	private int id;

	@SerializedName("url")
	private String url;

	public Cover(){

	}

	public Cover(int id, String url) {
		this.id = id;
		this.url = url;
	}

	public Cover(int id) {
		this.id = id;
		this.url = null;
	}


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