package com.truvision.app.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SampleResponse(
    @Json(name = "job_id") val jobId: String,
    @Json(name = "status") val status: String,
    @Json(name = "detected_count") val detectedCount: Int? = null,
    @Json(name = "timestamp") val timestamp: String? = null,
    @Json(name = "capture_method") val captureMethod: String? = null,
    @Json(name = "image_path") val imagePath: String? = null,
    @Json(name = "latitude") val latitude: Double? = null,
    @Json(name = "longitude") val longitude: Double? = null,
    @Json(name = "accuracy") val accuracy: Double? = null,
    @Json(name = "location_method") val locationMethod: String? = null,
    @Json(name = "primary_polymer") val primaryPolymer: String? = null
)
