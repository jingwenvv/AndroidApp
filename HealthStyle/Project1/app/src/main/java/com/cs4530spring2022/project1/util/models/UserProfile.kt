package com.cs4530spring2022.project1.util.models

import android.util.Log
import androidx.annotation.NonNull
import com.google.gson.annotations.SerializedName
import androidx.lifecycle.ViewModel
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

/**
 * This class serves as a view model for the User's Profile and stores
 * typical information like the user's name, height, weight, etc.
 */
@Entity(tableName = "user_table")
class UserProfile : Serializable {
    // Better initialization technique will come with time
    @PrimaryKey @NonNull @ColumnInfo @SerializedName("id") var id = 0
    @NonNull @ColumnInfo @SerializedName("age") var age = 0
        set (value) {
            field = min(max(value, 0), 150)
            BMR()
        }
    @NonNull @ColumnInfo @SerializedName("fName") var firstName = ""
    @NonNull @ColumnInfo @SerializedName("lName") var lastName = ""
    @NonNull @ColumnInfo @SerializedName("hgt") var height = 36.0 // Stored as inches
        set (value) {
            field = value
            BMI()
            BMR()
        }
    @NonNull @ColumnInfo @SerializedName("wgt") var weight = 0.0 // Stored as pounds
        set (value) {
            field = min(max(value, 0.0), 1500.0)
            BMI()
            BMR()
        }
    @NonNull @ColumnInfo @SerializedName("sex") var sex = "rather not say"
        set (value) {
            field = value
            BMI()
            BMR()
        }
    @NonNull @ColumnInfo @SerializedName("pfpPath") var profilePic = ""
    // Location
    // The location text itself
    @NonNull @ColumnInfo @SerializedName("city") var city = ""
    @NonNull @ColumnInfo @SerializedName("country") var country = ""
    @NonNull @ColumnInfo @SerializedName("countryCode") var countryCode = ""
    // Latitude and Longitude
    @NonNull @ColumnInfo @SerializedName("lon") var locationLong = 0.0
    @NonNull @ColumnInfo @SerializedName("lat") var locationLat = 0.0
    @NonNull @ColumnInfo @SerializedName("wgtGoal") var weightGoal = "Maintain"
    @NonNull @ColumnInfo @SerializedName("wgtChangeWeekly") var weeklyWeightChange = 0.0 // Stored as lbs/week
        set (value) {
            field = min(value, 25.0)
            BMI()
            BMR()
        }
    @NonNull @ColumnInfo @SerializedName("activityLevel") var activityLevel = "Sedentary"
    @NonNull @ColumnInfo @SerializedName("bmi") var bmi = 0.0
    @NonNull @ColumnInfo @SerializedName("bmr") var bmr = 0
    @NonNull @ColumnInfo @SerializedName("created") var created = false
    @NonNull @ColumnInfo @SerializedName("stepRec") var stepRec = 0
        set (value) {
            field = max(value, 0)
        }
    @NonNull @ColumnInfo @SerializedName("stepI") var stepI = 0
        set (value) {
            field = max(value, 0)
        }
    @NonNull @ColumnInfo @SerializedName("stepII") var stepII = 0
        set (value) {
            field = max(value, 0)
        }
    @NonNull @ColumnInfo @SerializedName("stepIII") var stepIII = 0
        set (value) {
            field = max(value, 0)
        }

    /**
     * This function computes the BMI associated with the UserProfile and updates it.
     *
     * -1 if unable to be calculated
     */
    fun BMI() {
        if(weight == 0.0 || height == 0.0){
            bmi = -1.0
            return
        }
        bmi = round(703.0 * weight / (height * height) * 10.0) / 10.0
    }

    /**
     * This function computes the BMR associated with the UserProfile and updates it.
     * Computed according to https://www.thecalculatorsite.com/articles/health/bmr-formula.php
     * The Mifflin - St Jeor BMR equation
     *
     * -1 if unable to be calculated
     */
    fun BMR() {
        if(weight == 0.0 || height == 0.0 || age == 0){
            bmr = -1
            return
        }
        var tempBmr = (4.536 * weight) + (15.88 * height) - (5.0 * age)
        if (sex == "male") {
            tempBmr += 5.0
        } else {
            tempBmr -= 161.0
        }
        bmr = max(round(tempBmr).toInt(), 0)
    }

    /**
     * @return the daily calorie requirements of someone based on their
     * self-reported activity level.
     *
     * The choices range from Sedentary to Lightly Active, Moderately Active
     * Very Active or Insanely Active.
     *
     * Source: https://www.thecalculatorsite.com/articles/health/bmr-formula.php
     *
     * -1 when unable to be calculated
     */
    fun dailyCalories() : Int {
        val tempBmr = bmr.toDouble()
        var calories = when (activityLevel) {
            "Sedentary" -> {
                tempBmr * 1.2
            }
            "Lightly Active" -> {
                tempBmr * 1.375
            }
            "Moderately Active" -> {
                tempBmr * 1.55
            }
            "Very Active" -> {
                tempBmr * 1.725
            }
            "Insanely Active" -> {
                tempBmr * 1.9
            }
            else -> {
                Log.e("UserProfile", "No activity level found!")
                return -1
            }
        }
        if(tempBmr<0){
            return -1;
        }
        if(weightGoal == "Lose"){
            calories -= 3500.0/7 * weeklyWeightChange
        }
        else if(weightGoal == "Gain"){
            calories += 3500.0/7 * weeklyWeightChange
        }
        return max(calories.toInt(), 0) // Clamp the daily calorie intake to 0
    }

    //clone
    fun clone(): UserProfile{
        val u = UserProfile()
        u.stepI = this.stepI
        u.stepII = this.stepII
        u.stepIII = this.stepIII
        u.stepRec = this.stepRec
        u.id = this.id
        u.age = this.age
        u.firstName = this.firstName
        u.lastName = this.lastName
        u.height = this.height
        u.weight = this.weight
        u.sex = this.sex
        u.profilePic = this.profilePic
        u.city = this.city
        u.country = this.country
        u.countryCode = this.countryCode
        u.locationLong = this.locationLong
        u.locationLat = this.locationLat
        u.weightGoal = this.weightGoal
        u.weeklyWeightChange = this.weeklyWeightChange
        u.activityLevel = this.activityLevel
        u.bmi = this.bmi
        u.weeklyWeightChange = this.weeklyWeightChange
        u.created = this.created
        return u
    }
}