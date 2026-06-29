package com.example.network

import com.example.data.BursaryOpportunity
import com.example.data.CareerOpportunity
import com.example.data.LearningMaterial
import java.util.UUID

/**
 * Data-driven models representing the Firestore Document Schemas
 * for ElimuHub Admin Portal.
 */

data class FirestoreBursaryDoc(
    val id: String,
    val title: String,
    val provider: String,
    val category: String, // Government, Corporate, International
    val amount: String,
    val deadline: String,
    val description: String,
    val eligibility: String,
    val county: String,
    val course: String,
    val level: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isVerified: Boolean = true
)

data class FirestoreInternshipDoc(
    val id: String,
    val title: String,
    val company: String,
    val location: String,
    val category: String, // Local Internships, Remote Jobs, International
    val matchScore: Int,
    val description: String,
    val requirement: String,
    val deadline: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class FirestoreLearningResourceDoc(
    val id: String,
    val title: String,
    val level: String, // High School, TVET, University, Professional
    val type: String, // Video, Book, Past Paper, Notes
    val url: String,
    val size: String,
    val course: String,
    val university: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class FirestoreUserApprovalDoc(
    val id: String,
    val opportunityId: String,
    val opportunityType: String, // Bursary, Career, Material
    val opportunityTitle: String,
    val approvedBy: String, // Admin name/email
    val approvedAt: Long = System.currentTimeMillis(),
    val status: String = "Approved",
    val notes: String = ""
)

/**
 * Metadata definition for collections
 */
data class FirestoreFieldMeta(
    val name: String,
    val type: String,
    val isRequired: Boolean,
    val description: String
)

data class FirestoreCollectionMeta(
    val collectionName: String,
    val description: String,
    val fields: List<FirestoreFieldMeta>
)

object ElimuHubFirestoreSchema {

    val collections = listOf(
        FirestoreCollectionMeta(
            collectionName = "bursaries",
            description = "Active educational funding, sponsorships, and county bursaries inside Kenya.",
            fields = listOf(
                FirestoreFieldMeta("id", "String (UUID/DocId)", true, "Unique identifier for the bursary opportunity"),
                FirestoreFieldMeta("title", "String", true, "Title of bursary, e.g. CDF Sub-county Bursary"),
                FirestoreFieldMeta("provider", "String", true, "Issuer organization, e.g. CDF Meru or Equity Bank"),
                FirestoreFieldMeta("category", "String", true, "Classification: 'Government', 'Corporate', 'International'"),
                FirestoreFieldMeta("amount", "String", true, "Funding value, e.g. 'KES 50,000' or 'Full scholarship'"),
                FirestoreFieldMeta("deadline", "String (YYYY-MM-DD)", true, "Closing date of the application"),
                FirestoreFieldMeta("description", "String", true, "Detailed context, scope, and contact details"),
                FirestoreFieldMeta("eligibility", "String", true, "Academic or socioeconomic prerequisites"),
                FirestoreFieldMeta("county", "String", false, "Target geographic filter or 'All'"),
                FirestoreFieldMeta("course", "String", false, "Specific course restriction or 'All'"),
                FirestoreFieldMeta("level", "String", false, "Educational level, e.g. 'TVET', 'University', or 'All'"),
                FirestoreFieldMeta("createdAt", "Timestamp (Epoch)", true, "Creation timestamp"),
                FirestoreFieldMeta("isVerified", "Boolean", true, "Status set by the admin panel")
            )
        ),
        FirestoreCollectionMeta(
            collectionName = "internships",
            description = "Internship and attachment opportunities for students and recent TVET/University grads.",
            fields = listOf(
                FirestoreFieldMeta("id", "String (UUID/DocId)", true, "Unique identifier for the internship offer"),
                FirestoreFieldMeta("title", "String", true, "Job/Internship Title, e.g., Software Engineering Intern"),
                FirestoreFieldMeta("company", "String", true, "Hiring organization, e.g., Safaricom"),
                FirestoreFieldMeta("location", "String", true, "Job city, e.g., 'Nairobi' or 'Remote'"),
                FirestoreFieldMeta("category", "String", true, "Segment: 'Local Internships', 'Remote Jobs', 'International'"),
                FirestoreFieldMeta("matchScore", "Number (Integer)", true, "Student alignment rating (0 to 100)"),
                FirestoreFieldMeta("description", "String", true, "Scope of responsibilities and goals"),
                FirestoreFieldMeta("requirement", "String", true, "Prerequisite skills, degrees, or certifications"),
                FirestoreFieldMeta("deadline", "String (YYYY-MM-DD)", true, "Application cutoff date"),
                FirestoreFieldMeta("createdAt", "Timestamp (Epoch)", true, "Timestamp of entry")
            )
        ),
        FirestoreCollectionMeta(
            collectionName = "learning_resources",
            description = "E-learning resources, revision notes, text books, past papers, and TVET curriculum guides.",
            fields = listOf(
                FirestoreFieldMeta("id", "String (UUID/DocId)", true, "Unique resource identification key"),
                FirestoreFieldMeta("title", "String", true, "Resource title, e.g., KCSE Chemistry Paper 1 Notes"),
                FirestoreFieldMeta("level", "String", true, "Level: 'High School', 'TVET', 'University', 'Professional'"),
                FirestoreFieldMeta("type", "String", true, "Format: 'Video', 'Book', 'Past Paper', 'Notes'"),
                FirestoreFieldMeta("url", "String (URL)", true, "Remote hosting URL for resource"),
                FirestoreFieldMeta("size", "String", true, "Estimated size, e.g., '1.5 MB'"),
                FirestoreFieldMeta("course", "String", false, "Domain major, e.g. 'Mechanical Engineering'"),
                FirestoreFieldMeta("university", "String", false, "Issuing campus/polytechnic or 'All'"),
                FirestoreFieldMeta("createdAt", "Timestamp (Epoch)", true, "Timestamp of creation")
            )
        ),
        FirestoreCollectionMeta(
            collectionName = "user_approvals",
            description = "System logs detailing administrative approvals of AI-scouted or user-submitted opportunities.",
            fields = listOf(
                FirestoreFieldMeta("id", "String (UUID/DocId)", true, "Document ID representing the audit log"),
                FirestoreFieldMeta("opportunityId", "String", true, "Reference key of the approved opportunity"),
                FirestoreFieldMeta("opportunityType", "String", true, "Classification: 'Bursary', 'Career', 'Material'"),
                FirestoreFieldMeta("opportunityTitle", "String", true, "Name of the item for quick identification"),
                FirestoreFieldMeta("approvedBy", "String (Email)", true, "Admin's identifier, e.g. kirimimoses399@gmail.com"),
                FirestoreFieldMeta("approvedAt", "Timestamp (Epoch)", true, "Timestamp of administrative action"),
                FirestoreFieldMeta("status", "String", true, "Current state of approval (usually 'Approved')"),
                FirestoreFieldMeta("notes", "String", false, "Audit trail remarks or modification notes")
            )
        )
    )

    /**
     * Generates a fully customized security rules text for the ElimuHub Firestore collections.
     */
    fun getSecurityRules(): String {
        return """
            rules_version = '2';
            service cloud.firestore {
              match /databases/{database}/documents {
                
                // Helper functions to check authentication and admin claims
                function isAuthenticated() {
                  return request.auth != null;
                }
                
                function isAdmin() {
                  // Admin access restricted by email or custom claim token
                  return isAuthenticated() && (
                    request.auth.token.email == 'kirimimoses399@gmail.com' ||
                    request.auth.token.admin == true
                  );
                }

                // Collection: bursaries
                match /bursaries/{bursaryId} {
                  allow read: if isAuthenticated(); // All logged-in students can view
                  allow write: if isAdmin();       // Only Moses Kirimi & authorized admins can write
                }

                // Collection: internships
                match /internships/{internshipId} {
                  allow read: if isAuthenticated(); // Students scan jobs & internships
                  allow write: if isAdmin();       // Admins curate internship offerings
                }

                // Collection: learning_resources
                match /learning_resources/{resourceId} {
                  allow read: if isAuthenticated(); // Students download revision materials
                  allow write: if isAdmin();       // Admins upload and approve modules
                }

                // Collection: user_approvals
                match /user_approvals/{approvalId} {
                  allow read, write: if isAdmin(); // Private audit trail restricted solely to admins
                }
              }
            }
        """.trimIndent()
    }

    /**
     * Map a local BursaryOpportunity to FirestoreBursaryDoc
     */
    fun mapBursary(b: BursaryOpportunity): FirestoreBursaryDoc {
        return FirestoreBursaryDoc(
            id = if (b.id == 0) UUID.randomUUID().toString() else "bur_${b.id}",
            title = b.title,
            provider = b.provider,
            category = b.category,
            amount = b.amount,
            deadline = b.deadline,
            description = b.description,
            eligibility = b.eligibility,
            county = b.county,
            course = b.course,
            level = b.level
        )
    }

    /**
     * Map a local CareerOpportunity to FirestoreInternshipDoc
     */
    fun mapCareer(c: CareerOpportunity): FirestoreInternshipDoc {
        return FirestoreInternshipDoc(
            id = if (c.id == 0) UUID.randomUUID().toString() else "job_${c.id}",
            title = c.title,
            company = c.company,
            location = c.location,
            category = c.category,
            matchScore = c.matchScore,
            description = c.description,
            requirement = c.requirement,
            deadline = c.deadline
        )
    }

    /**
     * Map a local LearningMaterial to FirestoreLearningResourceDoc
     */
    fun mapLearningMaterial(m: LearningMaterial): FirestoreLearningResourceDoc {
        return FirestoreLearningResourceDoc(
            id = if (m.id == 0) UUID.randomUUID().toString() else "res_${m.id}",
            title = m.title,
            level = m.level,
            type = m.type,
            url = m.url,
            size = m.size,
            course = m.course,
            university = m.university
        )
    }
}
