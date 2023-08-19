package uk.co.armedpineapple.cth

import android.os.Environment
import androidx.annotation.Keep
import java.nio.charset.Charset

fun ByteArray.toUtf8String(): String {
    return String(this, Charset.forName("UTF-8"))
}

@Keep
class NativeLogger : Logger("CorsixTH Native") {

    private val postLock = Any()

    @Keep
    fun info(message: ByteArray) {
        splitAndPost(message) { this.info(it) }
    }

    @Keep
    fun warn(message: ByteArray) {
        splitAndPost(message) { this.warn(it) }
    }

    @Keep
    fun error(message: ByteArray) {
        splitAndPost(message) { this.error(it) }
    }

    @Keep
    fun debug(message: ByteArray) {
        splitAndPost(message) { this.debug(it) }
    }

    private fun splitAndPost(message: ByteArray, poster: (s: String) -> Unit) {
        synchronized(postLock) {
            val asString = message.toUtf8String()

            for (m in asString.splitToSequence('\n')) {
                if (m.isNotEmpty()) {
                    poster(m)
                }
            }
        }
    }
}

class NativeLuaHandlerException(handler: ByteArray?, stack: ByteArray?) :
    NativeLuaException(stack, run {
        val handlerStr = handler?.toUtf8String() ?: "unknown"
        "Error in $handlerStr handler"
    })


open class NativeLuaException(stack: ByteArray?, message: String) : Exception(run {
    val strBuilder: StringBuilder = StringBuilder(message)
    if (stack != null) {
        for (m in stack.toUtf8String().splitToSequence('\n')) {
            strBuilder.append(System.lineSeparator()).append(m.trim())
        }
    }
    strBuilder.toString().trim()
})
