package com.eagskunst.emmanuel.gamingnews.fragments.releases.di

import com.eagskunst.emmanuel.gamingnews.api.GamesApi
import com.eagskunst.emmanuel.gamingnews.fragments.releases.ReleasesFragment
import com.eagskunst.emmanuel.gamingnews.fragments.releases.mvp.ReleaseModel
import com.eagskunst.emmanuel.gamingnews.fragments.releases.mvp.ReleasePresenter
import dagger.Module
import dagger.Provides

/**
 * Created by eagskunst on 11/01/2019
 */
@Module
class ReleasesModule(private val releasesFragment: ReleasesFragment) {

    @Provides
    @ReleasesScope
    fun provideModel(gamesApi: GamesApi): ReleaseModel {
        return ReleaseModel(gamesApi)
    }

    @Provides
    @ReleasesScope
    fun providePresenter(model: ReleaseModel): ReleasePresenter {
        return ReleasePresenter(model)
    }
}
