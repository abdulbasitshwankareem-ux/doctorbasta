package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.MessageEntity
import com.example.ui.theme.ElectricBlueGlow
import com.example.ui.theme.ModelBubbleBg
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueDark
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UserBubbleBg
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatMessageItem(
    message: MessageEntity,
    onRegenerate: () -> Unit,
    onCreatePdf: (String) -> Unit,
    onCreateExcel: (String) -> Unit,
    onOpenApiKeyDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isUser = message.role == "user"
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isCopied by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            // Basit AI Companion avatar
            Box(
                modifier = Modifier
                    .padding(top = 4.dp, end = 8.dp)
                    .size(34.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_anime_assistant),
                    contentDescription = "Basit AI",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(1.dp, PrimaryBlue, CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Column(
            modifier = Modifier.widthIn(max = 340.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            // Attachment Preview Card (if attached)
            if (!message.attachedFileName.isNullOrBlank()) {
                AttachmentPreviewCard(
                    fileName = message.attachedFileName,
                    fileType = message.attachedFileType ?: "",
                    fileUri = message.attachedFileUri,
                    isUser = isUser
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Message Bubble Surface
            Surface(
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isUser) 18.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 18.dp
                ),
                color = if (isUser) UserBubbleBg else ModelBubbleBg,
                border = BorderStroke(
                    1.dp,
                    if (isUser) PrimaryBlue.copy(alpha = 0.5f)
                    else PrimaryBlue.copy(alpha = 0.25f)
                ),
                shadowElevation = if (isUser) 2.dp else 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    if (message.isError) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "هەڵە لە وەڵامدانەوەدا",
                                color = Color(0xFFEF4444),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Render formatted text
                    MarkdownFormattedText(
                        text = message.content,
                        isUser = isUser
                    )

                    // If missing API key prompt, display quick action button
                    if (!isUser && message.content.contains("Gemini API")) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onOpenApiKeyDialog,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "دانانی کلیلی API لێرە",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Generated File Card (PDF, Excel, TXT, etc.)
                    if (!message.generatedFilePath.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        GeneratedFileCard(
                            filePath = message.generatedFilePath,
                            fileType = message.generatedFileType ?: "pdf",
                            fileName = message.generatedFileName ?: "فایل"
                        )
                    }

                    // Timestamp
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                        color = if (isUser) Color.White.copy(alpha = 0.6f) else TextMuted,
                        fontSize = 10.sp,
                        modifier = Modifier.align(if (isUser) Alignment.Start else Alignment.End)
                    )
                }
            }

            // Action Row for AI responses
            if (!isUser && message.content.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .padding(top = 4.dp, start = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A).copy(alpha = 0.7f))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Copy action (کۆپی)
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(message.content))
                            isCopied = true
                            scope.launch {
                                delay(2000)
                                isCopied = false
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "کۆپی",
                            tint = if (isCopied) Color(0xFF34D399) else TextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Regenerate action (دووبارە وەڵامدانەوە)
                    IconButton(
                        onClick = onRegenerate,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "دووبارە دروستکردن",
                            tint = TextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Share action (هاوبەشکردن)
                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, message.content)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "هاوبەشکردنی وەڵامی Basit AI"))
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "هاوبەشکردن",
                            tint = TextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Export PDF chip
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onCreatePdf(message.content) }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = ElectricBlueGlow,
                            modifier = Modifier.size(13.dp)
                        )
                        Text("PDF", color = ElectricBlueGlow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    // Export Excel chip
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onCreateExcel(message.content) }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TableChart,
                            contentDescription = null,
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(13.dp)
                        )
                        Text("Excel", color = Color(0xFF34D399), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Displays attached file with thumbnail or icon
 */
@Composable
fun AttachmentPreviewCard(
    fileName: String,
    fileType: String,
    fileUri: String?,
    isUser: Boolean
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isUser) PrimaryBlueDark.copy(alpha = 0.8f) else SurfaceCard,
        border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(6.dp)) {
            if (fileType.startsWith("image") && !fileUri.isNullOrBlank()) {
                AsyncImage(
                    model = fileUri,
                    contentDescription = fileName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = when {
                        fileType.contains("pdf") -> Icons.Default.PictureAsPdf
                        fileType.contains("sheet") || fileType.contains("excel") || fileType.contains("csv") -> Icons.Default.TableChart
                        fileType.contains("video") -> Icons.Default.Videocam
                        fileType.contains("image") -> Icons.Default.Image
                        else -> Icons.Default.FilePresent
                    },
                    contentDescription = null,
                    tint = ElectricBlueGlow,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = fileName,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Downloadable / shareable generated file card
 */
@Composable
fun GeneratedFileCard(
    filePath: String,
    fileType: String,
    fileName: String
) {
    val context = LocalContext.current
    val file = File(filePath)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                try {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, when (fileType.lowercase()) {
                            "pdf" -> "application/pdf"
                            "csv", "excel" -> "text/csv"
                            else -> "text/plain"
                        })
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Fallback to share intent
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        putExtra(Intent.EXTRA_TEXT, file.readText())
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "هاوبەشکردنی فایل"))
                }
            },
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF091322),
        border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(ElectricBlueGlow, PrimaryBlue)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryBlue.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (fileType.equals("pdf", true)) Icons.Default.PictureAsPdf else Icons.Default.TableChart,
                        contentDescription = null,
                        tint = ElectricBlueGlow,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = fileName,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "فایلی ئامادەکراو • کلیک بکە بۆ کردنەوە",
                        color = ElectricBlueGlow,
                        fontSize = 10.sp
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "داگرتن",
                tint = ElectricBlueGlow,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Lightweight clean markdown text renderer for Kurdish AI chat
 */
@Composable
fun MarkdownFormattedText(
    text: String,
    isUser: Boolean,
    modifier: Modifier = Modifier
) {
    val lines = text.split("\n")

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (line in lines) {
            val trimmed = line.trim()
            when {
                // Code block line or monospace
                trimmed.startsWith("```") -> {
                    // Code block fence
                    Spacer(modifier = Modifier.height(2.dp))
                }
                trimmed.startsWith("# ") -> {
                    Text(
                        text = trimmed.removePrefix("# "),
                        color = if (isUser) Color.White else PrimaryBlueLight,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp
                    )
                }
                trimmed.startsWith("## ") -> {
                    Text(
                        text = trimmed.removePrefix("## "),
                        color = if (isUser) Color.White else PrimaryBlueLight,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 20.sp
                    )
                }
                trimmed.startsWith("### ") -> {
                    Text(
                        text = trimmed.removePrefix("### "),
                        color = if (isUser) Color.White else ElectricBlueGlow,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 19.sp
                    )
                }
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text(
                            text = "•",
                            color = if (isUser) Color.White else PrimaryBlueLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formatInlineBold(trimmed.substring(2)),
                            color = if (isUser) Color.White else TextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                    }
                }
                trimmed.startsWith("|") && trimmed.endsWith("|") -> {
                    // Table line
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF1E293B).copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)
                    ) {
                        Text(
                            text = trimmed,
                            color = ElectricBlueGlow,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
                else -> {
                    Text(
                        text = formatInlineBold(line),
                        color = if (isUser) Color.White else TextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            }
        }
    }
}

private fun formatInlineBold(input: String): String {
    // Simple inline bold cleaner for display
    return input.replace("**", "")
}
