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
import kotlin.math.pow

@RunWith(AndroidJUnit4::class)
class StepCounterDataTest {
    // based on UserDaoTests
    private lateinit var userDao: UserDao
    private lateinit var weatherDao: WeatherDao
    private lateinit var db: MainDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, MainDatabase::class.java).build()
        userDao = db.userDao()
        weatherDao = db.weatherDao()
    }

    @Test
    fun initialValueTest() = runBlocking {
        userDao.insert(makeUser1())
        val user = userDao.getUser(1)
        assertThat(user).isNotNull()
        if (user != null){
            assertThat(user.stepI).isEqualTo(0)
            assertThat(user.stepII).isEqualTo(0)
            assertThat(user.stepIII).isEqualTo(0)
            assertThat(user.stepRec).isEqualTo(0)
        }
    }


    @Test
    fun updateValueTest() = runBlocking {
        userDao.insert(makeUser2())
        val user = userDao.getUser(2)
        assertThat(user).isNotNull()
        if (user != null) {
            assertThat(user.stepI).isEqualTo(1)
            assertThat(user.stepII).isEqualTo(2)
            assertThat(user.stepIII).isEqualTo(2)
            assertThat(user.stepRec).isEqualTo(3)
        }
    }

    @Test
    fun negtiveValueTest() = runBlocking {
        userDao.insert(makeUser3())
        val user = userDao.getUser(3)
        assertThat(user).isNotNull()
        if (user != null) {
            assertThat(user.stepI).isEqualTo(0)
            assertThat(user.stepII).isEqualTo(0)
            assertThat(user.stepIII).isEqualTo(0)
            assertThat(user.stepRec).isEqualTo(0)
        }
    }

    @Test
    fun extremeValueTest() = runBlocking {
        userDao.insert(makeUser4())
        val user = userDao.getUser(4)
        assertThat(user).isNotNull()
        if (user != null) {
            assertThat(user.stepI).isEqualTo(0)
            assertThat(user.stepII).isEqualTo(0)
            assertThat(user.stepIII).isEqualTo(0)
            assertThat(user.stepRec).isEqualTo(0)
        }
    }

    @Test
    fun extremeValueTest2() = runBlocking {
        userDao.insert(makeUser5())
        val user = userDao.getUser(5)
        assertThat(user).isNotNull()
        if (user != null) {
            assertThat(user.stepI).isEqualTo(150)
            assertThat(user.stepII).isEqualTo(150)
            assertThat(user.stepIII).isEqualTo(0)
            assertThat(user.stepRec).isEqualTo(0)
        }
    }


    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    fun makeUser1() : UserProfile {
        val user = UserProfile()
        user.id = 1
        user.stepI = 0
        user.stepII = 0
        user.stepIII = 0
        user.stepRec = 0

        return user
    }

    fun makeUser2() : UserProfile {
        val user = UserProfile()
        user.id = 2
        user.stepI = 1
        user.stepII = 2
        user.stepIII = 2
        user.stepRec = 3

        return user
    }

    fun makeUser3() : UserProfile {
        val user = UserProfile()
        user.id = 3
        user.stepI = -1
        user.stepII = -2
        user.stepIII = -2
        user.stepRec = -3

        return user
    }

    fun makeUser4() : UserProfile {
        val user = UserProfile()
        user.id = 4
        user.stepI = 2147483647 + 1
        user.stepII = 0
        user.stepIII = 0
        user.stepRec = 0

        return user
    }

    fun makeUser5() : UserProfile {
        val user = UserProfile()
        user.id = 5
        user.stepI = 150
        user.stepII = 150
        user.stepIII = -3
        user.stepRec = 2147483647 + 1

        return user
    }



}