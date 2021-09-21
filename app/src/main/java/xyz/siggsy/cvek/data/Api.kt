package xyz.siggsy.cvek.data

import okhttp3.OkHttpClient
import xyz.siggsy.cvek.utils.getCurrentYear
import xyz.siggsy.cvek.utils.network.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

const val URL = "https://www.easistent.com/m"
const val LOGIN = "$URL/login"
const val REFRESH_TOKEN = "$URL/refresh_token"

private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

/**
 * Request for access and refresh tokens
 * @param username - part of credentials to use for login
 * @param password - part of credentials to use for login
 */
suspend fun OkHttpClient.login(username: String, password: String): BodyResponse<LoginResponse> =
    jsonRequest(LOGIN.toRequest {
        defaultHeaders(authRequest = true)
        post(LoginRequest(username, password, listOf("child")))
    })

/**
 * Request for user absences
 */
suspend fun OkHttpClient.getAbsences(): BodyResponse<Absences> =
    jsonRequest("$URL/absences".toRequest())

/**
 * Get all scheduled future evaluations
 */
suspend fun OkHttpClient.getFutureEvaluations(): BodyResponse<NextMarkings> =
    jsonRequest("$URL/evaluations?filter=future".toRequest())

/**
 * Get child praises and improvements
 */
suspend fun OkHttpClient.getPraisesAndImprovements(): BodyResponse<PraisesAndImprovements> =
    jsonRequest("$URL/praises_and_improvements".toRequest())

/**
 * Get child recently received grade for the specified object.
 * @param subjectId - id of the subject to receive the grades
 */
suspend fun OkHttpClient.getGrades(subjectId: String): BodyResponse<Subject> =
    jsonRequest("$URL/grades/classes/$subjectId".toRequest())

/**
 * Get timetable in the specified time window
 * @param dateFrom - first date (inclusive)
 * @param dateTo - last date (inclusive)
 */
suspend fun OkHttpClient.getTimeTable(dateFrom: LocalDate, dateTo: LocalDate): BodyResponse<Week> =
    jsonRequest(
        "$URL/timetable/weekly".addParams(
            "from" to dateFrom.format(formatter),
            "to"   to dateTo.format(formatter)
        ).toRequest()
    )

/**
 * Get timetable from the entire school year
 * @param year - year integer representing the current school year; 2021 corresponds to year 2021/2022
 */
suspend fun OkHttpClient.getYearTimeTable(year: Int = Calendar.getInstance().getCurrentYear()): BodyResponse<Week> {
    val c = Calendar.getInstance()

    c.set(year, 7, 25)
    val from = LocalDate.from(c.time.toInstant())
    c.set(year + 1, 7, 31)
    val to = LocalDate.from(c.time.toInstant())

    return getTimeTable(from, to)
}

/**
 * Request recently entered grades from current week's subjects
 */
suspend fun OkHttpClient.getLatestGrades(): BatchResponse<Subject> =
    batchJsonRequest(
        *getSubjectIds().let {
            if (it.isSuccessful) {
                val ids = it.body()!!
                ids.map { id -> "$URL/grades/classes/$id".toRequest() }
            } else {
                null
            }
        }?.toTypedArray() ?: emptyArray()
    )

/**
 * Returns a Map<String, String> containing subjectId to color mapping
 */
suspend fun OkHttpClient.getSubjectColors(): BodyResponse<Map<String, String>> {
    val yearResponse = getYearTimeTable()

    return BodyResponse(yearResponse.response) {
        if (yearResponse.isSuccessful)
            yearResponse.body()!!
                .schoolHourEvents
                .map { it.subject.id.toString() to it.color }
                .toMap()
        else null
    }
}

/**
 * Get all subject IDs from the current running week; used for updating recent grades.
 */
private suspend fun OkHttpClient.getSubjectIds(): BodyResponse<Set<Int>> {
    val week = getYearTimeTable()
    return BodyResponse(week.response) {
        if (week.isSuccessful) {
            week.body()!!.schoolHourEvents.map { it.subject.id }.toSet()
        } else {
            null
        }
    }
}