package com.cs4530spring2022.project1.util.models

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.cs4530spring2022.project1.util.APIUtils
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class Repository private constructor(application: Application) {

    private val weatherData = MutableLiveData<WeatherData?>()
    private val userData: MutableLiveData<UserProfile> = MutableLiveData<UserProfile>(UserProfile())
    private val placesData = MutableLiveData<PlacesData?>()
    private var mUserDao: UserDao? = null
    private var mWeatherDao: WeatherDao? = null

    //data loading methods
    //relies on userdata locations and longitudes

    fun loadDataAsync(){
        //launch a coroutine
        GlobalScope.launch {
            //load the weather data
            var job = loadWeatherData()
            //if the weather data could be loaded
            if(job!=null) {
                //await the completion of the coroutine
                while (job.isActive) {
                    //spin  waiting for job to terminate
                }
                //then use weatherdata to load the places data
                loadPlacesData()
            }
        }
    }

    fun loadWeatherData(): Job? {
        var job: Job? = null
        //get values from userData for loading
        var city = userData.value?.city
        var country = userData.value?.countryCode
        //if they aren't null launch a thread that grabs the data
        if(city!=null && country!=null) {
            job = GlobalScope.launch {
                //update weather data
                var data = APIUtils.getWeather("${city},${country}")
                if(data!=null){
                    weatherData.postValue(data!!)
                    insertWeather()
                }
                else{
                    weatherData.postValue(null)
                }
            }
        }
        return job
    }
    //relies on longitude and lattitude from weatherdata
    fun loadPlacesData(): Job? {
        var job: Job? = null
        //get coordinates (c) from weather
        var c = weatherData.value?.coordinates
        //get lon and lat from c
        if(c!=null) {
            var lon = c.longitude
            var lat = c.latitude
            //if they aren't null launch a thread that grabs the data
            if (lon != null && lat != null) {
                job = GlobalScope.launch {
                    //update places data
                    var data = APIUtils.getHikes("$lat,$lon", 30000U)
                    if(data!=null){
                        placesData.postValue(data!!)
                    }
                }
            }
        }
        else{
            placesData.postValue(null)
        }
        return job
    }

    //get data methods
    fun getWeatherData(): MutableLiveData<WeatherData?> {
        return weatherData
    }
    fun getUserData(): MutableLiveData<UserProfile> {
        return userData
    }
    fun getPlacesData(): MutableLiveData<PlacesData?> {
        return placesData
    }

    fun saveUser(local: UserProfile){
        var diff = local.city != userData.value?.city || local.country != userData.value?.city
        userData.value = local.clone()
        if(diff){
            weatherData.value = null
            placesData.value = null
        }
        insertUser()
    }

    companion object {
        @Volatile
        private var instance: Repository? = null

        @Synchronized
        fun getInstance(app: Application): Repository? {
            if (instance == null) {
                instance = Repository(app)
            }
            return instance
        }
    }

    private fun insertWeather(){
        if(weatherData!= null && userData != null)
            CoroutineScope(IO).launch {
                //create weathertable
                var j = Gson().toJson(weatherData.value)
                if(j!=null && j!="null") {
                    var w = WeatherTable()
                    w.jsonData = j
                    w.id = userData.value?.id!!
                    mWeatherDao?.insert(w)
                }
            }
    }

    private fun insertUser() {
        if (userData != null) {
            CoroutineScope(IO).launch {
                mUserDao?.insert(userData.value)
            }
        }
    }

    fun saveSteps(steps : Int){
        //if we have a count over 0
        if(steps>0 && userData!=null){
            var u = userData.value?.clone()!!
            //if we beat the record, update it
            if(steps>u.stepRec){
                u.stepRec = steps
            }
            //update history
            u.stepIII = u.stepII
            u.stepII = u.stepI
            u.stepI = steps

            //save user profile to the Repository
            saveUser(u)
        }
    }

    fun loadUser(id: Int): Boolean {
        var userExists = false
        var job = CoroutineScope(IO).launch {
            var user = mUserDao?.getUser(id)
            if (user != null) {
                userData.postValue(user!!)
                userExists = true && user.created
            }
        }
        //ensure that the user exists BEFORE we return
        while(job.isActive) {
        }

        return userExists
    }

    fun deleteUser(id: Int){
        userData.value = UserProfile()
        loadDataAsync()
        var job = CoroutineScope(IO).launch {
            mUserDao?.deleteUser(id)
        }
        //ensure we are done with this BEFORE we return
        while(job.isActive) {
        }
    }

    init{
        val db: MainDatabase? = MainDatabase.getDatabase(application)
        if (db != null) {
            mUserDao = db.userDao()
            mWeatherDao = db.weatherDao()
        };
        if(userData.value != null) {
            loadDataAsync();
        }
    }
}
