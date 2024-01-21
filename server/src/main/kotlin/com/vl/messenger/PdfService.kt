package com.vl.messenger

import com.itextpdf.text.*
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import org.springframework.stereotype.Service
import java.io.OutputStream
import java.util.stream.Stream

@Service
class PdfService {
    companion object {
        private val FONT = FontFactory.getFont( // cyrillic chars support requires certain fonts and code page
            "geist-mono.ttf",
            "Cp1251",
            true
        )
    }

    fun generateConversationActivityReport(activity: List<DataMapper.UserActivity>, outputStream: OutputStream) {
        val document = Document()
        PdfWriter.getInstance(document, outputStream)
        document.open()
        document.add(PdfPTable(4).apply {
            /* Header */
            Stream.of(
                "Изображение",
                "Имя",
                "Сообщения",
                "Символы"
            ).forEach { text ->
                addCell(PdfPCell().apply {
                    borderWidth = 1.5f
                    backgroundColor = BaseColor.LIGHT_GRAY
                    phrase = Phrase(text, FONT)
                })
            }
            /* Regular rows */
            activity.forEach { row ->
                row.user.image?.let {
                    addCell(Image.getInstance("static/$it"))
                } ?: addCell(Phrase("Нет", FONT))
                Stream.of(
                    row.user.login,
                    row.messages.toString(),
                    row.characters.toString()
                ).forEach { addCell(Phrase(it, FONT)) }
            }
        })
        document.close()
    }
}