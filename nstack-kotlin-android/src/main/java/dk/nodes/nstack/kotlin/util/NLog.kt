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

    var level: Level = Level.INFO

    private val inclusiveLevelComparator: (Level) -> Boolean = { it.value >= level.value }
    private val exclusiveLevelComparator: (Level) -> Boolean = { it.value > level.value }

    fun e(parent: Any, message: String) {
        log(parent, Level.ERROR, inclusiveLevelComparator) { Log.e(it, message) }
    }

    fun e(parent: Any, message: String, exception: Exception) {
        log(parent, Level.ERROR, inclusiveLevelComparator) { Log.e(it, message, exception) }
    }

    fun w(parent: Any, message: String) {
        log(parent, Level.WARN) { Log.w(it, message) }
    }

    fun i(parent: Any, message: String) {
        log(parent, Level.INFO) { Log.v(it, message) }
    }

    fun v(parent: Any, message: String) {
        log(parent, Level.VERBOSE) { Log.v(it, message) }
    }

    fun d(parent: Any, message: String) {
        log(parent, Level.DEBUG) { Log.d(it, message) }
    }

    private fun log(
        parent: Any,
        level: Level,
        levelComparator: (Level) -> Boolean = exclusiveLevelComparator,
        logger: (tag: String) -> Unit
    ) {
        if (enableLogging && levelComparator(level)) {
            logger(parent.javaClass.simpleName)
        }
    }
}
