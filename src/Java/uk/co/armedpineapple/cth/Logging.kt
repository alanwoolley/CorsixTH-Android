package uk.co.armedpineapple.cth

import android.util.Log
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

open class Logger(tag: String) : Loggable {
    override val loggerTag: String = tag
}

fun logger(clazz: Class<*>): Loggable = Logger(getLogTag(clazz))

inline fun <reified T : Any> logger(): Loggable = logger(T::class.java)

interface Loggable {
    val loggerTag: String
        get() = getLogTag(javaClass)
}

private fun getLogTag(clazz: Class<*>): String {
    val tag = clazz.simpleName
    return if (tag.length <= 23) {
        tag
    } else {
        tag.substring(0, 23)
    }
}

fun Loggable.verbose(message: Any?, thr: Throwable? = null) {
    log(this,
        message,
        thr,
        Log.VERBOSE,
        { tag, msg -> Log.v(tag, msg) },
        { tag, msg, thr -> Log.v(tag, msg, thr) })
}

fun Loggable.debug(message: Any?, thr: Throwable? = null) {
    log(this,
        message,
        thr,
        Log.DEBUG,
        { tag, msg -> Log.d(tag, msg) },
        { tag, msg, thr -> Log.d(tag, msg, thr) })
}

fun Loggable.info(message: Any?, thr: Throwable? = null) {
    log(this,
        message,
        thr,
        Log.INFO,
        { tag, msg -> Log.i(tag, msg) },
        { tag, msg, thr -> Log.i(tag, msg, thr) })
}

fun Loggable.warn(message: Any?, thr: Throwable? = null) {
    log(this,
        message,
        thr,
        Log.WARN,
        { tag, msg -> Log.w(tag, msg) },
        { tag, msg, thr -> Log.w(tag, msg, thr) })
}

fun Loggable.error(message: Any?, thr: Throwable? = null) {
    log(this,
        message,
        thr,
        Log.ERROR,
        { tag, msg -> Log.e(tag, msg) },
        { tag, msg, thr -> Log.e(tag, msg, thr) })
}

inline fun Loggable.verbose(message: () -> Any?) {
    this.verbose(message())
}

inline fun Loggable.debug(message: () -> Any?) {
    this.debug(message())
}

inline fun Loggable.info(message: () -> Any?) {
    this.info(message())
}

inline fun Loggable.warn(message: () -> Any?) {
    this.warn(message())
}

inline fun Loggable.error(message: () -> Any?) {
    this.error(message())
}

fun Throwable.getStackTraceString(): String = Log.getStackTraceString(this)

private inline fun log(
    loggable: Loggable,
    message: Any?,
    throwable: Throwable?,
    level: Int,
    logFunction: (String, String) -> Unit,
    throwableLogFunction: (String, String, Throwable) -> Unit
) {
    val tag = loggable.loggerTag
    if (Log.isLoggable(tag, level)) {
        val logMessage = message?.toString() ?: "null"
        if (throwable != null) {
            throwableLogFunction(tag, logMessage, throwable)
            Firebase.crashlytics.log(logMessage)
            Firebase.crashlytics.recordException(throwable)
        } else {
            logFunction(tag, logMessage)
            Firebase.crashlytics.log(logMessage)
        }
    }
}