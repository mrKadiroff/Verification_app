package com.example.verification.fragments

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.verification.R
import com.example.verification.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {
    lateinit var binding: ActivityWelcomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.extras
        val phoneNumber = bundle?.getString("phone").toString()


        binding.aotuhor.text = phoneNumber
    }
}