package com.github.oOSatyamOo.tensorflow_object_detection_helloworld.android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.oOSatyamOo.tensorflow_object_detection_helloworld.android.databinding.FragmentFirstBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    // Binding object instance corresponding to the fragment_start.xml layout
    // This property is non-null between the onCreateView() and onDestroyView() lifecycle callbacks,
    // when the view hierarchy is attached to the fragment.

    // Syntax for property delegation
//    var <property-name> : <property-type> by <delegate-class>()

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.i("Permission: ", "Granted")
                findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
            } else {
                Log.i("Permission: ", "Denied")
            }
        }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
         val    binding = FragmentFirstBinding.inflate(inflater, container, false)
        binding.cameraButton.setOnClickListener { view ->
//            com.google.android.material.snackbar.Snackbar.make(view, "Replace with your own action", com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
//                .setAction("Action", null)
//                .setAnchorView(R.id.button_first).show()
            if(ContextCompat.checkSelfPermission(
                    this.requireContext(),
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED){
                requestPermissionLauncher.launch(
                    Manifest.permission.CAMERA
                )
        }else{
                findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)

            }
        }
        return binding.root
    }


//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        binding.startFragment = this@FirstFragment
//    }

    /**
     * Start an order with the desired quantity of cupcakes and navigate to the next screen.
     */

    /**
     * This fragment lifecycle method is called when the view hierarchy associated with the fragment
     * is being removed. As a result, clear out the binding object.
     */
    override fun onDestroyView() {
        super.onDestroyView()
//        binding = null
    }

}