package `in`.edu.pushparaj.activities

import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import org.ligi.pushparaj.R
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.util.Util


class FilePreviewActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        private const val KEY_PLAY_WHEN_READY = "play_when_ready"
        private const val KEY_WINDOW = "window"
        private const val KEY_POSITION = "position"
    }

    private var player: SimpleExoPlayer? = null
    private val playerView: PlayerView by lazy { findViewById<PlayerView>(R.id.player_view) }


    private var shouldAutoPlay: Boolean = true
    private var trackSelector: DefaultTrackSelector? = null
    private var lastSeenTrackGroupArray: TrackGroupArray? = null
    private lateinit var mediaDataSourceFactory: DataSource.Factory
    private val bandwidthMeter: BandwidthMeter = DefaultBandwidthMeter()

    private var playWhenReady: Boolean = false
    private var currentWindow: Int = 0
    private var playbackPosition: Long = 0
    private val progressBar: ProgressBar by lazy { findViewById<ProgressBar>(R.id.progress_bar) }
    private val ivHideControllerButton: ImageView by lazy { findViewById<ImageView>(R.id.exo_controller) }

    private var fileName:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_preview)

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        var bundle :Bundle ?=intent.extras
        fileName = bundle!!.getString("fileName")
        if (savedInstanceState != null) {

            with(savedInstanceState) {
                playWhenReady = getBoolean(KEY_PLAY_WHEN_READY)
                currentWindow = getInt(KEY_WINDOW)
                playbackPosition = getLong(KEY_POSITION)
            }
        }

        shouldAutoPlay = true
        mediaDataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, "mediaPlayerSample"),
                bandwidthMeter as TransferListener<in DataSource>)
    }

    public override fun onStart() {
        super.onStart()

        if (Util.SDK_INT > 23) initializePlayer()
    }

    public override fun onResume() {
        super.onResume()

        if (Util.SDK_INT <= 23 || player == null) initializePlayer()
    }

    public override fun onPause() {
        super.onPause()

        if (Util.SDK_INT <= 23) releasePlayer()
    }

    public override fun onStop() {
        super.onStop()

        if (Util.SDK_INT > 23) releasePlayer()
    }

    override fun onClick(v: View) {

    }

    override fun onSaveInstanceState(outState: Bundle) {
        updateStartPosition()

        with(outState) {
            putBoolean(KEY_PLAY_WHEN_READY, playWhenReady)
            putInt(KEY_WINDOW, currentWindow)
            putLong(KEY_POSITION, playbackPosition)
        }

        super.onSaveInstanceState(outState)
    }


    private fun initializePlayer() {

        playerView.requestFocus()

        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)

        trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        lastSeenTrackGroupArray = null

        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector)

        playerView.player = player

        with(player!!) {
            addListener(PlayerEventListener())
            playWhenReady = shouldAutoPlay
        }

        // Use Hls, Dash or other smooth streaming media source if you want to test the track quality selection.
        /*val mediaSource: MediaSource = HlsMediaSource(Uri.parse("https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8"),
                mediaDataSourceFactory, mainHandler, null)*/
        var path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath+"/"+fileName;
        val mediaSource = ExtractorMediaSource.Factory(mediaDataSourceFactory)
                .createMediaSource(Uri.parse(path))

        val haveStartPosition = currentWindow != C.INDEX_UNSET
        if (haveStartPosition) {
            player!!.seekTo(currentWindow, playbackPosition)
        }

        player!!.prepare(mediaSource, !haveStartPosition, false)
        updateButtonVisibilities()

        //ivHideControllerButton.setOnClickListener { playerView.hideController() }
    }

    private fun releasePlayer() {
        if (player != null) {
            updateStartPosition()
            shouldAutoPlay = player!!.playWhenReady
            player!!.release()
            player = null
            trackSelector = null
        }
    }

    private fun updateStartPosition() {

        with(player!!) {
            playbackPosition = currentPosition
            currentWindow = currentWindowIndex
            playWhenReady = playWhenReady
        }
    }

    private fun updateButtonVisibilities() {

    }


    private inner class PlayerEventListener : Player.DefaultEventListener() {

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                Player.STATE_IDLE       // The player does not have any media to play yet.
                -> progressBar.visibility = View.VISIBLE
                Player.STATE_BUFFERING  // The player is buffering (loading the content)
                -> progressBar.visibility = View.VISIBLE
                Player.STATE_READY      // The player is able to immediately play
                -> progressBar.visibility = View.GONE
                Player.STATE_ENDED      // The player has finished playing the media
                -> progressBar.visibility = View.GONE
            }
            updateButtonVisibilities()
        }

        override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
            updateButtonVisibilities()
            // The video tracks are no supported in this device.
            if (trackGroups !== lastSeenTrackGroupArray) {
                val mappedTrackInfo = trackSelector!!.currentMappedTrackInfo
                if (mappedTrackInfo != null) {
                    if (mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_VIDEO) == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                        Toast.makeText(this@FilePreviewActivity, "Error unsupported track", Toast.LENGTH_SHORT).show()
                    }
                }
                lastSeenTrackGroupArray = trackGroups
            }
        }
    }
}
