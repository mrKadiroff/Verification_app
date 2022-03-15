package com.example.verification

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.os.CountDownTimer
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.example.verification.databinding.ActivityVerifyBinding
import com.example.verification.fragments.WelcomeActivity
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit


class VerifyActivity : AppCompatActivity() {
    lateinit var binding: ActivityVerifyBinding
    lateinit var auth: FirebaseAuth
    private var storedVerificationId: String = ""
    private var phoneNumber: String = ""

    lateinit var storedVerificationId1: String
    lateinit var resendToken1: PhoneAuthProvider.ForceResendingToken
    private val TAG = "VerifyActivity"


    lateinit var countdown_timer: CountDownTimer
    var time_in_milli_seconds = 0L
    var time: String = "1"
    var START_MILLI_SECONDS = 60000L
    var isRunning: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyBinding.inflate(layoutInflater)
        setContentView(binding.root)





        auth = FirebaseAuth.getInstance()
        auth.setLanguageCode("uz")

        val bundle = intent.extras
        phoneNumber = bundle?.getString("phone").toString()
        storedVerificationId = bundle?.getString("verificationId").toString()


        binding.apply {
            aotuhor.text = phoneNumber.substring(0,phoneNumber.length-4)
            aotuhor.text = aotuhor.text.toString()+" -** -**"

            time_in_milli_seconds = time.toLong() * 60000L
            startTimer(time_in_milli_seconds)
        }



        binding.phoneInput.setOnEditorActionListener { v, actionId, event ->

            if (actionId == EditorInfo.IME_ACTION_DONE) {
                vertifyCode()
                val view = currentFocus
                if (view != null) {
                    val imm: InputMethodManager =
                        getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken,0)
                }
            }


            true
        }

        binding.phoneInput.addTextChangedListener {
            if (it.toString().length == 6) {
                vertifyCode()
            }
        }

        binding.repeat.setOnClickListener {
            time_in_milli_seconds = time.toLong() * 60000L
            startTimer(time_in_milli_seconds)
            resendCode(phoneNumber)


        }


}

    private fun pauseTimer() {


        countdown_timer.cancel()
        isRunning = false
        time_in_milli_seconds = START_MILLI_SECONDS
        updateTextUI1()

    }

    private fun updateTextUI1() {
        binding.timee.text = "00:00"
    }

    private fun startTimer(time_in_seconds: Long) {
        countdown_timer = object : CountDownTimer(time_in_seconds, 1000) {
            override fun onFinish() {
                binding.repeat.isClickable = true
            }

            override fun onTick(p0: Long) {
                binding.repeat.isClickable = false
                time_in_milli_seconds = p0
                updateTextUI()
            }
        }
        isRunning = true
        countdown_timer.start()
    }

    private fun updateTextUI() {
        val minute = (time_in_milli_seconds / 1000) / 60
        val seconds = (time_in_milli_seconds / 1000) % 60
        binding.timee.text = "0$minute:$seconds"
    }


    private fun resendCode(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private var callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d(TAG, "onVerificationCompleted:$credential")
            signInWithPhoneAuthCredential(credential)

        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w(TAG, "onVerificationFailed", e)

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
            }

            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId1: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {

            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d(TAG, "onCodeSent:$verificationId1")

            // Save verification ID and resending token so we can use them later
            storedVerificationId = verificationId1
            resendToken1 = token


        }


    }







    private fun vertifyCode() {
        val code = binding.phoneInput.text.toString()
        if (code.length == 6) {
            val credential = PhoneAuthProvider.getCredential(storedVerificationId,code)
            signInWithPhoneAuthCredential(credential)
        }
    }





    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")

                    Toast.makeText(this, "Verified", Toast.LENGTH_SHORT).show()
                   pauseTimer()
                    val intent = Intent(this,WelcomeActivity::class.java)
                    var bundle = Bundle()
                    bundle.putString("phone",phoneNumber)
                    intent.putExtras(bundle)
                    startActivity(intent)


                    val user = task.result?.user?.phoneNumber
                    Log.d(TAG, "signInWithPhoneAuthCredential: $user")
                } else {
                    // Sign in failed, display a message and update the UI
                    Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(this, "Invalid", Toast.LENGTH_SHORT).show()
                    }
                    // Update UI
                }
            }
    }

    override fun onResume() {
        super.onResume()
        binding.timee.text = "00:00"
        binding.phoneInput.setText("")
    }
}