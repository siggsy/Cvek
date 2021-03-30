package com.siggsy.cvek.ui.schedule

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.siggsy.cvek.R
import com.siggsy.cvek.data.easistent.Week
import java.time.LocalDate
import java.time.temporal.ChronoUnit

val MIN = LocalDate.of(1970, 1, 1)
val MAX = LocalDate.of(2100, 1, 1)
val MAX_DAYS = ChronoUnit.DAYS.between(MIN, MAX).toInt()



class ScheduleView : ViewPager {

    private val TAG = ScheduleView::class.java.name
    var onDataRequested: (ScheduleDayAdapter, LocalDate) -> Unit = {_,_ ->}
        set(value) {
            field = value
            adapter = ScheduleAdapter(field)
        }
    var day = MIN
        set(value) {
            field = value
            currentItem = ChronoUnit.DAYS.between(MIN, field).toInt()
        }

    init {
        offscreenPageLimit = 3
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attributes: AttributeSet?) : super(context, attributes)

}

private class ScheduleAdapter(
        val onDayChanged: (ScheduleDayAdapter, LocalDate) -> Unit
) : PagerAdapter() {

    override fun instantiateItem(parent: ViewGroup, position: Int): Any {
        val rv = RecyclerView(parent.context)
        rv.layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT)
        rv.adapter = ScheduleDayAdapter(onDayChanged)
        (rv.adapter as ScheduleDayAdapter).day = position
        rv.layoutManager = LinearLayoutManager(parent.context)
        parent.addView(rv)
        return rv
    }

    override fun destroyItem(parent: ViewGroup, position: Int, `object`: Any) {
        parent.removeView(`object` as RecyclerView)
    }

    override fun getCount(): Int = MAX_DAYS

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

}

class ScheduleDayAdapter (
        val onDayChanged: (ScheduleDayAdapter, LocalDate) -> Unit
) : RecyclerView.Adapter<ScheduleDayAdapter.ViewHolder>() {

    private var events: List<Event> = listOf()

    var day: Int = 0
        set(value) {
            field = value
            showLoadingIcon(true)
            dayChanged(
                    this,
                    MIN.plusDays(day.toLong())
            )
        }

    private fun showLoadingIcon(show: Boolean) {}

    private fun dayChanged(adapter: ScheduleDayAdapter, day: LocalDate) {
        onDayChanged(adapter, day)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val background: CardView = itemView.findViewById(R.id.background)
        val time: TextView = itemView.findViewById(R.id.time_tv)
        val title: TextView = itemView.findViewById(R.id.title_tv)
        val teacher: TextView = itemView.findViewById(R.id.teacher_tv)
        val location: TextView = itemView.findViewById(R.id.location_tv)
        val videoConference: TextView = itemView.findViewById(R.id.video_conferece_tv)
        val grading: LinearLayout = itemView.findViewById(R.id.grading_ll)
        val gradingTv: TextView = itemView.findViewById(R.id.grading_tv)
        val divider: View = itemView.findViewById(R.id.divider)
        val hour: TextView = itemView.findViewById(R.id.hour_num_tv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(
                R.layout.schedule_item,
                parent,
                false)
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val event = events[position]

        // Bind info to view
        holder.grading.visibility = if (event.grading) View.VISIBLE else View.GONE
        holder.time.text = "${event.timeFrom} - ${event.timeTo}"
        holder.title.text = event.title
        if (event.eventType == EventType.BREAK) {
            val drawable = ContextCompat.getDrawable(holder.itemView.context, R.drawable.ic_break)
            drawable?.setTint(textColor(event.color))
            holder.title.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
        } else {
            holder.title.setCompoundDrawables(null, null, null, null)
        }
        holder.teacher.text = event.teacher
        holder.location.text = event.location
        holder.teacher.visibility = if (event.teacher.isBlank()) View.GONE else View.VISIBLE
        holder.location.visibility = if (event.location.isBlank()) View.GONE else View.VISIBLE

        if (event.videoConference != null) {
            holder.videoConference.visibility = View.VISIBLE
        } else holder.videoConference.visibility = View.GONE
        holder.hour.text = event.hour
        holder.hour.visibility = if (event.showHour) View.VISIBLE else View.GONE

        // Colorize event

        val textColor = textColor(event.color)
        holder.background.setCardBackgroundColor(backgroundColor(event.color))
        holder.time.setTextColor(textColor)
        holder.title.setTextColor(textColor)
        holder.teacher.setTextColor(textColor)
        holder.location.setTextColor(textColor)
        holder.videoConference.setTextColor(textColor)

        val conferenceIcon = ContextCompat.getDrawable(holder.itemView.context, R.drawable.ic_video_conference)
        conferenceIcon?.setTint(textColor)
        holder.videoConference.setCompoundDrawablesWithIntrinsicBounds(conferenceIcon, null, null, null)
        holder.gradingTv.setTextColor(textColor)
        holder.divider.setBackgroundColor(backgroundColor(event.color))
        val gradingIcon = ContextCompat.getDrawable(holder.itemView.context, R.drawable.ic_grade)
        gradingIcon?.setTint(textColor)
        holder.gradingTv.setCompoundDrawablesWithIntrinsicBounds(gradingIcon, null, null, null)

        if (event.eventType == EventType.ALLDAY) {
            holder.time.text = event.timeFrom
        }

    }

    override fun getItemCount(): Int = events.count()

    fun setEvents(week: Week) {
        val events = ArrayList<Event>()

        // Check for all day event
        if (week.allDayEvents.isNotEmpty()) {
            val event = week.allDayEvents[0]
            events.add(
                Event(
                    "",
                    false,
                    "Whole day",
                    "",
                    event.name,
                    if (event.teachers?.isNotEmpty() == true) event.teachers[0]?.name ?:"" else "",
                    event.location?.name ?: "",
                    null,
                    false,
                    0xFFFFA500.toInt(),
                    EventType.ALLDAY
                )
            )
        } else {

            // Get school events
            week.schoolHourEvents.forEach { schoolEvent ->
                val fromTime = week.timeTable.first { schoolEvent.time.fromId == it.id }
                events.add(
                    Event(
                        fromTime.nameShort,
                        true,
                        fromTime.time.from,
                        fromTime.time.to,
                        schoolEvent.subject.name,
                        if (schoolEvent.teachers.isNotEmpty()) schoolEvent.teachers[0]?.name ?:"" else "",
                        schoolEvent.classroom?.name ?: "",
                        if (schoolEvent.videoConference.id == null) null else schoolEvent.videoConference.href ?: "",
                        schoolEvent.hourSpecialType == "exam",
                        ColorUtils.blendARGB(Color.parseColor(schoolEvent.color), Color.BLACK, 0.1f),
                        EventType.NORMAL
                    )
                )
            }

            // Get special events
            week.events.forEach { event ->
                events.add(
                    Event(
                        week.timeTable.first { event.time.from == it.time.from }.nameShort,
                        true,
                        event.time.from,
                        event.time.to,
                        event.name,
                        if (event.teachers?.isNotEmpty() == true) event.teachers[0]?.name ?:"" else "",
                        event.location?.name ?: "",
                        null,
                        false,
                        0xFFFFA500.toInt(),
                        EventType.SPECIAL
                    )
                )
            }

            if (events.isNotEmpty()) {
                week.timeTable.forEach { timeItem ->
                    if (timeItem.type == "break") {
                        events.add(
                                Event(
                                        "",
                                        false,
                                        timeItem.time.from,
                                        timeItem.time.to,
                                        "Break",
                                        "",
                                        "",
                                        null,
                                        false,
                                        0xFF673AB7.toInt(),
                                        EventType.BREAK
                                )
                        )
                    }
                }
            }

            // Sort all events, set hour headers and add "breaks" in between
            events.sortBy { it.timeFrom }
            var hour = ""
            for (event in events) {
                if (event.hour != hour) {
                    event.showHour = event.showHour && true
                    hour = event.hour
                } else {
                    event.showHour = false
                }
            }

        }
        showLoadingIcon(false)
        this.events = events
        notifyDataSetChanged()
    }

    private fun backgroundColor(color: Int): Int {
        return color and 0xFFFFFF or 0x10000000
    }

    private fun textColor(color: Int): Int {
        // TODO normalize main color for text
        return color
    }

    data class Event (
        val hour: String,
        var showHour: Boolean,
        val timeFrom: String,
        val timeTo: String,
        val title: String,
        val teacher: String,
        val location: String,
        val videoConference: String?,
        val grading: Boolean,
        val color: Int,
        val eventType: EventType
    )

    enum class EventType {
        NORMAL, SPECIAL, ALLDAY, BREAK
    }

}