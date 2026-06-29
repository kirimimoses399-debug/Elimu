package com.example.network

import android.content.Context
import android.util.Log
import com.example.data.BursaryOpportunity
import com.example.data.CareerOpportunity
import com.example.data.LearningMaterial
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

object FirebaseApiClient {
    private const val TAG = "FirebaseApiClient"
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val _syncStatus = MutableStateFlow<FirebaseSyncStatus>(FirebaseSyncStatus.Unconfigured)
    val syncStatus: StateFlow<FirebaseSyncStatus> = _syncStatus

    // Persistent settings loaded/saved from SharedPreferences
    private var firebaseDbUrl: String = ""
    private var firebaseSecretToken: String = ""

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences("elimuhub_firebase_prefs", Context.MODE_PRIVATE)
        firebaseDbUrl = prefs.getString("db_url", "") ?: ""
        firebaseSecretToken = prefs.getString("secret_token", "") ?: ""
        updateStatus()
    }

    fun configure(context: Context, url: String, token: String) {
        var cleanUrl = url.trim()
        if (cleanUrl.isNotEmpty() && !cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            cleanUrl = "https://$cleanUrl"
        }
        if (cleanUrl.isNotEmpty() && !cleanUrl.endsWith("/")) {
            cleanUrl = "$cleanUrl/"
        }

        firebaseDbUrl = cleanUrl
        firebaseSecretToken = token.trim()

        val prefs = context.getSharedPreferences("elimuhub_firebase_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("db_url", firebaseDbUrl)
            .putString("secret_token", firebaseSecretToken)
            .apply()

        updateStatus()
    }

    fun getDbUrl(): String = firebaseDbUrl
    fun getSecretToken(): String = firebaseSecretToken

    private fun updateStatus() {
        if (firebaseDbUrl.isEmpty()) {
            _syncStatus.value = FirebaseSyncStatus.Unconfigured
        } else {
            _syncStatus.value = FirebaseSyncStatus.ConfiguredOffline(firebaseDbUrl)
        }
    }

    fun setStatus(status: FirebaseSyncStatus) {
        _syncStatus.value = status
    }

    private fun escapeJson(value: String): String {
        return value.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    /**
     * Test the connection to the custom Firebase Realtime Database using a lightweight ping.
     */
    suspend fun testConnection(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (firebaseDbUrl.isEmpty()) {
            return@withContext Pair(false, "Firebase Database URL is not configured.")
        }

        val testPath = "${firebaseDbUrl}connection_test.json"
        val urlWithAuth = if (firebaseSecretToken.isNotEmpty()) {
            "$testPath?auth=$firebaseSecretToken"
        } else {
            testPath
        }

        val testPayload = """{"ping": ${System.currentTimeMillis()}, "sender": "ElimuHub Admin"}"""
        val request = Request.Builder()
            .url(urlWithAuth)
            .put(testPayload.toRequestBody(JSON_MEDIA_TYPE))
            .build()

        try {
            _syncStatus.value = FirebaseSyncStatus.Syncing
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    _syncStatus.value = FirebaseSyncStatus.Synced(firebaseDbUrl)
                    Pair(true, "Connection successful! Test payload published.")
                } else {
                    val errMsg = "HTTP Error Code: ${response.code} - ${response.message}"
                    _syncStatus.value = FirebaseSyncStatus.Error(firebaseDbUrl, errMsg)
                    Pair(false, errMsg)
                }
            }
        } catch (e: Exception) {
            val errMsg = e.localizedMessage ?: "Network Connection Failure."
            _syncStatus.value = FirebaseSyncStatus.Error(firebaseDbUrl, errMsg)
            Pair(false, errMsg)
        }
    }

    /**
     * Upload a Bursary Opportunity to Firebase RTDB
     */
    suspend fun uploadBursary(bursary: BursaryOpportunity): Boolean = withContext(Dispatchers.IO) {
        if (firebaseDbUrl.isEmpty()) return@withContext false

        // We use the ID as the key or a timestamp if it is 0
        val key = if (bursary.id == 0) "bursary_${System.currentTimeMillis()}" else "bursary_${bursary.id}"
        val uploadPath = "${firebaseDbUrl}bursary_opportunities/$key.json"
        val urlWithAuth = if (firebaseSecretToken.isNotEmpty()) {
            "$uploadPath?auth=$firebaseSecretToken"
        } else {
            uploadPath
        }

        val json = """
            {
              "id": ${if (bursary.id == 0) System.currentTimeMillis() % 1000000 else bursary.id},
              "title": "${escapeJson(bursary.title)}",
              "provider": "${escapeJson(bursary.provider)}",
              "category": "${escapeJson(bursary.category)}",
              "amount": "${escapeJson(bursary.amount)}",
              "deadline": "${escapeJson(bursary.deadline)}",
              "description": "${escapeJson(bursary.description)}",
              "eligibility": "${escapeJson(bursary.eligibility)}",
              "county": "${escapeJson(bursary.county)}",
              "course": "${escapeJson(bursary.course)}",
              "level": "${escapeJson(bursary.level)}"
            }
        """.trimIndent()

        val request = Request.Builder()
            .url(urlWithAuth)
            .put(json.toRequestBody(JSON_MEDIA_TYPE))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Log.d(TAG, "Successfully uploaded bursary to Firebase")
                    true
                } else {
                    Log.e(TAG, "Firebase Upload Failed: ${response.code} ${response.message}")
                    false
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Firebase Upload Error", e)
            false
        }
    }

    /**
     * Upload a Career/Internship Opportunity to Firebase RTDB
     */
    suspend fun uploadCareer(career: CareerOpportunity): Boolean = withContext(Dispatchers.IO) {
        if (firebaseDbUrl.isEmpty()) return@withContext false

        val key = if (career.id == 0) "career_${System.currentTimeMillis()}" else "career_${career.id}"
        val uploadPath = "${firebaseDbUrl}career_opportunities/$key.json"
        val urlWithAuth = if (firebaseSecretToken.isNotEmpty()) {
            "$uploadPath?auth=$firebaseSecretToken"
        } else {
            uploadPath
        }

        val json = """
            {
              "id": ${if (career.id == 0) System.currentTimeMillis() % 1000000 else career.id},
              "title": "${escapeJson(career.title)}",
              "company": "${escapeJson(career.company)}",
              "location": "${escapeJson(career.location)}",
              "category": "${escapeJson(career.category)}",
              "matchScore": ${career.matchScore},
              "description": "${escapeJson(career.description)}",
              "requirement": "${escapeJson(career.requirement)}",
              "deadline": "${escapeJson(career.deadline)}"
            }
        """.trimIndent()

        val request = Request.Builder()
            .url(urlWithAuth)
            .put(json.toRequestBody(JSON_MEDIA_TYPE))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Log.d(TAG, "Successfully uploaded career to Firebase")
                    true
                } else {
                    Log.e(TAG, "Firebase Upload Failed: ${response.code} ${response.message}")
                    false
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Firebase Upload Error", e)
            false
        }
    }

    /**
     * Upload a Learning Material (notes, videos, books, past papers) to Firebase RTDB
     */
    suspend fun uploadLearningMaterial(material: LearningMaterial): Boolean = withContext(Dispatchers.IO) {
        if (firebaseDbUrl.isEmpty()) return@withContext false

        val key = if (material.id == 0) "material_${System.currentTimeMillis()}" else "material_${material.id}"
        val uploadPath = "${firebaseDbUrl}learning_materials/$key.json"
        val urlWithAuth = if (firebaseSecretToken.isNotEmpty()) {
            "$uploadPath?auth=$firebaseSecretToken"
        } else {
            uploadPath
        }

        val json = """
            {
              "id": ${if (material.id == 0) System.currentTimeMillis() % 1000000 else material.id},
              "title": "${escapeJson(material.title)}",
              "level": "${escapeJson(material.level)}",
              "type": "${escapeJson(material.type)}",
              "url": "${escapeJson(material.url)}",
              "size": "${escapeJson(material.size)}",
              "isDownloaded": ${material.isDownloaded},
              "course": "${escapeJson(material.course)}",
              "university": "${escapeJson(material.university)}"
            }
        """.trimIndent()

        val request = Request.Builder()
            .url(urlWithAuth)
            .put(json.toRequestBody(JSON_MEDIA_TYPE))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Log.d(TAG, "Successfully uploaded material to Firebase")
                    true
                } else {
                    Log.e(TAG, "Firebase Upload Failed: ${response.code} ${response.message}")
                    false
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Firebase Upload Error", e)
            false
        }
    }
}

sealed class FirebaseSyncStatus {
    object Unconfigured : FirebaseSyncStatus()
    data class ConfiguredOffline(val url: String) : FirebaseSyncStatus()
    object Syncing : FirebaseSyncStatus()
    data class Synced(val url: String) : FirebaseSyncStatus()
    data class Error(val url: String, val error: String) : FirebaseSyncStatus()
}
