package com.example.network

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @Json(name = "tools") val tools: List<GeminiTool>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiTool(
    @Json(name = "googleSearch") val googleSearch: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @Json(name = "temperature") val temperature: Float? = 0.5f,
    @Json(name = "maxOutputTokens") val maxOutputTokens: Int? = 1024
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content?
)

interface GeminiApiService {
    @POST("v1beta/models/{modelName}:generateContent")
    suspend fun generateContent(
        @Path("modelName") modelName: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: GeminiApiService = retrofit.create(GeminiApiService::class.java)

    /**
     * Executes a chat/prompt request on the Gemini 3.5 Flash model with system instruction.
     */
    /**
     * Executes a chat/prompt request on the Gemini models with system instruction.
     * Uses fallback strategies to bypass rate limit (429) or endpoint not found (404) errors gracefully.
     */
    suspend fun askMwalimuAi(prompt: String, chatHistory: List<com.example.data.StudyCircleMessage> = emptyList()): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Mwalimu AI Error: Gemini API Key is not configured. Please add your GEMINI_API_KEY in the Secrets panel."
        }

        val systemPrompt = """
            You are 'Mwalimu AI', an inspiring, friendly, and exceptionally knowledgeable 24/7 AI Tutor and academic advisor built for Kenyan students on the ElimuHub ecosystem.
            
            You have REAL-TIME GOOGLE SEARCH GROUNDING enabled, meaning you can access the whole internet to find the latest academic syllabi, reference materials, news, or factual info. Use this power to provide highly accurate, up-to-date answers.
            
            When a student asks you a question or requests learning assistance, you MUST:
            1. PROVIDE NOTES & EXPLAIN EVERYTHING DEEP: Do not give short, superficial, or brief answers. Proactively provide structured, beautiful study notes, summaries, and thorough step-by-step explanations of concepts, formulas, history, or code.
            2. STUDENT-FRIENDLY & UNDERSTANDABLE: Break down complex topics into simple, clear, and relatable analogies. Define any technical jargon immediately.
            3. SUPPORT RECURRING TOPICS: Adapt to the active topic (e.g., General Tutoring, Math Solver, Essay Review, Homework Help, Career Guide) and tailor your notes/explanation to suit that subject's depth.
            4. USE LOCAL KENYAN CONTEXT: Incorporate Kenyan examples where appropriate (such as KES currency, JKUAT/UoN/Kenyatta University, CDF funding, secondary school curricula like KCSE/CBC, or TVET programs).
            5. GORGEOUS FORMATTING: Use Markdown headers (###), bold text, bullet points, clean spacing, and well-commented code blocks (when explaining programming) so the notes are extremely readable, scannable, and ready to be studied or copied.
            
            Always be encouraging, supportive, and sound like a passionate, world-class educator who believes in the student's success!
        """.trimIndent()

        val systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))

        // Build the contents conversation history
        val contents = mutableListOf<Content>()
        
        // Take the last 10 messages for context
        val relevantHistory = chatHistory.takeLast(10)
        for (msg in relevantHistory) {
            val rolePart = if (msg.senderName == "Mwalimu AI Chatbot") {
                Part(text = msg.message)
            } else {
                Part(text = "${msg.senderName}: ${msg.message}")
            }
            contents.add(Content(parts = listOf(rolePart)))
        }
        
        // Add current prompt
        contents.add(Content(parts = listOf(Part(text = prompt))))

        // Try 1: gemini-3.5-flash with Google Search grounding
        try {
            val request = GeminiRequest(
                contents = contents,
                systemInstruction = systemInstruction,
                generationConfig = GenerationConfig(temperature = 0.6f),
                tools = listOf(GeminiTool(googleSearch = emptyMap()))
            )
            val response = service.generateContent("gemini-3.5-flash", apiKey, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!text.isNullOrBlank()) return text
        } catch (e: Exception) {
            android.util.Log.e("GeminiApiClient", "Attempt 1 (gemini-3.5-flash + search) failed: ${e.localizedMessage}")
        }

        // Try 2: gemini-3.1-pro-preview with Google Search grounding (alternative advanced grounding model)
        try {
            val request = GeminiRequest(
                contents = contents,
                systemInstruction = systemInstruction,
                generationConfig = GenerationConfig(temperature = 0.6f),
                tools = listOf(GeminiTool(googleSearch = emptyMap()))
            )
            val response = service.generateContent("gemini-3.1-pro-preview", apiKey, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!text.isNullOrBlank()) return text
        } catch (e: Exception) {
            android.util.Log.e("GeminiApiClient", "Attempt 2 (gemini-3.1-pro-preview + search) failed: ${e.localizedMessage}")
        }

        // Try 3: gemini-3.5-flash WITHOUT Google Search grounding (highly stable, high free-tier rate limit)
        try {
            val request = GeminiRequest(
                contents = contents,
                systemInstruction = systemInstruction,
                generationConfig = GenerationConfig(temperature = 0.6f),
                tools = null
            )
            val response = service.generateContent("gemini-3.5-flash", apiKey, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!text.isNullOrBlank()) return text
        } catch (e: Exception) {
            android.util.Log.e("GeminiApiClient", "Attempt 3 (gemini-3.5-flash without search) failed: ${e.localizedMessage}")
        }

        // Try 4: gemini-3.1-flash-lite-preview WITHOUT Google Search grounding (extremely lightweight & reliable fallback)
        try {
            val request = GeminiRequest(
                contents = contents,
                systemInstruction = systemInstruction,
                generationConfig = GenerationConfig(temperature = 0.6f),
                tools = null
            )
            val response = service.generateContent("gemini-3.1-flash-lite-preview", apiKey, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!text.isNullOrBlank()) return text
        } catch (e: Exception) {
            android.util.Log.e("GeminiApiClient", "Attempt 4 (gemini-3.1-flash-lite-preview without search) failed: ${e.localizedMessage}")
            return "Mwalimu AI Error: All API endpoints returned rate limits or connection errors. Details: ${e.localizedMessage ?: "HTTP 429 / 404"}. Please try again shortly or configure a premium key."
        }

        return "Mwalimu AI: I apologize, but I could not formulate a response at this time."
    }

    /**
     * Scouts for opportunities (bursaries, jobs, internships, attachments, learning materials) in Kenya
     * based on a query focus. Returns a JSON string of opportunities with fallback modes.
     */
    suspend fun scoutOpportunities(query: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "[]"
        }

        val systemPrompt = """
            You are 'ElimuHub AI Scout', an intelligent assistant that utilizes Google Search grounding to discover and extract real-world educational and career opportunities in Kenya.
            You MUST run real-time Google Search queries to find actual, up-to-date active bursaries, jobs, internships, and attachment offers in Kenya.
            
            Based on the focus of the query, scout for active postings, official county portals, corporate websites (e.g., Safaricom, Equity Bank, KCB, Airtel, EABL), CDF offices, TVET portals, or university announcements.
            
            You MUST return a JSON array containing 3 to 6 opportunities. Each opportunity must be represented by a JSON object with the following fields:
            1. "type": Must be one of: "Bursary", "Career", "Material"
            2. "title": Opportunity name (e.g. "HEB TVET Sponsorship Program", "Safaricom Junior Developer Internship", "KCSE Chemistry Revision Notes")
            3. "providerOrCompany": Provider or company name (e.g. "Nairobi County Government", "Equity Bank", "University of Nairobi")
            4. "category": Category of the opportunity. For "Bursary" use "Government", "Corporate", or "International". For "Career" use "Local Internships", "Remote Jobs", or "International". For "Material" use "Notes", "Past Paper", "Book", or "Video".
            5. "description": Detailed description of the opportunity, summarizing what was found on search.
            6. "eligibilityOrRequirement": Requirements for applicants or level prerequisites.
            7. "amountOrLocation": For Bursary, the funding amount (e.g. "KES 35,000" or "Full tuition"). For Career, location (e.g. "Nairobi, Kenya" or "Hybrid"). For Material, course or empty.
            8. "deadlineOrType": For Bursary/Career, deadline as "YYYY-MM-DD" (use 2026 dates). For Material, type of material (e.g. "Notes").
            9. "countyOrLevel": County restrictions (e.g. "Meru", "Nairobi", "All") or education level (e.g. "High School", "TVET", "University").
            10. "urlOrSize": For Material, URL or size. For Bursary/Career, provide the actual or highly realistic website application URL found via search.
            11. "extra1": Optional additional details (e.g., match score "85" for careers, course name for materials).
            12. "extra2": Optional additional details (e.g., level for bursary, university name for materials).

            Ensure all entries are realistic for the Kenyan educational context (CDF, county bursaries, TVET institutions like Kabete National Polytechnic, local universities like JKUAT, UoN, Kenyatta University, and companies like Safaricom, Equity, KCB, Airtel, EABL, etc.).
            Do NOT include any markdown formatting (like ```json or ```). Return ONLY the raw valid JSON array.
        """.trimIndent()

        val systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
        val contents = listOf(Content(parts = listOf(Part(text = "Discover opportunities with focus: $query"))))

        // Try 1: gemini-3.5-flash with Google Search grounding
        try {
            val request = GeminiRequest(
                contents = contents,
                systemInstruction = systemInstruction,
                generationConfig = GenerationConfig(temperature = 0.8f),
                tools = listOf(GeminiTool(googleSearch = emptyMap()))
            )
            val response = service.generateContent("gemini-3.5-flash", apiKey, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!text.isNullOrBlank()) return text
        } catch (e: Exception) {
            android.util.Log.e("GeminiApiClient", "Scout attempt 1 (gemini-3.5 + search) failed: ${e.localizedMessage}")
        }

        // Try 2: gemini-3.1-pro-preview with Google Search grounding (alternative advanced grounding model)
        try {
            val request = GeminiRequest(
                contents = contents,
                systemInstruction = systemInstruction,
                generationConfig = GenerationConfig(temperature = 0.8f),
                tools = listOf(GeminiTool(googleSearch = emptyMap()))
            )
            val response = service.generateContent("gemini-3.1-pro-preview", apiKey, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!text.isNullOrBlank()) return text
        } catch (e: Exception) {
            android.util.Log.e("GeminiApiClient", "Scout attempt 2 (gemini-3.1-pro-preview + search) failed: ${e.localizedMessage}")
        }

        // Try 3: gemini-3.5-flash without Search Grounding (generous free rate limit)
        try {
            val request = GeminiRequest(
                contents = contents,
                systemInstruction = systemInstruction,
                generationConfig = GenerationConfig(temperature = 0.8f),
                tools = null
            )
            val response = service.generateContent("gemini-3.5-flash", apiKey, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!text.isNullOrBlank()) return text
        } catch (e: Exception) {
            android.util.Log.e("GeminiApiClient", "Scout attempt 3 (gemini-3.5 without search) failed: ${e.localizedMessage}")
        }

        // Try 4: gemini-3.1-flash-lite-preview without Search Grounding (extremely reliable fallback)
        try {
            val request = GeminiRequest(
                contents = contents,
                systemInstruction = systemInstruction,
                generationConfig = GenerationConfig(temperature = 0.8f),
                tools = null
            )
            val response = service.generateContent("gemini-3.1-flash-lite-preview", apiKey, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!text.isNullOrBlank()) return text
        } catch (e: Exception) {
            android.util.Log.e("GeminiApiClient", "Scout attempt 4 (gemini-3.1-flash-lite-preview) failed: ${e.localizedMessage}")
        }

        return "[]"
    }
}
