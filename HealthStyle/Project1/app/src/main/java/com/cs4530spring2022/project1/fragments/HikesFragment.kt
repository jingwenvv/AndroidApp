package com.cs4530spring2022.project1.fragments

import android.app.Activity
import com.cs4530spring2022.project1.util.HikesAdapter
import com.cs4530spring2022.project1.util.ItemsHikesRow
import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.cs4530spring2022.project1.MainProfileActivity
import com.cs4530spring2022.project1.R
import com.cs4530spring2022.project1.databinding.FragmentHikesBinding
import com.cs4530spring2022.project1.util.APIUtils
import com.cs4530spring2022.project1.util.UpdateNavBar
import com.cs4530spring2022.project1.util.models.PlacesData
import com.cs4530spring2022.project1.util.models.UserProfile
import com.cs4530spring2022.project1.util.models.ViewModel
import com.cs4530spring2022.project1.util.models.WeatherData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.ClassCastException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.round


class HikesFragment : Fragment() /*, View.OnClickListener*/ {
    private lateinit var binding: FragmentHikesBinding
    private lateinit var vm: ViewModel
    private var lat = 0.0
    private var lon = 0.0
    private var startTime: Long = 0
    var callback: UpdateNavBar? = null
    var timer:Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //get the view model
        vm = (activity as MainProfileActivity).vm
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        try {
            callback = activity as UpdateNavBar
        } catch (e: ClassCastException) {
        }
    }

    override fun onDetach() {
        callback = null
        super.onDetach()
    }

    private val userObserver: Observer<UserProfile> =
        Observer<UserProfile> { u ->
            if(u!=null){
                //load profile pic
                if (u.profilePic.isNotEmpty()) {
                    val pfp = BitmapFactory.decodeFile(u.profilePic)
                    if (pfp != null) {
                        binding.ivProfile.setImageBitmap(pfp)
                    }
                }
                //load location
                if (u.city.isNotEmpty() && u.countryCode.isNotEmpty()) {
                    // Country code is set if and only if country is set properly.
                    binding.tvCityCountry.text = "${u.city}, ${u.country}"
                } else {
                    val alertBuilder = AlertDialog.Builder(context)
                    alertBuilder.setCancelable(false).setPositiveButton("Ok") { _: DialogInterface, _: Int -> }
                    alertBuilder.setTitle("Location Data Uninitialzed")
                    alertBuilder.setMessage("There is no location associated with your profile. To use weather and map services, please set your location.")
                    alertBuilder.show()
                    binding.tvCityCountry.text = "No location set"
                }
            }
        }

    private val weatherObserver: Observer<WeatherData?> =
        Observer<WeatherData?> { w ->
            activity?.runOnUiThread {
                if (w == null) {
                    binding.tvTemp.text = "No Weather Data"
                } else {
                    val tempF = round(APIUtils.fahrenheit(w.condition.temperature) * 10) / 10.0
                    // MUST BE SET BEFORE LOADING HIKES!
                    lat = w.coordinates.latitude
                    lon = w.coordinates.longitude
                    binding.tvTemp.text = "$tempF°F" + "  -  " + w.weather[0].header
                }
            }
        }

    private val placesObserver: Observer<PlacesData?> =
        Observer<PlacesData?> { p ->
                activity?.runOnUiThread {
                    if (p == null) {
                        hikingFailed()
                    } else {
                        displayHikeOptions(p)
                    }
                }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHikesBinding.inflate(inflater, container, false)



        binding.swipeRefresh.setOnRefreshListener {
            vm.getPlacesData()
            vm.getWeatherData()
        }

        binding.swipeRefresh.isRefreshing = true
        startTime = System.nanoTime()
        //set observers for VM data and load weather/places data automatically
        vm.getUserData().observe(viewLifecycleOwner,userObserver)
        vm.getWeatherData().observe(viewLifecycleOwner,weatherObserver)
        vm.getPlacesData().observe(viewLifecycleOwner,placesObserver)

        //update nav bar settings
        callback?.showNavBar(true);
        callback?.setNavBar("hikes");

        //create layout manager
        context?.let {
            binding.lsHikes.layoutManager = LinearLayoutManager(it)
        }
        //setup and use adapter
        binding.lsHikes.adapter = HikesAdapter()

        return binding.root
    }

    private fun hikingFailed() {
        val hikesData = ArrayList<ItemsHikesRow>()

        //failed hike item
        hikesData.add(ItemsHikesRow(R.drawable.error, "No Hikes Found", "Check your location settings."))
        (binding.lsHikes.adapter as HikesAdapter).setHikesData(hikesData)

        if(startTime+2000000000<System.nanoTime()) {
            binding.swipeRefresh.isRefreshing = false
        }
        else{
            if(timer==null){
                timer = Timer()
                timer?.schedule(object : TimerTask(){
                    override fun run(){
                        binding.swipeRefresh.isRefreshing = false
                    }
                },(3000).toLong())
            }
        }

    }


    private fun noHikesFound() {
        val alertBuilder = AlertDialog.Builder(context)
        alertBuilder.setCancelable(false).setPositiveButton("Ok") { _: DialogInterface, _: Int -> }
        alertBuilder.setTitle("No Hikes Found")
        alertBuilder.setMessage("No hikes could be found within 30km of your location.")
        alertBuilder.show()
        // TODO: Allow user to try a larger radius
    }

    private fun displayHikeOptions(hikes: PlacesData){
        // define data list
        val hikesData = ArrayList<ItemsHikesRow>()

        if(hikes.results.count()==0){
            noHikesFound()
        }

        // fill in data
        for (hike in hikes.results) {
            val row = ItemsHikesRow(R.drawable.ic_maprow, hike.name, hike.address)
            row.latitude = hike.geometry.location.latitude
            row.longitude = hike.geometry.location.longitude
            hikesData.add(row)
        }

        (binding.lsHikes.adapter as HikesAdapter).setHikesData(hikesData)
        binding.swipeRefresh.isRefreshing = false
    }
}