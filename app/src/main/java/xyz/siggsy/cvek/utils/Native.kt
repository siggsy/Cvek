package xyz.siggsy.cvek.utils

/**
 * Function for replacing value in list on immutable lists
 * @receiver - immutable list to mutate
 * @param index - index of value to replace
 * @param value - value to use as replacement
 * @return - newly generated list
 */
fun <T> List<T>.replace(index: Int, value: T): List<T> =
    toMutableList().let { editor ->
        editor[index] = value
        editor
    }