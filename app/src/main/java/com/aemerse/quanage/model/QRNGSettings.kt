package com.aemerse.quanage.model

import com.aemerse.quanage.constants.SortType
import java.util.*

class QRNGSettings {
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