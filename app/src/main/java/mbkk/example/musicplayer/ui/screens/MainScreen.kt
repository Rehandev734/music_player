package mbkk.example.musicplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mbkk.example.musicplayer.R
import mbkk.example.musicplayer.SongModel
import mbkk.example.musicplayer.ui.components.MiniPlayerBar
import mbkk.example.musicplayer.ui.components.SearchBar
import mbkk.example.musicplayer.ui.components.SongItem
import mbkk.example.musicplayer.ui.theme.MusicPlayerTheme

@Composable
fun MainScreen(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    songs: List<SongModel>,
    onSongClick: (SongModel, Int) -> Unit,
    miniPlayerSong: SongModel?,
    isMiniPlayerPlaying: Boolean,
    onMiniPlayPause: () -> Unit,
    onMiniNext: () -> Unit,
    onMiniPlayerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 48.dp, end = 20.dp, bottom = 14.dp)
            ) {
                Text(
                    text = "WELCOME BACK",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your Library",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            // Section Header with Count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Songs",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${songs.size} tracks",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Empty State or Song List
            if (songs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_music),
                            contentDescription = "No Songs",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No matching tracks" else "No songs in library",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Try searching for another title or artist"
                            else "Add music files to your device storage",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    itemsIndexed(
                        items = songs,
                        key = { _, song -> song.id }
                    ) { index, song ->
                        SongItem(
                            song = song,
                            onClick = { onSongClick(song, index) }
                        )
                    }
                }
            }
        }

        // Floating Mini Player with smooth motion
        androidx.compose.animation.AnimatedVisibility(
            visible = miniPlayerSong != null,
            enter = androidx.compose.animation.slideInVertically(
                initialOffsetY = { it },
                animationSpec = androidx.compose.animation.core.tween(250)
            ) + androidx.compose.animation.fadeIn(
                animationSpec = androidx.compose.animation.core.tween(250)
            ),
            exit = androidx.compose.animation.slideOutVertically(
                targetOffsetY = { it },
                animationSpec = androidx.compose.animation.core.tween(200)
            ) + androidx.compose.animation.fadeOut(
                animationSpec = androidx.compose.animation.core.tween(200)
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            if (miniPlayerSong != null) {
                MiniPlayerBar(
                    song = miniPlayerSong,
                    isPlaying = isMiniPlayerPlaying,
                    onPlayPauseClick = onMiniPlayPause,
                    onNextClick = onMiniNext,
                    onBarClick = onMiniPlayerClick
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    MusicPlayerTheme {
        MainScreen(
            searchQuery = "",
            onSearchQueryChange = {},
            songs = listOf(
                SongModel(1L, "Sample Melody", "Artist One", 195000L, "", ""),
                SongModel(2L, "Electronic Beat", "Artist Two", 240000L, "", "")
            ),
            onSongClick = { _, _ -> },
            miniPlayerSong = SongModel(1L, "Sample Melody", "Artist One", 195000L, "", ""),
            isMiniPlayerPlaying = true,
            onMiniPlayPause = {},
            onMiniNext = {},
            onMiniPlayerClick = {}
        )
    }
}
