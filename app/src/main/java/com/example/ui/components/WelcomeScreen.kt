package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GTranslate
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.ElectricBlueGlow
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class WelcomeSuggestion(
    val title: String,
    val prompt: String,
    val icon: ImageVector,
    val iconColor: Color
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WelcomeScreen(
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val suggestions = listOf(
        WelcomeSuggestion(
            title = "📌 زانیارییەکی سەیرم پێ بڵێ",
            prompt = "سڵاو Basit AI، زانیارییەکی سەرسوڕهێنەر و سەرنجڕاکێشم لەسەر زانست یان گەردوون پێ بڵێ بە زمانی کوردی.",
            icon = Icons.Default.Lightbulb,
            iconColor = Color(0xFFFBBF24)
        ),
        WelcomeSuggestion(
            title = "📄 PDF ـێکم بۆ دروست بکە",
            prompt = "تکایە بەڵگەنامەیەکی فەرمی بە زمانی کوردی لەسەر گرنگی فێربوونی پرۆگرامسازی ئامادە بکە و بیکە بە PDF.",
            icon = Icons.Default.PictureAsPdf,
            iconColor = Color(0xFFEF4444)
        ),
        WelcomeSuggestion(
            title = "📊 خشتەیەکی Excel دروست بکە",
            prompt = "خشتەیەکی خەرجی و داهاتی مانگانە بە ستوونی ڕێکخراو بە فۆرماتی Excel/CSV بە کوردی بۆ دروست بکە.",
            icon = Icons.Default.TableChart,
            iconColor = Color(0xFF10B981)
        ),
        WelcomeSuggestion(
            title = "🔍 وێنەیەکم بۆ شیکاربکە",
            prompt = "ئامادەم وێنەیەک هاوپێچ بکەم، دەتوانیت بە کوردی هەموو وردەکارییەکانی وێنەکەم بۆ ڕوون بکەیتەوە؟",
            icon = Icons.Default.Image,
            iconColor = ElectricBlueGlow
        ),
        WelcomeSuggestion(
            title = "🌐 ئەم دەقە بۆم وەربگێڕە",
            prompt = "دەتوانیت هەر تێکست یان بابەتێکم بە زمانی ئینگلیزی یان عەرەبی هەبێت بۆ کوردییەکی زۆر پاراو و ڕوون وەربگێڕیت؟",
            icon = Icons.Default.GTranslate,
            iconColor = PrimaryBlueLight
        )
    )

    val infiniteTransition = rememberInfiniteTransition(label = "avatar_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatar_scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Glowing Anime Companion Hero Avatar
        Box(
            modifier = Modifier.size(110.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(pulseScale)
                    .blur(24.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                ElectricBlueGlow.copy(alpha = 0.6f),
                                PrimaryBlue.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )

            Image(
                painter = painterResource(id = R.drawable.img_anime_assistant),
                contentDescription = "Basit AI Hero",
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .border(
                        BorderStroke(
                            2.dp,
                            Brush.linearGradient(listOf(ElectricBlueGlow, PrimaryBlue))
                        ),
                        CircleShape
                    ),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Title
        Text(
            text = "بەخێربێیت بۆ Basit AI 🤖",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "چی دەتوانم بۆت بکەم؟",
            color = PrimaryBlueLight,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "یاریدەدەری زیرەکی کوردی بۆ وەڵامدانەوە، شیکاری وێنە، دروستکردنی فایلی PDF و Excel",
            color = TextSecondary,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(26.dp))

        // Suggestion Grid / Flow
        Text(
            text = "پێشنیازەکان بۆ دەستپێکردن:",
            color = TextMuted,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            suggestions.forEach { suggestion ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SurfaceCard,
                    border = BorderStroke(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(
                                PrimaryBlue.copy(alpha = 0.4f),
                                ElectricBlueGlow.copy(alpha = 0.15f)
                            )
                        )
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSuggestionClick(suggestion.prompt) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0F172A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = suggestion.icon,
                                contentDescription = null,
                                tint = suggestion.iconColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = suggestion.title,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )

                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = PrimaryBlueLight,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
