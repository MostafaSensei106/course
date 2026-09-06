package com.mostafasensei.course.core.config

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class CatalogCache {

    private data class Entry(val expiresAt: Long, val value: Any)

    private val store = ConcurrentHashMap<String, Entry>()
    private val maxEntries = 500

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String): T? {
        val hit = store[key] ?: return null
        if (hit.expiresAt <= System.currentTimeMillis()) {
            store.remove(key, hit)
            return null
        }
        return hit.value as T
    }

    fun put(key: String, value: Any, ttlMillis: Long) {
        if (store.size >= maxEntries) {
            val now = System.currentTimeMillis()
            store.entries.removeIf { it.value.expiresAt <= now }
            if (store.size >= maxEntries) store.clear()
        }
        store[key] = Entry(System.currentTimeMillis() + ttlMillis, value)
    }

    fun evictPublished() {
        store.keys.removeIf { it.startsWith(PUBLISHED_PREFIX) }
    }

    fun evictCategories() {
        store.remove(CATEGORIES_KEY)
    }

    companion object {
        const val CATEGORIES_KEY = "categories:all"
        const val PUBLISHED_PREFIX = "courses:published:"
        const val CATEGORIES_TTL_MILLIS = 10 * 60 * 1000L
        const val PUBLISHED_TTL_MILLIS = 60 * 1000L

        fun publishedKey(page: Int, size: Int, categoryId: Any?, search: String?): String =
            "$PUBLISHED_PREFIX$page:$size:$categoryId:${search.isNullOrBlank()}"
    }
}
