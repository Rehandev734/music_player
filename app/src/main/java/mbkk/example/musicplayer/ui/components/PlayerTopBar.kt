package mbkk.example.musicplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mbkk.example.musicplayer.ui.theme.MusicPlayerTheme

@Composable
fun PlayerTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = 48.dp, end = 20.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularIconButton(
            iconRes = mbkk.example.musicplayer.R.drawable.ic_arrow_back,
            onClick = onBackClick,
            size = 44.dp,
            padding = 10.dp,
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
            tint = MaterialTheme.colorScheme.onSurface,
            contentDescription = "Back"
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "NOW PLAYING",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.15.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Symmetrical spacer matching back button
        Box(modifier = Modifier.size(44.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun PlayerTopBarPreview() {
    MusicPlayerTheme {
        PlayerTopBar(onBackClick = {})
    }
}
