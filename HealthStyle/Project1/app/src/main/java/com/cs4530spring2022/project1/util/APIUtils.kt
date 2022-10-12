package com.cs4530spring2022.project1.util

import android.util.Log
import com.cs4530spring2022.project1.util.models.PlacesData
import com.cs4530spring2022.project1.util.models.WeatherData
import com.google.gson.Gson
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

object APIUtils {
    // Standard URLs
    // TODO: google maps URLs
    const val WEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5/weather?q="
    const val WEATHER_ID_QUERY = "&appid="
    const val GEOCODING_BASE_URL = "https://maps.googleapis.com/maps/api/geocode/json?address="
    const val MAPS_ID_QUERY = "&key="
    const val PLACES_BASE_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
    const val PLACES_HIKES_QUERY = "&keyword=hike"
    const val PLACES_RADIUS_QUERY = "&radius="
    // Technically this should NOT be hardcoded AT ALL.
    // we can worry about that once API calls are actually working.
    // DO NOT SHARE THESE FUCKING KEYS DAMNIT
    private const val WEATHER_KEY = "abc19b2582e61a9b8664312d1cb8251a"
    private const val MAPS_KEY = "AIzaSyC2932WPrBEaP91R5zLzZn13cp6jKJf-AM"
    private const val TAG = "APIUtils"

    // Aside: Some of this API shenanigans can be made easier with DI (see: Dagger)

    /**
     * Returns an OpenWeatherAPI URL for the given location or null if the input could
     * not be parsed as a URL (i.e. gave bad input).
     *
     * Currently this function assumes that the location is given the form of
     * <City>&<CountryCode> (perhaps should be <City>,<CountryCode>)
     */
    fun buildWeatherURL(location: String) : URL? {
        // TODO: parse the input string better
        // also lat/long version when?
        var url : URL? = null
        try {
            url = URL("$WEATHER_BASE_URL$location$WEATHER_ID_QUERY$WEATHER_KEY")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return url
    }

    // THIS MAY NOT NEED TO BE USED

    /**
     * Returns a google maps Geocoding URL for the given location or null if the input
     * could not be parsed as a URL (i.e. gave bad input).
     *
     * Currently this function assumes that the location is given in the form of
     * <City>%<Country> (may also work with country code
     */
    fun buildGeocodeURL(location: String) : URL? {
        var url : URL? = null
        try {
            url = URL("$GEOCODING_BASE_URL$location$MAPS_ID_QUERY$MAPS_KEY")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return url
    }

    /**
     * Returns a google places URL for nearby hikes or null if the input could not be
     * parsed as a URL (i.e. gave bad input).
     *
     * @param location - a string encoding the location's latitude and longitude in the form
     * "<latitude>,<longitude>"
     *
     * @param radius - An unsigned integer corresponding to the desired search radius in meters.
     * The default is 50,000 m.
     *
     * @return A google places URL for querying hikes near the given location.
     */
    fun buildHikesURL(location: String, radius: UInt = 50000U) : URL? {
        var url : URL? = null
        try {
            url = URL("$PLACES_BASE_URL$location$MAPS_ID_QUERY$MAPS_KEY$PLACES_HIKES_QUERY$PLACES_RADIUS_QUERY$radius")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return url
    }

    /**
     * Makes an API call and returns the output or null if nothing could be read.
     */
    fun callURL(url: URL) : String? {
        val conn: HttpURLConnection = url.openConnection() as HttpURLConnection
        return try {
            val scanner = Scanner(conn.inputStream)
            scanner.useDelimiter("\\A")
            if (scanner.hasNext()) {
                scanner.next()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            conn.disconnect()
        }
    }

    /**
     * Attempts to read the json input string and parse it as a WeatherData object.
     * Returns null on failure.
     */
    fun json2WeatherData(json: String) : WeatherData? {
        return try {
            Gson().fromJson(json, WeatherData::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Attempts to read a json input string and parse it as a PlacesData object.
     * Returns null on failure.
     *
     * @param json - A string representing a json object
     *
     * @return a PlacesData object encoding nearby places or null on failure.
     */
    fun json2PlacesData(json: String) : PlacesData? {
        return try {
            Gson().fromJson(json, PlacesData::class.java)
        } catch (e:Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * This function takes a location in the form of <City>,<CountryCode>
     * and makes an OpenWeatherAPI call and returns the result of the operation.
     */
    fun getWeather(location: String) : WeatherData? {
        Log.d(TAG, "Attempting to fetch weather")
        val url = buildWeatherURL(location)
        if (url != null) {
            Log.d(TAG, "Using URL:\n$url")
            val res = callURL(url)
            if (res != null) {
                Log.d(TAG, "Received response:\n$res")
                val obj = json2WeatherData(res)
                if (obj != null) {
                    Log.d(TAG, "Created WeatherData Object:\n$obj")
                } else {
                    Log.e(TAG, "Failed to create WeatherData object!")
                }
                return obj
            } else {
                Log.e(TAG, "URL response could not be read!")
            }
        } else {
            Log.e(TAG, "URL build failed!")
        }
        return null
    }

    fun getHikes(location: String, radius: UInt = 50000U) : PlacesData? {
        Log.d(TAG, "Attempting to fetch hikes")
        val url = buildHikesURL(location, radius)
        if (url != null) {
            Log.d(TAG, "Using URL:\n$url")
            val res = callURL(url)
            if (res != null) {
                Log.d(TAG, "Received non-null response") // will not display response since these objects are massive
                val obj = json2PlacesData(res)
                if (obj != null) {
                    Log.d(TAG, "Created PlacesData Object")
                    return obj
                } else {
                    Log.e(TAG, "Failed to create PlacesData object!")
                }
            } else {
                Log.e(TAG, "URL response could not be read!")
            }
        } else {
            Log.e(TAG, "URL build failed!")
        }
        return null
    }

    // Weather API temperatures are given in Kelvin. Some quick conversion methods.

    fun celsius(kelvin: Double) : Double {
        return kelvin - 273.15
    }

    fun fahrenheit(kelvin: Double) : Double {
        return (celsius(kelvin) * 1.8) + 32.0
    }
}