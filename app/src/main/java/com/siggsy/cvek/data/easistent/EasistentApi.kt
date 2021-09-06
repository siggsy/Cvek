package com.siggsy.cvek.data.easistent

import com.siggsy.cvek.utils.getCurrentYear
import com.siggsy.cvek.utils.jsonMedia
import com.siggsy.cvek.utils.toJson
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.*

const val URL = "https://www.easistent.com/m"
const val LOGIN = "$URL/login"
const val REFRESH_TOKEN = "$URL/refresh_token"

/**
 * Do not forget to close client when app closes
 */
class EasistentApi (
    private val httpClient: OkHttpClient,
) {
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    suspend fun getAbsences(): Absences {
        return apiCall("/m/absences")
    }

    suspend fun getFutureEvaluations(): NextMarkings {
        return apiCall("/m/evaluations?filter=future")
    }

    suspend fun getPraisesAndImprovements(): PraisesAndImprovements {
        return apiCall("/m/evaluations?filter=future")
    }

    suspend fun getGrades(subjectId: String): Subject {
        return apiCall("/m/grades/classes/$subjectId")
    }

    suspend fun getTimeTable(dateFrom: Date, dateTo: Date): Week {
        return apiCall(
            "/m/timetable/weekly",
            listOf(
                "from" to sdf.format(dateFrom),
                "to" to sdf.format(dateTo)
            )
        )
    }

    suspend fun getYearTimeTable(year: Int = Calendar.getInstance().getCurrentYear()): Week {

        val c = Calendar.getInstance()

        c.set(year, 7, 25)
        val from = c.time
        c.set(year + 1, 7, 31)
        val to = c.time

        return getTimeTable(from, to)

    }

    suspend fun getLatestGrades(): List<Subject> {
        val ids = getSubjectIds()
        val subjects = ArrayList<Subject>()
        for (id in ids) {
            val data = getGrades(id.toString())
            subjects.add(data)
        }
        return subjects
    }

    /**
     * Returns a Map<String, String> containing subject to color mapping
     */
    suspend fun getSubjectColors(): Map<String, String> {
        val year = getYearTimeTable()
        val colors = HashMap<String, String>()
        year.schoolHourEvents.forEach { colors[it.subject.id.toString()] = it.color }
        return colors
    }

    private suspend fun getSubjectIds(): Set<Int> {
        val week = getYearTimeTable()
        val ids = week.schoolHourEvents.map { it.subject.id }
        return ids.toSet()
    }

    /**
     * Returns a JSON serialized object from a specified sub-url call
     */
    private suspend inline fun <reified T> apiCall(
        url: String,
        params: List<Pair<String, String>> = emptyList()
    ) : T {
        return httpClient.get(
            if (params.isEmpty()) "$URL$url"
            else "$URL$url?${params.formUrlEncode()}"
        ) {
            headers {
                append("x-child-id", child)
            }
        }
    }

}

