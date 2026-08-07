package com.eagskunst.emmanuel.gamingnews.fragments.releases.mvp

import android.util.Log
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.api.GamesApi
import com.eagskunst.emmanuel.gamingnews.models.Response
import retrofit2.Call
import retrofit2.Callback

/**
 * Created by eagskunst on 11/01/2019
 */
class ReleaseModel(private val gamesApi: GamesApi) {
    private val TAG = ReleaseModel::class.java.simpleName

    fun getReleasesByPlatform(millis: Long, platform: Int, listener: ReleaseView.OnReleasesListener) {
        gamesApi.getReleasingSoonGames(millis, platform).enqueue(object : Callback<List<Response>> {
            override fun onResponse(call: Call<List<Response>>, response: retrofit2.Response<List<Response>>) {
                if (response.isSuccessful && response.body() != null) {
                    listener.onGetReleasesSuccess(response.body()!!)
                } else {
                    Log.e(TAG, "onResponse: Error in response, body: " + response.errorBody())
                }
            }

            override fun onFailure(call: Call<List<Response>>, t: Throwable) {
                Log.e(
                    TAG, "onFailure: Fail getting games for platform n° " + platform + " message: " + t.message,
                    t
                )
                listener.onGetReleasesFail(R.string.cant_get_releases)
            }
        })
    }
}
