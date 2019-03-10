package com.emperor95online.ashhfm;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashProgress extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_progress);

        Handler handler = new Handler();
        //just show a progress bar for a few seconds
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashProgress.this, HomeActivity.class));
                finish();
            }
        }, 3000);
    }
}
