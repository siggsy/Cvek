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
 * Request for access and refresh tokens using [username] and [password]
 */
suspend fun OkHttpClient.login(username: String, password: String): BodyResponse<LoginResponse> =
    jsonRequest(LOGIN.toRequest {
        defaultHeaders(authRequest = true)
        post(LoginRequest(username, password))
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
 * Get child recently received grade for the specified object using [subjectId].
 */
suspend fun OkHttpClient.getGrades(subjectId: String): BodyResponse<Subject> =
    jsonRequest("$URL/grades/classes/$subjectId".toRequest())

/**
 * Get timetable in the specified time window defined by [dateFrom] and [dateTo] inclusive
 */
suspend fun OkHttpClient.getTimeTable(dateFrom: LocalDate, dateTo: LocalDate): BodyResponse<Week> =
    jsonRequest(
        "$URL/timetable/weekly".addParams(
            "from" to dateFrom.format(formatter),
            "to"   to dateTo.format(formatter)
        ).toRequest()
    )
