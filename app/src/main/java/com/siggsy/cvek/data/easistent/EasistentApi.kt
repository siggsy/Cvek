package com.siggsy.cvek.data.easistent

import com.siggsy.cvek.utils.getCurrentYear
import com.siggsy.cvek.utils.network.*
import okhttp3.OkHttpClient
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

const val URL = "https://www.easistent.com/m"
const val LOGIN = "$URL/login"
const val REFRESH_TOKEN = "$URL/refresh_token"

/**
 * Do not forget to close client when app closes
 */

private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

suspend fun OkHttpClient.login(username: String, password: String): BodyResponse<LoginResponse> =
    await(LOGIN.toRequest {
        defaultHeaders(authRequest = true)
        post(LoginRequest(username, password, listOf("child")))
    })
suspend fun OkHttpClient.getAbsences(): BodyResponse<Absences> = await("$URL/absences".toRequest())
suspend fun OkHttpClient.getFutureEvaluations(): BodyResponse<NextMarkings> = await("$URL/evaluations?filter=future".toRequest())
suspend fun OkHttpClient.getPraisesAndImprovements(): BodyResponse<PraisesAndImprovements> = await("$URL/praises_and_improvements".toRequest())
suspend fun OkHttpClient.getGrades(subjectId: String): BodyResponse<Subject> = await("$URL/grades/classes/$subjectId".toRequest())
suspend fun OkHttpClient.getTimeTable(dateFrom: LocalDate, dateTo: LocalDate): BodyResponse<Week> = await(
        "$URL/timetable/weekly".addParams(
            "from" to dateFrom.format(formatter),
            "to"   to dateTo.format(formatter)
        ).toRequest()
    )
suspend fun OkHttpClient.getYearTimeTable(year: Int = Calendar.getInstance().getCurrentYear()): BodyResponse<Week> {

    val c = Calendar.getInstance()

    c.set(year, 7, 25)
    val from = LocalDate.from(c.time.toInstant())
    c.set(year + 1, 7, 31)
    val to = LocalDate.from(c.time.toInstant())

    return getTimeTable(from, to)

}
suspend fun OkHttpClient.getLatestGrades(): BatchResponse<Subject> =
    batchAwait(
        *getSubjectIds().let {
            if (it.failed) {
                null
            } else {
                val ids = it.body()!!
                ids.map { id -> "$URL/grades/classes/$id".toRequest() }
            }
        }?.toTypedArray() ?: emptyArray()
    )

/**
 * Returns a Map<String, String> containing subject to color mapping
 */
suspend fun OkHttpClient.getSubjectColors(): BodyResponse<Map<String, String>> {
    val yearResponse = getYearTimeTable()

    return BodyResponse(yearResponse.response) {
        if (!yearResponse.failed)
            yearResponse.body()!!
                .schoolHourEvents
                .map { it.subject.id.toString() to it.color }
                .toMap()
        else null
    }
}

private suspend fun OkHttpClient.getSubjectIds(): BodyResponse<Set<Int>> {
    val week = getYearTimeTable()
    return BodyResponse(week.response) {
        if (week.failed) {
            null
        } else {
            week.body()!!.schoolHourEvents.map { it.subject.id }.toSet()
        }
    }
}

