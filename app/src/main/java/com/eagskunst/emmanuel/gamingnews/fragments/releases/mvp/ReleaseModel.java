package com.eagskunst.emmanuel.gamingnews.fragments.releases.mvp;

import android.util.Log;

import com.eagskunst.emmanuel.gamingnews.api.GamesApi;
import com.eagskunst.emmanuel.gamingnews.models.Response;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by eagskunst on 11/01/2019
 */
public class ReleaseModel {
    private String TAG = ReleaseModel.class.getSimpleName();
    private GamesApi gamesApi;

    public ReleaseModel(GamesApi gamesApi){
        this.gamesApi = gamesApi;
    }

    public void getReleasesByPlatform(long millis, int platform, ReleaseView.OnReleasesListener listener){
        gamesApi.getReleasingSoonGames(millis, platform).enqueue(new Callback<List<Response>>() {
            @Override
            public void onResponse(Call<List<Response>> call, retrofit2.Response<List<Response>> response) {
                if(response.isSuccessful() && response.body() != null){
                    listener.onGetReleasesSuccess(response.body());
                }
                else {
                    Log.e(TAG, "onResponse: Error in response, body: "+response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<List<Response>> call, Throwable t) {
                Log.e(TAG, "onFailure: Fail getting games for platform n° "+platform+" message: "+t.getMessage()
                        ,t);
                listener.onGetReleasesFail("Retrofit fail!");
            }
        });
    }
}
