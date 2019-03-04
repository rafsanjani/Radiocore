package com.emperor95online.ashhfm;

import android.animation.Animator;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private RelativeLayout layoutBottomSheet, menuItems;
    private LinearLayout items;
    private BottomSheetBehavior sheetBehavior;

    private ImageButton smallPlay, smallPause;
    private ImageButton play, pause;
    private ImageButton collapseSheet, more_main;
    private ProgressBar smallProgressBar, progressBar;

    private TextView streamProgress, streamDuration;
    private AppCompatSeekBar seekBar;

    //
    private MediaPlayer mediaPlayer;
    private final String audioStreamUrl = "http://stream.zenolive.com/urp3bkvway5tv.aac?15474";

    private int duration = 0;
    private int currentProgress = 0;

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

        layoutBottomSheet = findViewById(R.id.bottom_sheet);
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);
        bottomSheetCallback();

        menuItems = findViewById(R.id.menuItems);
        items = findViewById(R.id.items);
        smallProgressBar = findViewById(R.id.smallProgressBar);
        progressBar = findViewById(R.id.progressBar);
        seekBar = findViewById(R.id.seekBar);
        streamProgress = findViewById(R.id.streamProgress);
        streamDuration = findViewById(R.id.streamDuration);
        more_main = findViewById(R.id.more_main);

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
        more_main.setOnClickListener(this);

        seekBar.setEnabled(false);

    }

    @Override
    protected void onStop() {
        super.onStop();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    @Override
    public void onClick(View v) {
        if (v == smallPlay) {
            playStream(smallProgressBar, smallPlay, smallPause);
        }
        if (v == play) {
            playStream(progressBar, play, pause);
        }
        if (v == smallPause) {
            pauseStream(smallPlay, smallPause);
        }
        if (v == pause) {
            pauseStream(play, pause);
        }
        if (v == collapseSheet){
            toggleBottomSheet();
        }
        if (v == more_main) {
            showPopupMenu(more_main);
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
                        if (mediaPlayer.isPlaying()) {
                            play.setVisibility(View.GONE);
                            pause.setVisibility(View.VISIBLE);
                        } else if (!mediaPlayer.isPlaying()) {
                            play.setVisibility(View.VISIBLE);
                            pause.setVisibility(View.GONE);
                        }

//                        if (mediaPlayer.isPlaying()) {
//
//                        }

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
                        if (mediaPlayer.isPlaying()) {
                            smallPlay.setVisibility(View.GONE);
                            smallPause.setVisibility(View.VISIBLE);
                        } else if (!mediaPlayer.isPlaying()) {
                            smallPlay.setVisibility(View.VISIBLE);
                            smallPause.setVisibility(View.GONE);
                        }
//                        Toast.makeText(HomeActivity.this, "Closed", Toast.LENGTH_SHORT).show();
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

    /**
     * manually opening / closing bottom sheet on button click
     */
    public void toggleBottomSheet() {
        if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }
    private void showPopupMenu(final View view){
        // inflate menu
        PopupMenu popupMenu = new PopupMenu(getApplicationContext(), view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.main_popup, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()){
                    case R.id.staff:
                        return true;
                    case R.id.about_station:
                        return true;
                    case R.id.privacy_policy:
                        return true;
                }

                return false;
            }
        });
        popupMenu.show();
    }
}
