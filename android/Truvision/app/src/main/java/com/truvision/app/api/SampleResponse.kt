package com.truvision.app.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SampleResponse(
    @Json(name = "job_id") val jobId: String,
    @Json(name = "status") val status: String,
    @Json(name = "detected_count") val detectedCount: Int? = null,
    @Json(name = "timestamp") val timestamp: String? = null
)
