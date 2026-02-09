package com.truvision.app.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Streaming

interface TruVisionApi {
    
    @POST("capture/start")
    suspend fun startCapture(): Response<CaptureStartResponse>
    
    @POST("capture")
    suspend fun captureImage(): Response<CaptureImageResponse>
    
    @POST("analyze")
    suspend fun analyzeImage(@Body request: AnalyzeImageRequest): Response<AnalyzeImageResponse>


    
    @GET("jobs/{job_id}")
    suspend fun getJobStatus(
        @Path("job_id") jobId: String
    ): Response<JobStatusResponse>
    
    @GET("health")
    suspend fun health(): Response<Map<String, String>>

    @GET("samples")
    suspend fun getSamples(): Response<List<SampleResponse>>
    
    @GET("export/{job_id}")
    @Streaming
    fun exportJob(@Path("job_id") jobId: String): Call<ResponseBody>

    @DELETE("samples/{job_id}")
    suspend fun deleteSample(@Path("job_id") jobId: String): Response<Map<String, Any>>
}
