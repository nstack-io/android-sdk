package dk.nodes.nstack.kotlin.util

import android.annotation.SuppressLint
import android.util.Log

@SuppressLint("LogNotTimber")
class NLog {
    companion object {
        var enableLogging = false

        fun i(tag: String, string: String) {
            if (!enableLogging) {
                return
            }

            Log.i(tag, string)
        }

        fun w(tag: String, string: String) {
            if (!enableLogging) {
                return
            }

            Log.w(tag, string)
        }

        fun v(tag: String, string: String) {
            if (!enableLogging) {
                return
            }

            Log.v(tag, string)
        }

        fun e(tag: String, string: String) {
            if (!enableLogging) {
                return
            }

            Log.e(tag, string)
        }

        fun e(tag: String, string: String, throwable: Throwable) {
            if (!enableLogging) {
                return
            }

            Log.e(tag, string, throwable)
        }

        fun d(tag: String, string: String) {
            if (!enableLogging) {
                return
            }

            Log.d(tag, string)
        }
    }
}