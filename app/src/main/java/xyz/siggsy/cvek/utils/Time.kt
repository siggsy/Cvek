package xyz.siggsy.cvek.utils

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Get current school year; eg. int 2021 corresponds to a year 2021/2022
 * @receiver calendar instance to use
 */
fun Calendar.getCurrentYear(): Int {
    var year: Int = get(Calendar.YEAR)
    set(year, 7, 25, 0, 0, 0)
    if (Date().time < time.time) {
        year--
    }
    return year
}

/**
 * Get current week date limits
 * @receiver calendar instance to use
 * @return date (min: monday, max: saturday) pair
 */
fun Calendar.getCurrentWeek(): Pair<Date, Date> {
    set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    val mon = time

    set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
    val sat = time

    return Pair(mon, sat)
}

/**
 * Extension function for decoding date string
 * @receiver string to decode
 * @return string decoded to LocalDateTime
 */
fun String.toDate(format: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE): LocalDateTime =
    LocalDateTime.parse(this, format)
