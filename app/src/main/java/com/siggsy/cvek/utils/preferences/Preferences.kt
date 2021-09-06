package com.siggsy.cvek.utils

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


open class CvekPreferences(context: Context, key: String) {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences(key, Context.MODE_PRIVATE)
}

open class PreferencesDelegate<T>(
    private val sharedPreferences: SharedPreferences,
    val store: SharedPreferences.Editor.(T) -> Unit,
    val get: SharedPreferences.() -> T
) : ReadWriteProperty<Any, T> {
    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return sharedPreferences.get()
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        val editor = sharedPreferences.edit()
        editor.store(value)
        editor.apply()
    }
}

fun CvekPreferences.stringPreference(key: String) =
    PreferencesDelegate(
        sharedPreferences,
        { this.putString(key, it) },
        { this.getString(key, "") ?: "" }
    )

fun CvekPreferences.booleanPreference(key: String) =
    PreferencesDelegate(
        sharedPreferences,
        { this.putBoolean(key, it) },
        { this.getBoolean(key, false) }
    )

inline fun <reified T> CvekPreferences.serializablePreference(key: String) =
    PreferencesDelegate<T>(
        sharedPreferences,
        { this.putString(key, Json.encodeToString(it)) },
        { Json.decodeFromString(this.getString(key, "") ?: "") }
    )