package xyz.siggsy.cvek.data

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor.*
import xyz.siggsy.cvek.utils.getCurrentYear
import xyz.siggsy.cvek.utils.network.*
import xyz.siggsy.cvek.utils.preferences.AuthPreferences
import xyz.siggsy.cvek.utils.preferences.User
import java.time.LocalDate
import java.util.*

class Repository(
    private val context: Context,
    private val http: OkHttpClient,
) {

    private val authPref = AuthPreferences(context)

    // Remote and local
    suspend fun getUserAuth(username: String, password: String): LoginResponse {
        val loginHttp = OkHttpClient.Builder()
            .default()
            .logger(Level.BODY)
            .build()

        val res = loginHttp.login(username, password)
        return res.bodyOrThrow()
    }


    fun saveUser(id: String, user: User) {
        authPref.users += (id to user)
        authPref.currentUserId = id
    }

    suspend fun updateGrades(vararg subjectIds: Int) {
        // TODO: get latest grades and save them to shared preferences
    }

    // Remote
    suspend fun getAbsences() =
        http.getAbsences().bodyOrThrow()

    suspend fun getFutureEvaluations() =
        http.getFutureEvaluations().bodyOrThrow()

    suspend fun getPraisesAndImprovements() =
        http.getPraisesAndImprovements().bodyOrThrow()

    suspend fun getTimeTable(
        dateFrom: LocalDate,
        dateTo: LocalDate
    ) = http.getTimeTable(dateFrom, dateTo).bodyOrThrow()

    suspend fun getGrades(subjectId: String) =
        http.getGrades(subjectId).bodyOrThrow()

    /**
     * Get timetable from the entire school [year]
     * @param year integer representing the current school year; 2021 corresponds to year 2021/2022
     */
    suspend fun getYearTimeTable(year: Int = Calendar.getInstance().getCurrentYear()) =
        getTimeTable(
            LocalDate.of(year, 7, 25),
            LocalDate.of(year + 1, 7, 31)
        )

    /**
     * Get all subject IDs from the current running week; used for updating recent grades.
     */
    suspend fun getSubjectIds() =
        getYearTimeTable()
            .schoolHourEvents
            .map { it.subject.id }
            .toSet()

    suspend fun getSubjectColors() =
        getYearTimeTable()
            .schoolHourEvents
            .map { it.subject.id.toString() to it.color }
            .toMap()

    suspend fun getLatestGrades() = withContext(Dispatchers.IO + Job()) {
        return@withContext awaitAll(*getSubjectIds().map { id ->
            async { getGrades(id.toString()) }
        }.toTypedArray())
    }

    // Local
    suspend fun getGrades() {}
    suspend fun currentUser() {}
    suspend fun users() {}

}
