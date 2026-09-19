package com.eagskunst.emmanuel.gamingnews.core.domain.model

data class ReviewSource(
    val id: String,
    val language: String,
    val name: String,
    val url: String,
    val eligibilityPolicy: ReviewEligibilityPolicy
)
