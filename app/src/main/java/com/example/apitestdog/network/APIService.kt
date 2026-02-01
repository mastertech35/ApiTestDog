package com.example.apitestdog.network

import com.example.apitestdog.network.model.BreedsListResponse
import com.example.apitestdog.network.model.BreedImagesResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface APIService {

    @GET("api/breeds/list/all")
    suspend fun getBreedsList(): BreedsListResponse

    @GET("api/breed/{breed}/images/random/3")
    suspend fun getRandomImagesByBreed(
        @Path("breed") breed: String,
    ): BreedImagesResponse
}
