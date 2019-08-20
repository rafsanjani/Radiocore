package com.foreverrafs.radiocore.activity

import android.content.Intent
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

