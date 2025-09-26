package com.example.qvapayappandroid.presentation.ui.p2p.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.example.qvapayappandroid.R

@Composable
fun P2PShimmerEffect() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth().background(color = colorResource(R.color.qvapay_surface_light))
    ) {
        items(4) {
            P2POfferCardShimmer()
        }
    }
}

@Composable
private fun P2POfferCardShimmer() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.qvapay_surface_light)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = 1.dp,
            color = colorResource(id = R.color.qvapay_purple_light)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            // Primera fila: avatar, user, rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar shimmer
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .shimmerEffect()
                )

                Spacer(Modifier.width(7.dp))

                // Username shimmer
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(16.dp)
                        .shimmerEffect()
                )

                Spacer(modifier = Modifier.weight(1f))

                // Rating shimmer
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(20.dp)
                        .clip(CircleShape)
                        .shimmerEffect()
                )
            }

            // Message shimmer
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(14.dp)
                    .shimmerEffect()
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Minicards shimmer
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Primera fila
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        MiniCardShimmer()
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        MiniCardShimmer()
                    }
                }
                // Segunda fila
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        MiniCardShimmer()
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        MiniCardShimmer()
                    }
                }
            }

            Spacer(modifier = Modifier.height(7.dp))

            // Chips shimmer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .shimmerEffect()
                )
                Box(
                    modifier = Modifier
                        .width(50.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .shimmerEffect()
                )
            }
        }
    }
}

@Composable
private fun MiniCardShimmer() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(8.dp))
            .shimmerEffect()
    )
}

@Composable
private fun Modifier.shimmerEffect(): Modifier = composed {
    val purpleLight = colorResource(id = R.color.qvapay_purple_light)
    val surfaceLight = colorResource(id = R.color.qvapay_surface_light)
    
    val shimmerColors = listOf(
        purpleLight.copy(alpha = 0.3f),
        surfaceLight.copy(alpha = 0.1f),
        purpleLight.copy(alpha = 0.3f),
    )

    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerAnimation"
    )

    background(
        brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnim.value, y = translateAnim.value)
        )
    )
}