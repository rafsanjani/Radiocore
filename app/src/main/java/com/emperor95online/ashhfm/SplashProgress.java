package com.emperor95online.ashhfm;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

public class SplashProgress extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_progress);

        final ImageView backgroundImage = findViewById(R.id.backgroundImage);

        Handler handler = new Handler();
        //just show a progress bar for a few seconds and transition to the HaomeActivity
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Pair pair = new Pair(backgroundImage, ViewCompat.getTransitionName(backgroundImage));

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(SplashProgress.this, pair);
                    startActivity(new Intent(SplashProgress.this, HomeActivity.class), activityOptions.toBundle());
                } else {
                    startActivity(new Intent(SplashProgress.this, HomeActivity.class));
                }

                finish();
            }
        }, 3000);
    }
}
