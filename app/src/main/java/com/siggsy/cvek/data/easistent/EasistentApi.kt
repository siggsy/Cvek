package com.siggsy.cvek.data.easistent

import android.content.Context
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.siggsy.cvek.data.local.getCurrentUserId
import com.siggsy.cvek.data.local.getUserCredentials
import com.siggsy.cvek.data.local.getUserToken
import com.siggsy.cvek.data.local.saveUserToken
import com.siggsy.cvek.utils.TimeManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import org.jsoup.Jsoup
import java.lang.NullPointerException
import java.text.SimpleDateFormat
import java.util.*

const val URL = "https://www.easistent.com"

val headers = Headers.headersOf(
    "user-agent", "Mozilla/5.0",
    "accept", "application/json, text/javascript, */*; q=0.01",
    "accept-language", "sl-SI,sl;q=0.9,en-GB;q=0.8,en;q=0.7,de;q=0.6",
    "referer", "https://www.easistent.com/",
    "content-type", "application/x-www-form-urlencoded; charset=UTF-8"
)

class EasistentApi (
    private val apiHeaders: Headers,
    private val client: OkHttpClient
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
            mapOf(
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
         * Function that runs eAsistent API calls and automatically refreshes token if it's expired
         *
         * @param context  Application context.
         * @param easistentApi  EasistentApi object to use for querying data.
         * @param task  Callback which runs API calls.
         * @param invalidCredentials  Callback that is executed when user credentials are invalid.
         */
        suspend fun expirableTokenSafeRun(
            context: Context,
            easistentApi: EasistentApi,
            task: suspend (EasistentApi) -> Unit,
            invalidCredentials: () -> Unit
        ) {
            try {
                task(easistentApi)
            } catch (e: EmptyResponseBodyException) {
                try {
                    val refreshedApi = getInstance(context, force = true)
                    task(refreshedApi)
                } catch (e: TokenQueryFailed) {
                    invalidCredentials()
                }
            }
        }

        /**
         * Returns EasistentApi instance from username and password
         *
         * @param context  Application context.
         * @param username  User's username.
         * @param password  User's password.
         * @param force  Forces function to not use stored token and queries a new one every time.
         */
        suspend fun getInstance(
            context: Context,
            username: String,
            password: String,
            force: Boolean = false
        ) : EasistentApi {

            // Cached token items
            val (token, childId) = getUserToken(context, username)
            // Http client with cookie jar
            val cookieJar =
                PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(context))
            val client = OkHttpClient.Builder().cookieJar(cookieJar).build()

            // If token items don't exist or if they are corrupted fetch new ones
            val headers =
                if (token.isBlank() or childId.isBlank() or force) {
                    val (newToken, newChildId) = useCredentials(client, username, password)
                    saveUserToken(context, username, newToken, newChildId)
                    createApiHeaders(newToken, newChildId)
                } else
                    createApiHeaders(token, childId)

            return EasistentApi(
                headers,
                client
            )

        }

        suspend fun getInstance(
            context: Context,
            userId: String? = getCurrentUserId(context),
            force: Boolean = false
        ) : EasistentApi {

            if (userId != null) {
                val (username, password) = getUserCredentials(context, userId)
                if (username != null && password != null)
                    return getInstance(context, username, password, force)
            }
            throw UserDoesNotExist()

        }

        private suspend fun useCredentials(
            client: OkHttpClient,
            username: String,
            password: String
        ) : Pair<String, String> = withContext(Dispatchers.IO) {

            // Setup request body with username and password
            val cookieRequestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("uporabnik", username)
                .addFormDataPart("geslo", password)
                .addFormDataPart("pin", "")
                .addFormDataPart("captcha", "")
                .addFormDataPart("koda", "")
                .build()

            // Get cookies for acquiring token and childId.
            val cookieRequest = Request.Builder()
                .headers(headers)
                .url("$URL/p/ajax_prijava")
                .post(cookieRequestBody)
                .build()

            val tokenRequest = Request.Builder()
                .headers(headers)
                .url("$URL/")
                .get()
                .build()

            // Get cookies to pass them to the next response
            client.newCall(cookieRequest).execute()
            val response = client.newCall(tokenRequest).execute()

            // Get token and childId and return EasistentApi instance
            val elements =
                Jsoup.parseBodyFragment(response.body?.byteString()?.utf8()).allElements
            var token: String? = null
            var childID: String? = null
            for (element in elements) {
                val name: String = element.attr("name")
                if (name == "access-token") token = element.attr("content")
                if (name == "x-child-id") childID = element.attr("content")
            }

            if (token != null && childID != null) {
                Pair(token, childID)
            } else throw TokenQueryFailed()

        }


        private fun createApiHeaders(
            token: String,
            childId: String
        ) : Headers = Headers.headersOf(
            "user-agent", "Mozilla/5.0",
            "accept", "application/json, text/html",
            "accept-language", "sl-SI,sl;q=0.9,en-GB;q=0.8,en;q=0.7,de;q=0.6",
            "authorization", token,
            "user-agent", "Mozilla/5.0",
            "x-child-id", childId,
            "x-client-platform", "web",
            "x-client-version", "13",
            "x-requested-with", "XMLHttpRequest"
        )

    }

    private inline fun <reified T> fromJson(json: String) : T {
        return Gson().fromJson(json, object : TypeToken<T>() {}.type)
    }

    /**
     * Returns a JSON serialized object from a specified sub-url call
     */
    private suspend inline fun <reified T> apiCall(
        url: String,
        params: Map<String, String>? = null
    ) : T = withContext (Dispatchers.IO) {

        // Convert params to url string.
        val paramsString = params?.map {
            "${it.key}=${it.value}"
        }?.joinToString("&") ?: ""

        // Create request body
        val request = Request.Builder()
            .headers(apiHeaders)
            .url("$URL$url$paramsString")
            .build()

        // Return JSON object.
        try {
            fromJson(client.newCall(request).execute().body!!.string()) as T
        } catch (e: NullPointerException) {
            throw EmptyResponseBodyException()
        }

    }

}