package com.sweetpro.sharedstoragereadsample

import java.util.*

public fun getDaysAgo(ago: Int): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DATE, ago)
    val date: Int = (calendar.timeInMillis / 1000).toInt()
    return date.toString()
}