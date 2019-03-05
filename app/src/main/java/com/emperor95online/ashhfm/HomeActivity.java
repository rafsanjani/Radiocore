package com.emperor95online.ashhfm;

import android.animation.Animator;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSeekBar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.emperor95online.ashhfm.fragment.Home;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.emperor95online.ashhfm.Constants.LOADING;
import static com.emperor95online.ashhfm.Constants.MESSAGE;
import static com.emperor95online.ashhfm.Constants.PAUSED;
import static com.emperor95online.ashhfm.Constants.PLAYING;
import static com.emperor95online.ashhfm.Constants.RESULT;
import static com.emperor95online.ashhfm.Constants.STATUS_PAUSED;
import static com.emperor95online.ashhfm.Constants.STATUS_PLAYING;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private RelativeLayout layoutBottomSheet, menuItems;
    private LinearLayout items;
    private BottomSheetBehavior sheetBehavior;

    private ImageButton smallPlay, smallPause;
    private ImageButton play, pause;
    private ImageButton collapseSheet;
    private ProgressBar smallProgressBar, progressBar;

    private TextView streamProgress, streamDuration;
    private AppCompatSeekBar seekBar;
    private ImageView smallLogo;

    private Toast toast;
    private PrefManager prefManager;

    //
    private MediaPlayer mediaPlayer;
    private final String audioStreamUrl = "http://stream.zenolive.com/urp3bkvway5tv.aac?15474";

    private int duration = 0;
    private int currentProgress = 0;
    private String status = "";

    //////////////////////////////////////////
    BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try{
            mediaPlayer.setDataSource(audioStreamUrl);
        }catch (IOException e){
            Toast.makeText(HomeActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }

        initViews();
        prefManager = new PrefManager(HomeActivity.this);
        if((isMyServiceRunning(NewService.class))){
            // service is running ..
            resolveStates();
        }

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String s = intent.getStringExtra(MESSAGE);
                // do something here.
                status = s;
                setupReceiver(s);
            }
        };

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, new Home())
                .commit();

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
        mediaPlayer.release();
        mediaPlayer = null;
    }

    @Override
    public void onClick(View v) {
        if (v == smallPlay || v == play) {
//            playStream(smallProgressBar, smallPlay, smallPause);
            Intent intent = new Intent(HomeActivity.this, NewService.class);
            intent.setAction("com.emperor95Online.ashhfm.PLAY");
            startService(intent);
        }
//        if (v == play) {
//            playStream(progressBar, play, pause);
//        }
        if (v == smallPause || v == pause) {
//            pauseStream(smallPlay, smallPause);
            Intent intent = new Intent(HomeActivity.this, NewService.class);
            intent.setAction("com.emperor95Online.ashhfm.PAUSE");
            startService(intent);
        }
//        if (v == pause) {
//            pauseStream(play, pause);
//        }
        if (v == collapseSheet || v == smallLogo){
            toggleBottomSheet();
        }
//        if (v == smallLogo) {
//            toggleBottomSheet();
//        }
    }

    private void initViews(){
        layoutBottomSheet = findViewById(R.id.bottom_sheet);
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);
        bottomSheetCallback();

        smallLogo = findViewById(R.id.smallLogo);
        smallLogo.setOnClickListener(this);

        menuItems = findViewById(R.id.menuItems);
        items = findViewById(R.id.items);
        smallProgressBar = findViewById(R.id.smallProgressBar);
        progressBar = findViewById(R.id.progressBar);
        seekBar = findViewById(R.id.seekBar);
        streamProgress = findViewById(R.id.streamProgress);
        streamDuration = findViewById(R.id.streamDuration);

        smallPlay = findViewById(R.id.smallPlay);
        smallPause = findViewById(R.id.smallPause);
        pause = findViewById(R.id.pause);
        play = findViewById(R.id.play);
        collapseSheet = findViewById(R.id.collapseSheet);

        smallPlay.setOnClickListener(this);
        smallPause.setOnClickListener(this);
        pause.setOnClickListener(this);
        play.setOnClickListener(this);
        collapseSheet.setOnClickListener(this);

        seekBar.setEnabled(false);
    }

    private void resolveStates(){
        if (TextUtils.equals(STATUS_PLAYING, prefManager.getStatus())) {
            findViewById(R.id.smallPause).setVisibility(View.VISIBLE);
            findViewById(R.id.smallPlay).setVisibility(View.GONE);
            findViewById(R.id.pause).setVisibility(View.VISIBLE);
            findViewById(R.id.play).setVisibility(View.GONE);
        } else if (TextUtils.equals(STATUS_PAUSED, prefManager.getStatus())) {
            findViewById(R.id.smallPause).setVisibility(View.GONE);
            findViewById(R.id.smallPlay).setVisibility(View.VISIBLE);
            findViewById(R.id.pause).setVisibility(View.GONE);
            findViewById(R.id.play).setVisibility(View.VISIBLE);
        }
    }

    public void pauseStream(ImageButton play, ImageButton pause){
        mediaPlayer.stop();
        play.setVisibility(View.VISIBLE);
        pause.setVisibility(View.GONE);
    }
    public void playStream(final ProgressBar progressBar, final ImageButton play, final ImageButton pause){
        final Toast toast = Toast.makeText(HomeActivity.this, "Loading ...", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

        progressBar.setVisibility(View.VISIBLE);
        mediaPlayer.prepareAsync();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                toast.cancel();
                progressBar.setVisibility(View.GONE);
                pause.setVisibility(View.VISIBLE);
                play.setVisibility(View.GONE);
                mediaPlayer.start();

                duration = mediaPlayer.getDuration();
                currentProgress = mediaPlayer.getCurrentPosition();

                streamProgress.setText(String.format("%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes((long) currentProgress),
                        TimeUnit.MILLISECONDS.toSeconds((long) currentProgress) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                        currentProgress))));

                streamDuration.setText(String.format("%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes((long) duration),
                        TimeUnit.MILLISECONDS.toSeconds((long) duration) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                        duration))));

                seekBar.setMax(duration);
                seekBar.setProgress(currentProgress);
            }
        });

    }



    public void bottomSheetCallback(){
        /**
         * bottom sheet state change listener
         * we are changing button text when sheet changed state
         * */
        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
//                        Toast.makeText(HomeActivity.this, "Hidden", Toast.LENGTH_SHORT).show();
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED: {
                        items.animate()
                                .translationY(items.getHeight())
//                                .alpha(0.0f)
                                .setDuration(300)
                                .setListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        menuItems.setVisibility(View.VISIBLE);
                                        items.setVisibility(View.GONE);
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animation) {

                                    }
                                });
//                        if (mediaPlayer.isPlaying()) {
//                            play.setVisibility(View.GONE);
//                            pause.setVisibility(View.VISIBLE);
//                        } else if (!mediaPlayer.isPlaying()) {
//                            play.setVisibility(View.VISIBLE);
//                            pause.setVisibility(View.GONE);
//                        }
                        setupReceiver(status);

                    }
                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        items.animate()
                                .translationY(0)
//                                .alpha(1.0f)
                                .setDuration(300)
                                .setListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        menuItems.setVisibility(View.GONE);
                                        items.setVisibility(View.VISIBLE);
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animation) {

                                    }
                                });
//                        if (mediaPlayer.isPlaying()) {
//                            smallPlay.setVisibility(View.GONE);
//                            smallPause.setVisibility(View.VISIBLE);
//                        } else if (!mediaPlayer.isPlaying()) {
//                            smallPlay.setVisibility(View.VISIBLE);
//                            smallPause.setVisibility(View.GONE);
//                        }
                        setupReceiver(status);
                    }
                    break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }
    public void toggleBottomSheet() {
        if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }
    private boolean isMyServiceRunning(Class<?> serviceClass){
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if(serviceClass.getName().equals(service.service.getClassName())){
                return true;
            }
        }
        return false;
    }
    private void setupReceiver(String s){
        if (TextUtils.equals(s, PLAYING)){
            findViewById(R.id.smallPause).setVisibility(View.VISIBLE);
            findViewById(R.id.smallPlay).setVisibility(View.GONE);

            findViewById(R.id.pause).setVisibility(View.VISIBLE);
            findViewById(R.id.play).setVisibility(View.GONE);

            findViewById(R.id.smallProgressBar).setVisibility(View.GONE);
            findViewById(R.id.progressBar).setVisibility(View.GONE);
            toast.cancel();
        }
        else if (TextUtils.equals(s, PAUSED)){
            findViewById(R.id.smallPause).setVisibility(View.GONE);
            findViewById(R.id.smallPlay).setVisibility(View.VISIBLE);

            findViewById(R.id.pause).setVisibility(View.GONE);
            findViewById(R.id.play).setVisibility(View.VISIBLE);
        }
        else if (TextUtils.equals(s, LOADING)) {
            findViewById(R.id.smallProgressBar).setVisibility(View.VISIBLE);

            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            toast = Toast.makeText(HomeActivity.this, "Loading ...", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }
}
