package com.siggsy.cvek.data.local

import android.content.Context

const val TOKEN = "TOKEN"
const val CHILD_ID = "CHILD_ID"
const val CREDENTIALS = "CREDENTIALS"
const val USER_IDS = "USER_IDS"
const val CURRENT_USER = "CURRENT_USER"
const val USERNAME = "USERNAME"
const val PASSWORD = "PASSWORD"

/**
 * Returns user's name and password from sharedPreferences.
 *
 * @param context  Application context.
 * @param userId  User Id/username of a user. Defaults to current active user ID.
 */
fun getUserCredentials(context: Context, userId: String = getCurrentUserId(context) ?: "") : Pair<String?, String?> {
    val preferences = context.getSharedPreferences(CREDENTIALS + userId, Context.MODE_PRIVATE)
    return Pair(preferences.getString(USERNAME, null), preferences.getString(PASSWORD, null))
}

/**
 * Returns id of a currently "default" user
 *
 * @param context  Application context.
 */
fun getCurrentUserId(context: Context) : String? {
    val preferences = context.getSharedPreferences(USER_IDS, Context.MODE_PRIVATE)
    return preferences.getString(CURRENT_USER, "")
}

/**
 * Returns stored user token. There is no grantee that the token is not exipred
 *
 * @param context  Application context.
 * @param userId  User Id/username of a user. Defaults to current active user ID.
 */
fun getUserToken(context: Context, userId: String = getCurrentUserId(context) ?: "") : Pair<String, String> {
    val preferences = context.getSharedPreferences(CREDENTIALS + userId, Context.MODE_PRIVATE)
    val token = preferences.getString(TOKEN, "")
    val childId = preferences.getString(CHILD_ID, "")
    return Pair(token ?: "", childId ?: "")
}

/**
 * Stores user token to sharedPreferences
 *
 * @param context  Application context.
 * @param userId  User Id/username of a user.
 * @param token  Token to be stored.
 * @param childId  A required header field for eAsistent API to work
 */
fun saveUserToken(context: Context, userId: String, token: String, childId: String) {
    val preferences = context.getSharedPreferences(CREDENTIALS + userId, Context.MODE_PRIVATE)
    preferences.edit()
        .putString(TOKEN, token)
        .putString(CHILD_ID, childId)
        .apply()
}