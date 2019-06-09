package com.foreverrafs.starfm.activity;

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
import android.os.Handler;
import android.os.Looper;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.crashlytics.android.Crashlytics;
import com.foreverrafs.starfm.BuildConfig;
import com.foreverrafs.starfm.R;
import com.foreverrafs.starfm.StreamPlayer;
import com.foreverrafs.starfm.adapter.SectionsPagerAdapter;
import com.foreverrafs.starfm.fragment.AboutFragment;
import com.foreverrafs.starfm.fragment.HomeFragment;
import com.foreverrafs.starfm.fragment.NewsFragment;
import com.foreverrafs.starfm.service.AudioStreamingService;
import com.foreverrafs.starfm.service.AudioStreamingService.AudioStreamingState;
import com.foreverrafs.starfm.util.RadioPreferences;
import com.foreverrafs.starfm.util.Tools;
import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;

import org.joda.time.Period;
import org.joda.time.Seconds;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.fabric.sdk.android.Fabric;

import static com.foreverrafs.starfm.util.Constants.ACTION_PLAY;
import static com.foreverrafs.starfm.util.Constants.ACTION_STOP;
import static com.foreverrafs.starfm.util.Constants.DEBUG_TAG;
import static com.foreverrafs.starfm.util.Constants.MESSAGE;
import static com.foreverrafs.starfm.util.Constants.RESULT;
import static com.foreverrafs.starfm.util.Constants.STATUS_STOPPED;

public class HomeActivity extends AppCompatActivity {
    private final int PERMISSION_RECORD_AUDIO = 6900;
    ///////////////////////////////////////////////////////////////////////////////////////////////
    BroadcastReceiver audioServiceBroadcastReceiver;
    //let's assume nothing is playing when application starts
    AudioStreamingService.AudioStreamingState audioStreamingState = AudioStreamingState.STATUS_STOPPED;

    //Declare UI variables
    @BindView(R.id.bottom_sheet)
    RelativeLayout layoutBottomSheet;

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

    @BindView(R.id.seekbar_streamprogress)
    SeekBar seekBarProgress;

    @BindView(R.id.smallLogo)
    ImageView smallLogo;

    @BindView(R.id.smallProgressBar)
    ProgressBar smallProgressBar;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.visualizer)
    BarVisualizer visualizer;

    @BindView(R.id.text_stream_duration)
    TextView textStreamDuration;

    @BindView(R.id.text_stream_progress)
    TextView textStreamProgress;


    //Radio settings
    RadioPreferences radioPreferences;
    private BottomSheetBehavior sheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        radioPreferences = new RadioPreferences(this);

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
     * Check if Audio Streaming Service is running and change the AudioStreamingState accordingly
     * Note: We Initially set it to STATUS_STOPPED, assuming that nothing is playing when we first run
     */
    private void setUpInitialPlayerState() {
        RadioPreferences radioPreferences = new RadioPreferences(this);

        audioStreamingState = AudioStreamingState.valueOf(radioPreferences.getStatus());

        if (!Tools.isServiceRunning(AudioStreamingService.class, this) ||
                radioPreferences.isAutoPlayOnStart() ||
                radioPreferences.getStatus().equals(STATUS_STOPPED))
            startPlayback();


        onAudioStreamingStateReceived(audioStreamingState);
    }

    private void startPlayback() {
        // RadioPreferences radioPreferences = new RadioPreferences(this);

        Intent audioServiceIntent = new Intent(HomeActivity.this, AudioStreamingService.class);
        audioServiceIntent.setAction(ACTION_PLAY);
        ContextCompat.startForegroundService(this, audioServiceIntent);

        startUpdateStreamProgress();
    }

    private void startUpdateStreamProgress() {
        //convert streaming timer to seconds
        seekBarProgress.setMax(radioPreferences.getStreamingTimer() * 3600);

        Handler mHandler = new Handler(Looper.getMainLooper());

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Seconds streamDurationHrs = Seconds.seconds(radioPreferences.getStreamingTimer() * 3600);
                Seconds currentPosition = Seconds.seconds((int) StreamPlayer.getPlayer(getApplicationContext()).getCurrentPosition() / 1000);


                Period streamDurationPeriod = new Period(streamDurationHrs);
                Period currentPositionPeriod = new Period(currentPosition);


                PeriodFormatter formatter = new PeriodFormatterBuilder()
                        .printZeroAlways()
                        .minimumPrintedDigits(2)
                        .appendHours()
                        .appendSuffix(":")
                        .appendMinutes()
                        .appendSuffix(":")
                        .appendSeconds()
                        .toFormatter();

                String totalStreamStr = formatter.print(streamDurationPeriod.normalizedStandard());
                String streamProgressStr = formatter.print(currentPositionPeriod.normalizedStandard());

                textStreamDuration.setText(totalStreamStr);
                textStreamProgress.setText(streamProgressStr);


                if (StreamPlayer.getPlayer(getApplicationContext()) != null) {
                    if (seekBarProgress != null)
                        seekBarProgress.setProgress(currentPosition.getSeconds());

                }
                mHandler.postDelayed(this, 1000);
            }
        });
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

        if (audioStreamingState == AudioStreamingState.STATUS_PLAYING)
            startUpdateStreamProgress();
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

        seekBarProgress.setEnabled(false);
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
        if (requestCode == PERMISSION_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                setUpAudioVisualizer();
            else
                Log.i(DEBUG_TAG, "Permission to record audio denied. Visualizer cannot be initialized");
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
            case STATUS_PLAYING:
                Log.i(DEBUG_TAG, "Media Playing");

                Tools.toggleViewsVisibility(View.INVISIBLE, smallProgressBar, progressBar);

                animateButtonDrawable(play, getResources().getDrawable(R.drawable.avd_play_pause));
                animateButtonDrawable(smallPlay, getResources().getDrawable(R.drawable.avd_play_pause_small));

                intiializeAudioVisualizer();
                break;
            case STATUS_STOPPED:
                Log.i(DEBUG_TAG, "Media Stopped");
                animateButtonDrawable(play, getResources().getDrawable(R.drawable.avd_pause_play));
                animateButtonDrawable(smallPlay, getResources().getDrawable(R.drawable.avd_pause_play_small));

                Tools.toggleViewsVisibility(View.INVISIBLE, smallProgressBar, progressBar);

                break;
            case STATUS_LOADING:
                Log.i(DEBUG_TAG, "Media is Loading");

                Tools.toggleViewsVisibility(View.VISIBLE, smallProgressBar, progressBar);
                break;
        }
    }


    private void setupViewPager(ViewPager viewPager) {
        SectionsPagerAdapter viewPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new HomeFragment(), "Live");    // index 0
        viewPagerAdapter.addFragment(new NewsFragment(), "News");   // index 1
        viewPagerAdapter.addFragment(new AboutFragment(), "About");   // index 1

//        viewPager.setOffscreenPageLimit(2);
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
