package com.xzentry.shorten.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Date, Hour and Minutes - Horizontal Display
 * ```
 * val myDateText = dateObject.toStringyyyyMMddhhmm()
 * ```
 */
fun Date?.toStringyyyyMMddhhmm(): CharSequence {
    this?.let { date ->
        return SimpleDateFormat("yyyy-MM-dd hh:mm aa", Locale.getDefault()).format(date)
    }
    return "----/--/-- --:--"
}

fun String?.toDateyyyyMMddhhmmss(): Date {
    this?.let { dateString ->
        return  SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(dateString)
    }
    return Date()
}

fun String?.toDateyyyyMMddhhmm(): Date {
    this?.let { dateString ->
        return  SimpleDateFormat("yyyy-MM-dd hh:mm").parse(dateString)
    }
    return Date()
}