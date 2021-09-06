package com.siggsy.cvek.utils.network

import com.siggsy.cvek.utils.decodeJson
import okhttp3.Request
import okhttp3.Response

fun Response.Builder.build(block: Response.Builder.() -> Unit) = apply(block).build()

inline fun <reified T> Response.decodeJson(): T =
    body!!.string().decodeJson()