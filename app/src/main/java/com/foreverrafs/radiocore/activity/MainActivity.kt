package com.foreverrafs.radiocore.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.foreverrafs.radiocore.R

/**
 * We just use this class to show a fake progress for 1/8th of a second and just proceeds to HomeActivity and dismiss it
 */
class MainActivity : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        Handler().postDelayed({
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }, 800)
    }
}
