package com.eagskunst.emmanuel.gamingnews.api;

import com.eagskunst.emmanuel.gamingnews.credentials.Credentials;
import com.eagskunst.emmanuel.gamingnews.models.Response;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

/**
 * Created by eagskunst on 10/01/2019
 */
public interface GamesApi {
    @Headers({
            "Accept: application/json",
            "user-key: "+Credentials.igdbCredential
    })
    @GET("release_dates/?fields=*,game.name,game.cover.*,game.url&order=date:asc&limit=50")
    Call<List<Response>> getReleasingSoonGames( @Query(value = "filter[date][gt]", encoded = true) long timeStamp, @Query(value = "filter[platform][eq]", encoded = true)int platform);
}
