package com.foreverrafs.radiocore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.foreverrafs.radiocore.R;

/**
 * We just use this class to show a fake progress for 1/8th of a second and just proceeds to HomeActivity and dismiss it
 */
public class MainActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //just show a progress bar for a few seconds and transition to the HomeActivity
        new Handler().postDelayed(() -> {
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();
        }, 800);
    }
}
