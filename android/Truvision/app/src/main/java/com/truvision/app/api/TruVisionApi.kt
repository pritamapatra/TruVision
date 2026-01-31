package com.truvision.app.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface TruVisionApi {
    
    @POST("capture/start")
    suspend fun startCapture(): Response<CaptureStartResponse>
    
    @GET("jobs/{job_id}")
    suspend fun getJobStatus(
        @Path("job_id") jobId: String
    ): Response<JobStatusResponse>
    
    @GET("health")
    suspend fun health(): Response<Map<String, String>>
}
