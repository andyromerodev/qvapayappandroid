package com.example.qvapayappandroid.presentation.ui.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.qvapayappandroid.R
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun SplashScreen(
    onNavigationReady: (String) -> Unit,
    viewModel: SplashViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var startAnimation by remember { mutableStateOf(false) }
    var startFadeOut by remember { mutableStateOf(false) }
    
    val alphaAnimation = animateFloatAsState(
        targetValue = when {
            startFadeOut -> 0f
            startAnimation -> 1f
            else -> 0f
        },
        animationSpec = tween(
            durationMillis = if (startFadeOut) 500 else 2000
        ),
        label = "splash_alpha"
    )
    
    LaunchedEffect(Unit) {
        startAnimation = true
    }
    
    LaunchedEffect(uiState.navigationDestination) {
        uiState.navigationDestination?.let { destination ->
            startFadeOut = true
            delay(500) // Esperar a que termine el fade-out
            onNavigationReady(destination)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.qvapay),
            contentDescription = "QvaPay Logo",
            modifier = Modifier
                .width(280.dp)
                .height(68.dp)
                .alpha(alphaAnimation.value)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.qvapay),  
                contentDescription = "QvaPay Logo",
                modifier = Modifier
                    .width(280.dp)
                    .height(68.dp)
            )
        }
    }
}