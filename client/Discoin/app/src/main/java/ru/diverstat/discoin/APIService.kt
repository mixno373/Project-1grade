package ru.diverstat.discoin

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface APIService {

    @Headers("Content-Type: application/json")
    @POST("auth")
    suspend fun auth(@Body requestBody: RequestBody): Response<Wallet>

}

data class Wallet(
    // on below line creating variables for our modal class
    // make sure that variable name should be same to
    // that of key which is used in json response.
    var balance: Int,
    var profit: Int,
)