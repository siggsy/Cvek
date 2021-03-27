package com.siggsy.cvek.data.easistent

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * File for JSON to Kotlin object mapping
 */

// TODO: find all possible null values
data class Absences(
    @SerializedName("summary") val summary: Summary,
    @SerializedName("items") val days: List<Days>
) {
    data class Summary(
        @SerializedName("excused_hours") val excusedHours: Int,
        @SerializedName("unexcused_hours") val unexcusedHours: Int,
        @SerializedName("unmanaged_absences") val unmanaged_absences: Int,
        @SerializedName("erased") val erased: Int
    )
    data class Hours(
        @SerializedName("class_name") val className: String,
        @SerializedName("class_short_name") val classShortName: String,
        @SerializedName("value") val value: String,
        @SerializedName("from") val from: String,
        @SerializedName("to") val to: String,
        @SerializedName("state") val state: String?
    )
    data class Days(
        @SerializedName("id") val id: Int,
        @SerializedName("date") val date: String,
        @SerializedName("missing_count") val missingCount: Int,
        @SerializedName("excused_count") val excusedCount: Int,
        @SerializedName("not_excused_count") val notExcusedCount: Int,
        @SerializedName("erased_count") val erasedCount: Int,
        @SerializedName("hours") val hours: List<Hours>,
        @SerializedName("state") val state: String
    )
}

data class Message(
    val id: String,
    val sender: String,
    val title: String,
    val time_date: String,
    val hint: String,
    var open: Boolean
) {
    data class Content(
        val title: String,
        val sender_name: String,
        val sender_email: String,
        val date_hour: String,
        val content: String,
        val replyTo: String,
        val attachments: List<Attachment>
    ) {
        data class Attachment(
            val href: String,
            val shownName: String,
            val applicationOpener: String,
            val fileSize: String
        )
    }
}

data class NextMarkings(@SerializedName("items") val items: List<Item>) {
    data class Item(
        @SerializedName("id") val id: Int,
        @SerializedName("course") val course: String,
        @SerializedName("subject") val subject: String,
        @SerializedName("type") val type: String,
        @SerializedName("grade") val grade: String?,
        @SerializedName("date") val date: String,
        @SerializedName("period") val period: String,
        @SerializedName("type_name") val typeName: String
    )
}

data class PraisesAndImprovements(@SerializedName("items") val items: List<Item>) {
    data class Item(
        @SerializedName("author") val author: String,
        @SerializedName("category") val category: String,
        @SerializedName("course") val course: String,
        @SerializedName("course_id") val courseId: Int,
        @SerializedName("date") val date: String,
        @SerializedName("event_type") val eventType: String,
        @SerializedName("id") val id: Int,
        @SerializedName("text") val text: String,
        @SerializedName("type") val type: String
    )
}

data class Week(
    @SerializedName("time_table") val timeTable: List<TimeTable>,
    @SerializedName("day_table") val dayTable: List<DayTable>,
    @SerializedName("school_hour_events") val schoolHourEvents: List<SchoolHourEvent>,
    @SerializedName("events") val events: List<Event>,
    @SerializedName("all_day_events") val allDayEvents: List<AllDayEvent>
) {
    data class TimeTable(
        @SerializedName("id") val id: Int,
        @SerializedName("name") val name: String,
        @SerializedName("name_short") val nameShort: String,
        @SerializedName("time") val time: Time,
        @SerializedName("type") val type: String
    )
    data class DayTable(
        @SerializedName("name") val name: String,
        @SerializedName("short_name") val shortName: String,
        @SerializedName("date") val date: String
    )
    data class SchoolHourEvent(
        @SerializedName("time") val time: TimeID,
        @SerializedName("event_id") val eventId: Int,
        @SerializedName("color") val color: String,
        @SerializedName("subject") val subject: Subject,
        @SerializedName("completed") val completed: Boolean,
        @SerializedName("hour_special_type") val hourSpecialType: String?,
        @SerializedName("departments") val departments: List<Department>,
        @SerializedName("classroom") val classroom: Classroom?,
        @SerializedName("teachers") val teachers: List<Grades.Teacher?>,
        @SerializedName("groups") val groups: List<Subject>,
        @SerializedName("videokonferenca") val videoConference: VideoConference
    ) {
        data class VideoConference(
            @SerializedName("id") val id: Int?,
            @SerializedName("link") var href: String?
        )
    }
    data class Event(
        @SerializedName("id") val id: Int,
        @SerializedName("date") val date: String,
        @SerializedName("time") val time: Time,
        @SerializedName("location") val location: Location?,
        @SerializedName("teachers") val teachers: List<Grades.Teacher?>?,
        @SerializedName("name") val name: String,
        @SerializedName("event_type") val eventType: Int
    )
    data class AllDayEvent(
        @SerializedName("id") val id: Int,
        @SerializedName("date") val date: String,
        @SerializedName("location") val location: Location?,
        @SerializedName("teachers") val teachers: List<Grades.Teacher?>?,
        @SerializedName("name") val name: String,
        @SerializedName("event_type") val eventType: Int
    )
    data class Time(
        @SerializedName("from") val from: String,
        @SerializedName("to") val to: String
    )
    data class TimeID(
        @SerializedName("from_id") val fromId: Int,
        @SerializedName("to_id") val toId: Int,
        @SerializedName("date") val date: String
    )
    data class Location(
        @SerializedName("name") val name: String?,
        @SerializedName("short_name") val shortName: String?
    )
    data class Classroom(
        @SerializedName("id") val id: String,
        @SerializedName("name") val name: String?
    )
    data class Department(
        @SerializedName("id") val id: String,
        @SerializedName("name") val name: String
    )
    data class Subject(
        @SerializedName("id") val id: Int,
        @SerializedName("name") val name: String
    )
}

data class Grades(
    @SerializedName("id") val id: Int,
    @SerializedName("value") val value: String,
    @SerializedName("date") val date: String,
    @SerializedName("average_grade") val averageGrade: String,
    @SerializedName("grade_rank") val gradeRank: String
) {
    data class FinalGrade(
        @SerializedName("id") val id: Int,
        @SerializedName("value") val value: String,
        @SerializedName("date") val date: String,
        @SerializedName("inserted_by") val insertedBy: Teacher
    )
    data class NormalGrade(
        @SerializedName("id") val id: Int,
        @SerializedName("value") val value: String,
        @SerializedName("date") val date: String,
        @SerializedName("inserted_by") val insertedBy: Teacher?,
        @SerializedName("type_name") val typeName: String,
        @SerializedName("comment") val comment: String,
        @SerializedName("overrides_ids") val overrideIds: List<Int>?,
        @SerializedName("subject") val subject: String?,
        @SerializedName("notified_at") val notifiedAt: String?,
        @SerializedName("inserted_at") val insertedAt: String
    )
    data class Semester(
        @SerializedName("id") val id: Int,
        @SerializedName("grades") var grades: ArrayList<NormalGrade>,
        @SerializedName("final_grade") var finalGrade: FinalGrade?
    )
    data class Teacher(
        @SerializedName("id") val id: Int,
        @SerializedName("name") val name: String
    )
}
data class Subject(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("short_name") val shortName: String,
    @SerializedName("grade_type") val gradeType: String,
    @SerializedName("final_grade") var finalGrade: Grades.FinalGrade?,
    @SerializedName("average_grade") var averageGrade: String?,
    @SerializedName("is_excused") val isExcused: Boolean,
    @SerializedName("semesters") var semesters: ArrayList<Grades.Semester>?
) {

    fun getAllGrades(): ArrayList<Grades.NormalGrade> {
        val grades = ArrayList<Grades.NormalGrade>()
        semesters?.forEach { it.grades.sortWith(compareByDescending { grade -> SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(grade.date) }) }
        semesters?.sortWith(compareByDescending { it.id })
        if (semesters != null) {
            for (sem in semesters as ArrayList)
                grades.addAll(sem.grades)
            return grades
        }
        return ArrayList()
    }
}

data class UserPersonalData(
    @SerializedName("id") val id: Int,
    @SerializedName("short_name") val shortName: String,
    @SerializedName("display_name") val displayName: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("avatar") val avatar: String?,
    @SerializedName("age_level") val ageLevel: String,
    @SerializedName("language") val language: String,
    @SerializedName("e_mail") val eMail: String,
    @SerializedName("timetable") val timetable: Timetable,
    @SerializedName("plus_enabled") val plusEnabled: Boolean,
    @SerializedName("did_try_plus") val didTryPlus: Boolean
) {
    data class Timetable(
        @SerializedName("date") val date: String,
        @SerializedName("hours") val hours: List<Hours>
    ) {
        data class Hours(
            @SerializedName("type") val type: String,
            @SerializedName("from") val from: String,
            @SerializedName("to") val to: String,
            @SerializedName("summary") val summary: String,
            @SerializedName("course_type") val courseType: String
        ) {
            data class Metadata(
                @SerializedName("tomorrow_normal") val tomorrowNormal: Boolean,
                @SerializedName("tomorrow_start") val tomorrowStart: String,
                @SerializedName("tomorrow_end") val tomorrowEnd: String,
                @SerializedName(" tomorrow_info") val tomorrowInfo: String
            )
        }
    }
}

data class Homework(
    @SerializedName("id") val id: Int,
    @SerializedName("subject") val subject: String,
    @SerializedName("title") val title: String,
    @SerializedName("date") val date: String,
    @SerializedName("deadline") val deadline: String
)

data class HomeworkDetailed(
    @SerializedName("id") val id: Int,
    @SerializedName("subject") val subject: String,
    @SerializedName("title") val title: String,
    @SerializedName("date") val date: String,
    @SerializedName("deadline") val deadline: String,
    @SerializedName("content") val content: String,
    @SerializedName("author") val author: String
)