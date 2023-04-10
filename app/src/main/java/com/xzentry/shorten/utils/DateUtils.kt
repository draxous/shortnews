package com.xzentry.shorten.utils

import android.content.Context
import android.content.res.Resources
import com.xzentry.shorten.R
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt


object DateUtils {
    fun timeAgo(context: Context?, date: Date): String {
        val sb = StringBuilder()
        context?.let { context ->
            val diff: Long = Date().time - date.time
            val r: Resources = context.resources

            val prefix = r.getString(R.string.time_ago_prefix)
            val suffix: String = r.getString(R.string.time_ago_suffix)
            val seconds = abs(diff) / 1000.toDouble()
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24
            val weeks = days / 7
            val months = weeks / 4
            val years = days / 365
            val words: String
            words = when {
                minutes < 60 -> {
                    r.getString(R.string.time_ago_minutes)
                }
                hours < 1  -> {
                    r.getString(R.string.time_ago_one_hour)
                }
                hours < 2  -> {
                    r.getString(R.string.time_ago_two_hour)
                }
                hours < 24 -> {
                    r.getString(R.string.time_ago_hours)
                }
                days.roundToInt() == 1 -> {
                    r.getString(R.string.time_ago_day, days.roundToInt())
                }
                days > 1 && days < 7 -> {
                    r.getString(R.string.time_ago_days, days.roundToInt())
                }
                weeks.roundToInt() == 1 -> {
                    r.getString(R.string.time_ago_week, weeks.roundToInt())
                }
                weeks > 1 && weeks < 4 -> {
                    r.getString(R.string.time_ago_weeks, weeks.roundToInt())
                }
                months.roundToInt() == 1 -> {
                    r.getString(R.string.time_ago_month, months.roundToInt())
                }
                months > 1 && months < 12 -> {
                    r.getString(R.string.time_ago_months, months.roundToInt())
                }
                years.roundToInt() == 1 -> {
                    r.getString(R.string.time_ago_year, years.roundToInt())
                }
                else -> {
                    r.getString(R.string.time_ago_years, years.roundToInt())
                }
            }
            if (prefix != null && prefix.isNotEmpty()) {
                sb.append(prefix).append(" ");
            }

            sb.append(words);
            if (suffix != null && suffix.isNotEmpty()) {
                sb.append(" ").append(suffix)
            }
        }
        return sb.toString().trim { it <= ' ' }
    }
}
