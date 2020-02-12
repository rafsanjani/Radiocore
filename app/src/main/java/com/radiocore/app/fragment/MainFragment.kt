package com.radiocore.app.fragment

import android.Manifest
import android.app.PendingIntent
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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.radiocore.app.R
import com.radiocore.app.activity.MainActivity
import com.radiocore.app.adapter.HomeSectionsPagerAdapter
import com.radiocore.app.concurrency.SimpleObserver
import com.radiocore.app.databinding.BottomSheetBinding
import com.radiocore.app.viewmodels.HomeViewModel
import com.radiocore.core.di.DaggerAndroidXFragment
import com.radiocore.core.util.*
import com.radiocore.core.util.Constants.STREAM_RESULT
import com.radiocore.news.ui.NewsListFragment
import com.radiocore.player.AudioServiceConnection
import com.radiocore.player.AudioStreamingService
import com.radiocore.player.AudioStreamingService.AudioStreamingState
import com.radiocore.player.StreamPlayer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_main.*
import org.joda.time.Seconds
import timber.log.Timber
import javax.inject.Inject


class MainFragment : DaggerAndroidXFragment(), View.OnClickListener {
    private val PERMISSION_RECORD_AUDIO = 6900
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private lateinit var mAudioServiceBroadcastReceiver: BroadcastReceiver
    private lateinit var mAudioServiceConnection: AudioServiceConnection


    private var mSheetBehaviour: BottomSheetBehavior<*>? = null
    private var mCompositeDisposable: CompositeDisposable? = null

    @Inject
    lateinit var mStreamPlayer: StreamPlayer

    @Inject
    lateinit var mRadioPreferences: RadioPreferences

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        btnSmallPlay.setOnClickListener(this)
        btnPlay.setOnClickListener(this)

        mCompositeDisposable = CompositeDisposable()

        initializeViews()
    }


    private fun intializeAudioVisualizer() {
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestAudioRecordingPermission()
        }

        setUpAudioVisualizer()
    }

    private fun initializeToolbar() {
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
    }

    private fun initializeTabComponents() {
        setupViewPager(viewPager)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = resources.getString(R.string.live)
                1 -> tab.text = resources.getString(R.string.news)
                2 -> tab.text = resources.getString(R.string.about)
            }
        }.attach()

        with(tabLayout) {
            getTabAt(0)?.setIcon(R.drawable.ic_radio_live)
            getTabAt(1)!!.setIcon(R.drawable.ic_news)
            getTabAt(2)!!.setIcon(R.drawable.ic_about)

            getTabAt(0)!!.icon!!.setTint(Color.RED)
            getTabAt(1)!!.icon!!.setTint(ContextCompat.getColor(context!!, R.color.grey_20))
            getTabAt(2)!!.icon!!.setTint(ContextCompat.getColor(context!!, R.color.grey_20))
        }

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
        val mainActivityPendingIntent = PendingIntent.getActivity(requireContext(), 3333,
                Intent(requireContext(), MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
        mAudioServiceConnection = AudioServiceConnection(mainActivityPendingIntent)


        if (!isServiceRunning(AudioStreamingService::class.java, requireContext())) {
            if (mRadioPreferences.isAutoPlayOnStart)
                startPlayback()
        } else {
            val state = if (mStreamPlayer.playBackState == StreamPlayer.PlaybackState.PLAYING)
                AudioStreamingState.STATUS_PLAYING else AudioStreamingState.STATUS_STOPPED
            viewModel.updatePlaybackState(state)

            val intent = Intent(STREAM_RESULT)
            intent.putExtra(Constants.STREAMING_STATUS, viewModel.playbackState.value.toString()) //mAudioStreamingState.toString())
            onAudioStreamingStateReceived(intent)
        }
    }

    /**
     * Update the stream progress seekbar and timer accordingly.
     * Also checks if the stream timer is up which triggers a shutdown of the app
     */
    private fun startUpdateStreamProgress() {
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
        mAudioServiceConnection.audioService?.startPlayback()
    }

    private fun pausePlayback() {
        mAudioServiceConnection.audioService?.pausePlayback()
    }

    private fun stopPlayback() {
        mAudioServiceConnection.audioService?.stopPlayback()
    }


    override fun onStart() {
        super.onStart()
        setUpInitialPlayerState()
        setUpAudioStreamingServiceReceiver()

        Intent(context!!, AudioStreamingService::class.java).apply {
            activity?.bindService(this, mAudioServiceConnection, Context.BIND_AUTO_CREATE)
            ContextCompat.startForegroundService(context!!, this)
        }
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(context!!).unregisterReceiver(mAudioServiceBroadcastReceiver)
        activity?.unbindService(mAudioServiceConnection)
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
        val audioSessionId = mStreamPlayer.audioSessionId
        try {
            if (audioSessionId != -1) {
                visualizer.setPlayer(audioSessionId)
                with(visualizer) {
                    setDensity(0.8F)
                    setGap(2)
                    setColor(ContextCompat.getColor(context, R.color.orange_900))
                }
            }
        } catch (exception: Exception) {
            Timber.e("setUpAudioVisualizer:${exception.message} ")
        }

    }

    private fun requestAudioRecordingPermission() {
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSION_RECORD_AUDIO)
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

        val binding: BottomSheetBinding? = DataBindingUtil.bind(layoutBottomSheet)
        binding?.viewModel = viewModel
        mSheetBehaviour!!.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
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
                Timber.i(intent.action)
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
     * @param intent The intent received from the audio service (STATUS_PAUSED, STATUS_PLAYING ETC)
     */
    private fun onAudioStreamingStateReceived(intent: Intent) {
        val receivedState = intent.getStringExtra(Constants.STREAMING_STATUS)
        val state = AudioStreamingState.valueOf(receivedState!!)
        viewModel.updatePlaybackState(state)

        when (state) {
            AudioStreamingState.STATUS_PLAYING -> {
                Timber.d("onAudioStreamingStateReceived: Playing")
                toggleViewsVisibility(View.INVISIBLE, smallProgressBar)
                animateButtonDrawable(btnPlay, ContextCompat.getDrawable(context!!, R.drawable.avd_play_pause)!!)
                animateButtonDrawable(btnSmallPlay, ContextCompat.getDrawable(context!!, R.drawable.avd_play_pause_small)!!)

                intializeAudioVisualizer()

                //start updating seekbar when something is actually playing
                startUpdateStreamProgress()
                textSwitcherPlayerState?.setText(getString(R.string.state_live))
                (textSwitcherPlayerState.currentView as TextView).setTextColor(ContextCompat.getColor(context!!, R.color.green_200))
            }
            AudioStreamingState.STATUS_STOPPED -> {
                Timber.d("onAudioStreamingStateReceived: STOPPED")
                animateButtonDrawable(btnPlay, ContextCompat.getDrawable(context!!, R.drawable.avd_pause_play)!!)
                animateButtonDrawable(btnSmallPlay, ContextCompat.getDrawable(context!!, R.drawable.avd_pause_play_small)!!)

                toggleViewsVisibility(View.INVISIBLE, smallProgressBar)
                textSwitcherPlayerState.setText(getString(R.string.state_stopped))
                (textSwitcherPlayerState.currentView as TextView).setTextColor(ContextCompat.getColor(context!!, R.color.pink_600))
            }
            AudioStreamingState.STATUS_LOADING -> {
                Timber.i("onAudioStreamingStateReceived: BUFFERING")
                textSwitcherPlayerState.setText(getString(R.string.state_buffering))
                (textSwitcherPlayerState.currentView as TextView).setTextColor(ContextCompat.getColor(context!!, R.color.pink_200))
                toggleViewsVisibility(View.VISIBLE, smallProgressBar)
            }

            AudioStreamingState.STATUS_PAUSED -> {
                Timber.i("onAudioStreamingStateReceived: PAUSED")
                animateButtonDrawable(btnPlay, ContextCompat.getDrawable(context!!, R.drawable.avd_pause_play)!!)
                animateButtonDrawable(btnSmallPlay, ContextCompat.getDrawable(context!!, R.drawable.avd_pause_play_small)!!)

                textSwitcherPlayerState.setText(getString(R.string.state_paused))
                (textSwitcherPlayerState.currentView as TextView).setTextColor(ContextCompat.getColor(context!!, R.color.yellow_400))
                toggleViewsVisibility(View.INVISIBLE, smallProgressBar)
            }

        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnSmallPlay, R.id.btnPlay -> {
                Timber.i("onPlay: ${viewModel.playbackState.value.toString()}")

                when (viewModel.playbackState.value) {
                    AudioStreamingState.STATUS_PLAYING -> pausePlayback()
                    AudioStreamingState.STATUS_PAUSED,
                    AudioStreamingState.STATUS_STOPPED -> startPlayback()
                    AudioStreamingState.STATUS_LOADING -> Timber.d("Loading")
                }
            }
        }
    }


    private fun setupViewPager(viewPager: ViewPager2) {
        val viewPagerAdapter = HomeSectionsPagerAdapter(requireActivity())
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
}
