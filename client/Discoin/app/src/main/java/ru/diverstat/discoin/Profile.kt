package ru.diverstat.discoin

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.GridView
import android.widget.ListView
import android.widget.Toast
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.diverstat.discoin.databinding.ActivityProfileBinding
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

class Profile : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    private var coroJob: Job = Job()
    private var pageScope = CoroutineScope(Dispatchers.IO)

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private var login = ""
    private var access_code = ""

    private var iListView: GridView? = null
    private lateinit var adapter: ItemsListViewAdapter
    private var shouldUpdateAdapter = true

    private var wallet: Wallet = Wallet(0,0,0, mutableListOf())

    private lateinit var retrofit: Retrofit

    private val okHttpClientvalor = OkHttpClient.Builder()
        .connectTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Открываем хранилище Shared Preferences
        sharedPreferences = getSharedPreferences(
            "shared_preferences",
            Context.MODE_PRIVATE
        )
        editor = sharedPreferences.edit()
        login = sharedPreferences.getString("login", "").toString()
        access_code = sharedPreferences.getString("access_code", "").toString()

        retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClientvalor)
            .baseUrl(API_BASE_URL)
            .build()

        // Запросить права на доступ в Интернет
        requestPerms()

        loadAdapter()
        binding.items.setOnItemClickListener { adapterView, view, i, l -> pageScope.launch { buyItem(wallet.items[i].name) } }
    }

    override fun onResume() {
        super.onResume()

        pageScope = CoroutineScope(Dispatchers.IO)

        pageScope.launch {
            while (true) {
                getWalletData()
                delay(30_000)
            }
        }

        pageScope.launch {
            while (true) {
                withContext(Dispatchers.Main) {
                    updateWalletInfo()
                }
                delay(1_000)
            }
        }
    }

    override fun onPause() {
        pageScope.coroutineContext.cancelChildren()
        pageScope.cancel()

        super.onPause()
    }

    override fun onDestroy() {
        pageScope.coroutineContext.cancelChildren()
        pageScope.cancel()

        super.onDestroy()
    }

    private fun requestPerms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    "android.permission.INTERNET"
                ), 1
            )
        }
    }

    private fun loadAdapter() {
        if (shouldUpdateAdapter == false) {
            return
        }
        shouldUpdateAdapter = false
        iListView = binding.items
        adapter = ItemsListViewAdapter(this, wallet.items)
        (iListView as GridView).adapter = adapter
        adapter.notifyDataSetChanged()
    }

    private fun updateWalletInfo() {
        val ts: Long = System.currentTimeMillis() / 1000
        wallet.balance = wallet.balance + (ts - wallet.ts) * wallet.profit
        wallet.ts = ts

        binding.balance.text = "${formatter(wallet.balance / 1_000_000)}.${(wallet.balance % 1_000_000).toString().padStart(6, '0')} \uD835\uDD6F"
    }

    private suspend fun getWalletData() {
        // Create Service
        val service = retrofit.create(APIService::class.java)

        // Create JSON using JSONObject
        val jsonObject = JSONObject()
        jsonObject.put("login", login)
        jsonObject.put("access_code", access_code)

        // Convert JSONObject to String
        val jsonObjectString = jsonObject.toString()

        // Create RequestBody ( We're not using any converter, like GsonConverter, MoshiConverter e.t.c, that's why we use RequestBody )
        val requestBody = jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())

        // Do the GET request and get response
        val response = service.wallet(requestBody)

        withContext(Dispatchers.Main) {
            if (response.isSuccessful) {
                try {
                    // Convert raw JSON to pretty JSON using GSON library
                    wallet = response.body() ?: Wallet(0,0,0, arrayListOf())

                    loadAdapter()

                } catch (e: Exception) {
                    Log.d("JSON Exception:", e.toString())
                }

            } else {
                Log.e("RETROFIT_ERROR", response.code().toString())
            }
        }

    }

    private suspend fun buyItem(name: String, count: Int = 1) {
        // Create Service
        val service = retrofit.create(APIService::class.java)

        // Create JSON using JSONObject
        val jsonObject = JSONObject()
        jsonObject.put("login", login)
        jsonObject.put("access_code", access_code)
        jsonObject.put("name", name)
        jsonObject.put("count", count)

        // Convert JSONObject to String
        val jsonObjectString = jsonObject.toString()

        // Create RequestBody ( We're not using any converter, like GsonConverter, MoshiConverter e.t.c, that's why we use RequestBody )
        val requestBody = jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())

        // Do the GET request and get response
        val response = service.buyitem(requestBody)

        withContext(Dispatchers.Main) {
            if (response.isSuccessful) {
                try {
                    val walletOrAuthData = response.body()

                    if ((walletOrAuthData?.status ?: 0) > 0) {
                        val authData = response.body() as AuthData

                        val error_text = when (authData.status) {
                            100 -> "Регистрация прошла успешно!"
                            101 -> "Авторизован!"

                            300 -> "Достигнут лимит запросов! Подождите немного."
                            301 -> "Предмет не существует."

                            400 -> "Операция не поддерживается."
                            401 -> "Не отправлены логин или пароль."
                            402 -> "Не авторизован."
                            403 -> "Ошибка регистрации. Логин занят."

                            501 -> "API не отвечает ¯\\_(ツ)_/¯"

                            else -> "Неизвестная ошибка ¯\\_(ツ)_/¯"
                        }

                        Toast.makeText(this@Profile, error_text, Toast.LENGTH_SHORT).show()
                    } else {
                        wallet = Wallet(
                            walletOrAuthData?.balance ?: 0,
                            walletOrAuthData?.profit ?: 0,
                            walletOrAuthData?.ts ?: 0,
                            walletOrAuthData?.items ?: mutableListOf(),
                        )

                        shouldUpdateAdapter = true
                        loadAdapter()
                    }
                } catch (e: Exception) {
                    Log.d("JSON Exception:", e.toString())
                }

            } else {
                Log.e("RETROFIT_ERROR", response.code().toString())
            }
        }

    }

}