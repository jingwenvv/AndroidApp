
package com.cs4530spring2022.project1.fragments

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.cs4530spring2022.project1.databinding.FragmentEditProfileBinding
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import android.app.Activity
import com.cs4530spring2022.project1.MainProfileActivity
import com.cs4530spring2022.project1.R
import com.cs4530spring2022.project1.util.UpdateNavBar
import com.cs4530spring2022.project1.util.models.UserProfile
import com.cs4530spring2022.project1.util.models.ViewModel
import java.lang.ClassCastException


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

class EditProfileFragment : Fragment(), View.OnClickListener {
    //params go here
    private lateinit var binding: FragmentEditProfileBinding
    private lateinit var countries: Array<String>
    private lateinit var countryCodes: Array<String>
    private var localUserData: UserProfile = UserProfile()
    private lateinit var vm: ViewModel

    val REQUEST_IMAGE_CAPTURE = 1

    // Spinner variables
    private val heightFt: Array<Int> = arrayOf(3,4,5,6,7,8)
    private val heightIn: Array<Int> = arrayOf(0,1,2,3,4,5,6,7,8,9,10,11)
    private val sexes: Array<String> = arrayOf("male", "female", "other", "rather not say")

    var callback: UpdateNavBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        countries = activity?.resources?.getStringArray(R.array.CountryNames) ?: arrayOf()
        countryCodes = activity?.resources?.getStringArray(R.array.CountryCodes) ?: arrayOf()
        //get the view model
        vm = (activity as MainProfileActivity).vm
        //initialize the data in the profile to be data in the userData
        //TODO later we will check userdata of a certain userid probably

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var userData = vm.getUserData().value
        if(userData!=null){
            localUserData = userData.clone()
        }
        // Inflate the layout for this fragment
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)

        if(vm.getUserData().value?.created == true){
            binding.tvHeader.text = "Edit Profile"
        }

        //update nav bar
        callback?.showNavBar(false);
        callback?.setNavBar("profile");

        registerAdapters()
        binding.btSubmitInfo.setOnClickListener(this)
        binding.ivUploadPic.setOnClickListener(this)

        // Populate existing info, if any
        loadProfile()

        // Register UI behavior
        registerTextChangedListeners()
        registerSpinnerSelectionListeners()
        return binding.root
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

    companion object {
        fun newInstance(param1: String, param2: String) =
            EditProfileFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                binding.btSubmitInfo.id -> {
                    //check for info unfilled
                    context?.let {
                        //create alert
                        val alertBuilder = AlertDialog.Builder(it)
                        alertBuilder.setCancelable(false).setPositiveButton("Ok") { _: DialogInterface, _: Int -> }
                        alertBuilder.setTitle("Required Input")

                        //name incomplete
                        if(binding.etFName.text.isBlank() || binding.etLName.text.isBlank()){
                            alertBuilder.setMessage("Please enter both a first and last name").create().show()
                        }

                        else if (binding.etAge.error != null) {
                            alertBuilder.setMessage("Please enter a valid age").create().show()
                        }


                        //weight incomplete
                        else if (binding.spHeightFeet.selectedItemPosition < 0
                            || binding.spHeightInches.selectedItemPosition < 0
                            || binding.etWeight.error != null
                            || localUserData.weight == 0.0) {
                            alertBuilder.setMessage("Please enter your weight").create().show()
                        }

                        else if (binding.etCountry.error != null
                            || binding.etCountry.text.toString() == ""
                            || binding.etCity.text.toString() == "") {
                            alertBuilder.setMessage("Please enter a both a city and a valid country").create().show()
                        }

                        //otherwise save info to model & navigate
                        else {
                            if (!localUserData.created) {
                                localUserData.id = 0
                            }
                            localUserData.created = true
                            //save the profile info to the vm
                            vm.saveUser(localUserData)

                            vm.getWeatherData()
                            // Navigate to the calorie calculator fragment if this profile is created
                            // Otherwise navigate back to the view profile fragment
                            // This is probably something we'll want to change down the line
                            if (localUserData.created) {
                                findNavController().navigate(R.id.action_global_viewProfileFragment)
                            } else {
                                findNavController().navigate(R.id.action_global_calorieCalculatorFragment)
                            }

                        }
                    }
                }
                binding.ivUploadPic.id -> {
                    val camInt = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    if(activity?.let { camInt.resolveActivity(it.packageManager) } != null) {
                        // technically this is deprecated but no real alternative exists atm
                        // TODO: Define request codes somewhere commonly accessible?
                        startActivityForResult(camInt, REQUEST_IMAGE_CAPTURE)
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Get the bitmap
            val extras = data?.extras
            val profileImage = extras?.get("data") as Bitmap

            // Write that bad boy to a file
            if (isExternalStorageWritable()) {
                saveProfilePicture(profileImage)
                // Update UI (Move into separate method?
                binding.ivUploadPic.setImageBitmap(profileImage)
            } else {
                Toast.makeText(context,"External storage not writable.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * This function attempts to write a bitmap profile to external storage
     */
    private fun saveProfilePicture(picture: Bitmap) {
        val root = context?.getExternalFilesDir(null).toString()
        Log.d("EPF", "Saving to: $root/saved_images")
        val dir = File("$root/saved_images")

        dir.mkdirs()

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val filn = "Profilepic_$timestamp.jpg"

        val file = File(dir, filn)
        if(file.exists()) file.delete()
        try {
            val out = FileOutputStream(file)
            picture.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.flush()
            out.close()
            Toast.makeText(context,"file saved!",Toast.LENGTH_SHORT).show()
            localUserData.profilePic = file.absolutePath // save the pfp filepath in model
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * This function determines if this device can write to external storage.
     *
     * @return True if the device can write to external storage, false otherwise
     */
    private fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == state) {
            return true
        }
        return false
    }

    /**
     * This function loads data from the User Profile model
     * and populates the appropriate entries with this information.
     */
    private fun loadProfile() {
        // Load editText values
        binding.etFName.setText(localUserData.firstName)
        binding.etLName.setText(localUserData.lastName)
        binding.etCity.setText(localUserData.city)
        binding.etCountry.setText(localUserData.country)
        if (localUserData.age != 0) {
            binding.etAge.setText(localUserData.age.toString())
        }
        if (localUserData.weight != 0.0) {
            binding.etWeight.setText(localUserData.weight.toString())
        }
        // Load spinner values
        binding.spSex.setSelection(sexes.indexOf(localUserData.sex))
        val hInt = localUserData.height.toInt()
        binding.spHeightFeet.setSelection(heightFt.indexOf(hInt/12))
        binding.spHeightInches.setSelection(heightIn.indexOf(hInt%12))

        if (localUserData.profilePic.isNotEmpty()) {
            val pfp = BitmapFactory.decodeFile(localUserData.profilePic)
            if (pfp != null) {
                binding.ivUploadPic.setImageBitmap(pfp)
            }
        }
    }

    private fun registerAdapters() {
        // initialize adapters

        //sex
        val sexAdp =
            context?.let {
                ArrayAdapter(
                    it,
                    android.R.layout.simple_spinner_dropdown_item,
                    sexes
                )
            }
        binding.spSex.adapter = sexAdp
        //...

        //height
        val htFtAdp =
            context?.let { ArrayAdapter(it, android.R.layout.simple_spinner_dropdown_item, heightFt) }
        val htInAdp =
            context?.let {
                ArrayAdapter(
                    it,
                    android.R.layout.simple_spinner_dropdown_item,
                    heightIn
                )
            }
        binding.spHeightFeet.adapter = htFtAdp
        binding.spHeightInches.adapter = htInAdp

        val countryAdp =
            context?.let { ArrayAdapter(it, android.R.layout.simple_list_item_1, countries) }
        binding.etCountry.setAdapter(countryAdp)
    }

    /**
     * This function defines the behavior of textChanged listeners for the
     * edit profile fragment.
     *
     * The purpose of this method is to cluster textChanged listener definitions
     * together so that (hopefully) they will be easier to maintain.
     */
    private fun registerTextChangedListeners() {
        // First name
        binding.etFName.doAfterTextChanged {
            // put any extra validation you may want to do here
            localUserData.firstName = binding.etFName.text.toString()
        }
        // Last name
        binding.etLName.doAfterTextChanged {
            localUserData.lastName = binding.etLName.text.toString()
        }
        // Location
        // City
        binding.etCity.doAfterTextChanged {
            localUserData.city = binding.etCity.text.toString()
        }
        // Country
        binding.etCountry.doAfterTextChanged {
            if (binding.etCountry.text.isNotEmpty()) {
                val index = countries.indexOf(binding.etCountry.text.toString())
                if (index < 0 && binding.etCountry.text.toString().isNotEmpty()) {
                    binding.etCountry.error = "Country Not Recognized"
                } else {
                    localUserData.country = countries[index]
                    localUserData.countryCode = countryCodes[index]
                    binding.etCountry.error = null
                }
            }
        }

        binding.etAge.doAfterTextChanged {
            if(binding.etAge.text.toString().toIntOrNull() == null) {
                binding.etAge.error = "Invalid number"
            } else {
                localUserData.age = binding.etAge.text.toString().toInt()
                if (localUserData.age.toString() != binding.etAge.text.toString()) {
                    binding.etAge.setText(localUserData.age.toString())
                }
                binding.etWeight.error = null
            }
        }

        binding.etWeight.doAfterTextChanged {
            if(binding.etWeight.text.toString().toDoubleOrNull() == null) {
                binding.etWeight.error = "Invalid number"
            } else {
                localUserData.weight = binding.etWeight.text.toString().toDouble()
                if ((binding.etWeight.text.toString().toDouble() > 1500) && localUserData.weight.toString() != binding.etWeight.text.toString()) {
                    binding.etWeight.setText(localUserData.weight.toString())
                }
                //binding.etWeight.setText((round(profile.weight * 10.0) / 10.0).toString())
                binding.etWeight.error = null
            }
        }
    }

    /**
     * This function defines the behavior of all spinners in the
     * edit profile fragment.
     *
     * The purpose of this method is to cluster onItemSelected listener definitions
     * together so that (hopefully) they will be easier to maintain.
     */
    private fun registerSpinnerSelectionListeners() {
        // to do this we instantiate anonymous classes to implement the lister class
        // this methodology is verbose BUT it results in cleaner & modular behavior

        // Sex
        binding.spSex.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                localUserData.sex = sexes[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { /** do nothing */ }
        }

        // Height (feet)
        binding.spHeightFeet.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val ft = heightFt[position]
                val inch = if(binding.spHeightInches.selectedItemPosition >= 0)
                    heightIn[binding.spHeightInches.selectedItemPosition] else 0
                localUserData.height = 12.0 * ft + inch
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { /** do nothing */ }
        }

        // Height (inches)
        binding.spHeightInches.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val ft = if(binding.spHeightFeet.selectedItemPosition >= 0)
                    heightFt[binding.spHeightFeet.selectedItemPosition] else 0
                val inch = heightIn[position]
                localUserData.height = 12.0 * ft + inch
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { /** do nothing */ }
        }
    }
}