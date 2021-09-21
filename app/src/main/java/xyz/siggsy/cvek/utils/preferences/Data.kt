package xyz.siggsy.cvek.utils.preferences

import android.content.Context

/**
 * Data preferences class for storing user specific data
 * @param context for acquiring shared preferences
 * @param userId from which user to access data
 */
class DataPreferences(context: Context, userId: String) : CvekPreferences(context, "DATA_$userId") {
    var classColor: Map<String, String> by serializablePreference("colors", mapOf())
}