package xyz.siggsy.cvek.utils.preferences

import android.content.Context

class DataPreferences(context: Context) : CvekPreferences(context, "DATA") {
    var classColor: Map<String, String> by serializablePreference("colors") { mapOf() }
}