package com.example.workouttracker

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.workouttracker.database.UserProfileDao
import com.example.workouttracker.database.UserProfileDatabase
import com.example.workouttracker.model.UserProfileEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * ProfileDaoTest
 * Covers integration tests for Room database profile storage:
 * - Insert a UserProfileEntity and retrieve it by uid.
 * - Update an existing profile photo and verify the new value.
 * - Insert multiple user profiles and verify separation/isolation by uid.
 */
@RunWith(AndroidJUnit4::class)
class ProfileDaoTest {

    private lateinit var database: UserProfileDatabase
    private lateinit var dao: UserProfileDao

    @Before
    fun setup() {
        // Build an in-memory database so the content is deleted when the process is killed
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            UserProfileDatabase::class.java
        ).allowMainThreadQueries() // allow main thread queries for test simplicity if needed
         .build()
        dao = database.userProfileDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveProfile() = runBlocking {
        val uid = "user_123"
        val photoBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
        
        val profile = UserProfileEntity(uid = uid, profilePicBase64 = photoBase64)
        dao.saveProfile(profile)

        val retrieved = dao.getProfile(uid)
        assertNotNull(retrieved)
        assertEquals(uid, retrieved?.uid)
        assertEquals(photoBase64, retrieved?.profilePicBase64)
    }

    @Test
    fun updateProfilePhoto() = runBlocking {
        val uid = "user_123"
        val firstPhoto = "photo_1"
        val secondPhoto = "photo_2"

        val profile = UserProfileEntity(uid = uid, profilePicBase64 = firstPhoto)
        dao.saveProfile(profile)

        // Save again with same uid to update (using REPLACE on conflict strategy)
        val updatedProfile = UserProfileEntity(uid = uid, profilePicBase64 = secondPhoto)
        dao.saveProfile(updatedProfile)

        val retrieved = dao.getProfile(uid)
        assertNotNull(retrieved)
        assertEquals(uid, retrieved?.uid)
        assertEquals(secondPhoto, retrieved?.profilePicBase64)
    }

    @Test
    fun insertMultipleProfilesVerifyIsolation() = runBlocking {
        val user1 = UserProfileEntity(uid = "user_1", profilePicBase64 = "photo_user_1")
        val user2 = UserProfileEntity(uid = "user_2", profilePicBase64 = "photo_user_2")

        dao.saveProfile(user1)
        dao.saveProfile(user2)

        val retrieved1 = dao.getProfile("user_1")
        val retrieved2 = dao.getProfile("user_2")

        assertNotNull(retrieved1)
        assertNotNull(retrieved2)

        assertEquals("photo_user_1", retrieved1?.profilePicBase64)
        assertEquals("photo_user_2", retrieved2?.profilePicBase64)
    }
}
