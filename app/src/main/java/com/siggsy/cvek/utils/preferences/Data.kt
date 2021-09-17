package com.siggsy.cvek.utils.preferences

import android.content.Context
import com.siggsy.cvek.utils.CvekPreferences
import com.siggsy.cvek.utils.serializablePreference

class DataPreferences(context: Context) : CvekPreferences(context, "DATA") {
    var classColor: Map<String, String> by serializablePreference("colors") { mapOf() }
}