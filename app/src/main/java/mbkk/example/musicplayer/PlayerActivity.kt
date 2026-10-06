package mbkk.example.musicplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import mbkk.example.musicplayer.ui.screens.PlayerScreen
import mbkk.example.musicplayer.ui.theme.MusicPlayerTheme

class PlayerActivity : AppCompatActivity(), MusicPlayerListener {

    private var isShuffleOn by mutableStateOf(false)
    private var isPlaying by mutableStateOf(false)
    private var currentPosition by mutableIntStateOf(0)
    private var duration by mutableIntStateOf(0)
    private var isSheetOpen by mutableStateOf(false)
    private var activeSongIndex by mutableIntStateOf(0)

    private var musicService: MusicService? = null
    private var isBound = false
    private val handler = Handler(Looper.getMainLooper())
    private var openedFromNotification = false

    companion object {
        var songsList = mutableListOf<SongModel>()
        var currentIndex = 0
    }

    private val seekBarRunnable = object : Runnable {
        override fun run() {
            musicService?.let {
                currentPosition = it.getCurrentPosition()
                duration = it.getDuration()
            }
            handler.postDelayed(this, 1000)
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true
            musicService?.listener = this@PlayerActivity

            if (openedFromNotification) {
                currentIndex = musicService?.currentSongIndex ?: 0
                if (songsList.isEmpty()) {
                    songsList = musicService?.songsList ?: mutableListOf()
                }
                updatePlayerState()
            } else {
                if (songsList.isNotEmpty()) {
                    musicService?.songsList = songsList
                    if (currentIndex in 0 until songsList.size) {
                        musicService?.playSong(currentIndex)
                        updatePlayerState()
                    } else {
                        currentIndex = 0
                        musicService?.playSong(0)
                        updatePlayerState()
                    }
                }
            }

            handler.removeCallbacks(seekBarRunnable)
            handler.post(seekBarRunnable)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val songIndex = intent.getIntExtra("SONG_INDEX", -1)
        openedFromNotification = songIndex == -1

        if (!openedFromNotification) {
            currentIndex = songIndex
        }
        activeSongIndex = currentIndex

        setContent {
            MusicPlayerTheme {
                val currentSong = songsList.getOrNull(activeSongIndex)

                PlayerScreen(
                    song = currentSong,
                    isPlaying = isPlaying,
                    isShuffleOn = isShuffleOn,
                    currentPosition = currentPosition,
                    duration = duration,
                    songsList = songsList,
                    currentIndex = activeSongIndex,
                    isSheetOpen = isSheetOpen,
                    onBackClick = {
                        onBackPressedDispatcher.onBackPressed()
                    },
                    onShuffleClick = {
                        isShuffleOn = !isShuffleOn
                    },
                    onPreviousClick = {
                        playPrevious()
                    },
                    onPlayPauseClick = {
                        musicService?.playPause()
                    },
                    onNextClick = {
                        playNext()
                    },
                    onMenuClick = {
                        showSongListSheet()
                    },
                    onSeek = { pos ->
                        musicService?.seekTo(pos)
                        currentPosition = pos
                    },
                    onDismissSheet = {
                        isSheetOpen = false
                    },
                    onSongSelectedFromSheet = { index ->
                        currentIndex = index
                        activeSongIndex = index
                        musicService?.playSong(index)
                        updatePlayerState()
                        isSheetOpen = false
                    }
                )
            }
        }

        if (savedInstanceState != null) {
            isShuffleOn = savedInstanceState.getBoolean("IS_SHUFFLE_ON", false)
        }

        val serviceIntent = Intent(this, MusicService::class.java)
        startService(serviceIntent)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("IS_SHUFFLE_ON", isShuffleOn)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        val songIndex = intent.getIntExtra("SONG_INDEX", -1)
        openedFromNotification = songIndex == -1

        if (!openedFromNotification) {
            currentIndex = songIndex
        }
        activeSongIndex = currentIndex

        musicService?.let {
            if (openedFromNotification) {
                currentIndex = it.currentSongIndex
                activeSongIndex = currentIndex
                if (songsList.isEmpty()) songsList = it.songsList
                updatePlayerState()
            } else {
                it.songsList = songsList
                it.playSong(currentIndex)
                updatePlayerState()
            }
        }
    }

    override fun onSongChanged(index: Int) {
        runOnUiThread {
            currentIndex = index
            activeSongIndex = index
            updatePlayerState()
        }
    }

    override fun onPlaybackStateChanged(isPlaying: Boolean) {
        runOnUiThread {
            this.isPlaying = isPlaying
        }
    }

    override fun onResume() {
        super.onResume()
        musicService?.let {
            it.listener = this
            currentIndex = it.currentSongIndex
            activeSongIndex = currentIndex
            if (songsList.isEmpty() && it.songsList.isNotEmpty()) {
                songsList = it.songsList
            }
            updatePlayerState()
        }
    }

    private fun playNext() {
        if (isShuffleOn && songsList.size > 1) {
            var randomIndex: Int
            do {
                randomIndex = (songsList.indices).random()
            } while (randomIndex == currentIndex)
            currentIndex = randomIndex
            activeSongIndex = currentIndex
            musicService?.playSong(currentIndex)
            updatePlayerState()
        } else {
            musicService?.nextSong()
        }
    }

    private fun playPrevious() {
        if (isShuffleOn && songsList.size > 1) {
            var randomIndex: Int
            do {
                randomIndex = (songsList.indices).random()
            } while (randomIndex == currentIndex)
            currentIndex = randomIndex
            activeSongIndex = currentIndex
            musicService?.playSong(currentIndex)
            updatePlayerState()
        } else {
            musicService?.previousSong()
        }
    }

    override fun showSongListSheet() {
        if (songsList.isNotEmpty()) {
            isSheetOpen = true
        }
    }

    private fun updatePlayerState() {
        if (songsList.isEmpty()) return
        if (activeSongIndex !in 0 until songsList.size) return
        isPlaying = musicService?.isPlaying() ?: false
        currentPosition = musicService?.getCurrentPosition() ?: 0
        duration = musicService?.getDuration() ?: 0
    }

    override fun onDestroy() {
        super.onDestroy()
        musicService?.listener = null
        handler.removeCallbacksAndMessages(null)
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }
}