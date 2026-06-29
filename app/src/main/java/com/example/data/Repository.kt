package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(private val appDao: AppDao) {

    val userProfile: Flow<UserProfile?> = appDao.getUserProfile()
    val userStats: Flow<UserStats?> = appDao.getUserStats()
    val bursaryOpportunities: Flow<List<BursaryOpportunity>> = appDao.getBursaryOpportunities()
    val bursaryApplications: Flow<List<BursaryApplication>> = appDao.getBursaryApplications()
    val learningMaterials: Flow<List<LearningMaterial>> = appDao.getLearningMaterials()
    val careerOpportunities: Flow<List<CareerOpportunity>> = appDao.getCareerOpportunities()
    val careerApplications: Flow<List<CareerApplication>> = appDao.getCareerApplications()

    fun getStudyCircleMessages(circleName: String): Flow<List<StudyCircleMessage>> =
        appDao.getStudyCircleMessages(circleName)

    suspend fun saveUserProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        appDao.insertUserProfile(profile)
    }

    suspend fun saveUserStats(stats: UserStats) = withContext(Dispatchers.IO) {
        appDao.insertUserStats(stats)
    }

    suspend fun applyForBursary(application: BursaryApplication) = withContext(Dispatchers.IO) {
        appDao.insertBursaryApplication(application)
    }

    suspend fun updateBursaryStatus(applicationId: Int, status: String) = withContext(Dispatchers.IO) {
        appDao.updateBursaryApplicationStatus(applicationId, status)
    }

    suspend fun applyForCareer(application: CareerApplication) = withContext(Dispatchers.IO) {
        appDao.insertCareerApplication(application)
    }

    suspend fun saveStudyCircleMessage(message: StudyCircleMessage) = withContext(Dispatchers.IO) {
        appDao.insertStudyCircleMessage(message)
    }

    fun getGroupMeetings(circleName: String): Flow<List<GroupMeeting>> =
        appDao.getGroupMeetings(circleName)

    suspend fun insertGroupMeeting(meeting: GroupMeeting) = withContext(Dispatchers.IO) {
        appDao.insertGroupMeeting(meeting)
    }

    suspend fun setLearningMaterialDownloaded(id: Int, isDownloaded: Boolean) = withContext(Dispatchers.IO) {
        appDao.updateLearningMaterialDownloadStatus(id, isDownloaded)
    }

    suspend fun insertLearningMaterial(material: LearningMaterial) = withContext(Dispatchers.IO) {
        appDao.insertLearningMaterial(material)
    }

    suspend fun insertBursaryOpportunity(bursary: BursaryOpportunity) = withContext(Dispatchers.IO) {
        appDao.insertBursaryOpportunity(bursary)
    }

    suspend fun insertCareerOpportunities(careers: List<CareerOpportunity>) = withContext(Dispatchers.IO) {
        appDao.insertCareerOpportunities(careers)
    }

    suspend fun insertCareerOpportunity(career: CareerOpportunity) = withContext(Dispatchers.IO) {
        appDao.insertCareerOpportunity(career)
    }

    suspend fun checkAndPrepopulateSeedData() = withContext(Dispatchers.IO) {
        // Prepopulate profile if missing
        val existingProfile = userProfile.firstOrNull()
        if (existingProfile == null) {
            appDao.insertUserProfile(UserProfile())
        }

        // Prepopulate stats if missing
        val existingStats = userStats.firstOrNull()
        if (existingStats == null) {
            appDao.insertUserStats(UserStats())
        }

        // Prepopulate bursaries if empty
        val existingBursaries = bursaryOpportunities.firstOrNull()
        if (existingBursaries.isNullOrEmpty()) {
            val seedBursaries = listOf(
                BursaryOpportunity(
                    title = "HELB Undergraduate Loan & Scholarship",
                    provider = "Higher Education Loans Board (HELB)",
                    category = "Government",
                    amount = "KES 40,000 - 60,000",
                    deadline = "2026-08-31",
                    description = "Comprehensive loan and scholarship scheme for undergraduate students in Kenyan public and private universities.",
                    eligibility = "Undergraduate student in a recognized university. Verified financial need.",
                    level = "University"
                ),
                BursaryOpportunity(
                    title = "NG-CDF Constituency Bursary (Imenti South)",
                    provider = "National Government Constituency Development Fund (NG-CDF)",
                    category = "Government",
                    amount = "KES 15,000",
                    deadline = "2026-07-20",
                    description = "Local constituency bursaries designed to assist needy secondary, TVET, and college students residing in Imenti South.",
                    eligibility = "Resident of Imenti South. Verified Chief's recommendation letter. Active admission.",
                    county = "Meru County",
                    level = "All"
                ),
                BursaryOpportunity(
                    title = "Wings to Fly Scholarship",
                    provider = "Equity Group Foundation",
                    category = "Corporate",
                    amount = "Fully Funded",
                    deadline = "2026-11-30",
                    description = "Prestigious high-school and university funding covering full tuition, shopping, mentoring, and leadership development.",
                    eligibility = "Top performer in KCPE (400+ marks) or KCSE. Economically disadvantaged background.",
                    level = "High School"
                ),
                BursaryOpportunity(
                    title = "Safaricom Foundation TVET Scholarship",
                    provider = "Safaricom Foundation",
                    category = "Corporate",
                    amount = "KES 50,000",
                    deadline = "2026-09-15",
                    description = "Funding support specifically designated for youth enrolling in Technical and Vocational Education and Training (TVET) institutions across Kenya.",
                    eligibility = "Enrolled in an accredited TVET center doing engineering, ICT, or plumbing courses.",
                    level = "TVET"
                ),
                BursaryOpportunity(
                    title = "Mastercard Foundation Scholars Program",
                    provider = "Mastercard Foundation",
                    category = "Corporate",
                    amount = "Fully Funded",
                    deadline = "2026-08-15",
                    description = "Full tuition, living stipend, medical, and travel cover for academically talented yet economically marginalized African youth to study at partner universities globally.",
                    eligibility = "Academic excellence, proven leadership potential, commitment to giving back to Africa.",
                    level = "University"
                ),
                BursaryOpportunity(
                    title = "Chevening Scholarships",
                    provider = "UK Government (FCDO)",
                    category = "International",
                    amount = "Fully Funded",
                    deadline = "2026-11-05",
                    description = "UK government's global scholarship programme offering full funding for master's degrees at any UK university.",
                    eligibility = "Undergraduate degree (2:1 equivalent). Minimum 2 years work or volunteering experience.",
                    level = "University"
                ),
                BursaryOpportunity(
                    title = "DAAD Scholarship",
                    provider = "German Academic Exchange Service (DAAD)",
                    category = "International",
                    amount = "Fully Funded + Stipend",
                    deadline = "2026-10-15",
                    description = "Scholarships for postgraduate degrees in Germany, emphasizing development-related sciences and engineering.",
                    eligibility = "Bachelor's degree completed within last 6 years. Relevant professional experience.",
                    level = "University"
                )
            )
            appDao.insertBursaryOpportunities(seedBursaries)
        }

        // Prepopulate learning materials if empty
        val existingMaterials = learningMaterials.firstOrNull()
        if (existingMaterials.isNullOrEmpty()) {
            val seedMaterials = listOf(
                LearningMaterial(
                    title = "KCSE Mathematics Past Papers & Solutions",
                    level = "High School",
                    type = "Past Paper",
                    size = "4.2 MB",
                    course = "Mathematics",
                    university = "Kenya National Examinations Council"
                ),
                LearningMaterial(
                    title = "Form 4 Physics Quick Revision Notes",
                    level = "High School",
                    type = "Notes",
                    size = "2.8 MB",
                    course = "Physics",
                    university = "Alliance High School"
                ),
                LearningMaterial(
                    title = "Introduction to Electrical Installation & Wiring",
                    level = "TVET",
                    type = "Book",
                    size = "12.4 MB",
                    course = "Electrical Engineering",
                    university = "Nairobi Technical Training Institute"
                ),
                LearningMaterial(
                    title = "Mobile App Dev with Jetpack Compose (Video Guide)",
                    level = "TVET",
                    type = "Video",
                    size = "45.0 MB",
                    course = "Computer Science",
                    university = "JKUAT"
                ),
                LearningMaterial(
                    title = "Data Structures & Algorithms Course Syllabus",
                    level = "University",
                    type = "Notes",
                    size = "1.5 MB",
                    course = "Computer Science",
                    university = "JKUAT"
                ),
                LearningMaterial(
                    title = "Object-Oriented Programming Lecture Slides",
                    level = "University",
                    type = "Notes",
                    size = "3.1 MB",
                    course = "Information Technology",
                    university = "Strathmore University"
                ),
                LearningMaterial(
                    title = "Machine Learning Engineering Handbook",
                    level = "Professional",
                    type = "Book",
                    size = "18.2 MB",
                    course = "Data Science",
                    university = "Google Cloud Certification"
                ),
                LearningMaterial(
                    title = "Google Professional Cloud Architect Revision Guide",
                    level = "Professional",
                    type = "Past Paper",
                    size = "5.6 MB",
                    course = "Cloud Computing",
                    university = "Google Cloud Academy"
                )
            )
            appDao.insertLearningMaterials(seedMaterials)
        }

        // Prepopulate careers if empty
        val existingCareers = careerOpportunities.firstOrNull()
        if (existingCareers.isNullOrEmpty()) {
            val seedCareers = listOf(
                CareerOpportunity(
                    title = "Graduate Software Engineer (Android/Kotlin)",
                    company = "Safaricom PLC",
                    location = "Nairobi, Kenya (Hybrid)",
                    category = "Local Internships",
                    matchScore = 96,
                    description = "Exciting entry-level graduate software engineer program at Safaricom focusing on developing modern native Android products using Jetpack Compose and Room.",
                    requirement = "B.Sc. Computer Science or related. Knowledge of Kotlin, coroutines, and Android SDK. Strong teamwork.",
                    deadline = "2026-07-30"
                ),
                CareerOpportunity(
                    title = "Graduate Relationship Banker Trainee",
                    company = "KCB Group",
                    location = "Mombasa, Kenya",
                    category = "Local Internships",
                    matchScore = 78,
                    description = "Structured training program covering core banking, retail customer relations, risk analysis, and microfinance management.",
                    requirement = "Bachelor's degree in Business, Economics, Finance, or any STEM discipline. Excellent communication skills.",
                    deadline = "2026-08-10"
                ),
                CareerOpportunity(
                    title = "Remote Android UI Designer & Developer",
                    company = "Turing Global Technologies",
                    location = "Remote (USA/Africa)",
                    category = "Remote Jobs",
                    matchScore = 92,
                    description = "Collaborate with international tech teams to prototype, polish, and code highly responsive and fluid Jetpack Compose layouts for modern consumer apps.",
                    requirement = "Portfolio showing high-quality Compose work. Understanding of Material Design 3 and window sizes. Highly disciplined.",
                    deadline = "2026-08-15"
                ),
                CareerOpportunity(
                    title = "Research Assistant - AI & EdTech",
                    company = "Mastercard Foundation",
                    location = "Kigali, Rwanda",
                    category = "International",
                    matchScore = 85,
                    description = "Assist in researching and measuring the impact of offline-first AI-tutor models on learning achievements for secondary and TVET students in Sub-Saharan Africa.",
                    requirement = "B.Sc. in Data Science, EdTech, or Statistics. Proficiency with Python, data modeling, and qualitative survey analysis.",
                    deadline = "2026-09-01"
                )
            )
            appDao.insertCareerOpportunities(seedCareers)
        }

        // Insert initial greetings/instructions in Study Circles so they are immediately interactive
        val defaultCircles = listOf("Computer Science", "Engineering", "Business", "TVET", "KCSE")
        for (circle in defaultCircles) {
            val existingMsgs = appDao.getStudyCircleMessages(circle).firstOrNull()
            if (existingMsgs.isNullOrEmpty()) {
                appDao.insertStudyCircleMessage(
                    StudyCircleMessage(
                        circleName = circle,
                        senderName = "Mwalimu AI Chatbot",
                        senderRole = "AI Mentor",
                        message = "Welcome to the $circle Study Circle! Here, you can share revision resources, discuss course challenges, and study collaboratively with your peers. Click 'Join Live Board' above to launch or join a virtual group study session!",
                        isAiGenerated = true
                    )
                )
                appDao.insertStudyCircleMessage(
                    StudyCircleMessage(
                        circleName = circle,
                        senderName = "Moses Kirimi",
                        senderRole = "Student Advocate",
                        message = "Hey everyone! Glad to join this group. Looking forward to preparing for exams together and sharing some helpful notes."
                    )
                )
            }
        }
    }
}
