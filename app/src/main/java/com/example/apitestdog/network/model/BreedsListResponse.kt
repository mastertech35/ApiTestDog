package com.example.apitestdog.network.model

import com.google.gson.annotations.SerializedName

data class BreedsListResponse(
    @SerializedName("message")
    val message: Map<String, List<String>>,
    @SerializedName("status")
    val status: String,
)
