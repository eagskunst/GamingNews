package com.eagskunst.emmanuel.gamingnews.core.data.source.local

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedProviderDto(
    val id: String,
    val name: String,
    val url: String
)

@Serializable
data class FeedUrlsCategoryDto(
    val language: String,
    @SerialName("all") val allProviders: List<FeedProviderDto>,
    @SerialName("sony") val sonyProviders: List<FeedProviderDto>,
    @SerialName("microsoft") val microsoftProviders: List<FeedProviderDto>,
    @SerialName("nintendo") val nintendoProviders: List<FeedProviderDto>,
    @SerialName("pc") val pcProviders: List<FeedProviderDto>
)
