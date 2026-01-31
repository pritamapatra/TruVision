package com.truvision.app.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CaptureStartResponse(
    @Json(name = "job_id") val jobId: String,
    @Json(name = "status") val status: String
)

@JsonClass(generateAdapter = true)
data class JobStatusResponse(
    @Json(name = "job_id") val jobId: String,
    @Json(name = "status") val status: String,
    @Json(name = "detected_count") val detectedCount: Int? = null
)

@JsonClass(generateAdapter = true)
data class ApiError(
    @Json(name = "error") val error: String
)
