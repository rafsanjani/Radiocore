package com.foreverrafs.radiocore.activity

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.foreverrafs.radiocore.R

class HostActivity : AppCompatActivity() {

    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = findNavController(R.id.nav_host_fragment)
        setContentView(R.layout.activity_host)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    //    private fun initializeToolbar() {
//        setSupportActionBar(toolbar)
//        if (supportActionBar != null) {
//            supportActionBar!!.title = getString(R.string.app_name)
//        }
//    }
}