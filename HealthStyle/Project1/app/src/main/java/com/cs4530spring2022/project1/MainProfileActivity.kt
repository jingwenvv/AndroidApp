package com.cs4530spring2022.project1

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.findNavController
import androidx.sqlite.db.SimpleSQLiteQuery
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.auth.result.AuthSignInResult
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.s3.AWSS3StoragePlugin
import com.cs4530spring2022.project1.databinding.ActivityMainProfileBinding
import com.cs4530spring2022.project1.util.UpdateNavBar
import com.cs4530spring2022.project1.util.models.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigationrail.NavigationRailView
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.io.File
import java.util.*
var timer = Timer()

class MainProfileActivity : AppCompatActivity(), UpdateNavBar {
    private lateinit var binding: ActivityMainProfileBinding
    val vm: ViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainProfileBinding.inflate(layoutInflater)
        supportActionBar?.hide()
        setContentView(binding.root)

        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.addPlugin(AWSS3StoragePlugin())
            Amplify.configure(applicationContext)

            Log.i("MyAmplifyApp", "Initialized Amplify")

            Amplify.Auth.signInWithWebUI(
                this,
                { result: AuthSignInResult ->
                    Log.i(
                        "AuthQuickStart",
                        result.toString()
                    )
                }
            ) { error: AuthException ->
                Log.e(
                    "AuthQuickStart",
                    error.toString()
                )
            }
        }
        catch (error: AmplifyException) {
            Log.e("MyAmplifyApp", "Could not initialize Amplify", error)
        }
        catch(error: Amplify.AlreadyConfiguredException){
            Log.e("MyAmplifyApp", "Already initialized", error)
        }

        Amplify.Auth.fetchAuthSession(
            { Log.i("AmplifyQuickstart", "Auth session = $it") },
            { error -> Log.e("AmplifyQuickstart", "Failed to fetch auth session", error) }
        )

        showNavBar(true)
        requestWritePermission()
        requestStepPermission()
        if(savedInstanceState != null) {
            if (savedInstanceState.getString("profile") != null) {
                vm.getUserData().value = Gson().fromJson(savedInstanceState.getString("profile"), UserProfile::class.java)
            }
            if (savedInstanceState.getString("weather") != null) {
                vm.getWeatherData().value = Gson().fromJson(savedInstanceState.getString("weather"), WeatherData::class.java)
            }
            if (savedInstanceState.getString("hikes") != null) {
                vm.getPlacesData().value = Gson().fromJson(savedInstanceState.getString("hikes"), PlacesData::class.java)
            }
            //load lightweight data
            vm.stepsInit =savedInstanceState.getInt("stepsInit")
            vm.stepsCurr = savedInstanceState.getInt("stepsCurr")
            vm.counting = savedInstanceState.getBoolean("counting")
            vm.countInitialized = savedInstanceState.getBoolean("countInitialized")
            vm.profileLoaded = savedInstanceState.getBoolean("profileLoaded")
        }

        if (isTablet()) {
            tabletLayout(binding.root)
        }
        else {
            phoneLayout(binding.root)
        }
        //peroidically save data to AWS
        timer.scheduleAtFixedRate(object : TimerTask(){
            override fun run(){
                CoroutineScope(IO).launch{
                    updateAWSPeriodically()
                }
            }
        }, 1, (5 * 60000).toLong()) //every 5 mins
    }

    private fun updateAWSPeriodically():Job
    {
        var job = CoroutineScope(IO).launch {
            if(vm.profileLoaded) {
                //user dao
                MainDatabase.getDatabase(applicationContext)?.userDao()
                    ?.checkpoint(SimpleSQLiteQuery("pragma wal_checkpoint(full)"))
                //weather dao
                MainDatabase.getDatabase(applicationContext)?.weatherDao()
                    ?.checkpoint(SimpleSQLiteQuery("pragma wal_checkpoint(full)"))
                uploadFile("user.db")
            }
        }
        return job
    }

    private fun uploadFile(file: String) {
        val exampleFile = File(applicationContext.getDatabasePath(file).absolutePath)

        if(exampleFile.exists())
        {
            Amplify.Storage.uploadFile(file, exampleFile,
                { Log.i("MyAmplifyApp", "Successfully uploaded: ${it.key}") },
                { Log.e("MyAmplifyApp", "Upload failed", it) }
            )
        }
    }

    override fun onStop(){
        var job = updateAWSPeriodically();// uploads database files to AWS before destroying
        while(job.isActive){
            //spin
        }
        super.onStop()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Log.e("MainActivity", "WRITE PERMISSION REQUEST FAILED")
                Toast.makeText(
                    this,
                    "You must allow permission write external storage to your mobile device.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    /**
     * Saves view model states when the instance state is saved.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("profile", Gson().toJson(vm.getUserData().value))
        outState.putString("weather", Gson().toJson(vm.getWeatherData().value))
        outState.putString("hikes", Gson().toJson((vm.getPlacesData().value)))
        //save lightweight data
        outState.putInt("stepsInit", vm.stepsInit)
        outState.putInt("stepsCurr", vm.stepsCurr)
        outState.putBoolean("counting", vm.counting)
        outState.putBoolean("countInitialized", vm.countInitialized)
        outState.putBoolean("profileLoaded", vm.profileLoaded)
        super.onSaveInstanceState(outState)
    }

    private fun requestWritePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "WRITE PERMISSIONS NOT GRANTED. REQUESTING PERMISSION.")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
            } else {
                Log.d("MainActivity", "WRITE PERMISSIONS GRANTED")
            }
        }
    }

    private fun requestStepPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "ACTIVITY PERMISSIONS NOT GRANTED. REQUESTING PERMISSION.")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    1
                )
            } else {
                Log.d("MainActivity", "ACTIVITY PERMISSIONS GRANTED")
            }
        }
    }

    override fun showNavBar(show: Boolean){
        if(show){
            binding.navBar.visibility = View.VISIBLE
            binding.div.visibility = View.VISIBLE
        }
        else{
            binding.navBar.visibility = View.GONE
            binding.div.visibility = View.GONE
        }
    }

    private fun isTablet(): Boolean{
        return getResources().getBoolean(R.bool.isTablet)
    }

    override fun setNavBar(name: String){
        var bar = binding.navBar;
        if(bar!=null && bar is BottomNavigationView) {
            if(name=="profile")
                bar.menu.findItem(R.id.profile).isChecked = true
            if(name=="hikes")
                bar.menu.findItem(R.id.hikes).isChecked = true
            if(name=="fitness")
                bar.menu.findItem(R.id.fitness).isChecked = true
            if(name=="stepCounter")
                bar.menu.findItem(R.id.stepCounter).isChecked = true
        }
        if(bar!=null && bar is NavigationRailView) {
            if(name=="profile")
                bar.menu.findItem(R.id.profile).isChecked = true
            if(name=="hikes")
                bar.menu.findItem(R.id.hikes).isChecked = true
            if(name=="fitness")
                bar.menu.findItem(R.id.fitness).isChecked = true
            if(name=="stepCounter")
                bar.menu.findItem(R.id.stepCounter).isChecked = true
        }
    }

    private fun phoneLayout(view: View) {
        var bar = binding.navBar
        if(bar!=null && bar is BottomNavigationView) {
            bar.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.profile -> {
                        findNavController(binding.fragmentContainerView.getChildAt(0).id).navigate(
                            R.id.action_global_viewProfileFragment
                        )
                    }
                    R.id.fitness -> {
                        findNavController(binding.fragmentContainerView.getChildAt(0).id).navigate(
                            R.id.action_global_calorieCalculatorFragment
                        )
                    }
                    R.id.hikes -> {
                        findNavController(binding.fragmentContainerView.getChildAt(0).id).navigate(
                            R.id.action_global_hikeFragment
                        )
                    }
                    R.id.stepCounter -> {
                        findNavController(binding.fragmentContainerView.getChildAt(0).id).navigate(
                            R.id.action_global_stepFragment
                        )
                    }
                }
                true
            }
        }
    }

    private fun tabletLayout(view: View) {
        var bar = binding.navBar
        if(bar!=null && bar is NavigationRailView) {
            bar.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.profile -> {
                        findNavController(binding.fragmentContainerView.getChildAt(0).id).navigate(
                            R.id.action_global_viewProfileFragment
                        )
                    }
                    R.id.fitness -> {
                        findNavController(binding.fragmentContainerView.getChildAt(0).id).navigate(
                            R.id.action_global_calorieCalculatorFragment
                        )
                    }
                    R.id.hikes -> {
                        findNavController(binding.fragmentContainerView.getChildAt(0).id).navigate(
                            R.id.action_global_hikeFragment
                        )
                    }
                    R.id.stepCounter -> {
                        findNavController(binding.fragmentContainerView.getChildAt(0).id).navigate(
                            R.id.action_global_stepFragment
                        )
                    }
                }
                true
            }
        }
    }
}