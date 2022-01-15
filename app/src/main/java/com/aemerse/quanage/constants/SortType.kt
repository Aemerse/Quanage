package com.aemerse.quanage.constants

import androidx.annotation.IntDef

@IntDef(SortType.NONE, SortType.ASCENDING, SortType.DESCENDING)
annotation class SortType {
    companion object {
        const val NONE = 0
        const val ASCENDING = 1
        const val DESCENDING = 2
    }
}