package com.example.data.repository

import com.example.data.local.ChatDao
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ChatRepository(private val dao: ChatDao) {

    val allConversations: Flow<List<ConversationEntity>> = dao.getAllConversations()

    fun getMessages(conversationId: String): Flow<List<MessageEntity>> =
        dao.getMessagesForConversation(conversationId)

    suspend fun getMessagesList(conversationId: String): List<MessageEntity> =
        dao.getMessagesList(conversationId)

    suspend fun createConversation(initialTitle: String = "چاتی نوێ"): String {
        val id = UUID.randomUUID().toString()
        val conv = ConversationEntity(
            id = id,
            title = initialTitle,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            lastSnippet = ""
        )
        dao.insertConversation(conv)
        return id
    }

    suspend fun updateTitle(conversationId: String, newTitle: String) {
        dao.updateConversationTitle(conversationId, newTitle)
    }

    suspend fun updateSnippet(conversationId: String, snippet: String) {
        dao.updateConversationSnippet(conversationId, snippet)
    }

    suspend fun deleteConversation(conversationId: String) {
        dao.deleteConversationById(conversationId)
    }

    suspend fun addMessage(message: MessageEntity): Long {
        val id = dao.insertMessage(message)
        // Also update conversation's updatedAt and snippet
        val snippet = if (message.content.length > 50) {
            message.content.take(50) + "..."
        } else {
            message.content
        }
        dao.updateConversationSnippet(message.conversationId, snippet)
        return id
    }

    suspend fun updateMessage(id: Long, content: String) {
        dao.updateMessageContent(id, content)
    }
}
