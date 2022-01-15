package com.aemerse.quanage.constants

import androidx.annotation.IntDef

@IntDef(RNGType.NUMBER, RNGType.DICE, RNGType.COINS)
annotation class RNGType {
    companion object {
        const val NUMBER = 0
        const val DICE = 1
        const val COINS = 2
    }
}