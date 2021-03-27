package com.siggsy.cvek.ui.schedule

import android.app.ActionBar
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.siggsy.cvek.R
import com.siggsy.cvek.data.easistent.Week
import java.time.LocalDate

val MIN = LocalDate.of(1970, 1, 1)
val MAX = LocalDate.of(2100, 1, 1)
val MAX_COUNT = MIN.until(MAX).days

fun setupScheduleView(
        viewPager: ViewPager2,
        day: LocalDate,
        onDayChanged: (ScheduleDayAdapter, LocalDate) -> Unit
) : ViewPager2 {

    viewPager.adapter = ScheduleAdapter(onDayChanged)
    viewPager.currentItem = MIN.until(day).days
    return viewPager

}

private class ScheduleAdapter(
        val onDayChanged: (ScheduleDayAdapter, LocalDate) -> Unit
) : RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

    class ViewHolder (
            itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {
        val rv: RecyclerView = itemView as RecyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rv = RecyclerView(parent.context)
        rv.layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT)
        rv.adapter = ScheduleDayAdapter(onDayChanged)
        rv.layoutManager = LinearLayoutManager(parent.context)
        return ViewHolder(rv)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        (holder.rv.adapter as ScheduleDayAdapter).day = position
    }

    override fun getItemCount(): Int = MAX_COUNT

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
        holder.teacher.text = event.teacher
        holder.location.text = event.location
        if (event.videoConference != null) {
            holder.videoConference.visibility = View.VISIBLE
            holder.videoConference.text = event.videoConference
        } else holder.videoConference.visibility = View.GONE
        holder.hour.text = event.hour
        holder.hour.visibility = if (event.showHour) View.VISIBLE else View.GONE

        // Colorize event
        holder.background.setBackgroundColor(backgroundColor(event.color))
        holder.time.setTextColor(textColor(event.color))
        holder.title.setTextColor(textColor(event.color))
        holder.teacher.setTextColor(textColor(event.color))
        holder.location.setTextColor(textColor(event.color))
        holder.videoConference.setTextColor(textColor(event.color))
        holder.videoConference.compoundDrawables[0].setTint(textColor(event.color))
        holder.gradingTv.setTextColor(textColor(event.color))
        holder.gradingTv.compoundDrawables[0].setTint(textColor(event.color))

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
                    Color.YELLOW,
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
                        Color.parseColor(schoolEvent.color),
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
                        Color.YELLOW,
                        EventType.SPECIAL
                    )
                )
            }

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
                            0x673AB7,
                            EventType.BREAK
                        )
                    )
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
        // TODO normalize main color for background
        return color
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
