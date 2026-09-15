package com.example.ui.screens

import android.app.Application
import android.content.Context
import android.graphics.BitmapFactory
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.file.FileGenerator
import com.example.data.local.ChatDatabase
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import com.example.data.model.DailyFact
import com.example.data.model.DailyFactProvider
import com.example.data.remote.GeminiService
import com.example.data.repository.ChatRepository
import com.example.ui.components.AttachedFileInfo
import com.example.ui.components.CompanionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChatRepository
    init {
        val db = ChatDatabase.getDatabase(application)
        repository = ChatRepository(db.chatDao())
    }

    val conversations: StateFlow<List<ConversationEntity>> = repository.allConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeConversationId = MutableStateFlow<String?>(null)
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val messages: StateFlow<List<MessageEntity>> = _activeConversationId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.getMessages(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _companionState = MutableStateFlow(CompanionState.IDLE)
    val companionState: StateFlow<CompanionState> = _companionState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _dailyFact = MutableStateFlow(DailyFactProvider.getTodayFact())
    val dailyFact: StateFlow<DailyFact> = _dailyFact.asStateFlow()
    private var dailyFactIndex = 0

    private val _attachedFile = MutableStateFlow<AttachedFileInfo?>(null)
    val attachedFile: StateFlow<AttachedFileInfo?> = _attachedFile.asStateFlow()

    private val _showToolsDialog = MutableStateFlow(false)
    val showToolsDialog: StateFlow<Boolean> = _showToolsDialog.asStateFlow()

    private val _showApiKeyDialog = MutableStateFlow(false)
    val showApiKeyDialog: StateFlow<Boolean> = _showApiKeyDialog.asStateFlow()

    private val _showApiKeyScreen = MutableStateFlow(false)
    val showApiKeyScreen: StateFlow<Boolean> = _showApiKeyScreen.asStateFlow()

    fun setShowToolsDialog(show: Boolean) {
        _showToolsDialog.value = show
    }

    fun setShowApiKeyDialog(show: Boolean) {
        _showApiKeyDialog.value = show
    }

    fun setShowApiKeyScreen(show: Boolean) {
        _showApiKeyScreen.value = show
    }

    fun rotateDailyFact() {
        dailyFactIndex = (dailyFactIndex + 1) % DailyFactProvider.facts.size
        _dailyFact.value = DailyFactProvider.facts[dailyFactIndex]
    }

    fun attachFile(file: AttachedFileInfo) {
        _attachedFile.value = file
    }

    fun removeAttachment() {
        _attachedFile.value = null
    }

    fun selectConversation(id: String) {
        _activeConversationId.value = id
    }

    fun createNewChat() {
        viewModelScope.launch {
            val newId = repository.createConversation("چاتی نوێ")
            _activeConversationId.value = newId
            _attachedFile.value = null
        }
    }

    fun renameConversation(id: String, newTitle: String) {
        viewModelScope.launch {
            repository.updateTitle(id, newTitle)
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            repository.deleteConversation(id)
            if (_activeConversationId.value == id) {
                _activeConversationId.value = null
            }
        }
    }

    fun sendMessage(userText: String, context: Context) {
        if (userText.isBlank() && _attachedFile.value == null) return

        viewModelScope.launch {
            var convId = _activeConversationId.value
            if (convId == null) {
                val title = if (userText.isNotBlank()) userText.take(28) else "چاتی نوێ"
                convId = repository.createConversation(title)
                _activeConversationId.value = convId
            }

            val currentAttachment = _attachedFile.value
            var attachmentBase64: String? = null
            val attachmentMime = currentAttachment?.mimeType

            // Read attachment data if image
            if (currentAttachment != null) {
                try {
                    withContext(Dispatchers.IO) {
                        if (currentAttachment.mimeType.startsWith("image")) {
                            val inputStream: InputStream? = context.contentResolver.openInputStream(currentAttachment.uri)
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            inputStream?.close()
                            if (bitmap != null) {
                                attachmentBase64 = GeminiService.bitmapToBase64(bitmap)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Save user message
            val userMsg = MessageEntity(
                conversationId = convId,
                role = "user",
                content = userText,
                attachedFileName = currentAttachment?.name,
                attachedFileType = currentAttachment?.mimeType,
                attachedFileUri = currentAttachment?.uri?.toString()
            )
            repository.addMessage(userMsg)

            // If conversation title is still default, update it with user's question
            if (userText.isNotBlank()) {
                val currentList = repository.getMessagesList(convId)
                if (currentList.size <= 2) {
                    repository.updateTitle(convId, userText.take(30))
                }
            }

            // Reset attachment input
            _attachedFile.value = null
            _isLoading.value = true
            _companionState.value = CompanionState.THINKING

            // Fetch recent history for context
            val historyEntities = repository.getMessagesList(convId)
            val historyPairs = historyEntities.takeLast(6).map { it.role to it.content }

            // Create placeholder model message in DB
            val modelMsgId = repository.addMessage(
                MessageEntity(
                    conversationId = convId,
                    role = "model",
                    content = ""
                )
            )

            // Stream response from Gemini
            var fullResponse = ""
            var hasStartedResponding = false

            GeminiService.streamChatResponse(
                prompt = userText.ifBlank { "تکایە ئەم فایلە بە زمانی کوردی شیکاربکە و ڕوونی بکەوە." },
                history = historyPairs,
                attachmentBase64 = attachmentBase64,
                attachmentMimeType = attachmentMime,
                context = context
            ).collect { chunk ->
                if (!hasStartedResponding) {
                    hasStartedResponding = true
                    _companionState.value = CompanionState.RESPONDING
                }
                fullResponse = chunk
                repository.updateMessage(modelMsgId, fullResponse)
            }

            // Check if user requested a downloadable file (PDF/Excel)
            val lowerText = userText.lowercase()
            if (lowerText.contains("pdf") || lowerText.contains("بەڵگەنامە")) {
                try {
                    val pdfFile = withContext(Dispatchers.IO) {
                        FileGenerator.createPdf(
                            context = context,
                            title = userText.take(30),
                            content = fullResponse
                        )
                    }
                    val updatedMsg = MessageEntity(
                        id = modelMsgId,
                        conversationId = convId,
                        role = "model",
                        content = fullResponse,
                        generatedFilePath = pdfFile.absolutePath,
                        generatedFileType = "pdf",
                        generatedFileName = pdfFile.name
                    )
                    repository.addMessage(updatedMsg)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (lowerText.contains("excel") || lowerText.contains("خشتە") || lowerText.contains("csv")) {
                try {
                    val csvFile = withContext(Dispatchers.IO) {
                        FileGenerator.createCsv(
                            context = context,
                            title = userText.take(30),
                            content = fullResponse
                        )
                    }
                    val updatedMsg = MessageEntity(
                        id = modelMsgId,
                        conversationId = convId,
                        role = "model",
                        content = fullResponse,
                        generatedFilePath = csvFile.absolutePath,
                        generatedFileType = "csv",
                        generatedFileName = csvFile.name
                    )
                    repository.addMessage(updatedMsg)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            _isLoading.value = false
            _companionState.value = CompanionState.IDLE
        }
    }

    fun regenerateLastResponse(context: Context) {
        val convId = _activeConversationId.value ?: return
        viewModelScope.launch {
            val list = repository.getMessagesList(convId)
            val lastUserMsg = list.lastOrNull { it.role == "user" }
            if (lastUserMsg != null) {
                sendMessage(lastUserMsg.content, context)
            }
        }
    }

    fun createPdfForMessage(content: String, context: Context) {
        val convId = _activeConversationId.value ?: return
        viewModelScope.launch {
            val pdfFile = withContext(Dispatchers.IO) {
                FileGenerator.createPdf(context, "بەڵگەنامەی Basit AI", content)
            }
            repository.addMessage(
                MessageEntity(
                    conversationId = convId,
                    role = "model",
                    content = "فایلی PDF بە سەرکەوتوویی ئامادەکرا. دەتوانیت لە ڕێگەی ئەم کارتەی خوارەوە بیکەیتەوە یان هاوبەشی بکەیت:",
                    generatedFilePath = pdfFile.absolutePath,
                    generatedFileType = "pdf",
                    generatedFileName = pdfFile.name
                )
            )
        }
    }

    fun createExcelForMessage(content: String, context: Context) {
        val convId = _activeConversationId.value ?: return
        viewModelScope.launch {
            val csvFile = withContext(Dispatchers.IO) {
                FileGenerator.createCsv(context, "خشتەی داتای Basit AI", content)
            }
            repository.addMessage(
                MessageEntity(
                    conversationId = convId,
                    role = "model",
                    content = "فایلی خشتەی Excel/CSV بە سەرکەوتوویی دروستکرا و پیتە کوردییەکان تێیدا پارێزراون:",
                    generatedFilePath = csvFile.absolutePath,
                    generatedFileType = "csv",
                    generatedFileName = csvFile.name
                )
            )
        }
    }
}
