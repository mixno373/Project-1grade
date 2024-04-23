package ru.diverstat.discoin

import java.text.DecimalFormat

val API_BASE_URL = "https://diverstat.ru/discoin/"

val WalletItemsList = mutableListOf(
    WalletItem("Монетка", 0, 1, 5, 150),
    WalletItem("Майнер", 0, 5, 55, 100),
    WalletItem("Зимняя заначка", 0, 10, 1500, 50),
    WalletItem("Золотой слиток", 0, 15, 3500, 41),
    WalletItem("Центральный Банк", 0, 20,6000, 36),
    WalletItem("Денежный Арсенал", 0, 40, 8000, 30),
    WalletItem("Тайник Императора", 0, 50, 10000, 25),
    WalletItem("Неприкосновенный Запас", 0, 75, 100000, 20),
    WalletItem("Драконий Сейф", 0, 90, 250000, 15),
    WalletItem("Казначейский Сундук", 0, 100, 700000, 12),
    WalletItem("Золотовалютный Резерв", 0, 115, 1000000, 5),
    WalletItem("Криптовалютный Кошель", 0, 130, 3500000, 3),
    WalletItem("Сокровищница Миллионера", 0, 150, 5000000, 2),
    WalletItem("Денежная Империя", 0, 250, 10000000, 1)
)

fun formatter(n: Long) = DecimalFormat("#,###").format(n).replace(",", ".")