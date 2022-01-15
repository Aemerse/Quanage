package com.aemerse.quanage.models

import com.aemerse.quanage.constants.SortType
import java.util.*

class RNGSettings {
    var minimum = 0
    var maximum = 0
    var numNumbers = 0
    var excludedNumbers: ArrayList<Int>? = null

    @SortType
    var sortType = 0
    var isNoDupes = false
    var isShowSum = false
    var isHideExcluded = false
}