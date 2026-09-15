package com.example.data.file

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileGenerator {

    /**
     * Generates a real PDF file with the given title and body text.
     * Saved to app's documents directory.
     */
    fun createPdf(context: Context, title: String, content: String): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 dimensions
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // Background
        val bgPaint = Paint().apply { color = Color.WHITE }
        canvas.drawRect(0f, 0f, 595f, 842f, bgPaint)

        // Header decorative banner
        val headerPaint = Paint().apply { color = Color.parseColor("#0F172A") }
        canvas.drawRect(0f, 0f, 595f, 75f, headerPaint)

        // Header Accent line
        val linePaint = Paint().apply { color = Color.parseColor("#3B82F6") }
        canvas.drawRect(0f, 75f, 595f, 78f, linePaint)

        // Brand Text in header
        val brandPaint = Paint().apply {
            color = Color.parseColor("#60A5FA")
            textSize = 16f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("Basit AI - بەڵگەنامەی فەرمی", 40f, 45f, brandPaint)

        val dateStr = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date())
        val datePaint = Paint().apply {
            color = Color.LTGRAY
            textSize = 10f
            isAntiAlias = true
        }
        canvas.drawText(dateStr, 440f, 45f, datePaint)

        // Document Title
        val titlePaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 20f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText(title.take(45), 40f, 120f, titlePaint)

        // Content Text - split into lines
        val bodyPaint = Paint().apply {
            color = Color.parseColor("#334155")
            textSize = 12f
            isAntiAlias = true
        }

        var yPos = 160f
        val maxWidth = 515f
        val paragraphs = content.split("\n")

        for (paragraph in paragraphs) {
            val words = paragraph.split(" ")
            var line = StringBuilder()
            for (word in words) {
                val testLine = if (line.isEmpty()) word else "$line $word"
                val measure = bodyPaint.measureText(testLine)
                if (measure > maxWidth) {
                    canvas.drawText(line.toString(), 40f, yPos, bodyPaint)
                    yPos += 20f
                    line = StringBuilder(word)
                    if (yPos > 790f) break
                } else {
                    line = StringBuilder(testLine)
                }
            }
            if (line.isNotEmpty() && yPos <= 790f) {
                canvas.drawText(line.toString(), 40f, yPos, bodyPaint)
                yPos += 24f
            }
            if (yPos > 790f) break
        }

        // Footer
        val footerPaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 9f
            isAntiAlias = true
        }
        canvas.drawText("دروستکراوە لەلایەن Basit AI • یاریدەدەری زیرەکی کوردی", 40f, 820f, footerPaint)

        pdfDocument.finishPage(page)

        val timestamp = System.currentTimeMillis()
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "BasitAI_Doc_$timestamp.pdf")
        val outputStream = FileOutputStream(file)
        pdfDocument.writeTo(outputStream)
        outputStream.close()
        pdfDocument.close()

        return file
    }

    /**
     * Generates a real CSV / Excel spreadsheet compatible file with UTF-8 BOM.
     */
    fun createCsv(context: Context, title: String, content: String): File {
        val timestamp = System.currentTimeMillis()
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "BasitAI_Sheet_$timestamp.csv")
        val outputStream = FileOutputStream(file)

        // Write UTF-8 Byte Order Mark (BOM) so Microsoft Excel opens Kurdish text properly
        outputStream.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))

        // Format CSV content
        val lines = content.lines()
        val csvBuilder = StringBuilder()
        csvBuilder.append("# Basit AI Spreadsheet: $title\n")
        csvBuilder.append("# Date: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}\n\n")

        for (line in lines) {
            // If the line contains tabs or pipes, convert to CSV commas
            val formattedLine = line
                .replace("|", ",")
                .replace("\t", ",")
            csvBuilder.append(formattedLine).append("\n")
        }

        outputStream.write(csvBuilder.toString().toByteArray(Charsets.UTF_8))
        outputStream.close()
        return file
    }

    /**
     * Generates a plain text document.
     */
    fun createTxt(context: Context, title: String, content: String): File {
        val timestamp = System.currentTimeMillis()
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "BasitAI_Text_$timestamp.txt")
        val outputStream = FileOutputStream(file)
        val text = "========================================\n" +
                "Basit AI - $title\n" +
                "بەروار: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}\n" +
                "========================================\n\n" +
                content
        outputStream.write(text.toByteArray(Charsets.UTF_8))
        outputStream.close()
        return file
    }

    /**
     * Generates an SRT subtitle file from transcript lines.
     */
    fun createSrt(context: Context, title: String, transcriptLines: List<String>): File {
        val timestamp = System.currentTimeMillis()
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "BasitAI_Subtitle_$timestamp.srt")
        val outputStream = FileOutputStream(file)
        val builder = StringBuilder()

        var currentSecond = 1
        transcriptLines.forEachIndexed { index, text ->
            val startSec = currentSecond
            val endSec = currentSecond + 4
            currentSecond += 5

            val startFormat = String.format(Locale.US, "00:%02d:%02d,000", startSec / 60, startSec % 60)
            val endFormat = String.format(Locale.US, "00:%02d:%02d,000", endSec / 60, endSec % 60)

            builder.append("${index + 1}\n")
            builder.append("$startFormat --> $endFormat\n")
            builder.append("$text\n\n")
        }

        outputStream.write(builder.toString().toByteArray(Charsets.UTF_8))
        outputStream.close()
        return file
    }
}
