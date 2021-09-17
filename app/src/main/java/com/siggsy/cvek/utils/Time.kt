package com.siggsy.cvek.utils

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

fun Calendar.getCurrentYear(): Int {
    var year: Int = get(Calendar.YEAR)
    set(year, 7, 25, 0, 0, 0)
    if (Date().time < time.time) {
        year--
    }
    return year
}
fun Calendar.getCurrentWeek(): Pair<Date, Date> {
    set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    val mon = time

    set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
    val sat = time

    return Pair(mon, sat)
}
fun convertDate(dateString: String): Date {
    val dateOnly = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return dateOnly.parse(dateString)!!
}

val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssx")
fun String.toDate(): LocalDateTime =
    LocalDateTime.parse(this, formatter)
