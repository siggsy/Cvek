package xyz.siggsy.cvek.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
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
    suspend fun getUserAuth(username: String, password: String) = flow {
        val loginHttp = OkHttpClient.Builder()
            .default()
            .logger(Level.BODY)
            .build()

        val res = loginHttp.login(username, password)
        emit(res.bodyOrThrow())
    }

    fun saveUser(id: String, user: User) {
        authPref.users += (id to user)
        authPref.currentUserId = id
    }

    suspend fun updateGrades(vararg subjectIds: Int) {
        // TODO: get latest grades and save them to shared preferences
    }

    // Remote
    suspend fun getAbsences(): Flow<Absences> = flow {
        val res = http.getAbsences()
        emit(res.bodyOrThrow())
    }

    suspend fun getFutureEvaluations() = flow {
        val res = http.getFutureEvaluations()
        emit(res.bodyOrThrow())
    }

    suspend fun getPraisesAndImprovements() = flow {
        val res = http.getPraisesAndImprovements()
        emit(res.bodyOrThrow())
    }

    suspend fun getTimeTable(dateFrom: LocalDate, dateTo: LocalDate) = flow {
        val res = http.getTimeTable(dateFrom, dateTo)
        emit(res.bodyOrThrow())
    }

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
    suspend fun getSubjectIds() = getYearTimeTable()
        .map { year ->
            year.schoolHourEvents
                .map { it.subject.id }
                .toSet()
        }

    suspend fun getSubjectColors() = getYearTimeTable()
        .map { year ->
            year
                .schoolHourEvents
                .map { it.subject.id.toString() to it.color }
                .toMap()
        }

    suspend fun getLatestGrades() = getSubjectIds().map {
        it.map { id -> http
            .jsonRequest<Subject>("$URL/grades/classes/$id".toRequest())
            .bodyOrThrow()
        }
    }

    // Local
    suspend fun getGrades() {}
    suspend fun currentUser() {}
    suspend fun users() {}

}
