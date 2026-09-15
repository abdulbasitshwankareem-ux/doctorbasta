package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["conversationId"])]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: String,
    val role: String, // "user" or "model"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val attachedFileName: String? = null,
    val attachedFileType: String? = null,
    val attachedFileUri: String? = null,
    val generatedFilePath: String? = null,
    val generatedFileType: String? = null, // "pdf", "excel", "csv", "txt"
    val generatedFileName: String? = null,
    val isError: Boolean = false
)
