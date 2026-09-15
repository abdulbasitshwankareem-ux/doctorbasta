package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricBlueGlow
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class AttachedFileInfo(
    val uri: Uri,
    val name: String,
    val mimeType: String,
    val sizeBytes: Long = 0
)

@Composable
fun ChatInputBar(
    inputText: String,
    onInputChanged: (String) -> Unit,
    onSendMessage: () -> Unit,
    attachedFile: AttachedFileInfo?,
    onFileSelected: (AttachedFileInfo) -> Unit,
    onRemoveAttachment: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isVoiceListening by remember { mutableStateOf(false) }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val contentResolver = context.contentResolver
            val mime = contentResolver.getType(uri) ?: "application/octet-stream"
            var name = "فایلی هاوپێچکراو"
            var size = 0L

            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) name = cursor.getString(nameIndex) ?: name
                    if (sizeIndex != -1) size = cursor.getLong(sizeIndex)
                }
            }

            onFileSelected(AttachedFileInfo(uri, name, mime, size))
        }
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val contentResolver = context.contentResolver
            val mime = contentResolver.getType(uri) ?: "image/jpeg"
            var name = "وێنەی هاوپێچ"
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex != -1) {
                    name = cursor.getString(nameIndex) ?: name
                }
            }
            onFileSelected(AttachedFileInfo(uri, name, mime))
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceDark.copy(alpha = 0.95f))
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        // Active Attachment Preview Chip
        AnimatedVisibility(
            visible = attachedFile != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            if (attachedFile != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceCard,
                    border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = when {
                                    attachedFile.mimeType.contains("image") -> Icons.Default.Image
                                    attachedFile.mimeType.contains("pdf") -> Icons.Default.PictureAsPdf
                                    attachedFile.mimeType.contains("sheet") || attachedFile.mimeType.contains("csv") -> Icons.Default.TableChart
                                    attachedFile.mimeType.contains("video") -> Icons.Default.Videocam
                                    else -> Icons.Default.Description
                                },
                                contentDescription = null,
                                tint = ElectricBlueGlow,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = attachedFile.name,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                                Text(
                                    text = "ئامادەیە بۆ شیکارکردن لەلایەن Basit AI",
                                    color = ElectricBlueGlow,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = onRemoveAttachment,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "سڕینەوە",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Main rounded input composer
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF090E17),
            border = BorderStroke(
                1.dp,
                Brush.horizontalGradient(
                    listOf(
                        PrimaryBlue.copy(alpha = 0.5f),
                        ElectricBlueGlow.copy(alpha = 0.3f)
                    )
                )
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // File upload button
                IconButton(
                    onClick = { filePickerLauncher.launch("*/*") },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "بارکردنی فایل",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Image upload button
                IconButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "بارکردنی وێنە",
                        tint = ElectricBlueGlow,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Text Input Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp, vertical = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (inputText.isEmpty()) {
                        Text(
                            text = "پرسیارێک لە Basit AI بکە بە کوردی...",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }

                    BasicTextField(
                        value = inputText,
                        onValueChange = onInputChanged,
                        textStyle = TextStyle(
                            color = TextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        ),
                        cursorBrush = SolidColor(ElectricBlueGlow),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("chat_input_field"),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputText.isNotBlank() || attachedFile != null) {
                                    onSendMessage()
                                }
                            }
                        ),
                        maxLines = 4
                    )
                }

                // Voice / mic input simulation button
                IconButton(
                    onClick = {
                        isVoiceListening = !isVoiceListening
                        if (isVoiceListening && inputText.isEmpty()) {
                            onInputChanged("سڵاو Basit AI، دەتوانیت یارمەتیم بدەیت؟")
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "دەنگ",
                        tint = if (isVoiceListening) ElectricBlueGlow else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Send Button
                val canSend = (inputText.isNotBlank() || attachedFile != null) && !isLoading
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            if (canSend) Brush.linearGradient(listOf(PrimaryBlue, ElectricBlueGlow))
                            else SolidColor(Color(0xFF1E293B))
                        )
                        .clickable(enabled = canSend) { onSendMessage() }
                        .testTag("send_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "بنێرە",
                        tint = if (canSend) Color.White else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
