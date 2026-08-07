package com.eagskunst.emmanuel.gamingnews.fragments.releases.di

import com.eagskunst.emmanuel.gamingnews.fragments.releases.ReleasesFragment
import com.eagskunst.emmanuel.gamingnews.utility.di.IgdbComponent
import dagger.Component

/**
 * Created by eagskunst on 11/01/2019
 */
@ReleasesScope
@Component(dependencies = [IgdbComponent::class], modules = [ReleasesModule::class])
interface ReleasesComponent {
    fun inject(releasesFragment: ReleasesFragment)
}
