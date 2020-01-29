package net.ntworld.mergeRequestIntegration.provider

import net.ntworld.mergeRequest.api.Cache
import net.ntworld.mergeRequest.api.CacheNotFoundException
import net.ntworld.mergeRequestIntegration.exception.InvalidCacheKeyException
import net.ntworld.mergeRequestIntegration.exception.InvalidTTLException
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime

class MemoryCache(ttl: Int? = null) : Cache {
    private val data = mutableMapOf<String, CachedData>()

    override var defaultTTL: Int = 0
        private set

    init {
        defaultTTL = if (null === ttl) {
            60000
        } else {
            if (ttl <= 0) {
                throw InvalidTTLException()
            }
            ttl
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(key: String): T {
        assertKeyIsValid(key)
        if (!data.containsKey(key)) {
            throw CacheNotFoundException()
        }

        val cachedItem = data[key]
        if (null === cachedItem || isExpired(cachedItem.expired)) {
            throw CacheNotFoundException()
        }
        return cachedItem.value as T
    }

    override fun has(key: String): Boolean {
        assertKeyIsValid(key)
        if (!data.containsKey(key)) {
            return false
        }
        val cachedItem = data[key]
        return null !== cachedItem && !isExpired(cachedItem.expired)
    }

    override fun remove(key: String) {
        assertKeyIsValid(key)
        data.remove(key)
    }

    override fun put(key: String, value: Any, ttl: Int) {
        data[key] = CachedData(value, now() + ttl)
    }

    override fun isExpiredAfter(key: String, datetime: DateTime): Boolean {
        assertKeyIsValid(key)
        val cachedItem = data[key]
        if (null === cachedItem) {
            throw CacheNotFoundException()
        }
        return cachedItem.expired < toUtc(datetime).millis
    }

    private fun assertKeyIsValid(key: String) {
        if (key.trim().isEmpty()) {
            throw InvalidCacheKeyException("Key is invalid")
        }
    }

    internal fun isExpired(expired: Long): Boolean {
        return expired < now()
    }

    internal fun now(): Long {
        return toUtc(DateTime.now()).millis
    }

    private fun toUtc(datetime: DateTime): DateTime {
        return LocalDateTime(datetime).toDateTime(DateTimeZone.UTC)
    }

    private data class CachedData(
        val value: Any,
        val expired: Long
    )
}