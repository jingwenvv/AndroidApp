package com.cs4530spring2022.project1.fragments

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.cs4530spring2022.project1.MainProfileActivity
import com.cs4530spring2022.project1.R
import com.cs4530spring2022.project1.databinding.FragmentStartingBinding
import com.cs4530spring2022.project1.databinding.FragmentViewProfileBinding
import com.cs4530spring2022.project1.util.UpdateNavBar
import com.cs4530spring2022.project1.util.models.ViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.ClassCastException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [StartingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StartingFragment : Fragment() {
    var callback: UpdateNavBar? = null
    private lateinit var binding: FragmentStartingBinding
    private lateinit var vm: ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //TODO later update to use different user settings
        vm = (activity as MainProfileActivity).vm
        // put it in new Blank Starting Page Fragment
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
        // put it in new Blank Starting Page Fragment
        GlobalScope.launch {
            // this is dirty and bad but also works
            Thread.sleep(2000)
            activity?.runOnUiThread {
                if(!vm.profileLoaded) {
                    if (vm.loadUser(0)) {
                        //load ViewProfileFragment
                        findNavController().navigate(R.id.viewProfileFragment)
                    } else {
                        //load ViewProfileFragment
                        findNavController().navigate(R.id.editProfileFragment)
                    }
                }
            }
        }

        //hide the nav bar
        callback?.showNavBar(false);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_starting, container, false)

    }
}