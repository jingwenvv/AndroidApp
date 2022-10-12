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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.cs4530spring2022.project1.MainProfileActivity
import com.cs4530spring2022.project1.R
import com.cs4530spring2022.project1.util.models.UserProfile
import com.cs4530spring2022.project1.databinding.FragmentCalorieCalculatorBinding
import com.cs4530spring2022.project1.util.UpdateNavBar
import com.cs4530spring2022.project1.util.models.ViewModel
import java.lang.ClassCastException


class CalorieCalculatorFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentCalorieCalculatorBinding
    private var localUserData: UserProfile = UserProfile()
    private lateinit var vm: ViewModel
    var callback: UpdateNavBar? = null
    var calculated = false

    private val goals: Array<String> = arrayOf("Maintain", "Lose", "Gain")
    private val levels: Array<String> = arrayOf("Sedentary", "Lightly Active", "Moderately Active",
        "Very Active", "Insanely Active")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //get the view model
        vm = (activity as MainProfileActivity).vm
        //initialize the data in the profile to be data in the userData
        //TODO later we will check userdata of a certain userid probbably
        var userData = vm.getUserData().value
        if(userData!=null){
            localUserData = userData.clone()
        }
    }

    //TODO decide if we want calculate button to save values to VM or if we want editing to do so

    //update data for BMI: BMR: DAILYCAL:
    private val userObserver: Observer<UserProfile> =
        Observer<UserProfile> { u ->
            if(u!=null){
                var cals = u.dailyCalories()

                if(u.bmr < 0) {
                    binding.tvBmrValue.text = "--"
                } else{
                    binding.tvBmrValue.text = u.bmr.toString()
                }

                if(cals < 0) {
                    binding.tvDailycalValue.text = "--"
                } else{
                    binding.tvDailycalValue.text = cals.toString()
                }

                if(u.bmi < 0){
                    binding.tvBmiValue.text = "--"
                    return@Observer
                } else if (u.bmi < 18.5 || (u.bmi in 25.0..30.0)) {
                    binding.tvBmiValue.setTextColor(Color.parseColor("#fc9003"))
                } else if (u.bmi in 18.5..25.0) {
                    binding.tvBmiValue.setTextColor(Color.parseColor("#1bab16"))
                } else {
                    binding.tvBmiValue.setTextColor(Color.parseColor("#ab2016"))
                }
                binding.tvBmiValue.text = u.bmi.toString()
                //load profile pic
                if (u.profilePic.isNotEmpty()) {
                    val pfp = BitmapFactory.decodeFile(u.profilePic)
                    if (pfp != null) {
                        binding.ivProfile.setImageBitmap(pfp)
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        binding = FragmentCalorieCalculatorBinding.inflate(inflater, container, false)

        //set observer for vm profile
        vm.getUserData().observe(viewLifecycleOwner,userObserver)

        registerAdapters()

        //update nav bar
        callback?.showNavBar(true);
        callback?.setNavBar("fitness");

        //set listeners
        binding.btCalculate.setOnClickListener(this)
        binding.ivProfile.setOnClickListener(this)
        binding.tbtQuestion.setOnClickListener(this)

        //populate data
        loadData()

        //spinner listeners
        registerSpinnerSelectionListeners()
        registerTextChangedListeners()

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

    //function for greying out weight amount when we select maintain
    // (needs to be used in multiple places)
    private fun checkWeightSpinners(){
        //if maintain -> disable goal weight spinner
        if(localUserData.weightGoal == "Maintain"){
            binding.etGlWgt.setText("")
            localUserData.weeklyWeightChange = 0.00
            binding.etGlWgt.error = null
            binding.etGlWgt.isEnabled = false
            binding.tvAmt.isEnabled = false
            binding.tvLbsTxt.isEnabled = false
        } else {
            if(binding.etGlWgt.text.toString().toDoubleOrNull() == null) {
                binding.etGlWgt.error = "Invalid number"
            }
            else{
                binding.etGlWgt.error = null
            }
            binding.etGlWgt.isEnabled = true
            binding.tvAmt.isEnabled = true
            binding.tvLbsTxt.isEnabled = true
        }
    }

    private fun loadData() {
        //load curr weight
        if (localUserData.weight > 0.0) {
            binding.etWgt.setText(localUserData.weight.toString())
        }
        //load goal type
        binding.spGoalType.setSelection(goals.indexOf(localUserData.weightGoal))
        checkWeightSpinners()
        //load goal type
        if (localUserData.weeklyWeightChange > 0) {
            binding.etGlWgt.setText(localUserData.weeklyWeightChange.toString())
        }

        //load activity level
        binding.spActivityLvl.setSelection(levels.indexOf(localUserData.activityLevel))

    }

    private fun registerTextChangedListeners() {
        binding.etWgt.doAfterTextChanged {
            if(binding.etWgt.text.toString().toDoubleOrNull() == null) {
                binding.etWgt.error = "Invalid number"
            } else {
                localUserData.weight = binding.etWgt.text.toString().toDouble()
                if ((binding.etWgt.text.toString().toDouble() > 1500) && localUserData.weight.toString() != binding.etWgt.text.toString()) {
                    binding.etWgt.setText(localUserData.weight.toString())
                }
                binding.etWgt.error = null
            }
        }
        binding.etGlWgt.doAfterTextChanged {
            if(binding.etGlWgt.text.toString().toDoubleOrNull() == null) {
                if (!(goals[binding.spGoalType.selectedItemPosition] == "Maintain" && binding.etGlWgt.text.toString() == "")) {
                    binding.etGlWgt.error = "Invalid number"
                }
            } else {
                localUserData.weeklyWeightChange = binding.etGlWgt.text.toString().toDouble()
                if ((binding.etGlWgt.text.toString().toDouble() > 25) && localUserData.weeklyWeightChange.toString() != binding.etGlWgt.text.toString()) {
                    binding.etGlWgt.setText(localUserData.weeklyWeightChange.toString())
                }
                binding.etGlWgt.error = null
            }
        }
    }

    private fun registerAdapters() {
        //goal type
        val goaltypeAdp =
            context?.let {
                ArrayAdapter(
                    it,
                    android.R.layout.simple_spinner_dropdown_item,
                    goals
                )
            }
        binding.spGoalType.adapter = goaltypeAdp

        //goal type
        val actAdp =
            context?.let {
                ArrayAdapter(
                    it,
                    android.R.layout.simple_spinner_dropdown_item,
                    levels
                )
            }
        binding.spActivityLvl.adapter = actAdp
    }

    private fun registerSpinnerSelectionListeners() {
        // goal type
        binding.spGoalType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                localUserData.weightGoal = goals[position]

                //check if maintain, disable (update error)
                checkWeightSpinners()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { /** do nothing */ }
        }
        // activity level
        binding.spActivityLvl.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                localUserData.activityLevel = levels[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { /** do nothing */ }
        }
    }

    override fun onClick(v: View?) {
        if (v != null){
            when (v.id){
                binding.btCalculate.id->{
                    context?.let {
                        //send incomplete data messages

                        //create alert
                        val alertBuilder = AlertDialog.Builder(it)
                        alertBuilder.setCancelable(false)
                        alertBuilder.setTitle("Input Incomplete").setPositiveButton("Ok") { _: DialogInterface, _: Int -> }

                        //goal incomplete
                        if (binding.spActivityLvl.selectedItemPosition < 0
                            || binding.etGlWgt.error != null) {
                        alertBuilder.setMessage("Please enter your weekly goal information.").create().show()
                        }

                        //weight / activitylvl incomplete
                        else if (binding.spActivityLvl.selectedItemPosition < 0
                            || binding.etWgt.error != null
                            || localUserData.weight <= 0.0
                        ) {
                        alertBuilder.setMessage("Please enter your current weight and activity level.").create().show()
                        }

                        else {
                            //update alert to be a warning
                            alertBuilder.setTitle("Health Warning").setPositiveButton("Ok") { _: DialogInterface, _: Int -> }

                            //gain & lose over two pounds
                            if (localUserData.weeklyWeightChange > 2f)
                            {
                                alertBuilder.setMessage("Warning, attempting to lose or gain more than two pounds a week can be dangerous.").create().show()
                            }

                            //Double Ensure BMI/BMR are Updated
                            localUserData.BMI()
                            localUserData.BMR()

                            //calculate calories
                            val calories = localUserData.dailyCalories()

                            //calorie needs not met
                            if ((calories<1000) || (localUserData.sex != "female" && calories<1200))
                            {
                                alertBuilder.setMessage("Warning, this daily calorie intake is less than the recommended minimum").create().show()
                            }
                            //we did the calculation
                            calculated = true

                            // show calories by updating the userprofile in the VM
                            vm.saveUser(localUserData)
                        }
                    }
                }
                binding.ivProfile.id->{
                    //on click profile, return to viewprofilefrag
                    findNavController().navigate(R.id.action_global_viewProfileFragment)
                }
                binding.tbtQuestion.id->{
                    context?.let {
                        //create alert
                        val alertBuilder = AlertDialog.Builder(it)
                        alertBuilder.setCancelable(false)
                        alertBuilder.setTitle("Help")
                            .setPositiveButton("Ok") { _: DialogInterface, _: Int -> }
                        alertBuilder.setMessage("To calculate BMR and Calorie intake you need to have an Age, Weight, and Height entered in your profile.")
                            .create().show()
                    }
                }
            }
        }
    }
}