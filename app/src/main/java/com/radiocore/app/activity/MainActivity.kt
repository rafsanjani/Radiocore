package com.radiocore.app.activity

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.foreverrafs.radiocore.R
import com.foreverrafs.radiocore.databinding.BottomSheetBinding
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.radiocore.RadioPreferences
import com.radiocore.app.adapter.HomePagerAdapter
import com.radiocore.app.fragment.AboutFragment
import com.radiocore.app.fragment.LiveFragment
import com.radiocore.app.util.animateButtonDrawable
import com.radiocore.app.util.isServiceRunning
import com.radiocore.app.util.toggleViewsVisibility
import com.radiocore.app.viewmodels.AppViewModel
import com.radiocore.news.ui.NewsListFragment
import com.radiocore.player.AudioServiceConnection
import com.radiocore.player.AudioStreamingService
import com.radiocore.player.StreamPlayer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.bogerchan.niervisualizer.NierVisualizerManager
import me.bogerchan.niervisualizer.renderer.columnar.ColumnarType2Renderer
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        private const val PERMISSION_RECORD_AUDIO = 6900
    }

    private val mAudioServiceIntent: Intent by lazy {
        Intent(this, AudioStreamingService::class.java)
    }

    private lateinit var mSheetBehaviour: BottomSheetBehavior<*>

    private var shouldStartPlayback: Boolean = false

    @Inject
    lateinit var mStreamPlayer: StreamPlayer

    @Inject
    lateinit var mRadioPreferences: RadioPreferences

    private var audioService: AudioStreamingService? = null

    private lateinit var viewModel: AppViewModel

    private var visualizerManager: NierVisualizerManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProvider(this).get(AppViewModel::class.java)

        btnSmallPlay.setOnClickListener(this)
        btnPlay.setOnClickListener(this)

        visualizer.setZOrderOnTop(true)

        initializeViews()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnSmallPlay, R.id.btnPlay -> {
                Timber.i("onClick: ${viewModel.playbackState.value.toString()}")

                when (viewModel.playbackState.value) {
                    AudioStreamingService.AudioStreamingState.STATUS_PLAYING -> stopPlayback()
                    AudioStreamingService.AudioStreamingState.STATUS_STOPPED -> startPlayback()
                    AudioStreamingService.AudioStreamingState.STATUS_LOADING -> Timber.d("Stream already loading; Ignore click event")
                }
            }
        }
    }

    private fun startPlayback() {
        if (viewModel.audioServiceConnection.isBound) {
            audioService?.startPlayback()

            return
        }

        startAudioService()
    }

    private fun startAudioService() {
        shouldStartPlayback = true
        ContextCompat.startForegroundService(this, mAudioServiceIntent)
        bindService(mAudioServiceIntent, viewModel.audioServiceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun stopPlayback() {
        viewModel.audioServiceConnection.audioService?.stopPlayback()
    }


    @FlowPreview
    override fun onStart() {
        super.onStart()
        setUpInitialPlayerState()
    }

    /**
     * Check if Audio Streaming Service is running and change the AudioStreamingState accordingly
     * Note: We Initially set it to STATUS_STOPPED, assuming that nothing is playing when we first run
     */
    @FlowPreview
    private fun setUpInitialPlayerState() {
        shouldStartPlayback = mRadioPreferences.autoPlayOnStart

        val mainActivityPendingIntent = PendingIntent.getActivity(this, 3333,
                Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT)

        viewModel.audioServiceConnection = AudioServiceConnection(mainActivityPendingIntent) {
            audioService = viewModel.audioServiceConnection.audioService

            audioService?.apply {
                metaData.observe(this@MainActivity, Observer { string ->
                    viewModel.updateStreamMetaData(string)
                })

                if (shouldStartPlayback)
                    startPlayback()

                playBackState.observe(this@MainActivity, Observer { streamingState ->

                    viewModel.updatePlaybackState(streamingState)
                    when (streamingState) {
                        AudioStreamingService.AudioStreamingState.STATUS_PLAYING -> streamPlaying()
                        AudioStreamingService.AudioStreamingState.STATUS_STOPPED -> streamStopped()
                        AudioStreamingService.AudioStreamingState.STATUS_LOADING -> streamLoading()
                        else -> {
                            Timber.e("setUpInitialPlayerState: Unknown Playback state")
                        }
                    }
                })
            }
        }

        if (!isServiceRunning(AudioStreamingService::class.java, this)) {
            if (mRadioPreferences.autoPlayOnStart)
                startPlayback()
        }
    }

    private fun streamStopped() {
        visualizerManager?.pause()
        Timber.d("Stream State Changed: STOPPED")
        animateButtonDrawable(btnPlay, ContextCompat.getDrawable(this, R.drawable.avd_pause_play)!!)
        animateButtonDrawable(btnSmallPlay, ContextCompat.getDrawable(this, R.drawable.avd_pause_play_small)!!)

        toggleViewsVisibility(View.INVISIBLE, progressBuffering)
        textSwitcherPlayerState.setText(getString(R.string.state_stopped))
        (textSwitcherPlayerState.currentView as TextView).setTextColor(ContextCompat.getColor(this, R.color.pink_600))
    }


    @FlowPreview
    private fun streamPlaying() {
        Timber.d("Stream State Changed: Playing")

        prepareVisualizer()
        toggleViewsVisibility(View.INVISIBLE, progressBuffering)
        animateButtonDrawable(btnPlay, ContextCompat.getDrawable(this, R.drawable.avd_play_pause)!!)
        animateButtonDrawable(btnSmallPlay, ContextCompat.getDrawable(this, R.drawable.avd_play_pause_small)!!)


        //start updating seekbar when something is actually playing
        startUpdateStreamProgress()
        textSwitcherPlayerState?.setText(getString(R.string.state_live))
        (textSwitcherPlayerState.currentView as TextView).setTextColor(ContextCompat.getColor(this, R.color.green_200))
    }

    /**
     * Update the stream progress seekbar and timer accordingly.
     * Also checks if the stream timer is up which triggers a shutdown of the app
     */
    @FlowPreview
    private fun startUpdateStreamProgress() {
        lifecycleScope.launch {
            mStreamPlayer.streamDurationStringsFlow.collect { playbackTime ->
                val (elapsed, total) = playbackTime

                seekBarProgress?.max = total.toInt()
                seekBarProgress?.progress = elapsed.toInt()

                textStreamProgress?.text = printDurationPretty(elapsed)
                textStreamDuration?.text = printDurationPretty(total)
            }
        }
    }

    private fun printDurationPretty(input: Long): String {
        var duration = input

        fun Long.printWithPadding(): String {
            return this.toString().padStart(2, '0')
        }

        val hours = TimeUnit.SECONDS.toHours(duration)
        duration -= TimeUnit.HOURS.toSeconds(hours)

        val minute = TimeUnit.SECONDS.toMinutes(duration)
        duration -= TimeUnit.MINUTES.toSeconds(minute)

        val seconds = duration

        return "${hours.printWithPadding()}:${minute.printWithPadding()}:${seconds.printWithPadding()}"
    }

    private fun prepareVisualizer() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestAudioRecordingPermission()
            return
        }

        visualizerManager?.let {
            it.resume()
            return
        }

        visualizerManager = NierVisualizerManager()

        val state = visualizerManager?.init(mStreamPlayer.audioSessionId)


        visualizer.setZOrderOnTop(true)
        visualizer.holder.setFormat(PixelFormat.TRANSLUCENT)

        if (state == NierVisualizerManager.SUCCESS) {
            visualizerManager?.start(
                    visualizer, arrayOf(ColumnarType2Renderer())
            )
        }
    }

    private fun requestAudioRecordingPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSION_RECORD_AUDIO)
    }

    private fun streamLoading() {
        visualizerManager?.pause()
        Timber.i("Stream State Changed: BUFFERING")
        textSwitcherPlayerState.setText(getString(R.string.state_buffering))
        (textSwitcherPlayerState.currentView as TextView).setTextColor(ContextCompat.getColor(this, R.color.pink_200))
        toggleViewsVisibility(View.VISIBLE, progressBuffering)
    }

    /**
     * Initialize all views by findViewById or @Bind when using ButterKnife.
     * Note: All view Initializing must be performed in context module or it's submodules
     */
    private fun initializeViews() {
        val textAnimationIn = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left)
        val textAnimationOut = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right)

        with(textSwitcherPlayerState) {
            inAnimation = textAnimationIn
            outAnimation = textAnimationOut
            setCurrentText("RadioCore")
        }
        setSupportActionBar(toolbar)

        initializeTabComponents()
        initializeBottomSheet()

        seekBarProgress.isEnabled = false
    }

    /**
     * bottom sheet state change listener
     * We are transitioning between collapsed and settled states, well that is what we are interested in, isn't it?
     */
    private fun initializeBottomSheet() {
        mSheetBehaviour = BottomSheetBehavior.from(layoutBottomSheet)

        //initialize the contact texts on the bottom sheet
        tvEmail.text = getString(R.string.email_and_value, getString(R.string.org_email))
        tvPhone.text = getString(R.string.phone_and_value, getString(R.string.org_phone))
        tvWebsite.text = getString(R.string.website_and_value, getString(R.string.org_website))

        BottomSheetBinding.inflate(layoutInflater).apply {
            lifecycleOwner = this@MainActivity
        }.viewModel = this.viewModel

        mSheetBehaviour.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    appBarLayout.setExpanded(false, true)
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    appBarLayout.setExpanded(true, true)
                    visualizer.visibility = View.VISIBLE
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                performAlphaTransition(slideOffset)
                rotateSmallLogo(slideOffset)
                visualizer.visibility = View.INVISIBLE
            }
        })
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
     * rotate the collapse button clockwise when collapsing and counter-clockwise when expanding
     *
     * @param slideOffset the initial angle where rotation begins
     */
    private fun rotateSmallLogo(slideOffset: Float) {
        val rotationAngle = slideOffset * -360
        smallLogo?.rotation = rotationAngle
    }

    private fun setupViewPager(viewPager: ViewPager2) {
        val viewPagerAdapter = HomePagerAdapter(this).apply {
            addFragment(LiveFragment(), "Live")
            addFragment(NewsListFragment(), "News")
            addFragment(AboutFragment(), "About")
        }

        viewPager.adapter = viewPagerAdapter
    }


    /**
     * Explicitly collapse the bottom sheet
     */
    fun collapseBottomSheet() {
        if (mSheetBehaviour.state != BottomSheetBehavior.STATE_COLLAPSED)
            mSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun expandBottomSheet() {
        if (mSheetBehaviour.state != BottomSheetBehavior.STATE_EXPANDED)
            mSheetBehaviour.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun hideBottomSheet() {
        if (mSheetBehaviour.state != BottomSheetBehavior.STATE_HIDDEN)
            mSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN
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
            getTabAt(1)?.setIcon(R.drawable.ic_news)
            getTabAt(2)?.setIcon(R.drawable.ic_about)

            getTabAt(0)?.icon?.setTint(Color.RED)
            getTabAt(1)?.icon?.setTint(ContextCompat.getColor(context, R.color.grey_20))
            getTabAt(2)?.icon?.setTint(ContextCompat.getColor(context, R.color.grey_20))
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                appBarLayout.setExpanded(true, true)
                if (tab.position != 0)
                    tab.icon?.setTint(Color.WHITE)

                collapseBottomSheet()

            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                if (tab.position != 0)
                    tab.icon?.setTint(ContextCompat.getColor(this@MainActivity, R.color.grey_20))
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                Timber.i("onTabReselected: No-Op")
            }
        })

        //todo:replace with something better in the future
        appBarLayout.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            var hidden: Boolean = false

            override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
                println("offset: $verticalOffset")
                hidden = if (verticalOffset != 0) {
                    if (!hidden && mSheetBehaviour.state == BottomSheetBehavior.STATE_COLLAPSED)
                        hideBottomSheet()
                    true
                } else {
                    if (hidden)
                        collapseBottomSheet()
                    false
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        startActivity(Intent(this, SettingsActivity::class.java))
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_RECORD_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                prepareVisualizer()
            } else
                Timber.i("onRequestPermissionsResult: Denied. Unable to initialize visualizer")
        }
    }

    override fun onDestroy() {
        if (mStreamPlayer.playBackState != StreamPlayer.PlaybackState.PLAYING) {
            stopPlayback()
        }

        visualizerManager?.release()

        if (viewModel.audioServiceConnection.isBound)
            unbindService(viewModel.audioServiceConnection)

        super.onDestroy()
    }
}