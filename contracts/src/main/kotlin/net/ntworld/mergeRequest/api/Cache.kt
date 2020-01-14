package net.ntworld.mergeRequest.api

import org.joda.time.DateTime

interface Cache {
    val defaultTTL: Int

    fun <T> get(key: String): T?

    fun has(key: String): Boolean

    fun remove(key: String)

    fun put(key: String, value: Any, ttl: Int)

    fun isExpiredAfter(key: String, datetime: DateTime): Boolean

    fun put(key: String, value: Any) = put(key, value, defaultTTL)

    fun set(key: String, value: Any) = put(key, value, defaultTTL)

    fun set(key: String, value: Any, ttl: Int) = put(key, value, ttl)

    @Suppress("UNCHECKED_CAST")
    fun<T> getOrRun(key: String, run: (() -> T)): T {
        if (!this.has(key)) {
            return run()
        }
        return this.get<T>(key) as T
    }

    fun removeIfExpiredAfter(key: String, datetime: DateTime) {
        if (isExpiredAfter(key, datetime)) {
            remove(key)
        }
    }
}