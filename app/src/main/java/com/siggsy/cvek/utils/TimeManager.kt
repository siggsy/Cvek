package com.siggsy.cvek.utils

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class TimeManager {
    companion object Tools {
        fun getCurrentYear(): Int {
            val c: Calendar = Calendar.getInstance()
            var year: Int = c.get(Calendar.YEAR)
            c.set(year, 7, 25, 0, 0, 0)
            if (Date().time < c.time.time) {
                year--
            }
            return year
        }
        fun getCurrentWeek(): Pair<Date, Date> {
            val cal = Calendar.getInstance()

            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            val mon = cal.time

            cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
            val sat = cal.time

            return Pair(mon, sat)
        }
        fun convertDate(dateString: String): Date {
            val dateOnly = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return dateOnly.parse(dateString)!!
        }
    }
}