package com.eagskunst.emmanuel.gamingnews.fragments.releases.mvp

import com.eagskunst.emmanuel.gamingnews.models.ReleasesModel
import com.eagskunst.emmanuel.gamingnews.models.Response

/**
 * Created by eagskunst on 11/01/2019
 */
interface ReleaseView {

    interface OnReleasesListener {
        fun onGetReleasesSuccess(gamesList: List<Response>)
        fun onGetReleasesFail(message: String)
        fun onGetReleasesFail(message: Int)
    }

    interface Presenter {
        fun getReleasesByPlatform()
        fun createView(view: View)
        fun destroyView()
        fun sortListByDate()
        fun erasePassedDate(list: MutableList<ReleasesModel>, day: Int)

        companion object {
            @JvmField
            val platforms = intArrayOf(6, 49, 48, 130)
        }
    }

    interface View {
        fun showToastError(message: String)
        fun showToastError(message: Int)
        fun updateList(releasesList: List<ReleasesModel>)
        fun getNewReleases()
        fun changeTextMessage(message: Int)
        fun setTryAgain()
    }
}
