package ru.diverstat.discoin

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.diverstat.discoin.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit
import kotlin.math.pow
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var coroJob: Job = Job()
    private var pageScope = CoroutineScope(Dispatchers.IO)

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private lateinit var retrofit: Retrofit

    private val okHttpClientvalor = OkHttpClient.Builder()
        .connectTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Открываем хранилище Shared Preferences
        sharedPreferences = getSharedPreferences(
            "shared_preferences",
            Context.MODE_PRIVATE
        )
        editor = sharedPreferences.edit()

        binding.login.setText(sharedPreferences.getString("login", "").toString())
        binding.password.setText(sharedPreferences.getString("password", "").toString())

        retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClientvalor)
            .baseUrl(API_BASE_URL)
            .build()

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
        requestPermissions(
            arrayOf(
                "android.permission.INTERNET"
            ), 1
        )
    }

    private fun validateLogin(): Boolean {
        val login = binding.login.text
        if (login.length < 3) {
            Toast.makeText(baseContext, "Логин не может быть менее 3 символов", Toast.LENGTH_SHORT)
                .show()
            return false
        }
        if (login.length > 15) {
            Toast.makeText(baseContext, "Логин не может быть более 15 символов", Toast.LENGTH_SHORT)
                .show()
            return false
        }
        if (!login.matches(Regex("[a-zA-Z-0-9]+"))) {
            Toast.makeText(
                baseContext,
                "Логин содержит недопустимые символы. Используйте: a-z, A-Z, -, 0-9",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        return true
    }

    private fun validatePassword(): Boolean {
        val password = binding.password.text
        if (password.length < 5) {
            Toast.makeText(baseContext, "Пароль не может быть менее 5 символов", Toast.LENGTH_SHORT)
                .show()
            return false
        }
        if (password.length > 20) {
            Toast.makeText(
                baseContext,
                "Пароль не может быть более 20 символов",
                Toast.LENGTH_SHORT
            ).show()
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
        if (!validateLogin()) {
            return
        }
        if (!validatePassword()) {
            return
        }

        val login = binding.login.text.toString()
        val password = binding.password.text.toString()
        val access_code = getAccessCode()

        editor = sharedPreferences.edit()
        editor.putString("login", login)
        editor.putString("password", password)
        editor.putString("access_code", access_code)
        editor.commit()

        getAuthData(login, access_code)
    }

    private fun getAuthData(login: String, access_code: String) {
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
                        val authData = response.body()

                        if (authData!!.status == 100 || authData!!.status == 101) {
                            val intent = Intent(this@MainActivity, Profile::class.java)
                            startActivity(intent)

                            finish()
                        } else {
                            val error_text = when (authData!!.status) {
                                100 -> "Регистрация прошла успешно!"
                                101 -> "Авторизован!"

                                300 -> "Достигнут лимит запросов! Подождите немного."

                                400 -> "Операция не поддерживается."
                                401 -> "Не отправлены логин или пароль."
                                402 -> "Не авторизован."
                                403 -> "Ошибка регистрации. Логин занят."

                                501 -> "API не отвечает ¯\\_(ツ)_/¯"

                                else -> "Неизвестная ошибка ¯\\_(ツ)_/¯"
                            }

                            Toast.makeText(this@MainActivity, error_text, Toast.LENGTH_SHORT).show()
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

}