package com.siggsy.cvek.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.siggsy.cvek.R
import com.siggsy.cvek.data.easistent.Week
import java.time.LocalDate

val MAX_COUNT = LocalDate.MIN.until(LocalDate.MAX).days

fun setupScheduleView(
        viewPager: ViewPager2,
        day: LocalDate,
        onDayChanged: (ScheduleDayAdapter, LocalDate) -> Unit
) : ViewPager2 {

    viewPager.adapter = ScheduleAdapter(onDayChanged)
    viewPager.currentItem = LocalDate.MIN.until(day).days
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
        rv.adapter = ScheduleDayAdapter(onDayChanged)
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
                    LocalDate.MIN.plusDays(day.toLong())
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
        holder.time.text = event.time
        holder.title.text = event.title
        holder.teacher.text = event.teacher
        holder.location.text = event.location
        if (event.videoConference != null) {
            holder.videoConference.visibility = View.VISIBLE
            holder.videoConference.text = event.videoConference
        } else holder.videoConference.visibility = View.GONE

        // Colorize event
        holder.background.setBackgroundColor(backgroundColor(event.color))
        holder.time.setTextColor(textColor(event.color))

    }

    override fun getItemCount(): Int = events.count()

    fun setEvents(week: Week) {
        this.events = listOf()
        showLoadingIcon(false)
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
        val hour: Int,
        val time: String,
        val title: String,
        val teacher: String,
        val location: String,
        val videoConference: String?,
        val grading: Boolean,
        val color: Int
    )

}
