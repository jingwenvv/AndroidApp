package com.cs4530spring2022.project1.util.models


import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Data class used to store google places responses.
 */
@Entity(tableName = "places_table")
data class PlacesData(@SerializedName("results") var results: List<Place>,
                      @SerializedName("status") var status: String) : Serializable

/**
 * Data class storing details about a specific Google Place
 */
data class Place(@SerializedName("business_status") var businessStatus: String,
                 @SerializedName("geometry") var geometry: Geometry,
                 @SerializedName("icon") var icon: String,
                 @SerializedName("icon_background_color") var iconColor: String,
                 @SerializedName("name") var name: String,
                 @SerializedName("vicinity") var address: String,
                 @SerializedName("opening_hours") var openingHours: OpeningHours,
                 @SerializedName("photos") var photos: List<PhotoInfo>,
                 @SerializedName("place_id") var id: String,
                 @SerializedName("rating") var rating: Double,
                 @SerializedName("user_ratings_total") var ratingCount: Int,
                 @SerializedName("reference") var reference: String,
                 @SerializedName("types") var types: List<String>) : Serializable

/**
 * This is a data class for images of locations as provided by the google places API.
 *
 * The photo reference is a unique identifier for the image, however it is NOT an image URI.
 *
 * Instead, unfortunately, to get the image one will have to use the google places photos API again.
 * Getting that to work is its own can of worms and may result in an app that exceeds our standard
 * quota.
 */
data class PhotoInfo(@SerializedName("height") var height: Int,
                     @SerializedName("width") var width: Int,
                     @SerializedName("photo_reference") var reference: String,
                     @SerializedName("html_attributions") var attributions: List<String>) : Serializable

data class Geometry(@SerializedName("location") var location: Coord,
                    @SerializedName("viewport") var viewport: Viewport) : Serializable

data class Viewport(@SerializedName("northeast") var northeast: Coord,
                    @SerializedName("southwest") var southwest: Coord) : Serializable

// This may have more content but I've never seen an instance with more stored
data class OpeningHours(@SerializedName("open_now") var openNow: Boolean) : Serializable