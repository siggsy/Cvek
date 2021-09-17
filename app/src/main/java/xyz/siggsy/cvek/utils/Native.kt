package xyz.siggsy.cvek.utils


fun <T> List<T>.replace(index: Int, value: T): List<T> =
    toMutableList().let { editor ->
        editor[index] = value
        editor
    }