package net.remmintan.mods.minefortress.core.services

import com.google.gson.Gson
import net.remmintan.mods.minefortress.core.dtos.SupportLevel
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

object PatronStatusService {
    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    private val gson = Gson()

    private val supporterCache = ConcurrentHashMap<String, CachedSupporterStatus>()
    private val CACHE_DURATION = Duration.ofHours(1)

    fun getSupporterStatus(playerName: String): SupportLevel {
        val cachedStatus = supporterCache[playerName]

        // If we have a cached result that hasn't expired, return it
        if (cachedStatus != null && !cachedStatus.isExpired()) {
            return cachedStatus.supportLevel
        }

        // Otherwise, fetch new data from the API
        val supporterLevel = fetchSupporterStatus(playerName)

        // Only cache non-error results
        if (supporterLevel != SupportLevel.ERROR) {
            supporterCache[playerName] = CachedSupporterStatus(
                supportLevel = supporterLevel,
                expirationTime = Instant.now().plus(CACHE_DURATION)
            )
        }

        return supporterLevel
    }

    private fun fetchSupporterStatus(playerName: String): SupportLevel {
        try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.minefortress.net/api/supporter?name=$playerName"))
                .GET()
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() == 200) {
                val apiResponse = gson.fromJson(response.body(), SupporterApiResponse::class.java)
                return SupportLevel.valueOf(apiResponse.supportLevel)
            } else {
                return SupportLevel.ERROR
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return SupportLevel.ERROR
        }
    }

    // Data classes for the cache and API response
    private data class CachedSupporterStatus(
        val supportLevel: SupportLevel,
        val expirationTime: Instant
    ) {
        fun isExpired(): Boolean = Instant.now().isAfter(expirationTime)
    }

    private data class SupporterApiResponse(
        val supportLevel: String
    )
}