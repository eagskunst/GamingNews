package com.eagskunst.emmanuel.gamingnews.models;

import com.google.gson.annotations.SerializedName;

public class Response{

	@SerializedName("date")
	private int date;

	@SerializedName("game")
	private Game game;

	@SerializedName("updated_at")
	private int updatedAt;

	@SerializedName("created_at")
	private int createdAt;

	@SerializedName("y")
	private int Y;

	@SerializedName("id")
	private int id;

	@SerializedName("category")
	private int category;

	@SerializedName("human")
	private String human;

	@SerializedName("m")
	private int M;

	@SerializedName("platform")
	private int platform;

	public void setDate(int date){
		this.date = date;
	}

	public int getDate(){
		return date;
	}

	public void setGame(Game game){
		this.game = game;
	}

	public Game getGame(){
		return game;
	}

	public void setUpdatedAt(int updatedAt){
		this.updatedAt = updatedAt;
	}

	public int getUpdatedAt(){
		return updatedAt;
	}

	public void setCreatedAt(int createdAt){
		this.createdAt = createdAt;
	}

	public int getCreatedAt(){
		return createdAt;
	}

	public void setY(int Y){
		this.Y = Y;
	}

	public int getY(){
		return Y;
	}

	public void setId(int id){
		this.id = id;
	}

	public int getId(){
		return id;
	}

	public void setCategory(int category){
		this.category = category;
	}

	public int getCategory(){
		return category;
	}

	public void setHuman(String human){
		this.human = human;
	}

	public String getHuman(){
		return human;
	}

	public void setM(int M){
		this.M = M;
	}

	public int getM(){
		return M;
	}

	public void setPlatform(int platform){
		this.platform = platform;
	}

	public int getPlatform(){
		return platform;
	}

	@Override
 	public String toString(){
		return 
			"Response{" + 
			"date = '" + date + '\'' + 
			",game = '" + game + '\'' + 
			",updated_at = '" + updatedAt + '\'' + 
			",created_at = '" + createdAt + '\'' + 
			",y = '" + Y + '\'' + 
			",id = '" + id + '\'' + 
			",category = '" + category + '\'' + 
			",human = '" + human + '\'' + 
			",m = '" + M + '\'' + 
			",platform = '" + platform + '\'' + 
			"}";
		}
}