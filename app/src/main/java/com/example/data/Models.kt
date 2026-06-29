package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val fullName: String = "Moses Kirimi",
    val email: String = "kirimimoses399@gmail.com",
    val nationalId: String = "38291042",
    val birthCertificateNo: String = "BC-9823019",
    val kcpeResult: String = "402 Marks",
    val kcseResult: String = "Grade A-",
    val university: String = "Jomo Kenyatta University of Agriculture and Technology (JKUAT)",
    val course: String = "B.Sc. Computer Science",
    val yearOfStudy: String = "Year 3, Semester 2",
    val parentName: String = "Jane Kirimi",
    val parentPhone: String = "+254 712 345 678",
    val county: String = "Meru County",
    val constituency: String = "Imenti South",
    val ward: String = "Abogeta West",
    val disabilityStatus: String = "None",
    val incomeCategory: String = "Below KES 50,000",
    val gpsLocation: String = "-0.0471, 37.6437",
    val isIdentityVerified: Boolean = true,
    val isAcademicVerified: Boolean = true,
    val isResidenceVerified: Boolean = true,
    val feeStructurePath: String = "fee_structure_jkuat_2026.pdf",
    val transcriptPath: String = "transcript_y3s1.pdf",
    val recommendationLetterPath: String = "chief_recommendation_imentisouth.pdf",
    val passportPhotoPath: String = "profile_photo.jpg",
    val isLoggedIn: Boolean = false
)

@Entity(tableName = "bursary_opportunities")
data class BursaryOpportunity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val provider: String,
    val category: String, // "Government", "Corporate", "International"
    val amount: String,
    val deadline: String,
    val description: String,
    val eligibility: String,
    val county: String = "All",
    val course: String = "All",
    val level: String = "All"
)

@Entity(tableName = "bursary_applications")
data class BursaryApplication(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val opportunityId: Int,
    val title: String,
    val provider: String,
    val appliedDate: String,
    val status: String // "Submitted", "Documents Verified", "Interview Phase", "Approved", "Disbursed"
)

@Entity(tableName = "learning_materials")
data class LearningMaterial(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val level: String, // "High School", "TVET", "University", "Professional"
    val type: String, // "Video", "Book", "Past Paper", "Notes"
    val url: String = "",
    val size: String = "0 MB",
    val isDownloaded: Boolean = false,
    val course: String = "",
    val university: String = ""
)

@Entity(tableName = "group_meetings")
data class GroupMeeting(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val circleName: String,
    val title: String,
    val dateTime: String,
    val meetUrl: String,
    val hostName: String = "Moses Kirimi"
)

@Entity(tableName = "study_circle_messages")
data class StudyCircleMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val circleName: String,
    val senderName: String,
    val senderRole: String = "Student",
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isAiGenerated: Boolean = false
)

@Entity(tableName = "career_opportunities")
data class CareerOpportunity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val company: String,
    val location: String,
    val category: String, // "Local Internships", "International", "Remote Jobs"
    val matchScore: Int,
    val description: String,
    val requirement: String,
    val deadline: String
)

@Entity(tableName = "career_applications")
data class CareerApplication(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val opportunityId: Int,
    val title: String,
    val company: String,
    val appliedDate: String,
    val status: String = "Applied" // "Applied", "Reviewed", "Interview Scheduled", "Offered"
)

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1,
    val xp: Int = 320,
    val quizStreak: Int = 7,
    val completedQuizzesCount: Int = 12,
    val badgesJson: String = "[\"Streak Master\", \"AI Scholar\", \"Fast Learner\"]",
    val dailyGoalAnswered: Int = 8
)

data class PendingAiOpportunity(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: String, // "Bursary", "Career", "Material"
    val title: String,
    val providerOrCompany: String,
    val category: String, // e.g. "Government", "Local Internships", "Notes"
    val description: String,
    val eligibilityOrRequirement: String = "",
    val amountOrLocation: String = "",
    val deadlineOrType: String = "",
    val countyOrLevel: String = "All",
    val urlOrSize: String = "",
    val extra1: String = "",
    val extra2: String = ""
)
