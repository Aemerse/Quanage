package com.aemerse.quanage.constants

import androidx.annotation.IntDef

@IntDef(QRNGType.NUMBER, QRNGType.DICE, QRNGType.COINS)
annotation class QRNGType {
    companion object {
        const val NUMBER = 0
        const val DICE = 1
        const val COINS = 2
    }
}