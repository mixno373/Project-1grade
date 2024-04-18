package ru.diverstat.discoin

import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import ru.diverstat.discoin.databinding.ActivityMainBinding
import java.lang.Math.pow
import java.util.concurrent.TimeUnit
import kotlin.math.pow
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var coroJob: Job = Job()
    private var pageScope = CoroutineScope(Dispatchers.IO)

    private val okHttpClientvalor = OkHttpClient.Builder()
        .connectTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.auth.setOnClickListener {
            onAuthCLick()
        }

        // Запросить права на доступ в Интернет
        requestPerms()
    }

    override fun onResume() {
        super.onResume()

        pageScope = CoroutineScope(Dispatchers.IO)
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

    private fun validateLogin(): Boolean {
        val login = binding.login.text
        if (login.length < 3) {
            Toast.makeText(baseContext, "Логин не может быть менее 3 символов", Toast.LENGTH_SHORT).show()
            return false
        }
        if (login.length > 10) {
            Toast.makeText(baseContext, "Логин не может быть более 10 символов", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!login.matches(Regex("[a-zA-Z-0-9]+"))) {
            Toast.makeText(baseContext, "Логин содержит недопустимые символы. Используйте: a-z, A-Z, -, 0-9", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun validatePassword(): Boolean {
        val password = binding.password.text
        if (password.length < 5) {
            Toast.makeText(baseContext, "Пароль не может быть менее 5 символов", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.length > 20) {
            Toast.makeText(baseContext, "Пароль не может быть более 20 символов", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun getAccessCode(): String {
        val login = binding.login.text.toString()
        var password = binding.password.text.toString()

        val characterSet = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-."

        var seed = 0L
        val seedStr = "Discoin${login}APP"
        for (i in seedStr.indices) {
            seed += seedStr[i].code * 2.0.pow((i % 10).toDouble()).toLong()
        }

        password = (password + "IRNITU_ASUbz-23-1_Mineev").substring(0, 25)

        val random = Random(seed)
        val access_code = StringBuilder()

        for (i in password.indices) {
            var rIndex = random.nextInt(characterSet.length) + password[i].code
            rIndex %= characterSet.length
            access_code.append(characterSet[rIndex])
        }

        return access_code.toString()
    }

    private fun onAuthCLick() {
        if (!validateLogin()) { return }
        if (!validatePassword()) { return }

        val login = binding.login.text.toString()
        val access_code = getAccessCode()

        getWallet(login, access_code)
    }

    private fun getWallet(login: String, access_code: String) {

        // Create Retrofit
        val retrofit = Retrofit.Builder()
            .client(okHttpClientvalor)
            .baseUrl(API_BASE_URL)
            .build()

        // Create Service
        val service = retrofit.create(APIService::class.java)

        pageScope.launch {
            /*
             * For @Query: You need to replace the following line with val response = service.getEmployees(2)
             * For @Path: You need to replace the following line with val response = service.getEmployee(53)
             */

            // Create JSON using JSONObject
            val jsonObject = JSONObject()
            jsonObject.put("login", login)
            jsonObject.put("access_code", access_code)

            // Convert JSONObject to String
            val jsonObjectString = jsonObject.toString()

            // Create RequestBody ( We're not using any converter, like GsonConverter, MoshiConverter e.t.c, that's why we use RequestBody )
            val requestBody = jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())

            // Do the GET request and get response
            val response = service.auth(requestBody)

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    try {
                        // Convert raw JSON to pretty JSON using GSON library
                        val wallet = response.body()

                        if (wallet!!.balance >= 0) {
                            // TODO load wallet activity
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

    private fun updateFields(fields: Gson) {

    }
}