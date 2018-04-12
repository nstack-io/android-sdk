package dk.nodes.nstack.kotlin.util

import android.annotation.SuppressLint
import android.util.Log

@SuppressLint("LogNotTimber")
class NLog {
    enum class Level(public val level: Int) {
        Error(0),
        Warning(1),
        Info(2),
        Verbose(3),
        Debug(4)
    }

    companion object {
        var enableLogging: Boolean = false
        var level: Level = Level.Error

        fun e(tag: String, string: String) {
            if (!enableLogging || level < Level.Error) {
                return
            }

            Log.e(tag, string)
        }

        fun e(tag: String, string: String, throwable: Throwable) {
            if (!enableLogging || level < Level.Error) {
                return
            }

            Log.e(tag, string, throwable)
        }

        fun w(tag: String, string: String) {
            if (!enableLogging || level < Level.Warning) {
                return
            }

            Log.w(tag, string)
        }

        fun i(tag: String, string: String) {
            if (!enableLogging || level < Level.Info) {
                return
            }

            Log.i(tag, string)
        }

        fun v(tag: String, string: String) {
            if (!enableLogging || level < Level.Verbose) {
                return
            }

            Log.v(tag, string)
        }

        fun d(tag: String, string: String) {
            if (!enableLogging || level < Level.Debug) {
                return
            }

            Log.d(tag, string)
        }
    }
}