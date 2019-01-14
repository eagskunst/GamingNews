package com.eagskunst.emmanuel.gamingnews.fragments.releases.mvp;

import com.eagskunst.emmanuel.gamingnews.models.ReleasesModel;
import com.eagskunst.emmanuel.gamingnews.models.Response;

import java.util.List;

/**
 * Created by eagskunst on 11/01/2019
 */
public interface ReleaseView {

    interface OnReleasesListener {
        void onGetReleasesSuccess(List<Response> gamesList);
        void onGetReleasesFail(String message);
        void onGetReleasesFail(int message);
    }

    interface Presenter {
        int platforms[] = new int[]{6,49,48,130};
        void getReleasesByPlatform();
        void createView(ReleaseView.View view);
        void destroyView();
        void sortListByDate();
        void erasePassedDate(List<ReleasesModel> list, int day);
    }

    interface View {
        void showToastError(String message);
        void showToastError(int message);
        void updateList(List<ReleasesModel> releasesList);
        void getNewReleases();
        void changeTextMessage(int message);
        void setTryAgain();
    }

}
