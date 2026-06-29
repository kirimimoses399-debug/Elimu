package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)

    // User Stats
    @Query("SELECT * FROM user_stats WHERE id = 1 LIMIT 1")
    fun getUserStats(): Flow<UserStats?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(stats: UserStats)

    // Bursary Opportunities
    @Query("SELECT * FROM bursary_opportunities")
    fun getBursaryOpportunities(): Flow<List<BursaryOpportunity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBursaryOpportunity(bursary: BursaryOpportunity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBursaryOpportunities(bursaries: List<BursaryOpportunity>)

    // Bursary Applications
    @Query("SELECT * FROM bursary_applications ORDER BY id DESC")
    fun getBursaryApplications(): Flow<List<BursaryApplication>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBursaryApplication(application: BursaryApplication)

    @Query("UPDATE bursary_applications SET status = :status WHERE id = :applicationId")
    suspend fun updateBursaryApplicationStatus(applicationId: Int, status: String)

    // Learning Materials
    @Query("SELECT * FROM learning_materials")
    fun getLearningMaterials(): Flow<List<LearningMaterial>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLearningMaterials(materials: List<LearningMaterial>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLearningMaterial(material: LearningMaterial)

    @Query("UPDATE learning_materials SET isDownloaded = :isDownloaded WHERE id = :id")
    suspend fun updateLearningMaterialDownloadStatus(id: Int, isDownloaded: Boolean)

    // Study Circle Messages
    @Query("SELECT * FROM study_circle_messages WHERE circleName = :circleName ORDER BY timestamp ASC")
    fun getStudyCircleMessages(circleName: String): Flow<List<StudyCircleMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyCircleMessage(message: StudyCircleMessage)

    // Group Meetings
    @Query("SELECT * FROM group_meetings WHERE circleName = :circleName ORDER BY id DESC")
    fun getGroupMeetings(circleName: String): Flow<List<GroupMeeting>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupMeeting(meeting: GroupMeeting)

    // Career Opportunities
    @Query("SELECT * FROM career_opportunities")
    fun getCareerOpportunities(): Flow<List<CareerOpportunity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCareerOpportunities(careers: List<CareerOpportunity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCareerOpportunity(career: CareerOpportunity)

    // Career Applications
    @Query("SELECT * FROM career_applications ORDER BY id DESC")
    fun getCareerApplications(): Flow<List<CareerApplication>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCareerApplication(application: CareerApplication)
}

@Database(
    entities = [
        UserProfile::class,
        BursaryOpportunity::class,
        BursaryApplication::class,
        LearningMaterial::class,
        StudyCircleMessage::class,
        GroupMeeting::class,
        CareerOpportunity::class,
        CareerApplication::class,
        UserStats::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "elimuhub_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
