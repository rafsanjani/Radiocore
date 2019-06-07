package com.foreverrafs.starfm;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
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
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.crashlytics.android.Crashlytics;
import com.foreverrafs.starfm.adapter.SectionsPagerAdapter;
import com.foreverrafs.starfm.fragment.AboutFragment;
import com.foreverrafs.starfm.fragment.HomeFragment;
import com.foreverrafs.starfm.fragment.NewsFragment;
import com.foreverrafs.starfm.service.AudioStreamingService;
import com.foreverrafs.starfm.service.AudioStreamingService.AudioStreamingState;
import com.foreverrafs.starfm.util.Preference;
import com.foreverrafs.starfm.util.Tools;
import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.fabric.sdk.android.Fabric;

import static com.foreverrafs.starfm.util.Constants.ACTION_PLAY;
import static com.foreverrafs.starfm.util.Constants.ACTION_STOP;
import static com.foreverrafs.starfm.util.Constants.DEBUG_TAG;
import static com.foreverrafs.starfm.util.Constants.MESSAGE;
import static com.foreverrafs.starfm.util.Constants.PERMISSION_RECORD_AUDIO;
import static com.foreverrafs.starfm.util.Constants.RESULT;
import static com.foreverrafs.starfm.util.Constants.STATUS_STOPPED;

public class HomeActivity extends AppCompatActivity {
    ///////////////////////////////////////////////////////////////////////////////////////////////
    BroadcastReceiver audioServiceBroadcastReceiver;
    //let's assume nothing is playing when application starts
    AudioStreamingService.AudioStreamingState audioStreamingState = AudioStreamingState.STATUS_STOPPED;

    //Declare UI variables
    @BindView(R.id.bottom_sheet)
    RelativeLayout layoutBottomSheet;//, bottomSheetMenuItems;

    @BindView(R.id.bottomsheet_playback_items)
    LinearLayout bottomSheetPlaybackItems;

    @BindView(R.id.smallPlay)
    ImageButton smallPlay;

    @BindView(R.id.mediacontrol_play)
    ImageButton play;

    @BindView(R.id.view_pager)
    ViewPager viewPager;

    @BindView(R.id.tab_layout)
    TabLayout tabLayout;

    @BindView(R.id.seekBar)
    SeekBar seekBar;

    @BindView(R.id.smallLogo)
    ImageView smallLogo;

    @BindView(R.id.smallProgressBar)
    ProgressBar smallProgressBar;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.visualizer)
    BarVisualizer visualizer;


    private BottomSheetBehavior sheetBehavior;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ButterKnife.bind(this);

        setUpCrashlytics();
        initializeViews();
        setUpInitialPlayerState();
        setUpAudioStreamingServiceReceiver();
    }

    private void setUpCrashlytics() {
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
            Log.i(DEBUG_TAG, "Enabled cloud crash reporting");
        }
    }

    private void intiializeAudioVisualizer() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED) {
            requestAudioRecordingPermission();
        }
        setUpAudioVisualizer();
    }

    private void initializeToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.app_name));
        Tools.setSystemBarColor(this);
    }

    private void initializeTabComponents() {
        setupViewPager(viewPager);

        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_radio_live);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_news);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_about);

        // set icon color pre-selected
        tabLayout.getTabAt(0).getIcon().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
        tabLayout.getTabAt(1).getIcon().setColorFilter(getResources().getColor(R.color.grey_20), PorterDuff.Mode.SRC_IN);
        tabLayout.getTabAt(2).getIcon().setColorFilter(getResources().getColor(R.color.grey_20), PorterDuff.Mode.SRC_IN);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                collapseBottomSheet();
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
    private void setUpInitialPlayerState() {
        Preference preference = new Preference(HomeActivity.this);

        audioStreamingState = AudioStreamingState.valueOf(preference.getStatus());

        if (!Tools.isServiceRunning(AudioStreamingService.class, this) ||
                preference.isAutoPlayOnStart() ||
                preference.getStatus().equals(STATUS_STOPPED))
            startPlayback();

        onAudioStreamingStateReceived(audioStreamingState);
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
        if (visualizer != null)
            visualizer.release();
    }

    @OnClick({R.id.smallPlay, R.id.mediacontrol_play})
    public void onPlay() {
        if (audioStreamingState == AudioStreamingState.STATUS_PLAYING) {
            stopPlayback();
        } else if (audioStreamingState == AudioStreamingState.STATUS_STOPPED) {
            startPlayback();
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
     * Initialize all views by findViewById or @Bind when using ButterKnife
     * Note: All view Initializing must be performed in this module or it's submodules
     */
    private void initializeViews() {
        initializeTabComponents();
        initializeToolbar();
        initializeBottomSheet();

        seekBar.setEnabled(false);
    }

    private void setUpAudioVisualizer() {
        int audioSessionId = StreamPlayer.getPlayer(this).getAudioSessionId();
        try {
            if (audioSessionId != -1)
                visualizer.setAudioSessionId(audioSessionId);
        } catch (Exception exception) {
            Log.e(DEBUG_TAG, exception.getMessage());
        }

    }

    private void requestAudioRecordingPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_RECORD_AUDIO);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_RECORD_AUDIO:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    setUpAudioVisualizer();
                else
                    Log.i(DEBUG_TAG, "Permission to record audio denied. Visualizer cannot be initialized");
                break;
        }
    }

    /**
     * bottom sheet state change listener
     * We are transitioning between collapsed and settled states, well that is what we are interested in, isn't it?
     */
    public void initializeBottomSheet() {
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);

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
     * Called when a broadcast is received from the AudioStreamingService so that the
     * UI can be resolved accordingly to correspond with the states
     *
     * @param streamingState The state of the Streaming Service (STATUS_PAUSED, STATUS_PLAYING ETC)
     */
    private void onAudioStreamingStateReceived(@NonNull AudioStreamingState streamingState) {
        switch (streamingState) {
            //we are only interested in PLAYING and PAUSED/STOPPED states
            case STATUS_PLAYING:
                Log.i(DEBUG_TAG, "Media Playing");
                //findViewById(R.id.progressBar).setVisibility(View.GONE);
                smallProgressBar.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                animateButtonDrawable(play, getResources().getDrawable(R.drawable.avd_play_pause));
                animateButtonDrawable(smallPlay, getResources().getDrawable(R.drawable.avd_play_pause_small));

                intiializeAudioVisualizer();
                break;
            case STATUS_STOPPED:
                Log.i(DEBUG_TAG, "Media Stopped");
                animateButtonDrawable(play, getResources().getDrawable(R.drawable.avd_pause_play));
                animateButtonDrawable(smallPlay, getResources().getDrawable(R.drawable.avd_pause_play_small));
                smallProgressBar.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                break;
            case STATUS_LOADING:
                Log.i(DEBUG_TAG, "Media is Loading");
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                smallProgressBar.setVisibility(View.VISIBLE);
                break;
        }
    }


    private void setupViewPager(ViewPager viewPager) {
        SectionsPagerAdapter viewPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new HomeFragment(), "Live");    // index 0
        viewPagerAdapter.addFragment(new NewsFragment(), "News");   // index 1
        viewPagerAdapter.addFragment(new AboutFragment(), "About");   // index 1

        viewPager.setOffscreenPageLimit(2);
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
