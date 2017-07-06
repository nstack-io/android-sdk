package dk.nodes.kstack.providers

import android.content.Context
import dk.nodes.kstack.kLog
import okhttp3.Cache

/**
 * Created by bison on 23-05-2017.
 */
object HttpCacheProvider {
    val TAG = HttpCacheProvider.javaClass.simpleName

    fun provideCache(context : Context) : Cache?
    {
        try {
            val cacheDirectory = context.cacheDir
            val cacheSize = 10 * 1024 * 1024 // 10 MiB
            val cache = Cache(cacheDirectory, cacheSize.toLong())
            return cache
        } catch (e: Exception) {
            kLog(TAG, e.toString())
        }
        return null
    }
}