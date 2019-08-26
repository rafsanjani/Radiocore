package com.foreverrafs.radiocore.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.StrictMode
import androidx.appcompat.app.AppCompatActivity
import com.foreverrafs.radiocore.BuildConfig
import com.foreverrafs.radiocore.R

/**
 * We just use this class to show a fake progress for 1/8th of a second and just proceeds to HomeActivity and dismiss it
 */
class MainActivity : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableStrictMode()


        Handler().postDelayed({
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }, 800)
    }

    private fun enableStrictMode() {
        if (BuildConfig.DEBUG) {
            val policy = StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()

            StrictMode.setVmPolicy(policy)
        }
    }
}
