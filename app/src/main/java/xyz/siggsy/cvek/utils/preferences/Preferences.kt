package xyz.siggsy.cvek.utils.preferences

import android.content.Context
import android.content.SharedPreferences
import xyz.siggsy.cvek.utils.decodeJson
import xyz.siggsy.cvek.utils.toJson
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
        with(sharedPreferences.edit()) {
            store(value)
            apply()
        }
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

inline fun <reified T> CvekPreferences.serializablePreference(key: String, crossinline default: () -> T) =
    PreferencesDelegate(
        sharedPreferences,
        { this.putString(key, it.toJson()) },
        { this.getString(key, null)?.decodeJson() ?: default() }
    )