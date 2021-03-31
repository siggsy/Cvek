package com.siggsy.cvek.ui.schedule

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.siggsy.cvek.R
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.collections.ArrayList

val MAX_MONTHS = ChronoUnit.MONTHS.between(MIN, MAX).toInt()
class CalendarView : ViewPager {

    init {
        adapter = MonthAdapter(true) {}
        offscreenPageLimit = 3
    }

    var day = MIN
        set(value) {
            field = value
            currentItem = ChronoUnit.MONTHS.between(MIN, field).toInt()
        }

    constructor(context: Context) : super(context)
    constructor(context: Context, attr: AttributeSet?) : super(context, attr)

}

private class MonthAdapter(
        val week: Boolean,
        val onSelected: (LocalDate) -> Unit,
) : PagerAdapter() {

    override fun instantiateItem(parent: ViewGroup, position: Int): Any {
        val gridLayout = GridLayout(parent.context)
        gridLayout.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        )
        gridLayout.rowCount = if (week) 3 else 8
        gridLayout.columnCount = 7
        val allViews = ArrayList<View>()

        if (week) {
            val weekOffsetDate = MIN.plusWeeks(position.toLong())
            allViews.add(getMonthNameView(parent.context, weekOffsetDate.month.value))
            allViews.addAll(getWeekNameViews(parent.context))
            allViews.addAll(getWeekDayViews(parent.context, weekOffsetDate))
        } else {
            val monthOffsetDate = MIN.plusMonths(position.toLong())
            allViews.add(getMonthNameView(parent.context, monthOffsetDate.month.value))
            allViews.addAll(getWeekNameViews(parent.context))
            allViews.addAll(getWeekDayViews(parent.context, monthOffsetDate))
        }
        allViews.forEach { gridLayout.addView(it) }
        parent.addView(gridLayout)
        return gridLayout
    }

    override fun destroyItem(parent: ViewGroup, position: Int, `object`: Any) {
        parent.removeView(`object` as GridLayout)
    }

    override fun getCount(): Int = MAX_MONTHS

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

    private fun getMonthNameView(context: Context, month: Int) : View {
        return TextView(context).apply {
            text = Month.of(month).getDisplayName(TextStyle.FULL, Locale.getDefault())
            gravity = Gravity.CENTER
            val layoutParamsNew = GridLayout.LayoutParams()
            layoutParamsNew.setGravity(Gravity.FILL_HORIZONTAL)
            layoutParamsNew.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 7, 1f)
            layoutParams = layoutParamsNew
        }
    }
    private fun getMonthDayViews(context: Context, selectedDate: LocalDate) : List<View> {

        val prevMonthDays = YearMonth.of(selectedDate.year, selectedDate.month - 1).lengthOfMonth()
        val currMonthDays = YearMonth.of(selectedDate.year, selectedDate.month).lengthOfMonth()
        val diff = selectedDate.dayOfWeek.value - 1

        val days = ArrayList<View>()

        // Previous month days
        for (day in (prevMonthDays - diff + 1) .. (prevMonthDays))
            days.add(getDayView(context, day, false, event = Math.random() > 0.5f))

        // Current month days
        for (day in 1 .. (currMonthDays))
            days.add(getDayView(context, day, true, event = Math.random() > 0.5f))

        // Next month days
        for (day in 1 .. (42 - diff - currMonthDays))
            days.add(getDayView(context, day, false, event = Math.random() > 0.5f))

        return days

    }

    private fun getWeekNameViews(context: Context) : List<View> {
        return (1 .. 7).map { day ->
            TextView(context).apply {
                text = DayOfWeek.of(day).getDisplayName(TextStyle.NARROW, Locale.getDefault())
                gravity = Gravity.CENTER
                val layoutParamsNew = GridLayout.LayoutParams()
                layoutParamsNew.setGravity(Gravity.FILL_HORIZONTAL)
                layoutParamsNew.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                layoutParams = layoutParamsNew
            }
        }
    }
    private fun getWeekDayViews(context: Context, selectedDate: LocalDate) : List<View> {
        val days = ArrayList<View>()
        val monday = selectedDate.minusDays(MIN.dayOfWeek.value - 1.toLong())
        val currMonth = selectedDate.month == monday.month
        val mondayMonthDays = YearMonth.of(monday.year, monday.month).lengthOfMonth()
        val nextMonthDays = YearMonth.of(monday.year, monday.month + 1).lengthOfMonth()

        if (mondayMonthDays - monday.dayOfMonth >= 6)
            for (day in monday.dayOfMonth..monday.dayOfMonth + 6)
                days.add(getDayView(context, day, currMonth))
        else {
            for (day in monday.dayOfMonth..mondayMonthDays)
                days.add(getDayView(context, day, currMonth))
            for (day in 1 .. 6 - mondayMonthDays + monday.dayOfMonth)
                days.add(getDayView(context, day, !currMonth))
        }

        return days
    }

    private fun getDayView(context: Context, day: Int, currentMonth: Boolean, color: Int = 0xFF673AB7.toInt(), event: Boolean = false) : View {

        return LayoutInflater.from(context).inflate(R.layout.calendar_day, null, false).apply {
            val dayText = findViewById<TextView>(R.id.calendar_day_tv)
            val background = findViewById<ImageView>(R.id.calendar_day_iv)

            dayText.text = "$day"
            val layoutParamsNew = GridLayout.LayoutParams()
            layoutParamsNew.setGravity(Gravity.FILL_HORIZONTAL)
            layoutParamsNew.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            layoutParams = layoutParamsNew



            if (event) {
                background.setColorFilter(color and 0xFFFFFF or (if (currentMonth) 0x10000000 else 0x04000000))
                dayText.setTextColor(color and 0xFFFFFF or (if (currentMonth) 0xFF000000.toInt() else 0x66000000))
            } else {
                dayText.setTextColor(if (currentMonth) Color.BLACK else Color.LTGRAY)
            }
        }
    }

}
