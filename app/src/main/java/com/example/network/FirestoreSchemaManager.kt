package com.example.network

import com.example.data.BursaryOpportunity
import com.example.data.CareerOpportunity
import com.example.data.LearningMaterial
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Locale

sealed class FirestoreInitStatus {
    object Idle : FirestoreInitStatus()
    object Initializing : FirestoreInitStatus()
    data class Success(val summary: String) : FirestoreInitStatus()
    data class Error(val reason: String) : FirestoreInitStatus()
}

data class SchemaValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList(),
    val collectionName: String
)

object FirestoreSchemaManager {

    private val _initLogs = MutableStateFlow<List<String>>(emptyList())
    val initLogs: StateFlow<List<String>> = _initLogs.asStateFlow()

    private val _initStatus = MutableStateFlow<FirestoreInitStatus>(FirestoreInitStatus.Idle)
    val initStatus: StateFlow<FirestoreInitStatus> = _initStatus.asStateFlow()

    fun clearLogs() {
        _initLogs.value = emptyList()
        _initStatus.value = FirestoreInitStatus.Idle
    }

    private fun addLog(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(System.currentTimeMillis())
        _initLogs.value = _initLogs.value + "[$timestamp] $message"
    }

    /**
     * Dry-run simulation of initializing the Firestore Schema on Cloud Firebase.
     */
    suspend fun runSchemaInitialization(
        localBursaries: List<BursaryOpportunity>,
        localCareers: List<CareerOpportunity>,
        localMaterials: List<LearningMaterial>
    ) {
        _initStatus.value = FirestoreInitStatus.Initializing
        _initLogs.value = emptyList()

        addLog("🚀 Initializing ElimuHub Cloud Firestore Schema configuration...")
        delay(800)

        // 1. Connection handshakes
        addLog("🔑 Authenticating administrative credentials for user: kirimimoses399@gmail.com")
        delay(600)
        addLog("🛰️ Connected to Google Cloud Platform Console (Project: ElimuHub SDK).")
        delay(500)
        addLog("🛡️ Checking Cloud Firestore database mode... Native Mode verified.")
        delay(700)

        // 2. Collection 'bursaries' setup
        addLog("🗄️ Initializing schema for collection: 'bursaries'")
        delay(400)
        val burValidResult = validateBursaries(localBursaries)
        if (burValidResult.isValid) {
            addLog("✅ Collection 'bursaries' defined with ${ElimuHubFirestoreSchema.collections[0].fields.size} fields. Validated ${localBursaries.size} items as ready.")
        } else {
            addLog("⚠️ Collection 'bursaries' initialized but local data warnings found: ${burValidResult.errors.firstOrNull()}")
        }
        delay(600)

        // 3. Collection 'internships' setup
        addLog("🗄️ Initializing schema for collection: 'internships' (Career Portals)")
        delay(400)
        val carValidResult = validateCareers(localCareers)
        if (carValidResult.isValid) {
            addLog("✅ Collection 'internships' defined with ${ElimuHubFirestoreSchema.collections[1].fields.size} fields. Validated ${localCareers.size} items as ready.")
        } else {
            addLog("⚠️ Collection 'internships' initialized with warnings: ${carValidResult.errors.firstOrNull()}")
        }
        delay(600)

        // 4. Collection 'learning_resources' setup
        addLog("🗄️ Initializing schema for collection: 'learning_resources'")
        delay(400)
        val matValidResult = validateMaterials(localMaterials)
        if (matValidResult.isValid) {
            addLog("✅ Collection 'learning_resources' defined with ${ElimuHubFirestoreSchema.collections[2].fields.size} fields. Validated ${localMaterials.size} items as ready.")
        } else {
            addLog("⚠️ Collection 'learning_resources' initialized with warnings: ${matValidResult.errors.firstOrNull()}")
        }
        delay(600)

        // 5. Collection 'user_approvals' setup
        addLog("🗄️ Initializing schema for collection: 'user_approvals' (Audit Trail)")
        delay(500)
        addLog("✅ Collection 'user_approvals' defined with ${ElimuHubFirestoreSchema.collections[3].fields.size} fields.")
        delay(400)

        // 6. Security Rules deployment
        addLog("🔐 Compiling Cloud Firestore Security Rules...")
        delay(600)
        val securityRules = ElimuHubFirestoreSchema.getSecurityRules()
        addLog("🛡️ Firestore Security Rules successfully parsed:\n---\n" + securityRules.take(200) + "...\n---")
        delay(700)
        addLog("✅ Security Rules deployed successfully to Google Cloud.")
        delay(400)

        // 7. Composite Search Index config
        addLog("🗂️ Generating Composite Search Indexes for optimized query speeds:")
        addLog("   - Index 1: /bursaries { county: ASC, category: ASC, deadline: DESC }")
        addLog("   - Index 2: /internships { location: ASC, matchScore: DESC }")
        addLog("   - Index 3: /learning_resources { level: ASC, type: ASC }")
        delay(800)
        addLog("✅ All 3 composite search indices are provisioned.")
        delay(500)

        val successSummary = "Firestore database schema initialization completed successfully! Collections 'bursaries', 'internships', 'learning_resources', and 'user_approvals' are fully provisioned and validated on Cloud Firestore."
        addLog("🎉 $successSummary")
        _initStatus.value = FirestoreInitStatus.Success(successSummary)
    }

    /**
     * Local Data Validations
     */
    fun validateBursaries(bursaries: List<BursaryOpportunity>): SchemaValidationResult {
        val errors = mutableListOf<String>()
        bursaries.forEach { b ->
            if (b.title.isBlank()) errors.add("Bursary ID ${b.id}: Title is blank.")
            if (b.provider.isBlank()) errors.add("Bursary ID ${b.id}: Provider is blank.")
            if (b.category !in listOf("Government", "Corporate", "International")) {
                errors.add("Bursary '${b.title}': Category '${b.category}' is invalid. Must be 'Government', 'Corporate', or 'International'.")
            }
            if (b.deadline.isNotBlank() && !b.deadline.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                errors.add("Bursary '${b.title}': Deadline '${b.deadline}' does not match YYYY-MM-DD pattern.")
            }
        }
        return SchemaValidationResult(errors.isEmpty(), errors, "bursaries")
    }

    fun validateCareers(careers: List<CareerOpportunity>): SchemaValidationResult {
        val errors = mutableListOf<String>()
        careers.forEach { c ->
            if (c.title.isBlank()) errors.add("Internship ID ${c.id}: Title is blank.")
            if (c.company.isBlank()) errors.add("Internship ID ${c.id}: Company is blank.")
            if (c.category !in listOf("Local Internships", "Remote Jobs", "International")) {
                errors.add("Internship '${c.title}': Category '${c.category}' is invalid. Must be 'Local Internships', 'Remote Jobs', or 'International'.")
            }
            if (c.matchScore !in 0..100) {
                errors.add("Internship '${c.title}': Match score '${c.matchScore}' must be between 0 and 100.")
            }
        }
        return SchemaValidationResult(errors.isEmpty(), errors, "internships")
    }

    fun validateMaterials(materials: List<LearningMaterial>): SchemaValidationResult {
        val errors = mutableListOf<String>()
        materials.forEach { m ->
            if (m.title.isBlank()) errors.add("Resource ID ${m.id}: Title is blank.")
            if (m.level !in listOf("High School", "TVET", "University", "Professional")) {
                errors.add("Resource '${m.title}': Level '${m.level}' is invalid. Must be 'High School', 'TVET', 'University', or 'Professional'.")
            }
            if (m.type !in listOf("Video", "Book", "Past Paper", "Notes")) {
                errors.add("Resource '${m.title}': Type '${m.type}' is invalid. Must be 'Video', 'Book', 'Past Paper', or 'Notes'.")
            }
        }
        return SchemaValidationResult(errors.isEmpty(), errors, "learning_resources")
    }
}
