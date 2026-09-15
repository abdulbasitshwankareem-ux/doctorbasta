package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ChatMessageItem
import com.example.ui.components.ChatInputBar
import com.example.ui.components.DailyFactCard
import com.example.ui.components.HistoryDrawerContent
import com.example.ui.components.KilluaBackgroundWatermark
import com.example.ui.components.KilluaHeaderBadge
import com.example.ui.components.SmartActionChips
import com.example.ui.components.ToolsDialog
import com.example.ui.components.WelcomeScreen
import com.example.ui.theme.BgDark
import com.example.ui.theme.ElectricBlueGlow
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val conversations by viewModel.conversations.collectAsState()
    val activeConvId by viewModel.activeConversationId.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val companionState by viewModel.companionState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val dailyFact by viewModel.dailyFact.collectAsState()
    val attachedFile by viewModel.attachedFile.collectAsState()
    val showToolsDialog by viewModel.showToolsDialog.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto scroll to bottom when new messages arrive
    LaunchedEffect(messages.size, messages.lastOrNull()?.content?.length) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Kurdish is Right-to-Left (RTL)
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = BgDark,
                    drawerContentColor = TextPrimary
                ) {
                    HistoryDrawerContent(
                        conversations = conversations,
                        activeConversationId = activeConvId,
                        onSelectConversation = { convId ->
                            viewModel.selectConversation(convId)
                            scope.launch { drawerState.close() }
                        },
                        onNewChat = {
                            viewModel.createNewChat()
                            scope.launch { drawerState.close() }
                        },
                        onRenameConversation = { id, newTitle ->
                            viewModel.renameConversation(id, newTitle)
                        },
                        onDeleteConversation = { id ->
                            viewModel.deleteConversation(id)
                        }
                    )
                }
            }
        ) {
            Scaffold(
                modifier = modifier
                    .fillMaxSize()
                    .background(BgDark)
                    .imePadding(),
                topBar = {
                    // Top App Header
                    Surface(
                        color = SurfaceDark.copy(alpha = 0.95f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Drawer Menu button
                                IconButton(
                                    onClick = { scope.launch { drawerState.open() } },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .testTag("drawer_menu_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "مێژووی چاتەکان",
                                        tint = TextPrimary
                                    )
                                }

                                // Killua Companion Badge
                                KilluaHeaderBadge(state = companionState)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Google Sheets & Image Gen Tools Dialog Button
                                IconButton(
                                    onClick = { viewModel.setShowToolsDialog(true) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TableChart,
                                        contentDescription = "ئامرازەکان",
                                        tint = ElectricBlueGlow,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }

                                // New Chat Quick Button
                                IconButton(
                                    onClick = { viewModel.createNewChat() },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "چاتی نوێ",
                                        tint = TextPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                bottomBar = {
                    Column {
                        // Smart Action Chips
                        SmartActionChips(
                            fileType = attachedFile?.mimeType,
                            onActionSelected = { prompt ->
                                inputText = prompt
                            }
                        )

                        // Chat Input Bar
                        ChatInputBar(
                            inputText = inputText,
                            onInputChanged = { inputText = it },
                            onSendMessage = {
                                val text = inputText
                                inputText = ""
                                viewModel.sendMessage(text, context)
                            },
                            attachedFile = attachedFile,
                            onFileSelected = { fileInfo ->
                                viewModel.attachFile(fileInfo)
                            },
                            onRemoveAttachment = {
                                viewModel.removeAttachment()
                            },
                            isLoading = isLoading
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(BgDark)
                ) {
                    // Subtle Animated Killua Watermark in Background
                    KilluaBackgroundWatermark(state = companionState)

                    Column(modifier = Modifier.fillMaxSize()) {
                        // Daily Information Card at top
                        DailyFactCard(
                            fact = dailyFact,
                            onAskAi = { prompt ->
                                viewModel.sendMessage(prompt, context)
                            },
                            onNextFact = {
                                viewModel.rotateDailyFact()
                            }
                        )

                        if (messages.isEmpty()) {
                            // Welcome / Empty State Screen with suggestion chips
                            WelcomeScreen(
                                onSuggestionClick = { prompt ->
                                    viewModel.sendMessage(prompt, context)
                                }
                            )
                        } else {
                            // Chat Messages List
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(messages, key = { it.id }) { msg ->
                                    ChatMessageItem(
                                        message = msg,
                                        onRegenerate = {
                                            viewModel.regenerateLastResponse(context)
                                        },
                                        onCreatePdf = { content ->
                                            viewModel.createPdfForMessage(content, context)
                                        },
                                        onCreateExcel = { content ->
                                            viewModel.createExcelForMessage(content, context)
                                        }
                                    )
                                }

                                // Typing / Thinking Indicator
                                if (isLoading) {
                                    item {
                                        ThinkingIndicator()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Tools Dialog (Google Sheets & Image Gen)
        if (showToolsDialog) {
            ToolsDialog(
                onDismiss = { viewModel.setShowToolsDialog(false) },
                onSendPrompt = { prompt ->
                    viewModel.sendMessage(prompt, context)
                }
            )
        }
    }
}

@Composable
fun ThinkingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "thinking_dots")
    val dotScale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_scale"
    )

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .scale(dotScale)
                    .clip(CircleShape)
                    .background(ElectricBlueGlow)
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .scale(1.8f - dotScale)
                    .clip(CircleShape)
                    .background(PrimaryBlue)
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .scale(dotScale)
                    .clip(CircleShape)
                    .background(ElectricBlueGlow)
            )
        }

        Text(
            text = "Basit AI خەریکی بیرکردنەوەیە...",
            color = TextSecondary,
            fontSize = 12.sp
        )
    }
}
