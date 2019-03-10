package com.emperor95online.ashhfm;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.emperor95online.ashhfm.service.AudioStreamingService;
import com.emperor95online.ashhfm.service.AudioStreamingService.AudioStreamingState;
import com.emperor95online.ashhfm.fragment.Home;
import com.emperor95online.ashhfm.util.PrefManager;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static com.emperor95online.ashhfm.util.Constants.ACTION_PAUSE;
import static com.emperor95online.ashhfm.util.Constants.ACTION_PLAY;
import static com.emperor95online.ashhfm.util.Constants.DEBUG_TAG;
import static com.emperor95online.ashhfm.util.Constants.MESSAGE;
import static com.emperor95online.ashhfm.util.Constants.RESULT;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    ///////////////////////////////////////////////////////////////////////////////////////////////
    BroadcastReceiver receiver;
    //let's assume nothing is playing when application starts
    AudioStreamingService.AudioStreamingState audioStreamingState = AudioStreamingState.STATUS_PAUSED;

    private RelativeLayout layoutBottomSheet, bottomSheetMenuItems;
    private LinearLayout bottomSheetPlaybackItems;
    private BottomSheetBehavior sheetBehavior;
    private ImageButton smallPlay;// smallPause;
    private ImageButton play, pause;
    private ImageButton bottomSheetMenuCollapse;
    private ProgressBar smallProgressBar, progressBar;
    private TextView streamProgress, streamDuration;
    private AppCompatSeekBar seekBar;
    private ImageView smallLogo;
    private Toast toast;
    private PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        initializeBottomSheetCallback();
        setUpInitPlayerState();
        setUpAudioStreamingServiceReceiver();


        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, new Home())
                .commit();
    }

    /**
     * Listen for broadcast events from the Audio Streaming Service and use the information to
     * resolve the player state accordingly
     */
    private void setUpAudioStreamingServiceReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String receivedState = intent.getStringExtra(MESSAGE);
                audioStreamingState = AudioStreamingService.AudioStreamingState.valueOf(receivedState);
                onAudioStreamingStateReceived(audioStreamingState);
            }
        };
    }

    private void setUpInitPlayerState() {
        //Check if AudioStreamingService is running and change the AudioStreamingState accordingly
        //Note: We Initially set it to Pause, assuming that noting is playing when we first run
        prefManager = new PrefManager(HomeActivity.this);
        if ((isMyServiceRunning(AudioStreamingService.class))) {
            audioStreamingState = AudioStreamingState.valueOf(prefManager.getStatus());
            //we only care about this if it's playing, yeah no one cares if you are dumb :) :) :)
            //TODO: Fix an ugly IllegalArgumentException thrown when the statement is unwrapped int the condition
            if (audioStreamingState == AudioStreamingState.STATUS_PLAYING)
                onAudioStreamingStateReceived(audioStreamingState);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(RESULT)
        );
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v == smallPlay || v == play) { //one button to handle both states
            if (audioStreamingState == AudioStreamingState.STATUS_PLAYING) {
                Intent intent = new Intent(HomeActivity.this, AudioStreamingService.class);
                intent.setAction(ACTION_PAUSE);
                startService(intent);
            } else if (audioStreamingState == AudioStreamingState.STATUS_PAUSED) {
                Intent intent = new Intent(HomeActivity.this, AudioStreamingService.class);
                intent.setAction(ACTION_PLAY);
                startService(intent);
            }
        }
    }

    /**
     * Morph a target Button's image property from it's present one to the drawable specified by toDrawable
     *
     * @param target
     * @param toDrawable
     */
    private void animateButtonDrawable(ImageButton target, Drawable toDrawable) {
        target.setImageDrawable(toDrawable);
        final Animatable animatable = (Animatable) target.getDrawable();
        animatable.start();
    }

    /**
     * Initialize all views by before findViewById or @Bind when using ButterKnife
     * Note: All view Initializing must be performed in this module
     */
    private void initViews() {
        layoutBottomSheet = findViewById(R.id.bottom_sheet);
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);

        smallLogo = findViewById(R.id.smallLogo);
        smallLogo.setOnClickListener(this);

        bottomSheetMenuItems = findViewById(R.id.bottomsheet_menu_items);
        bottomSheetPlaybackItems = findViewById(R.id.bottomsheet_playback_items);
        smallProgressBar = findViewById(R.id.smallProgressBar);
        progressBar = findViewById(R.id.progressBar);
        seekBar = findViewById(R.id.seekBar);
        streamProgress = findViewById(R.id.streamProgress);
        streamDuration = findViewById(R.id.streamDuration);

        smallPlay = findViewById(R.id.smallPlay);

        play = findViewById(R.id.mediacontrol_play);

        bottomSheetMenuCollapse = findViewById(R.id.bottomsheet_menu_collapse);


        smallPlay.setOnClickListener(this);

        play.setOnClickListener(this);

        seekBar.setEnabled(false);
    }

    /**
     * bottom sheet state change listener
     * We are transitioning between collapsed and settled states, well that is what we are interested in, isn't it?
     */
    public void initializeBottomSheetCallback() {
        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
//                switch (newState) {
//
//                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                performAlphaTransition(slideOffset);
                rotateCollapseButton(slideOffset);

            }
        });
    }

    /**
     * rotate the collapse button clockwise when collapsing and counter-clockwise when expanding
     *
     * @param slideOffset
     */
    private void rotateCollapseButton(float slideOffset) {
        float rotationAngle = slideOffset * -180;
        bottomSheetMenuCollapse.setRotation(rotationAngle);
        System.out.println("Angle: " + rotationAngle);
    }

    /**
     * Alpha 0 is transparent whilst 1 is visible so let's reverse the offset value obtained
     * with some basic math for the peek items while maintaining the original value for the sheet menu items
     * so that they crossfade
     **/
    private void performAlphaTransition(float slideOffset) {
        float alpha = 1 - slideOffset;
        bottomSheetPlaybackItems.setAlpha(alpha);
        bottomSheetMenuItems.setAlpha(slideOffset);
    }

    /**
     * Expand or collapse the bottom sheet based on it's current state
     */
    public void toggleBottomSheet() {
        if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    /**
     * Check if MediaPlayerService is running in the background, usually performed at first run
     * If it's running, we resolve the media player states accordingly
     *
     * @param serviceClass
     * @return
     */
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Called when a broadcast is received from the AudioStreamingService so that the
     * UI can be resolved accordingly to corresponding with the states
     *
     * @param streamingState The state of the Streaming Service (STATUS_PAUSED, STATUS_PLAYING ETC)
     */
    private void onAudioStreamingStateReceived(AudioStreamingState streamingState) {
        switch (streamingState) {
            //we are only interested in PLAYING and PAUSED/STOPPED states
            case STATUS_PLAYING:
                Log.i(DEBUG_TAG, "Media is Playing");
                findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                smallProgressBar.setVisibility(View.INVISIBLE);
                animateButtonDrawable(play, getResources().getDrawable(R.drawable.avd_play_pause));
                animateButtonDrawable(smallPlay, getResources().getDrawable(R.drawable.avd_play_pause_small));
                break;
            case STATUS_PAUSED:
                Log.i(DEBUG_TAG, "Media is Paused");
                animateButtonDrawable(play, getResources().getDrawable(R.drawable.avd_pause_play));
                animateButtonDrawable(smallPlay, getResources().getDrawable(R.drawable.avd_pause_play_small));
                break;
            case STATUS_LOADING:
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                smallProgressBar.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void onClickBottomSheetMore(View view) {
        toggleBottomSheet();
    }

    //TODO: These two methods below do not work. Clicking the arrow button does nothing in this current implementation
    //TODO: Trying to handle this in the general OnclickEventHandler doesn't seem to work. Fix it later.
    public void onClickMenuCollapse(View view) {
        toggleBottomSheet();
    }

    //TODO: Remove this ugly logic and replace it with a call to onMenuCollapse
    public void onLogoFrameClicked(View view) {
        toggleBottomSheet();
    }
}
