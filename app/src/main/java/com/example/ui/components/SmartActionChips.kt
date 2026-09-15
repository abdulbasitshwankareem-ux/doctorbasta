package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GTranslate
import androidx.compose.material.icons.filled.ShortText
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricBlueGlow
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary

data class SmartAction(
    val title: String,
    val prompt: String
)

@Composable
fun SmartActionChips(
    fileType: String?,
    onActionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val actions = when {
        fileType?.startsWith("image") == true -> listOf(
            SmartAction("🔍 ئەم وێنەیە شیکاربکە", "ئەم وێنەیە بە وردی شیکاربکە، تێیدا چی دەبینیت و هەر شتێکی تایبەت هەیە ڕوونی بکەوە."),
            SmartAction("📝 نووسینەکەی بخوێنەوە", "هەر نووسینێک لە ناو ئەم وێنەیەدا دەبینیت بیخوێنەوە و دەریبهێنە بە کوردی."),
            SmartAction("🌐 وەریبگێڕە بۆ کوردی", "دەق و ناوەڕۆکی ناو ئەم وێنەیە بە کوردییەکی زۆر جوان و ڕوون وەربگێڕە.")
        )
        fileType?.contains("pdf") == true -> listOf(
            SmartAction("📄 کورتەی بکەوە", "ناوەڕۆکی ئەم پەڕگەی PDF ـە بە چەند خاڵێکی کورت و پوخت بۆم ڕوون بکەوە."),
            SmartAction("🌐 بۆ کوردی وەریگێڕە", "هەموو تێکست و زانیارییەکانی ئەم PDF ـە بۆ زمانی کوردی وەربگێڕە."),
            SmartAction("❓ پرسیارە گرنگەکان دەربهێنە", "گرنگترین پرسیار و خاڵە سەرەکییەکانی ناو ئەم PDF ـە دەربهێنە.")
        )
        fileType?.contains("sheet") == true || fileType?.contains("excel") == true || fileType?.contains("csv") == true -> listOf(
            SmartAction("📊 داتاکان شیکاربکە", "ئەم خشتە و داتایانە شیکاربکە و ئەنجامە سەرەکییەکانم پێ بڵێ."),
            SmartAction("📈 ڕاپۆرتی کوردی ئامادە بکە", "ڕاپۆرتێکی گشتگیر بە زمانی کوردی دەربارەی داتاکانی ئەم خشتەیە دروست بکە."),
            SmartAction("✨ خشتەیەکی نوێ دروست بکە", "پێشنیازی ڕێکخستنی نوێ بۆ ئەم خشتەیە بکە و فۆرماتێکی پاکترم پێ بدە.")
        )
        fileType?.contains("video") == true -> listOf(
            SmartAction("🎬 کورتەی ڤیدیۆ بکەوە", "ناوەڕۆکی سەرەکی ئەم ڤیدیۆیە و ڕووداوەکانی بۆم کورت بکەوە."),
            SmartAction("🎙️ قسەکان دەربهێنە", "قسە و وتووێژەکانی ناو ڤیدیۆکە بنووسەرەوە و دەقیان لێ دروست بکە."),
            SmartAction("🌐 وەرگێڕان بۆ کوردی", "قسەکانی ناو ئەم ڤیدیۆیە بە تەواوی وەربگێڕە بۆ زمانی کوردی.")
        )
        else -> listOf(
            SmartAction("✨ کورتکردنەوە", "ئەم بابەتە بە پوختی کورت بکەوە."),
            SmartAction("🌐 وەرگێڕان بۆ کوردی", "ئەم دەقە وەربگێڕە بۆ زمانی کوردی."),
            SmartAction("📄 دروستکردنی PDF", "ئەم زانیارییە بکە بە بەڵگەنامەی فەرمی PDF."),
            SmartAction("📊 دروستکردنی خشتە", "ئەم زانیارییانە بکە بە خشتەیەکی Excel.")
        )
    }

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(actions) { action ->
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SurfaceCard,
                border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.4f)),
                modifier = Modifier.clickable { onActionSelected(action.prompt) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = ElectricBlueGlow,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = action.title,
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
