package com.cs4530spring2022.project1.util.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData


class ViewModel(app: Application?) : AndroidViewModel(app!!) {
    private val weatherData: MutableLiveData<WeatherData?>
    private val userData: MutableLiveData<UserProfile>
    private val placesData: MutableLiveData<PlacesData?>
    private val repository: Repository
    //lightweight profileData
    var profileLoaded = false;

    //lightweight step counter data
    var stepsCurr = 0;
    var stepsInit = 0;
    var counting = false;
    var countInitialized = false;


    //TODO make data loading more efficient

    fun saveSteps(){
        //reset vm values
        counting = false;
        countInitialized = false

        repository.saveSteps(stepsCurr)

        stepsInit = 0;
        stepsCurr = 0;
    }

    //TODO make data loading more efficient
    //get data
    fun getPlacesData(): MutableLiveData<PlacesData?> {
        repository.loadDataAsync()
        return placesData
    }

    fun getWeatherData(): MutableLiveData<WeatherData?> {
        repository.loadWeatherData()
        return weatherData
    }

    fun getUserData(): MutableLiveData<UserProfile> {
        return userData
    }

    fun loadUser(id: Int): Boolean {
        profileLoaded = true;
        return repository.loadUser(id)
    }

    fun saveUser(local: UserProfile) {repository.saveUser(local)}
    fun deleteUser(id: Int){repository.deleteUser(id)}
    fun deleteCurrUser() {
        if (userData.value!=null) {
            repository.deleteUser(userData.value!!.id)
        }
        profileLoaded = false
    }
    init {
        repository = Repository.getInstance(app!!)!!
        weatherData = repository.getWeatherData()
        placesData = repository.getPlacesData()
        userData = repository.getUserData()
    }
}
