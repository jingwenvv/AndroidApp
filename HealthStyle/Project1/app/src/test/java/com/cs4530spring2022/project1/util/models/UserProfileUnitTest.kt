package com.cs4530spring2022.project1.util.models

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class UserProfileUnitTest {
    val profile = UserProfile()

    @Test
    fun ageInputTypeTest() {
        var inputTypeMatch = true;
        when(profile.age) {
            is Int -> inputTypeMatch = true
            else -> inputTypeMatch = false
        }
        assertThat(inputTypeMatch).isEqualTo(true)
    }

    @Test
    fun firstNameInputTypeTest() {
        var inputTypeMatch = true;
        when(profile.firstName) {
            is String -> inputTypeMatch = true
            else -> inputTypeMatch = false
        }
        assertThat(inputTypeMatch).isEqualTo(true)
    }

    @Test
    fun lastNameInputTypeTest() {
        var inputTypeMatch = true;
        when(profile.firstName) {
            is String -> inputTypeMatch = true
            else -> inputTypeMatch = false
        }
        assertThat(inputTypeMatch).isEqualTo(true)
    }

    @Test
    fun heightInputTypeTest() {
        var inputTypeMatch = true;
        when(profile.height) {
            is Double -> inputTypeMatch = true
            else -> inputTypeMatch = false
        }
        assertThat(inputTypeMatch).isEqualTo(true)
    }

    @Test
    fun weightInputTypeTest() {
        var inputTypeMatch = true;
        when(profile.weight) {
            is Double -> inputTypeMatch = true
            else -> inputTypeMatch = false
        }
        assertThat(inputTypeMatch).isEqualTo(true)
    }

    @Test
    fun sexInputTypeTest() {
        var inputTypeMatch = true;
        when(profile.sex) {
            is String -> inputTypeMatch = true
            else -> inputTypeMatch = false
        }
        assertThat(inputTypeMatch).isEqualTo(true)
    }

    @Test
    fun profilePicInputTypeTest() {
        var inputTypeMatch = true;
        when(profile.profilePic) {
            is String -> inputTypeMatch = true
            else -> inputTypeMatch = false
        }
        assertThat(inputTypeMatch).isEqualTo(true)
    }

    @Test
    fun cityInputTypeTest() {
        var inputTypeMatch = true;
        when(profile.city) {
            is String -> inputTypeMatch = true
            else -> inputTypeMatch = false
        }
        assertThat(inputTypeMatch).isEqualTo(true)
    }

    @Test
    fun countryInputTypeTest() {
        var inputTypeMatch = true;
        when(profile.country) {
            is String -> inputTypeMatch = true
            else -> inputTypeMatch = false
        }
        assertThat(inputTypeMatch).isEqualTo(true)
    }

    @Test
    fun countryCodeInputTypeTest() {
        var inputTypeMatch = true;
        when(profile.countryCode) {
            is String -> inputTypeMatch = true
            else -> inputTypeMatch = false
        }
        assertThat(inputTypeMatch).isEqualTo(true)
    }

    @Test
    fun weightGoalInputTypeTest() {
        var inputTypeMatch = true;
        when(profile.weightGoal) {
            is String -> inputTypeMatch = true
            else -> inputTypeMatch = false
        }
        assertThat(inputTypeMatch).isEqualTo(true)
    }

    @Test
    fun weeklyWeightChangeInputTypeTest() {
        var inputTypeMatch = true;
        when(profile.weeklyWeightChange) {
            is Double -> inputTypeMatch = true
            else -> inputTypeMatch = false
        }
        assertThat(inputTypeMatch).isEqualTo(true)
    }

    @Test
    fun activityLevelInputTypeTest() {
        var inputTypeMatch = true;
        when(profile.activityLevel) {
            is String -> inputTypeMatch = true
            else -> inputTypeMatch = false
        }
        assertThat(inputTypeMatch).isEqualTo(true)
    }

    @Test
    fun bmiInputTypeTest() {
        var inputTypeMatch = true;
        when(profile.bmi) {
            is Double -> inputTypeMatch = true
            else -> inputTypeMatch = false
        }
        assertThat(inputTypeMatch).isEqualTo(true)
    }

    @Test
    fun bmrInputTypeTest() {
        var inputTypeMatch = true;
        when(profile.bmr) {
            is Int -> inputTypeMatch = true
            else -> inputTypeMatch = false
        }
        assertThat(inputTypeMatch).isEqualTo(true)
    }

    @Test
    fun createdInputTypeTest() {
        var inputTypeMatch = true;
        when(profile.created) {
            is Boolean -> inputTypeMatch = true
            else -> inputTypeMatch = false
        }
        assertThat(inputTypeMatch).isEqualTo(true)
    }

    @Test
    fun calculateBMITest() {
        profile.height = 71.0
        profile.weight = 200.0
        assertThat(profile.bmi).isEqualTo(27.9)
    }

    @Test
    fun calculateBMRTest() {
        profile.height = 71.0
        profile.weight = 200.0
        profile.age = 21
        profile.sex = "male"
        assertThat(profile.bmr).isEqualTo(1935)
        profile.sex = "female"
        assertThat(profile.bmr).isEqualTo(1768)
        profile.sex = "other"
        assertThat(profile.bmr).isEqualTo(1768)
        profile.sex = "rather not say"
        assertThat(profile.bmr).isEqualTo(1768)
    }

    @Test
    fun calculateDailyCaloriesTest() {
        profile.height = 71.0
        profile.weight = 200.0
        profile.age = 21
        profile.sex = "male"
        profile.activityLevel = "Sedentary"
        assertThat(profile.dailyCalories()).isEqualTo(2322)
        profile.activityLevel = "Lightly Active"
        assertThat(profile.dailyCalories()).isEqualTo(2660)
        profile.activityLevel = "Moderately Active"
        assertThat(profile.dailyCalories()).isEqualTo(2998)
        profile.activityLevel = "Very Active"
        assertThat(profile.dailyCalories()).isEqualTo(3337)
        profile.activityLevel = "Insanely Active"
        assertThat(profile.dailyCalories()).isEqualTo(3675)
        profile.sex = "female"
        profile.activityLevel = "Sedentary"
        assertThat(profile.dailyCalories()).isEqualTo(2122)
        profile.activityLevel = "Lightly Active"
        assertThat(profile.dailyCalories()).isEqualTo(2431)
        profile.activityLevel = "Moderately Active"
        assertThat(profile.dailyCalories()).isEqualTo(2741)
        profile.activityLevel = "Very Active"
        assertThat(profile.dailyCalories()).isEqualTo(3050)
        profile.activityLevel = "Insanely Active"
        assertThat(profile.dailyCalories()).isEqualTo(3360)
    }
}