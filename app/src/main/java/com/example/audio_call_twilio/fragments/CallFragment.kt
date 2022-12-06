package com.example.audio_call_twilio.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.audio_call_twilio.databinding.FragmentCallBinding

/**
 * A simple [CallFragment] subclass.
 * create an instance of this fragment.
 */
class CallFragment : Fragment() {

    private lateinit var binding: FragmentCallBinding
    private val args : CallFragmentArgs by navArgs()
    private var contactName ="User Name"
    private var contactImage =0

    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCallBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contactName = args.contactName
        contactImage = args.contactImage

        binding.apply {
            textView.text = contactName
            imageView.setImageResource(contactImage)
        }

    }
}