package com.eagskunst.emmanuel.gamingnews.core.data.source.local

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReviewFeedDto(
    val id: String,
    val language: String,
    val name: String,
    val url: String,
    @SerialName("eligibilityPolicy") val eligibilityPolicyName: String
)
