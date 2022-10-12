package com.cs4530spring2022.project1.util.models

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "weather_table")
class WeatherTable {
    @PrimaryKey @NonNull @ColumnInfo @SerializedName("id") var id = 0

    @NonNull @ColumnInfo @SerializedName("jsonData") var jsonData = ""
}