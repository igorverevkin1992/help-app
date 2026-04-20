package com.helpapp.therapy.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface ClaudeApi {
    @POST("v1/messages")
    suspend fun createMessage(@Body request: MessageRequest): MessageResponse
}
