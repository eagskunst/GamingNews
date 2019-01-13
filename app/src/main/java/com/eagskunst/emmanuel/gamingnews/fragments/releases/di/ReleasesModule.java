package com.eagskunst.emmanuel.gamingnews.fragments.releases.di;

import com.eagskunst.emmanuel.gamingnews.api.GamesApi;
import com.eagskunst.emmanuel.gamingnews.fragments.releases.ReleasesFragment;
import com.eagskunst.emmanuel.gamingnews.fragments.releases.mvp.ReleaseModel;
import com.eagskunst.emmanuel.gamingnews.fragments.releases.mvp.ReleasePresenter;

import dagger.Module;
import dagger.Provides;

/**
 * Created by eagskunst on 11/01/2019
 */
@Module
public class ReleasesModule {
    private ReleasesFragment releasesFragment;

    public ReleasesModule(ReleasesFragment releasesFragment) {
        this.releasesFragment = releasesFragment;
    }

    @Provides
    @ReleasesScope
    public ReleaseModel provideModel(GamesApi gamesApi){
        return new ReleaseModel(gamesApi);
    }

    @Provides
    @ReleasesScope
    public ReleasePresenter providePresenter(ReleaseModel model){
        return new ReleasePresenter(model);
    }
}
