package net.ntworld.mergeRequestIntegration.provider

import io.kotlintest.specs.DescribeSpec
import io.mockk.every
import io.mockk.spyk
import net.ntworld.mergeRequest.api.Cache
import net.ntworld.mergeRequest.api.CacheNotFoundException
import net.ntworld.mergeRequestIntegration.exception.InvalidCacheKeyException
import net.ntworld.mergeRequestIntegration.exception.InvalidTTLException
import org.joda.time.DateTime
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MemoryCacheTests : DescribeSpec({
    fun makeMemoryCache(defaultTTL: Int? = null) = MemoryCache(ttl = defaultTTL)
    fun makeMemoryCacheSpy() = spyk(MemoryCache())

    describe(".defaultTTL") {
        context("use default value") {
            it("return 60 seconds") {
                val cache = makeMemoryCache()

                val defaultTimeToLive = cache.defaultTTL

                assertEquals(60000, defaultTimeToLive)
            }
        }

        context("use constructable valid value") {
            it("return given value") {
                val cache = makeMemoryCache(1)

                val defaultTimeToLive = cache.defaultTTL

                assertEquals(1, defaultTimeToLive)
            }
        }

        context("use constructable invalid value") {
            it("throws InvalidTTLException") {
                assertFailsWith<InvalidTTLException> {
                    makeMemoryCache(0)
                }
            }
        }
    }

    describe(".get()") {
        context("invalid key") {
            it("throws InvalidCacheKeyException with empty key") {
                val cache = makeMemoryCache()

                assertFailsWith<InvalidCacheKeyException> {
                    cache.get<Any>("")
                }
            }

            it("throws InvalidCacheKeyException with key contains all space characters") {
                val cache = makeMemoryCache()

                assertFailsWith<InvalidCacheKeyException> {
                    cache.get<Any>(" ")
                }
            }
        }

        context("does not exists") {
            it("throws CacheNotFoundException") {
                val cache = makeMemoryCache()

                assertFailsWith<CacheNotFoundException> {
                    cache.get("key-not-found")
                }
            }
        }

        context("exists, still alive") {
            it("returns set value") {
                val cache = makeMemoryCache()
                cache.set("abcd", "value")

                val result: String = cache.get("abcd")

                assertEquals("value", result)
            }
        }

        context("exists, expired") {
            it("throws CacheNotFoundException") {
                val cache = makeMemoryCacheSpy()
                every { cache.isExpired(any()) } returns true
                cache.set("abc", "")

                assertFailsWith<CacheNotFoundException> {
                    cache.get("abc")
                }
            }
        }
    }

    describe(".has()") {
        context("invalid key") {
            it("throws InvalidCacheKeyException with empty key") {
                val cache = makeMemoryCache()

                assertFailsWith<InvalidCacheKeyException> {
                    cache.has("")
                }
            }

            it("throws InvalidCacheKeyException with key contains all space characters") {
                val cache = makeMemoryCache()

                assertFailsWith<InvalidCacheKeyException> {
                    cache.has(" ")
                }
            }
        }

        context("does not exists") {
            it("returns false") {
                val cache = makeMemoryCache()

                val result = cache.has("key-not-found")

                assertFalse(result)
            }
        }

        context("key exists, still alive") {
            it("returns true") {
                val cache = makeMemoryCache()
                cache.set("abc", "")

                val result = cache.has("abc")

                assertTrue(result)
            }
        }

        context("key exists, expired") {
            it("returns false") {
                val cache = makeMemoryCacheSpy()
                every { cache.isExpired(any()) } returns true
                cache.set("abc", "")

                val result = cache.has("abc")

                assertFalse(result)
            }
        }
    }

    describe(".remove()") {
        context("invalid key") {
            it("throws InvalidCacheKeyException with empty key") {
                val cache = makeMemoryCache()

                assertFailsWith<InvalidCacheKeyException> {
                    cache.remove("")
                }
            }

            it("throws InvalidCacheKeyException with key contains all space characters") {
                val cache = makeMemoryCache()

                assertFailsWith<InvalidCacheKeyException> {
                    cache.remove(" ")
                }
            }
        }

        context("valid key") {
            it("removes given key") {
                val cache = makeMemoryCache()
                cache.set("abc", "")

                val beforeRemoving = cache.has("abc")
                cache.remove("abc")
                val afterRemoving = cache.has("abc")

                assertTrue(beforeRemoving)
                assertFalse(afterRemoving)
            }
        }
    }

    describe(".isExpiredAfter()") {
        context("invalid key") {
            it("throws InvalidCacheKeyException with empty key") {
                val cache = makeMemoryCache()

                assertFailsWith<InvalidCacheKeyException> {
                    cache.isExpiredAfter("", DateTime.now())
                }
            }

            it("throws InvalidCacheKeyException with key contains all space characters") {
                val cache = makeMemoryCache()

                assertFailsWith<InvalidCacheKeyException> {
                    cache.isExpiredAfter(" ", DateTime.now())
                }
            }
        }

        context("key not found") {
            it("returns true") {
                val cache = makeMemoryCache()

                assertFailsWith<CacheNotFoundException> {
                    cache.isExpiredAfter("abc", DateTime.now())
                }
            }
        }

        context("key found") {
            it("returns false if not expired yet") {
                val cache = makeMemoryCache()
                cache.set("abc", "value")

                val result = cache.isExpiredAfter("abc", DateTime.now())

                assertFalse(result)
            }

            it("returns true if the key already expired") {
                val cache = makeMemoryCache()
                cache.set("abc", "value")

                val result = cache.isExpiredAfter("abc", DateTime.now().plus(86400))

                assertTrue(result)
            }
        }
    }
})
