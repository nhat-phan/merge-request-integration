package net.ntworld.mergeRequestIntegration.provider

import net.ntworld.mergeRequest.api.Cache
import net.ntworld.mergeRequestIntegration.exception.InvalidCacheKeyException
import org.joda.time.DateTime

class MemoryCache : Cache {
    override val defaultTTL: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun <T> get(key: String): T? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun has(key: String): Boolean {
        throw InvalidCacheKeyException("Key is invalid")
    }

    override fun remove(key: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun put(key: String, value: Any, ttl: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isExpiredAfter(key: String, datetime: DateTime): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}