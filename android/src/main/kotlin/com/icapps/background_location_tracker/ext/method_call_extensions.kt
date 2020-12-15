package com.icapps.background_location_tracker.ext

import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import java.lang.StringBuilder

fun MethodCall.checkRequiredFields(keys: Iterable<String>, result: MethodChannel.Result): Boolean {
    val sb = StringBuilder()

    keys.forEach {
        if (!hasArgument(it)) {
            sb.appendln("$it not found, but required")
        }
    }
    if (sb.isEmpty()) return true
    result.error("404", sb.toString(), null)
    return false
}