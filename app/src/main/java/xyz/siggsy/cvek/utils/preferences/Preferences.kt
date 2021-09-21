package xyz.siggsy.cvek.utils.preferences

import android.content.Context
import android.content.SharedPreferences
import xyz.siggsy.cvek.utils.decodeJson
import xyz.siggsy.cvek.utils.toJson
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Open class for overriding when creating new shared preferences
 * @param context for acquiring shared preferences
 * @param key for shared preferences
 */
open class CvekPreferences(context: Context, key: String) {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences(key, Context.MODE_PRIVATE)
}

/**
 * Delegate for creating var field instead of getters and setters
 * @param T type to store/get
 * @param sharedPreferences to use
 * @param store lambda for refactoring T types to correct format
 * @param get lambda for acquiring T types
 */
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

/**
 * String preference delegate
 * @param key for preference
 */
fun CvekPreferences.stringPreference(key: String) =
    PreferencesDelegate(
        sharedPreferences,
        { this.putString(key, it) },
        { this.getString(key, "") ?: "" }
    )

/**
 * Boolean preference delegate
 * @param key for preference
 */
fun CvekPreferences.booleanPreference(key: String) =
    PreferencesDelegate(
        sharedPreferences,
        { this.putBoolean(key, it) },
        { this.getBoolean(key, false) }
    )

/**
 * Serializable preference delegate
 * @param key for preference
 * @param default value for unset values
 */
inline fun <reified T> CvekPreferences.serializablePreference(key: String, default: T) =
    PreferencesDelegate(
        sharedPreferences,
        { this.putString(key, it.toJson()) },
        { this.getString(key, null)?.decodeJson() ?: default }
    )