package com.cs4530spring2022.project1.fragments

import android.Manifest
import android.R.string
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.cs4530spring2022.project1.MainProfileActivity
import com.cs4530spring2022.project1.databinding.FragmentStepBinding
import com.cs4530spring2022.project1.util.APIUtils
import com.cs4530spring2022.project1.util.UpdateNavBar
import com.cs4530spring2022.project1.util.models.UserProfile
import com.cs4530spring2022.project1.util.models.ViewModel
import com.cs4530spring2022.project1.util.models.WeatherData
import kotlin.math.abs
import kotlin.math.round


/**
 * A simple [Fragment] subclass.
 * Use the [StepFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StepFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentStepBinding
    private lateinit var vm: ViewModel
    var callback: UpdateNavBar? = null

    private var sm: SensorManager? = null
    private var acc: Sensor? = null
    private var stepC: Sensor? = null

    //positions
    private var xPrev = 0.0f  //Previous positions
    private var yPrev = 0.0f  //Previous positions
    private var zPrev = 0.0f
    private var x = 0.0f
    private var y = 0.0f
    private var z = 0.0f

    private var tryGesture = false

    private var initializing = true
    private var tPrevGesture : Long = Long.MIN_VALUE
    private val tS : Long = 1000000000



    fun SetStepCount(steps : Int){
        binding.tvSteps.text = steps.toString()
        if(vm.stepsCurr<steps) {
            vm.stepsCurr = steps
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sm = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        acc = sm?.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        stepC = sm?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        //get the view model
        vm = (activity as MainProfileActivity).vm

        val pm: PackageManager = (activity as MainProfileActivity).packageManager
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
                // update step count from user profile
                binding.tvPreviousOne.text = u.stepI.toString()
                binding.tvPreviousTwo.text = u.stepII.toString()
                binding.tvPreviousThree.text = u.stepIII.toString()
                binding.tvRecordValue.text = u.stepRec.toString()
            }
        }

    private val weatherObserver: Observer<WeatherData?> =
        Observer<WeatherData?> { w ->
            activity?.runOnUiThread {
                if (w == null) {
                    binding.tvTemp.text = "No Weather Data"
                } else {
                    val tempF = round(APIUtils.fahrenheit(w.condition.temperature) * 10) / 10.0
                    binding.tvTemp.text = "$tempF°F" + "  -  " + w.weather[0].header
                }
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentStepBinding.inflate(inflater, container, false)

        binding.btStop.setOnClickListener(this)
        binding.btStart.setOnClickListener(this)

        //set observers for VM data and load weather/places data automatically
        vm.getUserData().observe(viewLifecycleOwner,userObserver)
        vm.getWeatherData().observe(viewLifecycleOwner,weatherObserver)

        //update nav bar settings
        callback?.showNavBar(true);
        callback?.setNavBar("stepCounter");

        //setup buttons
        binding.btStart.enableClick(!vm.counting)
        binding.btStop.enableClick(vm.counting)
        //setup counter
        SetStepCount(vm.stepsCurr)

        var userData = vm.getUserData().value
        return binding.root
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                binding.btStart.id -> {StartCounter()}
                binding.btStop.id -> {StopCounter()}
            }
        }
    }

    fun Button.enableClick(b : Boolean){
        if(b) {
            setAlpha(1f)
        }
        else{
            setAlpha(.3f)
        }
        setClickable(b)
    }

    fun StartCounter(){
        //disable / enable buttons
        vm.counting = true;
        vm.countInitialized = false
        tryGesture = false;
        binding.btStart.enableClick(false)
        binding.btStop.enableClick(true)

        //setup to listen for steps (PLEASE USE SetStepCount(#) when updating steps to the value sent by the sensor!!!)
        if(stepC!=null) {
            sm?.registerListener(stepListener, stepC, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun StopCounter(){
        //we call this in 3 places
            //1 when we hit stop button
            //2 when we stop via a gesture
            //3 when the fragment is destroyed

        //disable / enable buttons
        tryGesture = false;
        binding.btStart.enableClick(true)
        binding.btStop.enableClick(false)

        SaveCounterData()


        //reset count
        SetStepCount(0)
    }

    fun SaveCounterData(){

        //stop listening for steps
        if(stepC!=null){
            sm?.unregisterListener(stepListener);
        }

        vm.saveSteps()
    }

    private val stepListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(se: SensorEvent) {
            var temp = (se.values[0]).toInt()
            if(!vm.countInitialized){
                vm.countInitialized = true
                //we just took one step
                vm.stepsInit = temp
            }
            var steps = temp-vm.stepsInit
            SetStepCount(steps)
        }

        override fun onAccuracyChanged(sensor: Sensor, i: Int) {}
    }

    private val gestureListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(se: SensorEvent) {

            //get acceleration in three axes
            x = se.values[0]
            y = se.values[1]
            z = se.values[2]

            if (initializing) {
                xPrev = x
                yPrev = y
                zPrev = z
            }
            val dx = abs(xPrev - x)
            val dy = abs(yPrev - y)
            val dz = abs(zPrev - z)

            val minX = 9f
            val maxY = 3.7f
            val maxZ = 3.7f
            val maxX = 50f
            val tryMax = 0.5f
            val tryMaxX = 6.5f

            //TESTING
            //binding.tvPreviousOne.text = dx.toInt().toString()
            //binding.tvPreviousTwo.text = dy.toInt().toString()
            //binding.tvPreviousThree.text = dz.toInt().toString()

            //check our gesture (a shake in x direction)
            if (dx > minX && dx<maxX &&! tryGesture){ // && dy > minY) {
                //initialize a gesture for when the phone stops moving
                tryGesture = true
            }

            //if too high in the other directions DO NOT try to gesture
            if (!(dy < maxY && dz < maxZ)) {
                tryGesture = false
            }

            //if we have stopped and trying to gesture, then gesture
            if(dx < tryMaxX  && dy < tryMax && dz < tryMax && tryGesture){
                tryGesture = false
                val t = System.nanoTime()
                if(tPrevGesture + tS <= t  ) {
                    //update prev gesture time
                    tPrevGesture = t;
                    //start or stop step counter
                    if (vm.counting) {
                        StopCounter()
                    } else {
                        StartCounter()
                    }
                }
            }


            //update previous values to x y and z
            xPrev = x
            yPrev = y
            zPrev = z
            initializing = false
        }

        override fun onAccuracyChanged(sensor: Sensor, i: Int) {}
    }

    override fun onResume() {
        super.onResume()
        //for GESTURE re-enable listening on resume
        if (acc != null) {
            sm?.registerListener(
                gestureListener,
                acc,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        if(stepC!=null && vm.counting) {
            sm?.registerListener(stepListener, stepC, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        //TODO if you are trying to unregister/register
        // the listener for STEP counter,
        // DONT! we want it to persist
        // while the app is "paused"
        // instead, do this in StopCounter
        //for GESTURE stop listening on pause
        super.onPause()
        if (acc != null) {
            sm?.unregisterListener(gestureListener)
        }
    }

}