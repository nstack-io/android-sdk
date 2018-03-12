package dk.nodes.nstack.kotlin.providers

import android.content.Context
import dk.nodes.nstack.kotlin.util.nLog
import okhttp3.Cache

object HttpCacheProvider {
    private val TAG = HttpCacheProvider::class.simpleName!!

    fun provideCache(context: Context): Cache? {
        try {
            val cacheDirectory = context.cacheDir
            val cacheSize = 10 * 1024 * 1024 // 10 MiB
            val cache = Cache(cacheDirectory, cacheSize.toLong())
            return cache
        } catch (e: Exception) {
            nLog(TAG, e.toString())
        }
        return null
    }
}