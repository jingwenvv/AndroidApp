package com.cs4530spring2022.project1.util.models

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class UserDaoTests {
    // based on https://developer.android.com/training/data-storage/room/testing-db
    private lateinit var userDao: UserDao
    private lateinit var db: MainDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, MainDatabase::class.java).build()
        userDao = db.userDao()
    }

    @Test
    fun testInsertUser() = runBlocking {
        userDao.insert(makeUser())

        val user = userDao.getUser(1)

        // validate usr values are correct
        assertThat(user).isNotNull()
        if (user != null) {
            assertThat(user.created).isTrue()
            assertThat(user.firstName).isEqualTo("Jotaro")
            assertThat(user.lastName).isEqualTo("Kujo")
            assertThat(user.age).isEqualTo(25)
            assertThat(user.sex).isEqualTo("male")
            assertThat(user.height).isEqualTo(72.0)
            assertThat(user.weight).isEqualTo(170.2)
            assertThat(user.city).isEqualTo("Cairo")
            assertThat(user.country).isEqualTo("Egypt")
            assertThat(user.countryCode).isEqualTo("EG")
            assertThat(user.activityLevel).isEqualTo("Lightly Active")
            assertThat(user.weightGoal).isEqualTo("Gain")
            assertThat(user.weeklyWeightChange).isEqualTo(0.25)
            assertThat(user.locationLat).isEqualTo(30.0444)
            assertThat(user.locationLong).isEqualTo(31.2357)
        }
    }

    @Test
    fun testDeleteUser() = runBlocking {
        userDao.insert(makeUser())
        // that the user was in fact added before deleting (above test asserts that values are correct)
        assertThat(userDao.getUser(1)).isNotNull()
        userDao.deleteUser(1)
        // now we expect the user to be null after deleting
        assertThat(userDao.getUser(1)).isNull()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    fun makeUser() : UserProfile {
        // ideally a method would be prepared to automate this but really
        // i-is this a jojo reference???
        val user = UserProfile()
        user.id = 1
        user.firstName = "Jotaro"
        user.lastName = "Kujo"
        user.age = 25
        user.sex = "male"
        user.height = 72.0
        user.weight = 170.2
        user.city = "Cairo"
        user.country = "Egypt"
        user.countryCode = "EG"
        user.activityLevel = "Lightly Active"
        user.weightGoal = "Gain"
        user.weeklyWeightChange = 0.25
        user.created = true
        user.locationLat = 30.0444
        user.locationLong = 31.2357
        return user
    }
}