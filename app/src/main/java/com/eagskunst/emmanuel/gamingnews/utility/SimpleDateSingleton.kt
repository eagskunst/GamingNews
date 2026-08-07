package com.eagskunst.emmanuel.gamingnews.utility

import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Created by eagskunst in 27/2/2020.
 */
class SimpleDateSingleton private constructor() {
    val inputSdf: SimpleDateFormat = SimpleDateFormat(Constants.NEWS_DATE_RSS_FORMAT, Locale.US)
    val toSdf: SimpleDateFormat = SimpleDateFormat(Constants.OUTPUT_FORMAT, Locale.US)

    companion object {
        private val ourInstance = SimpleDateSingleton()

        @JvmStatic
        fun getInstance(): SimpleDateSingleton {
            return ourInstance
        }
    }
}
