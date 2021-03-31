package com.siggsy.cvek.data.easistent

import android.util.Log
import com.siggsy.cvek.utils.TimeManager
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.*
import io.ktor.client.features.cookies.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.*

const val URL = "https://www.easistent.com"

val client = HttpClient(Android) {
    install(JsonFeature) {
        serializer = GsonSerializer()
    }
    HttpResponseValidator {
        validateResponse { response ->
            when(response.status.value) {
                401 -> throw InvalidTokenError()
            }
        }
    }
    defaultRequest {
        headers {
            append("user-agent", "Mozilla/5.0")
            append("accept", "application/json, text/html")
            append("accept-language", "sl-SI,sl;q=0.9,en-GB;q=0.8,en;q=0.7,de;q=0.6")
            append("x-client-platform", "web")
            append("x-client-version", "13")
            append("x-requested-with", "XMLHttpRequest")
        }
    }
}

/**
 * Do not forget to close client when app closes
 */
class EasistentApi (
        private val token: String,
        private val childId: String,
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

    suspend fun getYearTimeTable(year: Int = TimeManager.getCurrentYear()): Week {

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

    companion object {

        /**
         * Returns EasistentApi instance from username and password
         *
         * @param username  User's username.
         * @param password  User's password.
         */
        suspend fun getInstance(
            username: String,
            password: String,
        ) : EasistentApi {
            val (token, childId) = useCredentials(username, password)
            return EasistentApi(token, childId)
        }

        private suspend fun useCredentials(
            username: String,
            password: String
        ) : Pair<String, String> {
            val loginClient = HttpClient(Android) {
                install(HttpCookies) {
                    storage = AcceptAllCookiesStorage()
                }
                defaultRequest {
                    headers {
                        append("user-agent", "Mozilla/5.0")
                        append("accept", "application/json, text/javascript, */*; q=0.01")
                        append("accept-language", "sl-SI,sl;q=0.9,en-GB;q=0.8,en;q=0.7,de;q=0.6")
                        append("referer", "https://www.easistent.com/")
                        append("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
                    }
                }
                HttpResponseValidator {
                    validateResponse { response ->
                        when (response.status.value) {
                            401 -> throw InvalidTokenError()
                        }
                    }
                }
            }

            // Setup request body with username and password
            val ajaxResponse: String = loginClient.submitForm(
                Parameters.build {
                    append("uporabnik", username)
                    append("geslo", password)
                    append("pin", "")
                    append("captcha", "")
                    append("koda", "")
                },
            ) {
               url("$URL/p/ajax_prijava")
               method = HttpMethod.Post
            }

            // Invalid username or password
            if (!ajaxResponse.contains("\"status\":\"ok\"")) {
                throw TokenQueryFailed()
            }

            val tokenRequest: String = loginClient.get("$URL/webapp")
            loginClient.close()

            // Get token and childId and return EasistentApi instance
            val elements = Jsoup.parseBodyFragment(tokenRequest).allElements
            var token: String? = null
            var childID: String? = null
            for (element in elements) {
                val name: String = element.attr("name")
                if (name == "access-token") token = element.attr("content")
                if (name == "x-child-id") childID = element.attr("content")
            }

            if (token != null && childID != null) {
                return Pair(token, childID)
            } else throw TokenQueryFailed()
        }

    }

    /**
     * Returns a JSON serialized object from a specified sub-url call
     */
    private suspend inline fun <reified T> apiCall(
        url: String,
        params: List<Pair<String, String>> = emptyList()
    ) : T {
        return client.get(
            if (params.isEmpty()) "$URL$url"
            else "$URL$url?${params.formUrlEncode()}"
        ) {
            headers {
                append("x-child-id", childId)
                append("authorization", token)
            }
        }
    }

}