package net.ntworld.mergeRequestIntegration.provider

import io.kotlintest.specs.DescribeSpec
import net.ntworld.mergeRequest.api.Cache
import net.ntworld.mergeRequestIntegration.exception.InvalidCacheKeyException
import kotlin.test.assertFailsWith

class MemoryCacheTests : DescribeSpec({
    describe(".defaultTTL") {
    }

    describe(".get()") {
    }

    describe(".has()") {
        fun makeMemoryCache(): Cache = MemoryCache()

        it ("throws InvalidCacheKeyException with empty key") {
            val cache = makeMemoryCache()

            assertFailsWith<InvalidCacheKeyException> {
                cache.has("")
            }
        }
    }
})
