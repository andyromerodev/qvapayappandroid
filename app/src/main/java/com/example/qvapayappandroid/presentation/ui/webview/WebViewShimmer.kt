package com.example.qvapayappandroid.presentation.ui.webview

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

@Composable
fun WebViewShimmer(
    modifier: Modifier = Modifier
) {
    // Fondo sólido y oscuro para tapar el WebView
    val bg = MaterialTheme.colorScheme.surfaceVariant

    // Helpers para variar brillo (0..1)
    fun Color.lighten(f: Float) = Color(
        red = red + (1f - red) * f,
        green = green + (1f - green) * f,
        blue = blue + (1f - blue) * f,
        alpha = alpha
    )
    fun Color.darken(f: Float) = Color(
        red = red * (1f - f),
        green = green * (1f - f),
        blue = blue * (1f - f),
        alpha = alpha
    )

    val dark = bg.darken(0.18f)
    val light = bg.lighten(0.28f) // banda más clara para que se note el “barrido”

    // Capturamos el tamaño para que el barrido cubra todo el ancho
    var size by remember { mutableStateOf(IntSize.Zero) }

    val transition = rememberInfiniteTransition(label = "ShimmerTransition")
    // Barrido más largo que el ancho para que entre y salga suavemente
    val xAnim by transition.animateFloat(
        initialValue = -2f * size.width,
        targetValue =  2f * size.width,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerX"
    )

    val brush = remember(size, xAnim, dark, light) {
        Brush.linearGradient(
            colors = listOf(dark, light, dark),
            start = Offset(xAnim, 0f),
            end   = Offset(xAnim + size.width, 0f)
        )
    }

    // Layout del skeleton
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bg) // 🔹 fondo opaco
            .onSizeChanged { size = it }
            .padding(0.dp)
    ) {
        // “AppBar” del sitio
        ShimmerBox(brush, Modifier.fillMaxWidth().height(64.dp))

        Column(Modifier.padding(16.dp)) {
            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ShimmerBox(brush, Modifier.width(120.dp).height(40.dp).clip(RoundedCornerShape(8.dp)))
                ShimmerBox(brush, Modifier.width(80.dp).height(32.dp).clip(RoundedCornerShape(16.dp)))
            }

            Spacer(Modifier.height(24.dp))

            ShimmerBox(brush, Modifier.fillMaxWidth(0.6f).height(28.dp).clip(RoundedCornerShape(4.dp)))
            Spacer(Modifier.height(16.dp))

            repeat(3) { i ->
                ShimmerBox(brush, Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(12.dp)))
                if (i < 2) Spacer(Modifier.height(16.dp))
            }

            Spacer(Modifier.height(24.dp))

            repeat(4) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    ShimmerBox(brush, Modifier.width(100.dp).height(16.dp).clip(RoundedCornerShape(2.dp)))
                    ShimmerBox(brush, Modifier.width(60.dp).height(16.dp).clip(RoundedCornerShape(2.dp)))
                    ShimmerBox(brush, Modifier.width(80.dp).height(16.dp).clip(RoundedCornerShape(2.dp)))
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun ShimmerBox(brush: Brush, modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(brush))
}
