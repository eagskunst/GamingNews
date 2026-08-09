package com.eagskunst.emmanuel.gamingnews.core.data.source.local

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedUrlsCategoryDto(
    val language: String,
    @SerialName("all") val allUrls: List<String>,
    @SerialName("sony") val sonyUrls: List<String>,
    @SerialName("microsoft") val microsoftUrls: List<String>,
    @SerialName("nintendo") val nintendoUrls: List<String>,
    @SerialName("pc") val pcUrls: List<String>
)
