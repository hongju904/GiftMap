package com.example.giftmap

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface KakaoAPI {
    @GET("v2/local/search/keyword.json")
    fun getSearchKeyword(
        @Header("Authorization") key: String, // 카카오 API 인증키
        @Query("query") query: String // 검색을 원하는 질의어

    ): Call<ResultSearchKeyword>
}