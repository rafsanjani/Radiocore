package com.foreverrafs.starfm;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.foreverrafs.starfm.adapter.SectionsPagerAdapter;
import com.foreverrafs.starfm.fragment.AboutFragment;
import com.foreverrafs.starfm.fragment.HomeFragment;
import com.foreverrafs.starfm.service.AudioStreamingService;
import com.foreverrafs.starfm.service.AudioStreamingService.AudioStreamingState;
import com.foreverrafs.starfm.util.Preference;
import com.foreverrafs.starfm.util.Tools;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;

import static com.foreverrafs.starfm.util.Constants.ACTION_PLAY;
import static com.foreverrafs.starfm.util.Constants.ACTION_STOP;
import static com.foreverrafs.starfm.util.Constants.DEBUG_TAG;
import static com.foreverrafs.starfm.util.Constants.MESSAGE;
import static com.foreverrafs.starfm.util.Constants.RESULT;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    ///////////////////////////////////////////////////////////////////////////////////////////////
    BroadcastReceiver audioServiceBroadcastReceiver;
    //let's assume nothing is playing when application starts
    AudioStreamingService.AudioStreamingState audioStreamingState = AudioStreamingState.STATUS_STOPPED;


    //Declare UI variables
    private RelativeLayout layoutBottomSheet;//, bottomSheetMenuItems;
    private LinearLayout bottomSheetPlaybackItems;
    private BottomSheetBehavior sheetBehavior;
    private ImageButton smallPlay, play, bottomSheetMenuCollapse;
    private ProgressBar smallProgressBar, progressBar;
    // private TextView streamProgress, streamDuration;
    private AppCompatSeekBar seekBar;
    private ImageView smallLogo;
    private Preference preference;

    private ViewPager viewPager;
    private SectionsPagerAdapter viewPagerAdapter;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        initTabComponents();
        initToolbar();
        initializeBottomSheetCallback();
        setUpInitPlayerState();
        setUpAudioStreamingServiceReceiver();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.app_name));
        Tools.setSystemBarColor(this);
    }

    private void initTabComponents() {
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);
        setupViewPager(viewPager);

        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_radio_live);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_about);

        // set icon color pre-selected
        tabLayout.getTabAt(0).getIcon().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
        tabLayout.getTabAt(1).getIcon().setColorFilter(getResources().getColor(R.color.grey_20), PorterDuff.Mode.SRC_IN);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                collapseBottomSheet();
                // getSupportActionBar().setTitle(viewPagerAdapter.getTitle(tab.getPosition()));
                if (tab.getPosition() != 0)
                    tab.getIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getPosition() != 0)
                    tab.getIcon().setColorFilter(getResources().getColor(R.color.grey_20), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    /**
     * Listen for broadcast events from the Audio Streaming Service and use the information to
     * resolve the player state accordingly
     */
    private void setUpAudioStreamingServiceReceiver() {
        audioServiceBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String receivedState = intent.getStringExtra(MESSAGE);
                audioStreamingState = AudioStreamingService.AudioStreamingState.valueOf(receivedState);
                onAudioStreamingStateReceived(audioStreamingState);
            }
        };
    }

    /**
     * Check if AudioStreamingService is running and change the AudioStreamingState accordingly
     * Note: We Initially set it to STATUS_STOPPED, assuming that nothing is playing when we first run
     */
    private void setUpInitPlayerState() {
        preference = new Preference(HomeActivity.this);
        //SharedPreferences appSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


        if ((isMyServiceRunning(AudioStreamingService.class))) {
            audioStreamingState = AudioStreamingState.valueOf(preference.getStatus());
            //we only care about this if it's playing, yeah no one cares if you are dumb :) :) :)
            //TODO: Fix an ugly IllegalArgumentException thrown when the statement is unwrapped int the condition
            if (audioStreamingState == AudioStreamingState.STATUS_PLAYING)
                onAudioStreamingStateReceived(audioStreamingState);
            else if (preference.isAutoPlayOnStart()) {
                //service is running but nothing is playing so if this flag is set, then start playback right away
                startPlayback();

            }
        } else if (preference.isAutoPlayOnStart()/*preference.isAutoPlayOnStart()*/) {
            //service is not running, apparently nothing is playing so if this flag is set, then start playback right away
            startPlayback();

        }
    }

    private void startPlayback() {
        Intent audioServiceIntent = new Intent(HomeActivity.this, AudioStreamingService.class);
        audioServiceIntent.setAction(ACTION_PLAY);
        ContextCompat.startForegroundService(this, audioServiceIntent);
    }

    private void stopPlayback() {
        Intent audioServiceIntent = new Intent(HomeActivity.this, AudioStreamingService.class);
        audioServiceIntent.setAction(ACTION_STOP);
        ContextCompat.startForegroundService(this, audioServiceIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((audioServiceBroadcastReceiver),
                new IntentFilter(RESULT)
        );
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(audioServiceBroadcastReceiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        //Intent audioServiceIntent = new Intent(HomeActivity.this, AudioStreamingService.class);
        //one button to handle both states, stop when playing, play when stopped. Pretty cool huh :) :)
        if (v == smallPlay || v == play) {
            if (audioStreamingState == AudioStreamingState.STATUS_PLAYING) {
                stopPlayback();
            } else if (audioStreamingState == AudioStreamingState.STATUS_STOPPED) {
                startPlayback();
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

        bottomSheetPlaybackItems = findViewById(R.id.bottomsheet_playback_items);
        smallProgressBar = findViewById(R.id.smallProgressBar);
        progressBar = findViewById(R.id.progressBar);
        seekBar = findViewById(R.id.seekBar);


        smallPlay = findViewById(R.id.smallPlay);

        play = findViewById(R.id.mediacontrol_play);

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
                AppBarLayout appBarLayout = findViewById(R.id.appbar);

                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    appBarLayout.setExpanded(false, true);
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    appBarLayout.setExpanded(true, true);
                }
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
        //bottomSheetMenuCollapse.setRotation(rotationAngle);
        //System.out.println("Angle: " + rotationAngle);
    }

    /**
     * Alpha 0 is transparent whilst 1 is visible so let's reverse the offset value obtained
     * with some basic math for the peek items while maintaining the original value for the sheet menu items
     * so that they crossfade
     **/
    private void performAlphaTransition(float slideOffset) {
        float alpha = 1 - slideOffset;
        bottomSheetPlaybackItems.setAlpha(alpha);
        //bottomSheetMenuItems.setAlpha(slideOffset);
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
     * Explicitly collapse the bottom sheet
     */
    public void collapseBottomSheet() {
        if (sheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED)
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
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
    private void onAudioStreamingStateReceived(@NonNull AudioStreamingState streamingState) {
        switch (streamingState) {
            //we are only interested in PLAYING and PAUSED/STOPPED states
            case STATUS_PLAYING:
                Log.i(DEBUG_TAG, "Media Playing");
                findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                smallProgressBar.setVisibility(View.INVISIBLE);
                animateButtonDrawable(play, getResources().getDrawable(R.drawable.avd_play_pause));
                animateButtonDrawable(smallPlay, getResources().getDrawable(R.drawable.avd_play_pause_small));
                break;
            case STATUS_STOPPED:
                Log.i(DEBUG_TAG, "Media Stopped");
                animateButtonDrawable(play, getResources().getDrawable(R.drawable.avd_pause_play));
                animateButtonDrawable(smallPlay, getResources().getDrawable(R.drawable.avd_pause_play_small));
                break;
            case STATUS_LOADING:
                Log.i(DEBUG_TAG, "Media is Loading");
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                smallProgressBar.setVisibility(View.VISIBLE);
                break;
        }
    }

//    public void onClickBottomSheetMore(View view) {
//        toggleBottomSheet();
//    }
//
//    //TODO: These two methods below do not work. Clicking the arrow button does nothing in this current implementation
//    //TODO: Trying to handle this in the general OnclickEventHandler doesn't seem to work. Fix it later.
//    public void onClickMenuCollapse(View view) {
//        toggleBottomSheet();
//    }

    //TODO: Remove this ugly logic and replace it with a call to onMenuCollapse
    public void onLogoFrameClicked(View view) {
        toggleBottomSheet();
    }


    private void setupViewPager(ViewPager viewPager) {
        viewPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new HomeFragment(), "Live");    // index 0
        viewPagerAdapter.addFragment(new AboutFragment(), "About");   // index 1
        viewPager.setAdapter(viewPagerAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void onSettings(MenuItem item) {
        startActivity(new Intent(this, SettingsActivity.class));

    }
}
