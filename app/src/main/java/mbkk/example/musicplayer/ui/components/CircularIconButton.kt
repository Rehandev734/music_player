package mbkk.example.musicplayer.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import mbkk.example.musicplayer.ui.theme.CharcoalSurface
import mbkk.example.musicplayer.ui.theme.MusicPlayerTheme
import mbkk.example.musicplayer.ui.theme.TextPureWhite

@Composable
fun CircularIconButton(
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    padding: Dp = 14.dp,
    backgroundColor: Color = CharcoalSurface,
    tint: Color = TextPureWhite,
    contentDescription: String? = null
) {
    Box(
        modifier = modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = Color.White),
                onClick = onClick
            )
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(size - (padding * 2))
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CircularIconButtonPreview() {
    MusicPlayerTheme {
        CircularIconButton(
            iconRes = mbkk.example.musicplayer.R.drawable.ic_play_arrow,
            onClick = {}
        )
    }
}
