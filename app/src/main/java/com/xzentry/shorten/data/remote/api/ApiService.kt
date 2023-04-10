package com.xzentry.shorten.data.remote.api


import com.xzentry.shorten.data.remote.model.response.*
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiService {

    @GET("posts")
    fun fetchLatestPosts(@Query("lang") lang:String,
                         @Query("status") status:Int,
                         @Query("cat") catId:Int,
                         @Query("tstamp") lastPostUpdatedAt:String,
                         @Query("source") sourceId:Int
    ): Observable<PostApiResponse>

    @GET("oldPosts")
    fun fetchOlderPosts(@Query("lang") lang:String,
                        @Query("status") status:Int,
                        @Query("cat") catId:Int,
                        @Query("tstamp") updatedAt:String): Observable<PostApiResponse>

    @GET("categories")
    fun fetchCategories(): Observable<CategoryApiResponse>

    @POST("createUser")
    fun createUser(@Body device: RequestBody): Observable<LoginApiResponse>

    @POST("findPosts")
    fun searchPosts(@Body keyword: RequestBody): Observable<SearchApiResponse>

    @GET("advertisements")
    fun fetchAds(): Observable<AdsApiResponse>

}
