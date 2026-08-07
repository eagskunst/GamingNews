package com.eagskunst.emmanuel.gamingnews.core.data.source.local

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedUrlsCategoryDto(
    val language: String,
    @SerialName("all_urls") val allUrls: List<String>,
    @SerialName("ps4_urls") val ps4Urls: List<String>,
    @SerialName("xboxO_urls") val xboxUrls: List<String>,
    @SerialName("switch_urls") val switchUrls: List<String>,
    @SerialName("pc_urls") val pcUrls: List<String>
)
