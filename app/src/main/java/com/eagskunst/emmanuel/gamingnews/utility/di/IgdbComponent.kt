package com.eagskunst.emmanuel.gamingnews.utility.di

import com.eagskunst.emmanuel.gamingnews.api.GamesApi
import dagger.Component

/**
 * Created by eagskunst on 11/01/2019
 */
@IgbScope
@Component(modules = [IgdbModule::class])
interface IgdbComponent {
    fun createApi(): GamesApi
}
