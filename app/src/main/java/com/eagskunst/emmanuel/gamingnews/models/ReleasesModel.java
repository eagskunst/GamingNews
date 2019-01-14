package com.eagskunst.emmanuel.gamingnews.models;

/**
 * Created by eagskunst on 10/01/2019
 */
public class ReleasesModel{
    private String gameCoverUrl;
    private String gameName;
    private String gameReleaseDate;
    private String gamePlatforms;
    private String gameUrl;

    public ReleasesModel(String gameCoverUrl, String gameName, String gameReleaseDate, String gamePlatforms, String gameUrl) {
        this.gameCoverUrl = gameCoverUrl;
        this.gameName = gameName;
        this.gameReleaseDate = gameReleaseDate;
        this.gamePlatforms = gamePlatforms;
        this.gameUrl = gameUrl;
    }

    public String getGameCoverUrl() {
        return gameCoverUrl;
    }

    public void setGameCoverUrl(String gameCoverUrl) {
        this.gameCoverUrl = gameCoverUrl;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getGameReleaseDate() {
        return gameReleaseDate;
    }

    public void setGameReleaseDate(String gameReleaseDate) {
        this.gameReleaseDate = gameReleaseDate;
    }

    public String getGamePlatforms() {
        return gamePlatforms;
    }

    public void setGamePlatforms(String gamePlatforms) {
        this.gamePlatforms = gamePlatforms;
    }

    public String getGameUrl() {
        return gameUrl;
    }

    public void setGameUrl(String gameUrl) {
        this.gameUrl = gameUrl;
    }
}
