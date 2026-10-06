package mbkk.example.musicplayer

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import mbkk.example.musicplayer.ui.screens.MainScreen
import mbkk.example.musicplayer.ui.theme.MusicPlayerTheme

class MainActivity : AppCompatActivity(), MusicPlayerListener {

    private val allSongs = mutableStateListOf<SongModel>()
    private var searchQuery by mutableStateOf("")
    private var miniPlayerSong by mutableStateOf<SongModel?>(null)
    private var isMiniPlayerPlaying by mutableStateOf(false)

    private var musicService: MusicService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true
            musicService?.listener = this@MainActivity

            val index = musicService?.currentSongIndex ?: -1
            val isPlaying = musicService?.isPlaying() ?: false

            if (index >= 0 && isPlaying) {
                val song = allSongs.getOrNull(index)
                    ?: musicService?.songsList?.getOrNull(index)
                if (song != null) {
                    updateMiniPlayer(song, isPlaying)
                }
            } else {
                miniPlayerSong = null
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.values.all { it }) {
                loadSongs()
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MusicPlayerTheme {
                val filteredSongs = if (searchQuery.isEmpty()) {
                    allSongs
                } else {
                    allSongs.filter {
                        it.title.contains(searchQuery, ignoreCase = true) ||
                                it.artist.contains(searchQuery, ignoreCase = true)
                    }
                }

                MainScreen(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    songs = filteredSongs,
                    onSongClick = { song, position ->
                        searchQuery = ""
                        val originalIndex = allSongs.indexOfFirst { it.id == song.id }
                        val indexToPlay = if (originalIndex != -1) originalIndex else position
                        PlayerActivity.songsList = ArrayList(allSongs)
                        PlayerActivity.currentIndex = indexToPlay
                        val intent = Intent(this@MainActivity, PlayerActivity::class.java)
                        intent.putExtra("SONG_INDEX", indexToPlay)
                        startActivity(intent)
                    },
                    miniPlayerSong = miniPlayerSong,
                    isMiniPlayerPlaying = isMiniPlayerPlaying,
                    onMiniPlayPause = {
                        val currentlyPlaying = musicService?.isPlaying() ?: false
                        isMiniPlayerPlaying = !currentlyPlaying
                        musicService?.playPause()
                    },
                    onMiniNext = {
                        musicService?.nextSong()
                    },
                    onMiniPlayerClick = {
                        val intent = Intent(this@MainActivity, PlayerActivity::class.java)
                        intent.putExtra("SONG_INDEX", -1)
                        startActivity(intent)
                    }
                )
            }
        }

        val serviceIntent = Intent(this, MusicService::class.java)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

        checkAndRequestPermissions()
    }

    override fun onSongChanged(index: Int) {
        runOnUiThread {
            val song = allSongs.getOrNull(index) ?: return@runOnUiThread
            val isPlaying = musicService?.isPlaying() ?: false
            updateMiniPlayer(song, isPlaying)
        }
    }

    override fun onPlaybackStateChanged(isPlaying: Boolean) {
        runOnUiThread {
            val index = musicService?.currentSongIndex ?: return@runOnUiThread
            val song = allSongs.getOrNull(index) ?: return@runOnUiThread
            updateMiniPlayer(song, isPlaying)
        }
    }

    override fun showSongListSheet() {}

    private fun updateMiniPlayer(song: SongModel, isPlaying: Boolean) {
        miniPlayerSong = song
        isMiniPlayerPlaying = isPlaying
    }

    override fun onResume() {
        super.onResume()
        searchQuery = ""

        if (isBound && musicService != null) {
            musicService!!.listener = this
            val index = musicService!!.currentSongIndex
            val isPlaying = musicService!!.isPlaying()

            if (isPlaying || (index >= 0 && musicService!!.songsList.isNotEmpty())) {
                if (allSongs.isEmpty()) allSongs.addAll(musicService!!.songsList)
                val song = allSongs.getOrNull(index)
                if (song != null) {
                    updateMiniPlayer(song, isPlaying)
                }
            } else {
                miniPlayerSong = null
            }
        } else {
            miniPlayerSong = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            musicService?.listener = null
            unbindService(serviceConnection)
            isBound = false
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isEmpty()) loadSongs()
        else requestPermissionLauncher.launch(notGranted.toTypedArray())
    }

    private fun loadSongs() {
        allSongs.clear()
        allSongs.addAll(getAllSongs())
    }

    private fun getAllSongs(): List<SongModel> {
        val list = mutableListOf<SongModel>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        contentResolver.query(uri, projection, selection, null, sortOrder)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

            while (cursor.moveToNext()) {
                val albumId = cursor.getLong(albumIdCol)
                list.add(
                    SongModel(
                        id = cursor.getLong(idCol),
                        title = cursor.getString(titleCol),
                        artist = cursor.getString(artistCol),
                        duration = cursor.getLong(durationCol),
                        path = cursor.getString(dataCol),
                        albumArt = "content://media/external/audio/albumart/$albumId"
                    )
                )
            }
        }
        return list
    }
}