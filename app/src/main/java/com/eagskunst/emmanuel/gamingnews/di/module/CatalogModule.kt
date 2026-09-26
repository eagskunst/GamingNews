package com.eagskunst.emmanuel.gamingnews.di.module

import com.eagskunst.emmanuel.gamingnews.core.data.source.local.BundledPlatformCatalog
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.PlatformCatalog
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CatalogModule {

    @Binds
    @Singleton
    abstract fun bindPlatformCatalog(impl: BundledPlatformCatalog): PlatformCatalog
}
