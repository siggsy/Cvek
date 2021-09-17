package com.siggsy.cvek.data.easistent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * File for JSON to Kotlin object mapping
 */

@Serializable
data class RefreshResponse(
    @SerialName("access_token") val accessToken: AccessToken,
    @SerialName("refresh_token") val refreshToken: String
)

@Serializable
data class LoginResponse(
    @SerialName("access_token") val accessToken: AccessToken,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("user") val user: User
)

@Serializable
data class User(
    val id: Int,
    val language: String,
    val username: String,
    val name: String,
    val type: String,
)

@Serializable
data class AccessToken(
    @SerialName("token") val token: String,
    @SerialName("expiration_date") val expirationDate: String
)

@Serializable
data class Absences(
    @SerialName("summary") val summary: Summary,
    @SerialName("items") val days: List<Days>
) {
    @Serializable
    data class Summary(
        @SerialName("excused_hours") val excusedHours: Int,
        @SerialName("unexcused_hours") val unexcusedHours: Int,
        @SerialName("unmanaged_absences") val unmanaged_absences: Int,
        @SerialName("erased") val erased: Int
    )
    @Serializable
    data class Hours(
        @SerialName("class_name") val className: String,
        @SerialName("class_short_name") val classShortName: String,
        @SerialName("value") val value: String,
        @SerialName("from") val from: String,
        @SerialName("to") val to: String,
        @SerialName("state") val state: String?
    )
    @Serializable
    data class Days(
        @SerialName("id") val id: Int,
        @SerialName("date") val date: String,
        @SerialName("missing_count") val missingCount: Int,
        @SerialName("excused_count") val excusedCount: Int,
        @SerialName("not_excused_count") val notExcusedCount: Int,
        @SerialName("erased_count") val erasedCount: Int,
        @SerialName("hours") val hours: List<Hours>,
        @SerialName("state") val state: String
    )
}

@Serializable
data class NextMarkings(@SerialName("items") val items: List<Item>) {
    @Serializable
    data class Item(
        @SerialName("id") val id: Int,
        @SerialName("course") val course: String,
        @SerialName("subject") val subject: String,
        @SerialName("type") val type: String,
        @SerialName("grade") val grade: String?,
        @SerialName("date") val date: String,
        @SerialName("period") val period: String,
        @SerialName("type_name") val typeName: String
    )
}

@Serializable
data class PraisesAndImprovements(@SerialName("items") val items: List<Item>) {
    @Serializable
    data class Item(
        @SerialName("author") val author: String,
        @SerialName("category") val category: String,
        @SerialName("course") val course: String,
        @SerialName("course_id") val courseId: Int,
        @SerialName("date") val date: String,
        @SerialName("event_type") val eventType: String,
        @SerialName("id") val id: Int,
        @SerialName("text") val text: String,
        @SerialName("type") val type: String
    )
}

@Serializable
data class Week(
    @SerialName("time_table") val timeTable: List<TimeTable>,
    @SerialName("day_table") val dayTable: List<DayTable>,
    @SerialName("school_hour_events") val schoolHourEvents: List<SchoolHourEvent>,
    @SerialName("events") val events: List<Event>,
    @SerialName("all_day_events") val allDayEvents: List<AllDayEvent>
) {
    @Serializable
    data class TimeTable(
        @SerialName("id") val id: Int,
        @SerialName("name") val name: String,
        @SerialName("name_short") val nameShort: String,
        @SerialName("time") val time: Time,
        @SerialName("type") val type: String
    )
    @Serializable
    data class DayTable(
        @SerialName("name") val name: String,
        @SerialName("short_name") val shortName: String,
        @SerialName("date") val date: String
    )
    @Serializable
    data class SchoolHourEvent(
        @SerialName("time") val time: TimeID,
        @SerialName("event_id") val eventId: Int,
        @SerialName("color") val color: String,
        @SerialName("subject") val subject: Subject,
        @SerialName("completed") val completed: Boolean,
        @SerialName("hour_special_type") val hourSpecialType: String?,
        @SerialName("departments") val departments: List<Department>,
        @SerialName("classroom") val classroom: Classroom?,
        @SerialName("teachers") val teachers: List<Grades.Teacher?>,
        @SerialName("groups") val groups: List<Subject>,
        @SerialName("videokonferenca") val videoConference: VideoConference
    ) {
        @Serializable
        data class VideoConference(
            @SerialName("id") val id: Int?,
            @SerialName("link") var href: String?
        )
    }
    @Serializable
    data class Event(
        @SerialName("id") val id: Int,
        @SerialName("date") val date: String,
        @SerialName("time") val time: Time,
        @SerialName("location") val location: Location?,
        @SerialName("teachers") val teachers: List<Grades.Teacher?>?,
        @SerialName("name") val name: String,
        @SerialName("event_type") val eventType: Int
    )
    @Serializable
    data class AllDayEvent(
        @SerialName("id") val id: Int,
        @SerialName("date") val date: String,
        @SerialName("location") val location: Location?,
        @SerialName("teachers") val teachers: List<Grades.Teacher?>?,
        @SerialName("name") val name: String,
        @SerialName("event_type") val eventType: Int
    )
    @Serializable
    data class Time(
        @SerialName("from") val from: String,
        @SerialName("to") val to: String
    )
    @Serializable
    data class TimeID(
        @SerialName("from_id") val fromId: Int,
        @SerialName("to_id") val toId: Int,
        @SerialName("date") val date: String
    )
    @Serializable
    data class Location(
        @SerialName("name") val name: String?,
        @SerialName("short_name") val shortName: String?
    )
    @Serializable
    data class Classroom(
        @SerialName("id") val id: String,
        @SerialName("name") val name: String?
    )
    @Serializable
    data class Department(
        @SerialName("id") val id: String,
        @SerialName("name") val name: String
    )
    @Serializable
    data class Subject(
        @SerialName("id") val id: Int,
        @SerialName("name") val name: String
    )
}

@Serializable
data class Grades(
    @SerialName("id") val id: Int,
    @SerialName("value") val value: String,
    @SerialName("date") val date: String,
    @SerialName("average_grade") val averageGrade: String,
    @SerialName("grade_rank") val gradeRank: String
) {
    @Serializable
    data class FinalGrade(
        @SerialName("id") val id: Int,
        @SerialName("value") val value: String,
        @SerialName("date") val date: String,
        @SerialName("inserted_by") val insertedBy: Teacher
    )
    @Serializable
    data class NormalGrade(
        @SerialName("id") val id: Int,
        @SerialName("value") val value: String,
        @SerialName("date") val date: String,
        @SerialName("inserted_by") val insertedBy: Teacher?,
        @SerialName("type_name") val typeName: String,
        @SerialName("comment") val comment: String,
        @SerialName("overrides_ids") val overrideIds: List<Int>?,
        @SerialName("subject") val subject: String?,
        @SerialName("notified_at") val notifiedAt: String?,
        @SerialName("inserted_at") val insertedAt: String
    )
    @Serializable
    data class Semester(
        @SerialName("id") val id: Int,
        @SerialName("grades") var grades: ArrayList<NormalGrade>,
        @SerialName("final_grade") var finalGrade: FinalGrade?
    )
    @Serializable
    data class Teacher(
        @SerialName("id") val id: Int,
        @SerialName("name") val name: String
    )
}
@Serializable
data class Subject(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("short_name") val shortName: String,
    @SerialName("grade_type") val gradeType: String,
    @SerialName("final_grade") var finalGrade: Grades.FinalGrade?,
    @SerialName("average_grade") var averageGrade: String?,
    @SerialName("is_excused") val isExcused: Boolean,
    @SerialName("semesters") var semesters: ArrayList<Grades.Semester>?
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

@Serializable
data class UserPersonalData(
    @SerialName("id") val id: Int,
    @SerialName("short_name") val shortName: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("gender") val gender: String,
    @SerialName("avatar") val avatar: String?,
    @SerialName("age_level") val ageLevel: String,
    @SerialName("language") val language: String,
    @SerialName("e_mail") val eMail: String,
    @SerialName("timetable") val timetable: Timetable,
    @SerialName("plus_enabled") val plusEnabled: Boolean,
    @SerialName("did_try_plus") val didTryPlus: Boolean
) {
    @Serializable
    data class Timetable(
        @SerialName("date") val date: String,
        @SerialName("hours") val hours: List<Hours>
    ) {
        @Serializable
        data class Hours(
            @SerialName("type") val type: String,
            @SerialName("from") val from: String,
            @SerialName("to") val to: String,
            @SerialName("summary") val summary: String,
            @SerialName("course_type") val courseType: String
        ) {
            @Serializable
            data class Metadata(
                @SerialName("tomorrow_normal") val tomorrowNormal: Boolean,
                @SerialName("tomorrow_start") val tomorrowStart: String,
                @SerialName("tomorrow_end") val tomorrowEnd: String,
                @SerialName(" tomorrow_info") val tomorrowInfo: String
            )
        }
    }
}

@Serializable
data class Homework(
    @SerialName("id") val id: Int,
    @SerialName("subject") val subject: String,
    @SerialName("title") val title: String,
    @SerialName("date") val date: String,
    @SerialName("deadline") val deadline: String
)

@Serializable
data class HomeworkDetailed(
    @SerialName("id") val id: Int,
    @SerialName("subject") val subject: String,
    @SerialName("title") val title: String,
    @SerialName("date") val date: String,
    @SerialName("deadline") val deadline: String,
    @SerialName("content") val content: String,
    @SerialName("author") val author: String
)

@Serializable
data class ErrorResponse(
    @SerialName("error") val error: Error
) {
    @Serializable
    data class Error(
        @SerialName("code") val code: Int,
        @SerialName("developer_message") val developerMessage: String?,
        @SerialName("user_message") val userMessage: String?,
    )
}