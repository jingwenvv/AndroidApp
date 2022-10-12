package com.cs4530spring2022.project1.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.cs4530spring2022.project1.MainProfileActivity
import com.cs4530spring2022.project1.R
import com.cs4530spring2022.project1.util.models.UserProfile
import com.cs4530spring2022.project1.databinding.FragmentViewProfileBinding
import com.cs4530spring2022.project1.util.UpdateNavBar
import com.cs4530spring2022.project1.util.models.ViewModel
import com.google.android.libraries.places.api.Places
import java.lang.ClassCastException

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

class ViewProfileFragment : Fragment(), View.OnClickListener {

    //params
    private lateinit var binding: FragmentViewProfileBinding
    var callback: UpdateNavBar? = null
    private lateinit var vm: ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setup view model
        vm = (activity as MainProfileActivity).vm
    }

    private val userObserver: Observer<UserProfile> =
        Observer<UserProfile> { u ->
            if(u!=null){
                activity?.runOnUiThread {
                    binding.tvFullName.text = "${u.firstName} ${u.lastName}"

                    //setup special for no age
                    var age = u.age
                    if (age == 0) {
                        binding.tvAgeValue.text = "--"
                    } else {
                        binding.tvAgeValue.text = age.toString()
                    }
                    binding.tvBmiValue.text = u.bmi.toString()
                    colorBMI(u.bmi)

                    //setup special for no city
                    var city = u.city
                    if (city == "") {
                        binding.tvCityValue.text = "--"
                    } else {
                        binding.tvCityValue.text = city
                    }

                    //setup special for no city
                    var country = u.country
                    if (country == "") {
                        binding.tvCountryValue.text = "--"
                    } else {
                        binding.tvCountryValue.text = country
                    }


                    binding.tvSexValue.text = u.sex
                    binding.tvWeightValue.text = u.weight.toString()

                    //show amount as well if not maintain
                    if (u.weightGoal == "Maintain") {
                        binding.tvFitValue.text = "Maintain Weight"
                    } else {
                        binding.tvFitValue.text = u.weightGoal + " " + u.weeklyWeightChange + " lbs"
                    }

                    // height shenanigans
                    val hInt = u.height.toInt()
                    binding.tvHeightValue.text = "${hInt / 12}\' ${hInt % 12}\""

                    if (u.profilePic.isNotEmpty()) {
                        val pfp = BitmapFactory.decodeFile(u.profilePic)
                        if (pfp != null) {
                            binding.ivProfile.setImageBitmap(pfp)
                        }
                    }
                }
            }
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        binding = FragmentViewProfileBinding.inflate(inflater, container, false)


        //set observer for VM profile
        vm.getUserData().observe(viewLifecycleOwner, userObserver)

        //update nav bar
        callback?.showNavBar(true);
        callback?.setNavBar("profile");

        //set click listeners
        binding.ivGear.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)
        binding.btDelete.setOnClickListener(this)

        return binding.root
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                binding.ivGear.id -> {
                    findNavController().navigate(R.id.action_global_editProfileFragment)
                }
                binding.ivBack.id -> {
                    findNavController().navigate(R.id.action_global_calorieCalculatorFragment)
                }
                binding.btDelete.id ->{
                    context?.let {
                        //send incomplete data messages

                        //create alert
                        val alertBuilder = AlertDialog.Builder(it)
                        alertBuilder.setCancelable(false)
                        alertBuilder.setTitle("ACCOUNT DELETION WARNING")
                            .setPositiveButton("Yes") { _: DialogInterface, _: Int ->
                                vm.deleteCurrUser()
                                findNavController().navigate(R.id.action_global_startingFragment)
                            }
                        alertBuilder.setNegativeButton("No") { _: DialogInterface, _: Int -> }
                        alertBuilder.setMessage("Warning your account and information will be permanently deleted. It cannot be recovered. Delete your account?")
                            .create().show()
                    }
                }
            }
        }
    }

    private fun colorBMI(bmi: Double) {
        if(bmi < 0) {
            binding.tvBmiValue.text = "--"
        } else if (bmi < 18.5 || (bmi in 25.0..30.0)) {
            binding.tvBmiValue.setTextColor(Color.parseColor("#fc9003"))
        } else if (bmi in 18.5..25.0) {
            binding.tvBmiValue.setTextColor(Color.parseColor("#1bab16"))
        } else {
            binding.tvBmiValue.setTextColor(Color.parseColor("#ab2016"))
        }
    }
}