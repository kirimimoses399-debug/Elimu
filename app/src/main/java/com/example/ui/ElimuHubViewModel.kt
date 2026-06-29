package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.network.GeminiApiClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ElimuHubViewModel(application: Application, private val repository: AppRepository) : AndroidViewModel(application) {

    // Current screen navigation state
    private val _currentTab = MutableStateFlow(ScreenTab.HOME)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

    // Screen-specific state
    val userProfile: StateFlow<UserProfile?> = repository.userProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val userStats: StateFlow<UserStats?> = repository.userStats.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val bursaryOpportunities: StateFlow<List<BursaryOpportunity>> = repository.bursaryOpportunities.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val bursaryApplications: StateFlow<List<BursaryApplication>> = repository.bursaryApplications.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val learningMaterials: StateFlow<List<LearningMaterial>> = repository.learningMaterials.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val careerOpportunities: StateFlow<List<CareerOpportunity>> = repository.careerOpportunities.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val careerApplications: StateFlow<List<CareerApplication>> = repository.careerApplications.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Selected study circle
    private val _selectedCircle = MutableStateFlow("Computer Science")
    val selectedCircle: StateFlow<String> = _selectedCircle.asStateFlow()

    val studyCircleMessages: StateFlow<List<StudyCircleMessage>> = _selectedCircle
        .flatMapLatest { circle -> repository.getStudyCircleMessages(circle) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val groupMeetings: StateFlow<List<GroupMeeting>> = _selectedCircle
        .flatMapLatest { circle -> repository.getGroupMeetings(circle) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Private Mwalimu AI chat conversation
    private val _mwalimuChatMessages = MutableStateFlow<List<StudyCircleMessage>>(
        listOf(
            StudyCircleMessage(
                circleName = "Mwalimu_AI_Private",
                senderName = "Mwalimu AI Chatbot",
                senderRole = "AI Tutor",
                message = "Jambo! I am Mwalimu AI, your 24/7 learning companion. Select an academic category below (like Homework Help, Essay Review, or Math Solver) or simply ask me any question about your studies!",
                isAiGenerated = true
            )
        )
    )
    val mwalimuChatMessages: StateFlow<List<StudyCircleMessage>> = _mwalimuChatMessages.asStateFlow()

    private val _isMwalimuLoading = MutableStateFlow(false)
    val isMwalimuLoading: StateFlow<Boolean> = _isMwalimuLoading.asStateFlow()

    private val _activeMwalimuTopic = MutableStateFlow("General Tutoring")
    val activeMwalimuTopic: StateFlow<String> = _activeMwalimuTopic.asStateFlow()

    // Sub-category filters for UI
    val selectedFundingCategory = MutableStateFlow("Government")
    val fundingSearchQuery = MutableStateFlow("")
    val fundingCountyFilter = MutableStateFlow("All")

    val selectedLearningLevel = MutableStateFlow("High School")
    val learningSearchQuery = MutableStateFlow("")
    val isOfflineMode = MutableStateFlow(false)
    val selectedCareerCategory = MutableStateFlow("Local Internships")

    // Active subviews within tabs (e.g. detailed screens or specialized dashboards)
    private val _activeDetailView = MutableStateFlow<DetailView?>(null)
    val activeDetailView: StateFlow<DetailView?> = _activeDetailView.asStateFlow()

    init {
        // Kick off database pre-population
        viewModelScope.launch {
            repository.checkAndPrepopulateSeedData()
        }
    }

    // Tab Navigation
    fun setTab(tab: ScreenTab) {
        _currentTab.value = tab
        _activeDetailView.value = null // Reset details when switching tabs
    }

    fun setDetailView(detail: DetailView?) {
        _activeDetailView.value = detail
    }

    fun selectCircle(circleName: String) {
        _selectedCircle.value = circleName
    }

    fun setMwalimuTopic(topic: String) {
        _activeMwalimuTopic.value = topic
        val msg = when (topic) {
            "Math Solver" -> "I am ready to help you solve your math problems! Paste your equation or describe the problem, and we will work through it step-by-step."
            "Essay Review" -> "Paste your essay draft here. I will critique your structure, grammar, and arguments, and suggest ways to refine it."
            "Homework Help" -> "Which subject or assignment are we working on today? Let's break it down together."
            "Programming Tutor" -> "Let's code! Post your Kotlin, Java, Python, or Web code or describe what you want to implement."
            else -> "How can I support your studies today?"
        }
        _mwalimuChatMessages.value = _mwalimuChatMessages.value + StudyCircleMessage(
            circleName = "Mwalimu_AI_Private",
            senderName = "Mwalimu AI Chatbot",
            senderRole = "AI Tutor",
            message = msg,
            isAiGenerated = true
        )
    }

    // Profile Actions
    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveUserProfile(profile)
        }
    }

    fun loginWithGoogleEmail(googleEmail: String, name: String = "") {
        viewModelScope.launch {
            val currentProfile = userProfile.value ?: UserProfile()
            val finalName = if (name.isNotBlank()) name else {
                if (googleEmail.contains("kirimi", ignoreCase = true)) "Moses Kirimi" else googleEmail.substringBefore("@").replace(".", " ").capitalize()
            }
            val updated = currentProfile.copy(
                fullName = finalName,
                email = googleEmail,
                isLoggedIn = true
            )
            repository.saveUserProfile(updated)
        }
    }

    fun logout() {
        viewModelScope.launch {
            val currentProfile = userProfile.value ?: UserProfile()
            val cleared = currentProfile.copy(
                isLoggedIn = false
            )
            repository.saveUserProfile(cleared)
        }
    }

    // Apply for Bursary (Smart One-Click Native)
    fun applyForBursary(opportunity: BursaryOpportunity) {
        viewModelScope.launch {
            val app = BursaryApplication(
                opportunityId = opportunity.id,
                title = opportunity.title,
                provider = opportunity.provider,
                appliedDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                status = "Submitted"
            )
            repository.applyForBursary(app)
            incrementXp(20) // Gain XP on applying!
        }
    }

    // Apply for Job / Internship
    fun applyForCareer(opportunity: CareerOpportunity) {
        viewModelScope.launch {
            val app = CareerApplication(
                opportunityId = opportunity.id,
                title = opportunity.title,
                company = opportunity.company,
                appliedDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                status = "Applied"
            )
            repository.applyForCareer(app)
            incrementXp(30)
        }
    }

    // Send Message in Study Circle (With Simulated AI Response optionally if addressed)
    fun sendStudyCircleMessage(circleName: String, messageText: String) {
        if (messageText.isBlank()) return
        viewModelScope.launch {
            val profile = userProfile.value ?: UserProfile()
            val userMsg = StudyCircleMessage(
                circleName = circleName,
                senderName = profile.fullName,
                senderRole = "Student",
                message = messageText,
                isAiGenerated = false
            )
            repository.saveStudyCircleMessage(userMsg)

            // Trigger Mwalimu AI response in the circle if tagged with "@mwalimu" or "@ai"
            if (messageText.contains("@mwalimu", ignoreCase = true) || messageText.contains("@ai", ignoreCase = true)) {
                val cleanPrompt = messageText
                    .replace("@mwalimu", "", ignoreCase = true)
                    .replace("@ai", "", ignoreCase = true)
                    .trim()

                val response = GeminiApiClient.askMwalimuAi(cleanPrompt, emptyList())
                val aiMsg = StudyCircleMessage(
                    circleName = circleName,
                    senderName = "Mwalimu AI Chatbot",
                    senderRole = "AI Mentor",
                    message = response,
                    isAiGenerated = true
                )
                repository.saveStudyCircleMessage(aiMsg)
            }
        }
    }

    // Send private tutoring message to Mwalimu AI
    fun askMwalimuAi(promptText: String) {
        if (promptText.isBlank()) return
        viewModelScope.launch {
            val profile = userProfile.value ?: UserProfile()
            val userMsg = StudyCircleMessage(
                circleName = "Mwalimu_AI_Private",
                senderName = profile.fullName,
                senderRole = "Student",
                message = promptText,
                isAiGenerated = false
            )
            _mwalimuChatMessages.value = _mwalimuChatMessages.value + userMsg
            _isMwalimuLoading.value = true

            // Send actual REST API request to Gemini with history
            val aiResponseText = GeminiApiClient.askMwalimuAi(promptText, _mwalimuChatMessages.value)

            val aiMsg = StudyCircleMessage(
                circleName = "Mwalimu_AI_Private",
                senderName = "Mwalimu AI Chatbot",
                senderRole = "AI Tutor",
                message = aiResponseText,
                isAiGenerated = true
            )
            _mwalimuChatMessages.value = _mwalimuChatMessages.value + aiMsg
            _isMwalimuLoading.value = false
            incrementXp(10)
        }
    }

    // Trigger LMS File Download Toggle
    fun toggleDownloadMaterial(material: LearningMaterial) {
        viewModelScope.launch {
            repository.setLearningMaterialDownloaded(material.id, !material.isDownloaded)
            if (!material.isDownloaded) {
                incrementXp(5)
            }
        }
    }

    // Simulate Quiz Answer / Streak
    fun submitQuizScore(correctCount: Int, total: Int) {
        viewModelScope.launch {
            val current = userStats.value ?: UserStats()
            val newXp = current.xp + (correctCount * 10)
            val updatedStats = current.copy(
                xp = newXp,
                completedQuizzesCount = current.completedQuizzesCount + 1,
                quizStreak = current.quizStreak + 1,
                dailyGoalAnswered = (current.dailyGoalAnswered + total).coerceAtMost(20)
            )
            repository.saveUserStats(updatedStats)
        }
    }

    // Employer Dashboard Action: Add Internship
    fun addCustomCareer(
        title: String, company: String, location: String, category: String,
        description: String, requirement: String, deadline: String
    ) {
        viewModelScope.launch {
            val career = CareerOpportunity(
                title = title,
                company = company,
                location = location,
                category = category,
                matchScore = (70..99).random(),
                description = description,
                requirement = requirement,
                deadline = deadline
            )
            repository.insertCareerOpportunity(career)

            // Live upload to Firebase Database if configured
            if (com.example.network.FirebaseApiClient.getDbUrl().isNotEmpty()) {
                val success = com.example.network.FirebaseApiClient.uploadCareer(career)
                if (success) {
                    com.example.network.FirebaseApiClient.setStatus(
                        com.example.network.FirebaseSyncStatus.Synced(com.example.network.FirebaseApiClient.getDbUrl())
                    )
                } else {
                    com.example.network.FirebaseApiClient.setStatus(
                        com.example.network.FirebaseSyncStatus.Error(com.example.network.FirebaseApiClient.getDbUrl(), "Last career failed to upload.")
                    )
                }
            }
        }
    }

    // County Dashboard Action: Add Bursary
    fun addCustomBursary(
        title: String, provider: String, category: String, amount: String,
        deadline: String, description: String, eligibility: String,
        county: String, level: String
    ) {
        viewModelScope.launch {
            val opp = BursaryOpportunity(
                title = title,
                provider = provider,
                category = category,
                amount = amount,
                deadline = deadline,
                description = description,
                eligibility = eligibility,
                county = county,
                level = level
            )
            repository.insertBursaryOpportunity(opp)

            // Live upload to Firebase Database if configured
            if (com.example.network.FirebaseApiClient.getDbUrl().isNotEmpty()) {
                val success = com.example.network.FirebaseApiClient.uploadBursary(opp)
                if (success) {
                    com.example.network.FirebaseApiClient.setStatus(
                        com.example.network.FirebaseSyncStatus.Synced(com.example.network.FirebaseApiClient.getDbUrl())
                    )
                } else {
                    com.example.network.FirebaseApiClient.setStatus(
                        com.example.network.FirebaseSyncStatus.Error(com.example.network.FirebaseApiClient.getDbUrl(), "Last bursary failed to upload.")
                    )
                }
            }
        }
    }

    // Admin Action: Add Custom Learning Material
    fun addCustomLearningMaterial(
        title: String, level: String, type: String, url: String, size: String,
        course: String = "", university: String = ""
    ) {
        viewModelScope.launch {
            val mat = LearningMaterial(
                title = title,
                level = level,
                type = type,
                url = url,
                size = size,
                isDownloaded = false,
                course = course,
                university = university
            )
            repository.insertLearningMaterial(mat)

            // Live upload to Firebase Database if configured
            if (com.example.network.FirebaseApiClient.getDbUrl().isNotEmpty()) {
                val success = com.example.network.FirebaseApiClient.uploadLearningMaterial(mat)
                if (success) {
                    com.example.network.FirebaseApiClient.setStatus(
                        com.example.network.FirebaseSyncStatus.Synced(com.example.network.FirebaseApiClient.getDbUrl())
                    )
                } else {
                    com.example.network.FirebaseApiClient.setStatus(
                        com.example.network.FirebaseSyncStatus.Error(com.example.network.FirebaseApiClient.getDbUrl(), "Last resource failed to upload.")
                    )
                }
            }
        }
    }

    // Expose Firebase Sync Status to UI
    val firebaseSyncStatus = com.example.network.FirebaseApiClient.syncStatus

    fun configureFirebase(url: String, token: String) {
        com.example.network.FirebaseApiClient.configure(getApplication(), url, token)
    }

    fun testFirebaseConnection(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = com.example.network.FirebaseApiClient.testConnection()
            onResult(result.first, result.second)
        }
    }

    // AI Opportunity Scouting properties and methods
    private val _pendingAiOpportunities = MutableStateFlow<List<PendingAiOpportunity>>(emptyList())
    val pendingAiOpportunities: StateFlow<List<PendingAiOpportunity>> = _pendingAiOpportunities.asStateFlow()

    private val _isAiScouting = MutableStateFlow(false)
    val isAiScouting: StateFlow<Boolean> = _isAiScouting.asStateFlow()

    fun triggerAiScout(query: String) {
        viewModelScope.launch {
            _isAiScouting.value = true
            try {
                val rawJson = GeminiApiClient.scoutOpportunities(query)
                val jsonArray = org.json.JSONArray(rawJson)
                val newList = mutableListOf<PendingAiOpportunity>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val type = obj.optString("type", "Bursary")
                    val title = obj.optString("title", "")
                    val providerOrCompany = obj.optString("providerOrCompany", "")
                    val category = obj.optString("category", "")
                    val description = obj.optString("description", "")
                    val eligibilityOrRequirement = obj.optString("eligibilityOrRequirement", "")
                    val amountOrLocation = obj.optString("amountOrLocation", "")
                    val deadlineOrType = obj.optString("deadlineOrType", "")
                    val countyOrLevel = obj.optString("countyOrLevel", "All")
                    val urlOrSize = obj.optString("urlOrSize", "")
                    val extra1 = obj.optString("extra1", "")
                    val extra2 = obj.optString("extra2", "")

                    if (title.isNotEmpty()) {
                        newList.add(
                            PendingAiOpportunity(
                                type = type,
                                title = title,
                                providerOrCompany = providerOrCompany,
                                category = category,
                                description = description,
                                eligibilityOrRequirement = eligibilityOrRequirement,
                                amountOrLocation = amountOrLocation,
                                deadlineOrType = deadlineOrType,
                                countyOrLevel = countyOrLevel,
                                urlOrSize = urlOrSize,
                                extra1 = extra1,
                                extra2 = extra2
                            )
                        )
                    }
                }
                _pendingAiOpportunities.value = newList
            } catch (e: Exception) {
                // Ignore or log
            } finally {
                _isAiScouting.value = false
            }
        }
    }

    fun approveAiOpportunity(opp: PendingAiOpportunity) {
        viewModelScope.launch {
            when (opp.type) {
                "Bursary" -> {
                    addCustomBursary(
                        title = opp.title,
                        provider = opp.providerOrCompany,
                        category = opp.category.ifBlank { "Government" },
                        amount = opp.amountOrLocation.ifBlank { "Unspecified" },
                        deadline = opp.deadlineOrType.ifBlank { "2026-12-31" },
                        description = opp.description,
                        eligibility = opp.eligibilityOrRequirement,
                        county = opp.countyOrLevel.ifBlank { "All" },
                        level = opp.extra2.ifBlank { "All" }
                    )
                }
                "Career" -> {
                    addCustomCareer(
                        title = opp.title,
                        company = opp.providerOrCompany,
                        location = opp.amountOrLocation.ifBlank { "Nairobi" },
                        category = opp.category.ifBlank { "Local Internships" },
                        description = opp.description,
                        requirement = opp.eligibilityOrRequirement,
                        deadline = opp.deadlineOrType.ifBlank { "2026-12-31" }
                    )
                }
                "Material" -> {
                    addCustomLearningMaterial(
                        title = opp.title,
                        level = opp.countyOrLevel.ifBlank { "University" },
                        type = opp.deadlineOrType.ifBlank { "Notes" },
                        url = opp.urlOrSize.ifBlank { "ai_scouted_resource.pdf" },
                        size = opp.extra1.ifBlank { "2.0 MB" },
                        course = opp.category,
                        university = opp.providerOrCompany
                    )
                }
            }
            // Remove from pending list
            _pendingAiOpportunities.value = _pendingAiOpportunities.value.filter { it.id != opp.id }
        }
    }

    fun rejectAiOpportunity(opp: PendingAiOpportunity) {
        _pendingAiOpportunities.value = _pendingAiOpportunities.value.filter { it.id != opp.id }
    }

    fun updatePendingOpportunity(updated: PendingAiOpportunity) {
        _pendingAiOpportunities.value = _pendingAiOpportunities.value.map {
            if (it.id == updated.id) updated else it
        }
    }

    // Study Circle Action: Add Google Meet Session
    fun addGroupMeeting(circleName: String, title: String, dateTime: String, meetUrl: String) {
        viewModelScope.launch {
            val meeting = GroupMeeting(
                circleName = circleName,
                title = title,
                dateTime = dateTime,
                meetUrl = meetUrl
            )
            repository.insertGroupMeeting(meeting)
        }
    }

    // Help Helper to increase XP
    private fun incrementXp(amount: Int) {
        viewModelScope.launch {
            val current = userStats.value ?: UserStats()
            repository.saveUserStats(current.copy(xp = current.xp + amount))
        }
    }

    // Firestore Schema Manager States
    val firestoreInitStatus: StateFlow<com.example.network.FirestoreInitStatus> = com.example.network.FirestoreSchemaManager.initStatus
    val firestoreInitLogs: StateFlow<List<String>> = com.example.network.FirestoreSchemaManager.initLogs

    private val _isEcosystemAuthorized = MutableStateFlow(false)
    val isEcosystemAuthorized: StateFlow<Boolean> = _isEcosystemAuthorized.asStateFlow()

    fun setEcosystemHubAuthorized(authorized: Boolean) {
        _isEcosystemAuthorized.value = authorized
    }

    fun triggerFirestoreSchemaInitialization() {
        viewModelScope.launch {
            com.example.network.FirestoreSchemaManager.runSchemaInitialization(
                localBursaries = bursaryOpportunities.value,
                localCareers = careerOpportunities.value,
                localMaterials = learningMaterials.value
            )
        }
    }

    fun clearFirestoreLogs() {
        com.example.network.FirestoreSchemaManager.clearLogs()
    }
}

enum class ScreenTab {
    HOME, FUNDING, LEARN, CAREERS, PROFILE
}

sealed class DetailView {
    data class BursaryDetails(val opportunity: BursaryOpportunity) : DetailView()
    data class JobDetails(val opportunity: CareerOpportunity) : DetailView()
    data object ResumeBuilderView : DetailView()
    data object QuizView : DetailView()
    data object CountyDashboardView : DetailView()
    data object EmployerDashboardView : DetailView()
    data object InstitutionDashboardView : DetailView()
    data object AdminPortalView : DetailView()
}

class ElimuHubViewModelFactory(
    private val application: Application,
    private val repository: AppRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ElimuHubViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ElimuHubViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
