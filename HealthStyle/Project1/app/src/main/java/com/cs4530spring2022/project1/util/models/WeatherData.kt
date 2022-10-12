package com.cs4530spring2022.project1.util.models

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Primary data class for storing weather information.
 * Intended to primarily be used with Weather APIs.
 *
 * Based on Lecture 7 example files.
 * in CS 4530 at the University of Utah by Varun Shankar (example 27).
 *
 * Each field should be initialized as available.
 */
/*data class WeatherData(var locationData: LocationData) {
    // you'll notice that kotlin makes this much more condensed
    var currentCondition = CurrentCondition()
    var temperature = Temperature()
    var wind = Wind()
    var rain = Percipitation()
    var snow = Percipitation()
    var cloudPerc: Long = 0

    data class CurrentCondition(var weatherID: Long = 0) {
        var condition: String = ""
        var description: String = ""
        var icon: String = ""
        var pressure: Double = 0.0
        var humidity: Double = 0.0
    }

    data class Temperature(var temperature: Double = 0.0) {
        var minTempature: Double = 0.0
        var maxTempature: Double = 0.0
    }

    data class Wind(var speed: Double = 0.0, var degrees: Double = 0.0)

    data class Percipitation(var time: String = "", var amount: Double = 0.0)
}*/

/**
 * Primary data class for storing OpenWeatherAPI responses. This version is GJON compatible.
 *
 * Designed to store current weather API responses (i.e. does not support forecasts).
 *
 * Based on: https://muhammedsalihguler.medium.com/developing-a-weather-application-with-kotlin-on-android-studio-part-2-b09147b55409
 */
data class WeatherData (@SerializedName("coord") var coordinates : Coord,
                        @SerializedName("weather") var weather : List<WeatherDescription>,
                        @SerializedName("main") var condition: Condition,
                        @SerializedName("wind") var wind: Wind,
                        @SerializedName("name") var name: String) : Serializable

// may be useful later
data class City(@SerializedName("name") var cityName : String,
                @SerializedName("country") var country : String) : Serializable

data class Coord(@SerializedName("lat") var latitude : Double,
                 @SerializedName("lon", alternate = ["lng"]) var longitude : Double) : Serializable

data class Condition(@SerializedName("temp") var temperature: Double,
                     @SerializedName("feels_like") var feelsLike: Double,
                     @SerializedName("temp_min") var minTempature: Double,
                     @SerializedName("temp_max") var maxTempature: Double,
                     @SerializedName("pressure") var pressure : Double,
                     @SerializedName("humidity") var humidity : Double) : Serializable

data class WeatherDescription(@SerializedName("main") var header : String,
                              @SerializedName("description") var description: String,
                              @SerializedName("icon") var icon: String) : Serializable

data class Wind(@SerializedName("speed") var speed : Double,
                @SerializedName("deg") var degreeDirection : Double) : Serializable