package ru.diverstat.discoin

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface APIService {

    @Headers("Content-Type: application/json")
    @POST("auth")
    suspend fun auth(@Body requestBody: RequestBody): Response<AuthData>

    @Headers("Content-Type: application/json")
    @POST("wallet")
    suspend fun wallet(@Body requestBody: RequestBody): Response<Wallet>

    @Headers("Content-Type: application/json")
    @POST("buyitem")
    suspend fun buyitem(@Body requestBody: RequestBody): Response<Wallet>

}

data class AuthData(
    var message: String? = "",
    var status: Int? = 0
)

data class Wallet(
    var balance: Long,
    var profit: Long,
    var ts: Long,
    var items: List<WalletItem>
)

data class WalletItem(
    var name: String,
    var count: Long,
    var profit: Long,
    var cost: Long,
    var max: Long
)