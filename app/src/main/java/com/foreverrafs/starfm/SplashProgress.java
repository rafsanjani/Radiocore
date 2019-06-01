package com.foreverrafs.starfm;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashProgress extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_progress);

//        final ImageView backgroundImage = findViewById(R.id.backgroundImage);

        Handler handler = new Handler();
        //just show a progress bar for a few seconds and transition to the HomeActivity
        handler.postDelayed(() -> {
//                Pair pair = new Pair(backgroundImage, ViewCompat.getTransitionName(backgroundImage));

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//                    ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(SplashProgress.this, pair);
                startActivity(new Intent(SplashProgress.this, HomeActivity.class)/*, activityOptions.toBundle()*/);
            } else {
                startActivity(new Intent(SplashProgress.this, HomeActivity.class));
            }

            finish();
        }, 1000);
    }
}
