package uk.co.armedpineapple.cth

import android.content.*
import android.util.*
import android.widget.*

object Reporting {
    private val TAG = "CorsixTH"

    fun reportWithToast(ctx: Context, msg: String, e: Exception) {
        Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show()
        Log.w(TAG, msg, e)
    }

    fun event(msg: String) {
        Log.i(TAG, msg)
    }

    fun report(e: Exception) {
        Log.e(TAG, "Exception", e)

    }

    fun report(log: String, e: Exception) {
        Log.e(TAG, log, e)
    }

    fun setBool(key: String, value: Boolean) {}

    fun setDouble(key: String, value: Double) {}

    fun setFloat(key: String, value: Float) {}

    fun setInt(key: String, value: Int) {}

    fun setLong(key: String, value: Long) {}

    fun getLogger(tag: String): Logger {
        return Logger(tag)
    }

    class Logger internal constructor(private val tag: String) {

        fun d(msg: String) {

            Log.d(tag, msg)

        }

        fun e(msg: String) {
            Log.e(tag, msg)

        }

        fun i(msg: String) {

            Log.i(tag, msg)
        }

        fun w(msg: String) {
            Log.w(tag, msg)
        }

        fun v(msg: String) {
            Log.v(tag, msg)

        }
    }
}
