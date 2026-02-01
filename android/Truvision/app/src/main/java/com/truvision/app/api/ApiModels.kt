package com.truvision.app.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CaptureStartResponse(
    @Json(name = "job_id") val jobId: String,
    @Json(name = "status") val status: String
)

@JsonClass(generateAdapter = true)
data class Detection(
    @Json(name = "id") val id: Int,
    @Json(name = "polymer_type") val polymerType: String,
    @Json(name = "confidence") val confidence: Double,
    @Json(name = "bbox") val bbox: List<Int>
)

@JsonClass(generateAdapter = true)
data class JobStatusResponse(
    @Json(name = "job_id") val jobId: String,
    @Json(name = "status") val status: String,
    @Json(name = "detected_count") val detectedCount: Int? = null,
    @Json(name = "image_path") val imagePath: String? = null,
    @Json(name = "detections") val detections: List<Detection>? = null
)

@JsonClass(generateAdapter = true)
data class ApiError(
    @Json(name = "error") val error: String
)
