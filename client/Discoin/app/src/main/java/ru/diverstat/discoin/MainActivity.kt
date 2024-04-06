package ru.diverstat.discoin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.*
import org.json.JSONArray
import ru.diverstat.discoin.databinding.ActivityMainBinding
import java.lang.Math.pow
import kotlin.math.pow
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var coroJob: Job = Job()
    private var pageScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.auth.setOnClickListener {
            onAuthCLick()
        }
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

        val login = binding.login.text
        val access_code = getAccessCode()

        coroJob = pageScope.launch {
            Log.d("MainActivity", "Access_code = '$access_code'")
        }
    }
}