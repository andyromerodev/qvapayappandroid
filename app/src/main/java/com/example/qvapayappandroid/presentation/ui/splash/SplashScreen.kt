package com.example.qvapayappandroid.presentation.ui.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.qvapayappandroid.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun SplashScreen(
    onNavigationReady: (String) -> Unit,
    viewModel: SplashViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var shouldExit by remember { mutableStateOf(false) }
    var hasNavigated by remember { mutableStateOf(false) }

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.doublecheck)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1
    )

    val alpha by animateFloatAsState(
        targetValue = if (shouldExit) 0f else 1f,
        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
        label = "splash_alpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (shouldExit) 1.04f else 1f,
        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
        label = "splash_scale"
    )

    LaunchedEffect(progress) {
        val destination = uiState.navigationDestination
        if (progress >= 1f && destination != null) {
            shouldExit = true
        }
    }

    LaunchedEffect(alpha) {
        val destination = uiState.navigationDestination
        if (alpha <= 0f && destination != null && !hasNavigated) {
            hasNavigated = true
            onNavigationReady(destination)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF716EC5))
            .alpha(alpha)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(160.dp),
            contentScale = ContentScale.Fit
        )
    }
}
