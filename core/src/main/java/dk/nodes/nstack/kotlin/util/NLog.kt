package dk.nodes.nstack.kotlin.util

import android.annotation.SuppressLint
import android.util.Log

@SuppressLint("LogNotTimber")
object NLog {
    var enableLogging: Boolean = false

    enum class Level(var value: Int) {
        OFF(5),
        ERROR(4),
        WARN(3),
        INFO(2),
        VERBOSE(1),
        DEBUG(0)
    }

    var level: Level = NLog.Level.INFO

    fun e(parent: Any, message: String) {
        if (!enableLogging) {
            return
        }

        if (level.value >= NLog.Level.ERROR.value) {
            return
        }

        Log.e(parent.javaClass.simpleName, message)
    }

    fun e(parent: Any, message: String, exception: Exception) {
        if (!enableLogging) {
            return
        }

        if (level.value >= NLog.Level.ERROR.value) {
            return
        }

        Log.e(parent.javaClass.simpleName, message, exception)
    }

    fun w(parent: Any, message: String) {
        if (!enableLogging) {
            return
        }

        if (level.value > NLog.Level.WARN.value) {
            return
        }
        Log.w(parent.javaClass.simpleName, message)
    }

    fun i(parent: Any, message: String) {
        if (!enableLogging) {
            return
        }

        if (level.value > NLog.Level.INFO.value) {
            return
        }
        Log.i(parent.javaClass.simpleName, message)
    }

    fun v(parent: Any, message: String) {
        if (!enableLogging) {
            return
        }

        if (level.value > NLog.Level.VERBOSE.value) {
            return
        }

        Log.v(parent.javaClass.simpleName, message)
    }

    fun d(parent: Any, message: String) {
        if (!enableLogging) {
            return
        }

        if (level.value > NLog.Level.DEBUG.value) {
            return
        }

        Log.d(parent.javaClass.simpleName, message)
    }
}