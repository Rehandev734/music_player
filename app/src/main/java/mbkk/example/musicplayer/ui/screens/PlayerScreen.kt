package mbkk.example.musicplayer.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import mbkk.example.musicplayer.R
import mbkk.example.musicplayer.SongModel
import mbkk.example.musicplayer.ui.components.CircularIconButton
import mbkk.example.musicplayer.ui.components.PlayerTopBar
import mbkk.example.musicplayer.ui.components.SongListBottomSheet
import mbkk.example.musicplayer.ui.theme.MusicPlayerTheme
import mbkk.example.musicplayer.ui.theme.TonalSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    song: SongModel?,
    isPlaying: Boolean,
    isShuffleOn: Boolean,
    currentPosition: Int,
    duration: Int,
    songsList: List<SongModel>,
    currentIndex: Int,
    isSheetOpen: Boolean,
    onBackClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onMenuClick: () -> Unit,
    onSeek: (Int) -> Unit,
    onDismissSheet: () -> Unit,
    onSongSelectedFromSheet: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            PlayerTopBar(onBackClick = onBackClick)

            Spacer(modifier = Modifier.height(8.dp))

            // Hero Album Art
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1f)
                    .shadow(20.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(TonalSurface, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = song?.albumArt,
                    contentDescription = song?.title ?: "Album Art",
                    placeholder = painterResource(id = R.drawable.ic_music),
                    error = painterResource(id = R.drawable.ic_music),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Track Info Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = song?.title ?: "Song Title",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = song?.artist ?: "Artist Name",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Progress & Timeline Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
            ) {
                Slider(
                    value = currentPosition.toFloat().coerceIn(0f, duration.toFloat().coerceAtLeast(1f)),
                    onValueChange = { onSeek(it.toInt()) },
                    valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = formatTime(currentPosition),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = formatTime(duration),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Controls Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 40.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle Button
                CircularIconButton(
                    iconRes = R.drawable.ic_shuffle,
                    onClick = onShuffleClick,
                    size = 48.dp,
                    padding = 13.dp,
                    backgroundColor = if (isShuffleOn) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.surfaceVariant,
                    tint = if (isShuffleOn) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    contentDescription = "Shuffle"
                )

                // Previous Button
                CircularIconButton(
                    iconRes = R.drawable.ic_skip_previous,
                    onClick = onPreviousClick,
                    size = 54.dp,
                    padding = 14.dp,
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = "Previous"
                )

                // Play / Pause Button
                CircularIconButton(
                    iconRes = if (isPlaying) R.drawable.ic_pause
                    else R.drawable.ic_play_arrow,
                    onClick = onPlayPauseClick,
                    size = 72.dp,
                    padding = 18.dp,
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    contentDescription = if (isPlaying) "Pause" else "Play"
                )

                // Next Button
                CircularIconButton(
                    iconRes = R.drawable.ic_skip_next,
                    onClick = onNextClick,
                    size = 54.dp,
                    padding = 14.dp,
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = "Next"
                )

                // Menu / Queue Button
                CircularIconButton(
                    iconRes = R.drawable.ic_queue_music,
                    onClick = onMenuClick,
                    size = 48.dp,
                    padding = 13.dp,
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    contentDescription = "Song List"
                )
            }
        }

        // Song List Bottom Sheet
        if (isSheetOpen) {
            SongListBottomSheet(
                songs = songsList,
                currentIndex = currentIndex,
                onSongSelected = onSongSelectedFromSheet,
                onDismissRequest = onDismissSheet
            )
        }
    }
}

@SuppressLint("DefaultLocale")
private fun formatTime(ms: Int): String {
    val minutes = (ms / 1000) / 60
    val seconds = (ms / 1000) % 60
    return String.format("%d:%02d", minutes, seconds)
}

@Preview(showBackground = true)
@Composable
private fun PlayerScreenPreview() {
    MusicPlayerTheme {
        PlayerScreen(
            song = SongModel(1L, "Now Playing Track", "Featured Artist", 220000L, "", ""),
            isPlaying = true,
            isShuffleOn = true,
            currentPosition = 65000,
            duration = 220000,
            songsList = listOf(
                SongModel(1L, "Now Playing Track", "Featured Artist", 220000L, "", "")
            ),
            currentIndex = 0,
            isSheetOpen = false,
            onBackClick = {},
            onShuffleClick = {},
            onPreviousClick = {},
            onPlayPauseClick = {},
            onNextClick = {},
            onMenuClick = {},
            onSeek = {},
            onDismissSheet = {},
            onSongSelectedFromSheet = {}
        )
    }
}
