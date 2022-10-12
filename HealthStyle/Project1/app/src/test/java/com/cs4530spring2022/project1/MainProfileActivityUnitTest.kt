package com.cs4530spring2022.project1

import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class MainProfileActivityUnitTest {

    private val mainProfileActivity = mockk<MainProfileActivity>()

    @Test
    fun lifeCycleAwarenessTest() {
    }

    //test if fragments are saving correctly
    @Test
    fun fragmentSavingTest() {
    }

    //test if fragments are loading correctly
    @Test
    fun fragmentLoadingTest() {
    }

    @Test
    fun onCreateTest() {
        //mainProfileActivity.onCreate()
    }
}