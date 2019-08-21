package com.foreverrafs.radiocore.activity

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.ViewPager
import com.crashlytics.android.Crashlytics
import com.foreverrafs.radiocore.BuildConfig
import com.foreverrafs.radiocore.R
import com.foreverrafs.radiocore.StreamPlayer
import com.foreverrafs.radiocore.adapter.HomeSectionsPagerAdapter
import com.foreverrafs.radiocore.concurrency.CustomObserver
import com.foreverrafs.radiocore.fragment.AboutFragment
import com.foreverrafs.radiocore.fragment.HomeFragment
import com.foreverrafs.radiocore.fragment.NewsFragment
import com.foreverrafs.radiocore.service.AudioStreamingService
import com.foreverrafs.radiocore.service.AudioStreamingService.AudioStreamingState
import com.foreverrafs.radiocore.util.Constants
import com.foreverrafs.radiocore.util.RadioPreferences
import com.foreverrafs.radiocore.util.Tools
import com.foreverrafs.radiocore.util.Tools.animateButtonDrawable
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import io.fabric.sdk.android.Fabric
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.bottom_sheet.*

class HomeActivity : AppCompatActivity(), View.OnClickListener {
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnSmallPlay, R.id.btnPlay -> {
                Log.i(TAG, "onPlay: " + audioStreamingState.name)
                when (audioStreamingState) {
                    AudioStreamingState.STATUS_PLAYING -> pausePlayback()
                    AudioStreamingState.STATUS_PAUSED,
                    AudioStreamingState.STATUS_STOPPED -> startPlayback()
                    AudioStreamingState.STATUS_LOADING -> TODO()
                }
//                if (audioStreamingState == AudioStreamingState.STATUS_PLAYING) {
//                    pausePlayback()
//                } else if (audioStreamingState == AudioStreamingState.STATUS_STOPPED
//                        || audioStreamingState == AudioStreamingState.STATUS_PAUSED) {
//                    startPlayback()
//                }

            }
        }
    }


    private val TAG = "HomeActivity"
    private val PERMISSION_RECORD_AUDIO = 6900
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private lateinit var audioServiceBroadcastReceiver: BroadcastReceiver
    //let's assume nothing is playing when application starts
    internal var audioStreamingState: AudioStreamingState = AudioStreamingState.STATUS_STOPPED
    //Declare UI variables


    //bottom sheet
    private var sheetBehavior: BottomSheetBehavior<*>? = null
    private var mCompositeDisposable: CompositeDisposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        btnSmallPlay.setOnClickListener(this)
        btnPlay.setOnClickListener(this)

        mCompositeDisposable = CompositeDisposable()

        setUpCrashlytics()
        initializeViews()
        setUpInitialPlayerState()
        setUpAudioStreamingServiceReceiver()
    }

    private fun setUpCrashlytics() {
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, Crashlytics())
            Log.i(TAG, "setUpCrashlytics: Enabled")
        }
    }

    private fun intiializeAudioVisualizer() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestAudioRecordingPermission()
        }

        setUpAudioVisualizer()
    }

    private fun initializeToolbar() {
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.title = getString(R.string.app_name)
            Tools.setSystemBarColor(this)
        }
    }

    private fun initializeTabComponents() {
        setupViewPager(viewPager!!)

        tabLayout.setupWithViewPager(viewPager)

        tabLayout.getTabAt(0)!!.setIcon(R.drawable.ic_radio_live)
        tabLayout.getTabAt(1)!!.setIcon(R.drawable.ic_news)
        tabLayout.getTabAt(2)!!.setIcon(R.drawable.ic_about)

        // set icon color pre-selected
        tabLayout.getTabAt(0)!!.icon!!.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
        tabLayout.getTabAt(1)!!.icon!!.setColorFilter(resources.getColor(R.color.grey_20), PorterDuff.Mode.SRC_IN)
        tabLayout.getTabAt(2)!!.icon!!.setColorFilter(resources.getColor(R.color.grey_20), PorterDuff.Mode.SRC_IN)

        tabLayout!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                collapseBottomSheet()
                if (tab.position != 0)
                    tab.icon!!.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                if (tab.position != 0)
                    tab.icon!!.setColorFilter(resources.getColor(R.color.grey_20), PorterDuff.Mode.SRC_IN)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })
    }

    /**
     * Listen for broadcast events from the Audio Streaming Service and use the information to
     * resolve the player state accordingly
     */
    private fun setUpAudioStreamingServiceReceiver() {
        audioServiceBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val receivedState = intent.getStringExtra(Constants.STREAMING_STATUS)
                audioStreamingState = AudioStreamingState.valueOf(receivedState!!)
                onAudioStreamingStateReceived(audioStreamingState)
            }
        }
    }

    /**
     * Check if Audio Streaming Service is running and change the AudioStreamingState accordingly
     * Note: We Initially set it to STATUS_STOPPED, assuming that nothing is playing when we first run
     */
    private fun setUpInitialPlayerState() {
        val radioPreferences = RadioPreferences(this)

        audioStreamingState = AudioStreamingState.valueOf(radioPreferences.status!!)

        if (!Tools.isServiceRunning(AudioStreamingService::class.java, this) || radioPreferences.isAutoPlayOnStart/* ||
                radioPreferences.getStatus().equals(STATUS_STOPPED)*/)
            startPlayback()

        onAudioStreamingStateReceived(audioStreamingState)
    }

    /**
     * Update the stream progress seekbar and timer accordingly.
     * Also checks if the stream timer is up which triggers a shutdown of the app
     */
    private fun startUpdateStreamProgress() {
        Log.d(TAG, "startUpdateStreamProgress: ")
        StreamPlayer.getInstance(this).streamDurationStringsObservable
                .subscribe(object : CustomObserver<Array<out String?>>() {
                    override fun onSubscribe(d: Disposable) {
                        mCompositeDisposable?.add(d)
                    }

                    override fun onNext(strings: Array<out String?>) {
                        textStreamProgress?.text = strings[1]
                        textStreamDuration?.text = strings[0]
                    }
                })
    }

    private fun startPlayback() {
        val audioServiceIntent = Intent(this, AudioStreamingService::class.java)
        audioServiceIntent.action = Constants.ACTION_PLAY
        ContextCompat.startForegroundService(this, audioServiceIntent)
    }

    private fun pausePlayback() {
        val audioServiceIntent = Intent(this, AudioStreamingService::class.java)
        audioServiceIntent.action = Constants.ACTION_PAUSE
        ContextCompat.startForegroundService(this, audioServiceIntent)
    }


    override fun onStop() {
        super.onStop()
        Log.i(TAG, "onStop: ")
        LocalBroadcastManager.getInstance(this).unregisterReceiver(audioServiceBroadcastReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()

        visualizer?.release()

        mCompositeDisposable?.clear()
    }


    /**
     * Initialize all views by findViewById or @Bind when using ButterKnife
     * Note: All view Initializing must be performed in this module or it's submodules
     */
    private fun initializeViews() {
        val textAnimationIn = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left)

        val textAnimationOut = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right)

        textSwitcherPlayerState.inAnimation = textAnimationIn
        textSwitcherPlayerState.outAnimation = textAnimationOut

        initializeTabComponents()
        initializeToolbar()
        initializeBottomSheet()

        seekBarProgress.isEnabled = false
    }

    private fun setUpAudioVisualizer() {
        val audioSessionId = StreamPlayer.getInstance(applicationContext).audioSessionId
        try {
            if (audioSessionId != -1)
                visualizer?.setAudioSessionId(audioSessionId)
        } catch (exception: Exception) {
            Log.e(TAG, "setUpAudioVisualizer: " + exception.message)
        }

    }

    private fun requestAudioRecordingPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSION_RECORD_AUDIO)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_RECORD_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                setUpAudioVisualizer()
            else
                Log.i(TAG, "onRequestPermissionsResult: Denied. Unable to initialize visualizer")
        }
    }

    /**
     * bottom sheet state change listener
     * We are transitioning between collapsed and settled states, well that is what we are interested in, isn't it?
     */
    private fun initializeBottomSheet() {
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet!!)

        sheetBehavior!!.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                val appBarLayout = findViewById<AppBarLayout>(R.id.appbar)

                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    appBarLayout.setExpanded(false, true)
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    appBarLayout.setExpanded(true, true)
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                performAlphaTransition(slideOffset)
                rotateSmallLogo(slideOffset)
            }
        })
    }

    /**
     * rotate the collapse button clockwise when collapsing and counter-clockwise when expanding
     *
     * @param slideOffset the initial angle where rotation begins
     */
    private fun rotateSmallLogo(slideOffset: Float) {
        val rotationAngle = slideOffset * -360
        smallLogo!!.rotation = rotationAngle
    }

    /**
     * Alpha 0 is transparent whilst 1 is visible so let's reverse the offset value obtained
     * with some basic math for the peek items while maintaining the original value for the sheet menu items
     * so that they crossfade
     */
    private fun performAlphaTransition(slideOffset: Float) {
        val alpha = 1 - slideOffset
        bottomSheetPlaybackItems!!.alpha = alpha
    }

    /**
     * Explicitly collapse the bottom sheet
     */
    fun collapseBottomSheet() {
        if (sheetBehavior?.state != BottomSheetBehavior.STATE_COLLAPSED)
            sheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    /**
     * Called when a broadcast is received from the AudioStreamingService so that the
     * UI can be resolved accordingly to correspond with the states
     *
     * @param streamingState The state of the Streaming Service (STATUS_PAUSED, STATUS_PLAYING ETC)
     */
    private fun onAudioStreamingStateReceived(streamingState: AudioStreamingState) {
        when (streamingState) {
            AudioStreamingState.STATUS_PLAYING -> {
                Log.d(TAG, "onAudioStreamingStateReceived: Playing")
                Tools.toggleViewsVisibility(View.INVISIBLE, smallProgressBar, progressBar)

                animateButtonDrawable(btnPlay, resources.getDrawable(R.drawable.avd_play_pause))
                animateButtonDrawable(btnSmallPlay, resources.getDrawable(R.drawable.avd_play_pause_small))

                intiializeAudioVisualizer()

                //start updating seekbar when something is actually playing
                startUpdateStreamProgress()
                textSwitcherPlayerState?.setText(getString(R.string.state_live))
                (textSwitcherPlayerState.currentView as TextView).setTextColor(resources.getColor(R.color.green_200))
            }
            AudioStreamingState.STATUS_STOPPED -> {
                animateButtonDrawable(btnPlay, resources.getDrawable(R.drawable.avd_pause_play))
                animateButtonDrawable(btnSmallPlay, resources.getDrawable(R.drawable.avd_pause_play_small))

                Tools.toggleViewsVisibility(View.INVISIBLE, smallProgressBar, progressBar)
                textSwitcherPlayerState.setText(getString(R.string.state_stopped))
                (textSwitcherPlayerState.currentView as TextView).setTextColor(resources.getColor(R.color.pink_600))
            }
            AudioStreamingState.STATUS_LOADING -> {
                textSwitcherPlayerState.setText(getString(R.string.state_buffering))
                (textSwitcherPlayerState.currentView as TextView).setTextColor(resources.getColor(R.color.pink_200))
                Tools.toggleViewsVisibility(View.VISIBLE, smallProgressBar, progressBar)
            }

            AudioStreamingState.STATUS_PAUSED -> {
                animateButtonDrawable(btnPlay, resources.getDrawable(R.drawable.avd_pause_play))
                animateButtonDrawable(btnSmallPlay, resources.getDrawable(R.drawable.avd_pause_play_small))

                textSwitcherPlayerState.setText(getString(R.string.state_paused))
                (textSwitcherPlayerState.currentView as TextView).setTextColor(resources.getColor(R.color.yellow_400))
                Tools.toggleViewsVisibility(View.INVISIBLE, smallProgressBar, progressBar)
            }
        }
    }


    private fun setupViewPager(viewPager: ViewPager) {
        val viewPagerAdapter = HomeSectionsPagerAdapter(supportFragmentManager)
        viewPagerAdapter.addFragment(HomeFragment(), "Live")    // index 0
        viewPagerAdapter.addFragment(NewsFragment(), "News")   // index 1
        viewPagerAdapter.addFragment(AboutFragment(), "About")   // index 2

        viewPager.adapter = viewPagerAdapter
    }

    override fun onResume() {
        Log.i(TAG, "onResume: ")
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(audioServiceBroadcastReceiver,
                IntentFilter(Constants.STREAM_RESULT)
        )

        //        if (audioStreamingState == AudioStreamingState.STATUS_PLAYING)
        //            startUpdateStreamProgress();
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> startActivity(Intent(this, SettingsActivity::class.java))
        }

        return super.onOptionsItemSelected(item)
    }
}
