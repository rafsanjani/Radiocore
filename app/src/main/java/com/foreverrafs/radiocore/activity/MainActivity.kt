package com.foreverrafs.radiocore.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.StrictMode
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.crashlytics.android.Crashlytics
import com.foreverrafs.radiocore.BuildConfig
import com.foreverrafs.radiocore.R
import com.foreverrafs.radiocore.data.NewsOpenHelper
import com.foreverrafs.radiocore.data.NewsService
import io.fabric.sdk.android.Fabric

/**
 * We just use this class to show a fake progress for 1/8th of a second and just proceeds to HomeActivity and dismiss it
 */
class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"
    lateinit var mDbOpenHelper: NewsOpenHelper
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mDbOpenHelper = NewsOpenHelper(this)

        NewsService(applicationContext).fetchNews()


        setContentView(R.layout.activity_main)
        enableStrictMode()
        setUpCrashlytics()

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

    private fun setUpCrashlytics() {
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, Crashlytics())
            Log.i(TAG, "setUpCrashlytics: Enabled")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mDbOpenHelper.close()
    }


}
