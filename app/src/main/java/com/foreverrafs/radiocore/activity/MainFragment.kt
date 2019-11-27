package com.foreverrafs.radiocore.activity

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.widget.ViewPager2
import com.foreverrafs.radiocore.R
import com.foreverrafs.radiocore.adapter.HomeSectionsPagerAdapter
import com.foreverrafs.radiocore.concurrency.SimpleObserver
import com.foreverrafs.radiocore.fragment.AboutFragment
import com.foreverrafs.radiocore.fragment.HomeFragment
import com.foreverrafs.radiocore.fragment.NewsListFragment
import com.foreverrafs.radiocore.player.StreamPlayer
import com.foreverrafs.radiocore.service.AudioStreamingService
import com.foreverrafs.radiocore.service.AudioStreamingService.AudioStreamingState
import com.foreverrafs.radiocore.util.Constants
import com.foreverrafs.radiocore.util.Constants.STREAM_RESULT
import com.foreverrafs.radiocore.util.RadioPreferences
import com.foreverrafs.radiocore.util.Tools
import com.foreverrafs.radiocore.util.Tools.animateButtonDrawable
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.bottom_sheet.*
import org.joda.time.Seconds
import timber.log.Timber


class MainFragment : Fragment(), View.OnClickListener {
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnSmallPlay, R.id.btnPlay -> {
                Timber.i("onPlay: ${mAudioStreamingState.name}")

                when (mAudioStreamingState) {
                    AudioStreamingState.STATUS_PLAYING -> pausePlayback()
                    AudioStreamingState.STATUS_PAUSED,
                    AudioStreamingState.STATUS_STOPPED -> startPlayback()
                    AudioStreamingState.STATUS_LOADING -> Timber.d("Loading")
                }
            }
        }
    }

    private val PERMISSION_RECORD_AUDIO = 6900
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private lateinit var mAudioServiceBroadcastReceiver: BroadcastReceiver
    //let's assume nothing is playing when application starts
    private var mAudioStreamingState: AudioStreamingState = AudioStreamingState.STATUS_STOPPED


    private var mSheetBehaviour: BottomSheetBehavior<*>? = null
    private var mCompositeDisposable: CompositeDisposable? = null
    private lateinit var mStreamPlayer: StreamPlayer

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        btnSmallPlay.setOnClickListener(this)
        btnPlay.setOnClickListener(this)

        mCompositeDisposable = CompositeDisposable()
        mStreamPlayer = StreamPlayer.getInstance(context!!)

        initializeViews()
        setUpInitialPlayerState()
        setUpAudioStreamingServiceReceiver()

    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        setContentView(R.layout.activity_home)
//
//        btnSmallPlay.setOnClickListener(context!!)
//        btnPlay.setOnClickListener(context!!)
//
//        mCompositeDisposable = CompositeDisposable()
//        mStreamPlayer = StreamPlayer.getInstance(context = applicationContext)
//
//        initializeViews()
//        setUpInitialPlayerState()
//        setUpAudioStreamingServiceReceiver()
//
//    }


    private fun intiializeAudioVisualizer() {
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestAudioRecordingPermission()
        }

        setUpAudioVisualizer()

    }

    private fun initializeToolbar() {
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
    }

//    private fun initializeToolbar() {
//        activity.setSupportActionBar(toolbar)
//        if (supportActionBar != null) {
//            supportActionBar!!.title = getString(R.string.app_name)
//        }
//    }

    private fun initializeTabComponents() {
        setupViewPager(viewPager)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = resources.getString(R.string.live)
                1 -> tab.text = resources.getString(R.string.news)
                2 -> tab.text = resources.getString(R.string.about)
            }
        }.attach()

        tabLayout.getTabAt(0)!!.setIcon(R.drawable.ic_radio_live)
        tabLayout.getTabAt(1)!!.setIcon(R.drawable.ic_news)
        tabLayout.getTabAt(2)!!.setIcon(R.drawable.ic_about)

        // set icon color pre-selected
        tabLayout.getTabAt(0)!!.icon!!.setTint(Color.RED)
        tabLayout.getTabAt(1)!!.icon!!.setTint(ContextCompat.getColor(context!!, R.color.grey_20))
        tabLayout.getTabAt(2)!!.icon!!.setTint(ContextCompat.getColor(context!!, R.color.grey_20))

        tabLayout!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                collapseBottomSheet()
                if (tab.position != 0)
                    tab.icon!!.setTint(Color.WHITE)
                else
                    appBarLayout.setExpanded(true, true)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                if (tab.position != 0)
                    tab.icon!!.setTint(ContextCompat.getColor(context!!, R.color.grey_20))
            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })
    }


    /**
     * Check if Audio Streaming Service is running and change the AudioStreamingState accordingly
     * Note: We Initially set it to STATUS_STOPPED, assuming that nothing is playing when we first run
     */
    private fun setUpInitialPlayerState() {
        val radioPreferences = RadioPreferences(context!!)

        if (!Tools.isServiceRunning(AudioStreamingService::class.java, context!!)) {
            if (radioPreferences.isAutoPlayOnStart)
                startPlayback()
        } else {
            mAudioStreamingState = if (mStreamPlayer.playBackState == StreamPlayer.PlaybackState.PLAYING)
                AudioStreamingState.STATUS_PLAYING
            else
                AudioStreamingState.STATUS_STOPPED

            val intent = Intent(STREAM_RESULT)
            intent.putExtra(Constants.STREAMING_STATUS, mAudioStreamingState.toString())
            onAudioStreamingStateReceived(intent)

        }
    }

    /**
     * Update the stream progress seekbar and timer accordingly.
     * Also checks if the stream timer is up which triggers a shutdown of the app
     */
    private fun startUpdateStreamProgress() {
        Timber.d("startUpdateStreamProgress: ")
        mStreamPlayer.streamDurationStringsObservable
                .subscribe(object : SimpleObserver<Array<out String?>>() {
                    override fun onSubscribe(d: Disposable) {
                        mCompositeDisposable?.add(d)
                    }

                    override fun onNext(strings: Array<out String?>) {
                        val streamTimer = Integer.parseInt(RadioPreferences(context!!).streamingTimer!!) * 3600
                        val currentPosition = Seconds.seconds((mStreamPlayer.currentPosition / 1000).toInt())
                        seekBarProgress?.max = streamTimer
                        seekBarProgress?.progress = currentPosition.seconds

                        textStreamProgress?.text = strings[1]
                        textStreamDuration?.text = strings[0]
                    }
                })
    }

    private fun startPlayback() {
        val audioServiceIntent = Intent(context!!, AudioStreamingService::class.java)
        audioServiceIntent.action = Constants.ACTION_PLAY
        ContextCompat.startForegroundService(context!!, audioServiceIntent)
    }

    private fun pausePlayback() {
        val audioServiceIntent = Intent(context!!, AudioStreamingService::class.java)
        audioServiceIntent.action = Constants.ACTION_PAUSE
        ContextCompat.startForegroundService(context!!, audioServiceIntent)
    }

    private fun stopPlayback() {
        val audioServiceIntent = Intent(context!!, AudioStreamingService::class.java)
        audioServiceIntent.action = Constants.ACTION_STOP
        ContextCompat.startForegroundService(context!!, audioServiceIntent)
    }


    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(context!!).unregisterReceiver(mAudioServiceBroadcastReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mStreamPlayer.playBackState != StreamPlayer.PlaybackState.PLAYING) {
            stopPlayback()
        }

        if (visualizer != null)
            visualizer.release()

        mCompositeDisposable?.clear()
    }


    /**
     * Initialize all views by findViewById or @Bind when using ButterKnife.
     * Note: All view Initializing must be performed in context!! module or it's submodules
     */
    private fun initializeViews() {
        val textAnimationIn = AnimationUtils.loadAnimation(context!!, android.R.anim.slide_in_left)

        val textAnimationOut = AnimationUtils.loadAnimation(context!!, android.R.anim.slide_out_right)

        textSwitcherPlayerState.inAnimation = textAnimationIn
        textSwitcherPlayerState.outAnimation = textAnimationOut
        textSwitcherPlayerState.setCurrentText("Hello")

        initializeTabComponents()
        initializeToolbar()
        initializeBottomSheet()



        seekBarProgress.isEnabled = false
    }

    private fun setUpAudioVisualizer() {
        val audioSessionId = StreamPlayer.getInstance(context!!).audioSessionId
        try {
            if (audioSessionId != -1) {
                visualizer.setPlayer(audioSessionId)
                with(visualizer) {
                    setDensity(0.8F)
                    setGap(2)
                    setColor(ContextCompat.getColor(context!!, R.color.orange_900))
                }
            }
        } catch (exception: Exception) {
            Timber.e("setUpAudioVisualizer:${exception.message} ")
        }

    }

    private fun requestAudioRecordingPermission() {
        ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSION_RECORD_AUDIO)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_RECORD_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                setUpAudioVisualizer()
            else
                Timber.i("onRequestPermissionsResult: Denied. Unable to initialize visualizer")
        }
    }

    /**
     * bottom sheet state change listener
     * We are transitioning between collapsed and settled states, well that is what we are interested in, isn't it?
     */
    private fun initializeBottomSheet() {
        mSheetBehaviour = BottomSheetBehavior.from(layoutBottomSheet!!)

        mSheetBehaviour!!.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
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
        if (mSheetBehaviour?.state != BottomSheetBehavior.STATE_COLLAPSED)
            mSheetBehaviour?.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    /**
     * Listen for broadcast events from the Audio Streaming Service and use the information to
     * resolve the player state accordingly
     */
    private fun setUpAudioStreamingServiceReceiver() {
        mAudioServiceBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == STREAM_RESULT) {
                    onAudioStreamingStateReceived(intent)
                }
            }
        }
    }

    /**
     * Called when a broadcast is received from the AudioStreamingService so that the
     * UI can be resolved accordingly to correspond with the states
     *
     * @param intent The intent received fromt he audio service (STATUS_PAUSED, STATUS_PLAYING ETC)
     */
    private fun onAudioStreamingStateReceived(intent: Intent) {
        val receivedState = intent.getStringExtra(Constants.STREAMING_STATUS)
        mAudioStreamingState = AudioStreamingState.valueOf(receivedState!!)

        when (mAudioStreamingState) {
            AudioStreamingState.STATUS_PLAYING -> {
                Timber.d("onAudioStreamingStateReceived: Playing")
                Tools.toggleViewsVisibility(View.INVISIBLE, smallProgressBar)
                animateButtonDrawable(btnPlay, ContextCompat.getDrawable(context!!, R.drawable.avd_play_pause)!!)
                animateButtonDrawable(btnSmallPlay, ContextCompat.getDrawable(context!!, R.drawable.avd_play_pause_small)!!)

                intiializeAudioVisualizer()

                //start updating seekbar when something is actually playing
                startUpdateStreamProgress()
                textSwitcherPlayerState?.setText(getString(R.string.state_live))
                (textSwitcherPlayerState.currentView as TextView).setTextColor(ContextCompat.getColor(context!!, R.color.green_200))
            }
            AudioStreamingState.STATUS_STOPPED -> {
                Timber.d("onAudioStreamingStateReceived: STOPPED")
                animateButtonDrawable(btnPlay, ContextCompat.getDrawable(context!!, R.drawable.avd_pause_play)!!)
                animateButtonDrawable(btnSmallPlay, ContextCompat.getDrawable(context!!, R.drawable.avd_pause_play_small)!!)

                Tools.toggleViewsVisibility(View.INVISIBLE, smallProgressBar)
                textSwitcherPlayerState.setText(getString(R.string.state_stopped))
                (textSwitcherPlayerState.currentView as TextView).setTextColor(ContextCompat.getColor(context!!, R.color.pink_600))
            }
            AudioStreamingState.STATUS_LOADING -> {
                Timber.i("onAudioStreamingStateReceived: BUFFERING")
                textSwitcherPlayerState.setText(getString(R.string.state_buffering))
                (textSwitcherPlayerState.currentView as TextView).setTextColor(ContextCompat.getColor(context!!, R.color.pink_200))
                Tools.toggleViewsVisibility(View.VISIBLE, smallProgressBar)
            }

            AudioStreamingState.STATUS_PAUSED -> {
                Timber.i("onAudioStreamingStateReceived: PAUSED")
                animateButtonDrawable(btnPlay, ContextCompat.getDrawable(context!!, R.drawable.avd_pause_play)!!)
                animateButtonDrawable(btnSmallPlay, ContextCompat.getDrawable(context!!, R.drawable.avd_pause_play_small)!!)

                textSwitcherPlayerState.setText(getString(R.string.state_paused))
                (textSwitcherPlayerState.currentView as TextView).setTextColor(ContextCompat.getColor(context!!, R.color.yellow_400))
                Tools.toggleViewsVisibility(View.INVISIBLE, smallProgressBar)
            }

        }
    }


    private fun setupViewPager(viewPager: ViewPager2) {
        val viewPagerAdapter = HomeSectionsPagerAdapter(activity!!)
        viewPagerAdapter.addFragment(HomeFragment(), "Live")    // index 0
        viewPagerAdapter.addFragment(NewsListFragment(), "News")   // index 1
        viewPagerAdapter.addFragment(AboutFragment(), "About")   // index 2

        viewPager.adapter = viewPagerAdapter
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(context!!).registerReceiver(mAudioServiceBroadcastReceiver,
                IntentFilter(STREAM_RESULT)
        )
    }

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.main, menu)
//    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.main, menu)
//        return true
//    }

}
