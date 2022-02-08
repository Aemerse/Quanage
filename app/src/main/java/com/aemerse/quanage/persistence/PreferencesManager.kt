package com.aemerse.quanage.persistence

import android.content.Context
import android.content.SharedPreferences
import com.aemerse.quanage.constants.SortType
import com.aemerse.quanage.model.QRNGSettings
import java.util.*

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("sharedPrefFile",Context.MODE_PRIVATE)

    val numSides: String get() = prefs.getInt(NUM_SIDES, DEFAULT_NUM_DICE_SIDES).toString()
    val numDice: String get() = prefs.getInt(NUM_DICE, DEFAULT_NUM_DICE).toString()

    fun saveDiceSettings(numSides: String, numDice: String) {
        try {
            prefs.edit().putInt(NUM_SIDES, numSides.toInt()).apply()
            prefs.edit().putInt(NUM_DICE, numDice.toInt()).apply()
        } catch (exception: NumberFormatException) {
            prefs.edit().putInt(NUM_SIDES, DEFAULT_NUM_DICE_SIDES).apply()
            prefs.edit().putInt(NUM_DICE, DEFAULT_NUM_DICE).apply()
        }
    }

    val numCoins: Int
        get() = prefs.getInt(NUM_COINS, DEFAULT_NUM_COINS)

    fun saveNumCoins(numCoins: String) {
        try {
            prefs.edit().putInt(NUM_COINS, numCoins.toInt()).apply()
        } catch (ignored: NumberFormatException) {
        }
    }

    fun shouldPlaySounds(): Boolean {
        return prefs.getBoolean(PLAY_SOUNDS, true)
    }

    fun setPlaySounds(shouldPlay: Boolean) {
        prefs.edit().putBoolean(PLAY_SOUNDS, shouldPlay).apply()
    }

    var isShakeEnabled: Boolean
        get() = prefs.getBoolean(ENABLE_SHAKE, true)
        set(enableShake) {
            prefs.edit().putBoolean(ENABLE_SHAKE, enableShake).apply()
        }
    val rNGSettings: QRNGSettings
        get() {
            val rngSettings = QRNGSettings()
            rngSettings.minimum = prefs.getInt(MINIMUM_KEY, DEFAULT_MIN)
            rngSettings.maximum = prefs.getInt(MAXIMUM_KEY, DEFAULT_MAX)
            rngSettings.numNumbers = prefs.getInt(NUM_NUMBERS_KEY, DEFAULT_NUM_NUMBERS)
            val excludedNumberStrings = prefs.getStringSet(EXCLUDED_NUMBERS_KEY, DEFAULT_EXCLUDED_NUMBERS)
            val excludedNumbers = ArrayList<Int>()
            for (number in excludedNumberStrings!!) {
                excludedNumbers.add(Integer.valueOf(number))
            }
            excludedNumbers.sort()
            rngSettings.excludedNumbers = excludedNumbers
            rngSettings.sortType = prefs.getInt(SORT_TYPE_KEY, DEFAULT_SORT_TYPE)
            rngSettings.isNoDupes = prefs.getBoolean(NO_DUPES_KEY, DEFAULT_NO_DUPES)
            rngSettings.isShowSum = prefs.getBoolean(SHOW_SUM_KEY, DEFAULT_SHOW_SUM)
            rngSettings.isHideExcluded = prefs.getBoolean(HIDE_EXCLUDED_KEY, DEFAULT_HIDE_EXCLUDED)
            return rngSettings
        }

    fun saveRNGSettings(QRNGSettings: QRNGSettings) {
        val excludedNumbers: List<Int> = QRNGSettings.excludedNumbers!!
        val excludedNumberStrings: MutableSet<String> = HashSet()
        for (number in excludedNumbers) {
            excludedNumberStrings.add(number.toString())
        }
        prefs.edit().putInt(MINIMUM_KEY, QRNGSettings.minimum)
                .putInt(MAXIMUM_KEY, QRNGSettings.maximum)
                .putInt(NUM_NUMBERS_KEY, QRNGSettings.numNumbers)
                .putStringSet(EXCLUDED_NUMBERS_KEY, excludedNumberStrings)
                .putInt(SORT_TYPE_KEY, QRNGSettings.sortType)
                .putBoolean(NO_DUPES_KEY, QRNGSettings.isNoDupes)
                .putBoolean(SHOW_SUM_KEY, QRNGSettings.isShowSum)
                .putBoolean(HIDE_EXCLUDED_KEY, QRNGSettings.isHideExcluded)
                .apply()
    }

    companion object {
        private const val PLAY_SOUNDS = "playSounds"
        private const val ENABLE_SHAKE = "enableShake"

        private const val MINIMUM_KEY = "minimum"
        private const val MAXIMUM_KEY = "maximum"
        private const val NUM_NUMBERS_KEY = "numNumbers"
        private const val EXCLUDED_NUMBERS_KEY = "excludedNumbers"
        private const val SORT_TYPE_KEY = "sortType"
        private const val NO_DUPES_KEY = "noDupes"
        private const val SHOW_SUM_KEY = "showSum"
        private const val HIDE_EXCLUDED_KEY = "hideExcluded"
        private const val DEFAULT_MIN = 1
        private const val DEFAULT_MAX = 100
        private const val DEFAULT_NUM_NUMBERS = 1
        private val DEFAULT_EXCLUDED_NUMBERS: Set<String> = HashSet()

        @SortType
        private val DEFAULT_SORT_TYPE = SortType.NONE
        private const val DEFAULT_NO_DUPES = false
        private const val DEFAULT_SHOW_SUM = false
        private const val DEFAULT_HIDE_EXCLUDED = false

        // Dice
        private const val NUM_SIDES = "numSides"
        private const val NUM_DICE = "numDice"
        private const val DEFAULT_NUM_DICE = 2
        private const val DEFAULT_NUM_DICE_SIDES = 6

        // Coins
        private const val NUM_COINS = "numCoins"
        private const val DEFAULT_NUM_COINS = 1
    }

}