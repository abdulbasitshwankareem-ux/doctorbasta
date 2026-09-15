package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.ElectricBlueGlow
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class CompanionState {
    IDLE,
    THINKING,
    RESPONDING
}

/**
 * Subtle background watermark of the white-haired anime companion
 * with soft breathing and glow animations.
 */
@Composable
fun KilluaBackgroundWatermark(
    state: CompanionState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "companion_bg_anim")

    // Breathing / floating vertical offset
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating_offset"
    )

    // Pulse scale for thinking / responding
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = if (state == CompanionState.THINKING) 0.98f else 1.0f,
        targetValue = if (state == CompanionState.THINKING) 1.05f else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (state == CompanionState.THINKING) 1200 else 4000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Glow alpha
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = when (state) {
            CompanionState.THINKING -> 0.45f
            CompanionState.RESPONDING -> 0.35f
            CompanionState.IDLE -> 0.15f
        },
        targetValue = when (state) {
            CompanionState.THINKING -> 0.85f
            CompanionState.RESPONDING -> 0.65f
            CompanionState.IDLE -> 0.28f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (state == CompanionState.THINKING) 900 else 2500,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Glowing aura behind character
        Box(
            modifier = Modifier
                .size(320.dp)
                .offset(y = floatOffset.dp)
                .scale(pulseScale)
                .blur(60.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if (state == CompanionState.THINKING) ElectricBlueGlow.copy(alpha = glowAlpha)
                            else PrimaryBlue.copy(alpha = glowAlpha * 0.7f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // The Character Image - Watermark style
        Image(
            painter = painterResource(id = R.drawable.img_anime_assistant),
            contentDescription = "Basit AI Companion",
            modifier = Modifier
                .size(360.dp)
                .offset(y = floatOffset.dp)
                .scale(pulseScale)
                .alpha(if (state == CompanionState.THINKING) 0.28f else 0.16f)
                .clip(RoundedCornerShape(32.dp)),
            contentScale = ContentScale.Crop
        )
    }
}

/**
 * Compact top bar companion avatar with status indicator and glow
 */
@Composable
fun KilluaHeaderBadge(
    state: CompanionState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "badge_anim")

    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (state == CompanionState.THINKING) 600 else 1800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badge_glow"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.9f))
            .border(
                BorderStroke(
                    1.dp,
                    if (state == CompanionState.THINKING) ElectricBlueGlow.copy(alpha = pulseGlow)
                    else PrimaryBlue.copy(alpha = 0.35f)
                ),
                RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Avatar circle with glow
        Box(
            modifier = Modifier.size(28.dp),
            contentAlignment = Alignment.Center
        ) {
            if (state != CompanionState.IDLE) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .scale(pulseGlow)
                        .blur(4.dp)
                        .background(ElectricBlueGlow.copy(alpha = 0.5f), CircleShape)
                )
            }

            Image(
                painter = painterResource(id = R.drawable.img_anime_assistant),
                contentDescription = "Companion Avatar",
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .border(1.dp, PrimaryBlue, CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        // Status text in Kurdish
        Column {
            Text(
                text = "Basit AI",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Status dot
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            when (state) {
                                CompanionState.THINKING -> ElectricBlueGlow
                                CompanionState.RESPONDING -> Color(0xFF34D399)
                                CompanionState.IDLE -> Color(0xFF60A5FA)
                            }
                        )
                )
                Text(
                    text = when (state) {
                        CompanionState.THINKING -> "بیردەکاتەوە..."
                        CompanionState.RESPONDING -> "وەڵام دەداتەوە..."
                        CompanionState.IDLE -> "ئامادەیە"
                    },
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }
        }
    }
}
