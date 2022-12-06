package com.example.audio_call_twilio.fragments

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audio_call_twilio.BuildConfig
import com.example.audio_call_twilio.R
import com.example.audio_call_twilio.SoundPoolManager
import com.example.audio_call_twilio.adapter.ContactListAdapter
import com.example.audio_call_twilio.databinding.FragmentContactBinding
import com.example.audio_call_twilio.model.Contact
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging
import com.twilio.audioswitch.AudioSwitch
import com.twilio.voice.*
import com.twilio.voice.Call.CallQualityWarning
import java.util.*

/**
 * A simple [ContactFragment] subclass.
 * create an instance of this fragment.
 */
@Suppress("DEPRECATION")
class ContactFragment : Fragment() {
    private val PERMISSIONS_REQUEST_CODE = 100
    private val MIC_PERMISSION_REQUEST_CODE = 1

    private lateinit var binding: FragmentContactBinding
    private lateinit var contactAdapter: ContactListAdapter
    private val audioSwitch: AudioSwitch? = null
    private var activeCall: Call? = null
    private var notificationManager: NotificationManager? = null
    private val accessToken =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImN0eSI6InR3aWxpby1mcGE7dj0xIn0.eyJqdGkiOiJTSzJjN2IxODc4MjUzYWE3OGY3N2UwMWMwNDQ5NmI4ZDRkLTE2NzAyNzg5MDYiLCJncmFudHMiOnsiaWRlbnRpdHkiOiJhbGljZSIsInZvaWNlIjp7ImluY29taW5nIjp7ImFsbG93Ijp0cnVlfSwib3V0Z29pbmciOnsiYXBwbGljYXRpb25fc2lkIjoiQVBiOTBhMDkyODZlMGQ3MzJkODQwYjNmYzQ5OWU0Y2QzYyJ9fX0sImlhdCI6MTY3MDI3ODkwNiwiZXhwIjoxNjcwMjgyNTA2LCJpc3MiOiJTSzJjN2IxODc4MjUzYWE3OGY3N2UwMWMwNDQ5NmI4ZDRkIiwic3ViIjoiQUMwNWM1NTdiYmQxMGEwMzgwM2M2NmIwMTlmZGIwNGNkNSJ9.7f45N3eqFfS42j6NKdi27BBG-Wtzr_MAPLfQOSwG-vE"

    var registrationListener: RegistrationListener = registrationListener()
    var callListener: Call.Listener = callListener()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentContactBinding.inflate(inflater, container, false)
        notificationManager =
            requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        /*
         * Ensure required permissions are enabled
         */
        if (Build.VERSION.SDK_INT > VERSION_CODES.R) {
            if (!hasPermissions(
                    context, Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            ) {
                requestPermissionForMicrophoneAndBluetooth()
            } else {
                registerForCallInvites()
            }
        } else {
            if (!hasPermissions(
                    context,
                    Manifest.permission.RECORD_AUDIO
                )
            ) {
                requestPermissionForMicrophone()
            } else {
                registerForCallInvites()
            }
        }
        return binding.root
    }

    private fun requestPermissionForMicrophone() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this@ContactFragment.requireActivity(),
                Manifest.permission.RECORD_AUDIO
            )
        ) {
            Snackbar.make(
                binding.coordinatorLayout,
                "Microphone permissions needed. Please allow in your application settings.",
                Snackbar.LENGTH_LONG
            ).show()
        } else {
            ActivityCompat.requestPermissions(
                this@ContactFragment.requireActivity(), arrayOf(Manifest.permission.RECORD_AUDIO),
                MIC_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun requestPermissionForMicrophoneAndBluetooth() {
        if (!hasPermissions(
                this@ContactFragment.requireActivity(), Manifest.permission.RECORD_AUDIO,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        ) {
            if (Build.VERSION.SDK_INT >= VERSION_CODES.S) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ),
                    PERMISSIONS_REQUEST_CODE
                )
            }
        } else {
            registerForCallInvites()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val contactImageId = arrayOf(
            R.drawable.user_profile,
            R.drawable.user_profile,
            R.drawable.user_profile,
            R.drawable.user_profile,
            R.drawable.user_profile,
            R.drawable.user_profile,
            R.drawable.user_profile,
            R.drawable.user_profile,
            R.drawable.user_profile,
            )

        val contactName = arrayOf(
            "User Profile 1",
            "User Profile 1",
            "User Profile 1",
            "User Profile 1",
            "User Profile 1",
            "User Profile 1",
            "User Profile 1",
            "User Profile 1",
            "User Profile 1"
        )
        val contactList = arrayListOf<Contact>()
        for (i in contactImageId.indices) {
            val contactInfo = Contact(contactImageId[i], contactName[i])
            contactList += contactInfo
        }
        contactAdapter = ContactListAdapter(contactList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this.context)

        with(binding) {
            recyclerView.adapter = contactAdapter
        }

        contactAdapter.onItemClickListener(object : ContactListAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {
                val direction =
                    ContactFragmentDirections.actionContactFragmentToCallFragment(
                        contactList[position].contactName,
                        contactList[position].contactImage
                    )
                findNavController().navigate(direction)
            }
        }
        )
        binding.recyclerView.setOnClickListener {
            val connectOptions = ConnectOptions.Builder(accessToken)
                .build()
            activeCall = Voice.connect(requireContext(), connectOptions, callListener)
        }
    }

    private fun hasPermissions(context: Context?, vararg permissions: String?): Boolean {
        for (permission in permissions) {
            if (context?.let {
                    ActivityCompat.checkSelfPermission(
                        it,
                        permission!!
                    )
                } != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }


    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        /*
         * Check if required permissions are granted
         */
        if (Build.VERSION.SDK_INT >= VERSION_CODES.S) {
            if (!hasPermissions(context, Manifest.permission.RECORD_AUDIO)) {
                Snackbar.make(
                    binding.coordinatorLayout,
                    "Microphone permission needed. Please allow in your application settings.",
                    Snackbar.LENGTH_LONG
                ).show()
            } else {
                if (!hasPermissions(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    )
                ) {
                    Snackbar.make(
                        binding.coordinatorLayout,
                        "Without bluetooth permission app will fail to use bluetooth.",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                registerForCallInvites()
            }
        } else {
            if (hasPermissions(
                    context,
                    Manifest.permission.RECORD_AUDIO
                )
            ) {
                Snackbar.make(
                    binding.coordinatorLayout,
                    "Microphone permissions needed. Please allow in your application settings.",
                    Snackbar.LENGTH_LONG
                ).show()
            } else {
                startAudioSwitch()
                registerForCallInvites()
            }
        }
    }

    private fun registerForCallInvites() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task: Task<String?> ->
                if (!task.isSuccessful) {
                    return@addOnCompleteListener
                }
                if (null != task.result) {
                    val fcmToken =
                        Objects.requireNonNull(task.result)
                    Log.i("MainActivity", "Registering with FCM")

                    fcmToken?.let {
                        Voice.register(
                            accessToken,
                            Voice.RegistrationChannel.FCM,
                            it,
                            registrationListener
                        )
                    }

                }
            }
    }

    private fun startAudioSwitch() {

    }

    private fun registrationListener(): RegistrationListener = object : RegistrationListener {
        override fun onRegistered(accessToken: String, fcmToken: String) {
            Log.d(
                "MainActivity",
                "Successfully registered FCM $fcmToken"
            )
        }

        override fun onError(
            error: RegistrationException,
            accessToken: String,
            fcmToken: String
        ) {
            val message = String.format(
                Locale.US,
                "Registration Error: %d, %s",
                error.errorCode,
                error.message
            )
            Log.e("MainActivity", message)
            Snackbar.make(binding.coordinatorLayout, message, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun callListener(): Call.Listener {
        return object : Call.Listener {
            override fun onRinging(call: Call) {
                Log.d("MainActivity", "Ringing")
                /*
                 * When [answerOnBridge](https://www.twilio.com/docs/voice/twiml/dial#answeronbridge)
                 * is enabled in the <Dial> TwiML verb, the caller will not hear the ringback while
                 * the call is ringing and awaiting to be accepted on the callee's side. The application
                 * can use the `SoundPoolManager` to play custom audio files between the
                 * `Call.Listener.onRinging()` and the `Call.Listener.onConnected()` callbacks.
                 */if (BuildConfig.playCustomRingback) {
                    context?.let { SoundPoolManager.getInstance(it)?.playRinging() }
                }
            }

            override fun onConnectFailure(call: Call, error: CallException) {
                audioSwitch!!.deactivate()
                if (BuildConfig.playCustomRingback) {
                    context?.let { SoundPoolManager.getInstance(it)?.stopRinging() }
                }
                Log.d("MainActivity", "Connect failure")
                val message = String.format(
                    Locale.US,
                    "Call Error: %d, %s",
                    error.errorCode,
                    error.message
                )
                Log.e("MainActivity", message)
                Snackbar.make(binding.coordinatorLayout, message, Snackbar.LENGTH_LONG).show()
            }

            override fun onConnected(call: Call) {
                audioSwitch!!.activate()
                if (BuildConfig.playCustomRingback) {
                    context?.let { SoundPoolManager.getInstance(it)?.stopRinging() }
                }
                Log.d("MainActivity", "Connected")
                activeCall = call
            }

            override fun onReconnecting(call: Call, callException: CallException) {
                Log.d("MainActivity", "onReconnecting")
            }

            override fun onReconnected(call: Call) {
                Log.d("MainActivity", "onReconnected")
            }

            override fun onDisconnected(call: Call, error: CallException?) {
                audioSwitch!!.deactivate()
                if (BuildConfig.playCustomRingback) {
                    context?.let { SoundPoolManager.getInstance(it)?.stopRinging() }
                }
                Log.d("MainActivity", "Disconnected")
                if (error != null) {
                    val message = String.format(
                        Locale.US,
                        "Call Error: %d, %s",
                        error.errorCode,
                        error.message
                    )
                    Log.e("MainActivity", message)
                    Snackbar.make(binding.coordinatorLayout, message, Snackbar.LENGTH_LONG).show()
                }
            }

            override fun onCallQualityWarningsChanged(
                call: Call,
                currentWarnings: MutableSet<CallQualityWarning>,
                previousWarnings: MutableSet<CallQualityWarning>
            ) {
                if (previousWarnings.size > 1) {
                    val intersection: MutableSet<CallQualityWarning> = HashSet(currentWarnings)
                    currentWarnings.removeAll(previousWarnings)
                    intersection.retainAll(previousWarnings)
                    previousWarnings.removeAll(intersection)
                }
                val message = String.format(
                    Locale.US,
                    "Newly raised warnings: $currentWarnings Clear warnings $previousWarnings"
                )
                Log.e("MainActivity", message)
                Snackbar.make(binding.coordinatorLayout, message, Snackbar.LENGTH_LONG).show()
            }
        }
    }
}