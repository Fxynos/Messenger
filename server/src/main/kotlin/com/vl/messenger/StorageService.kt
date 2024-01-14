package com.vl.messenger

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File

@Service
class StorageService {
    companion object {
        private const val PATH_RESOURCES = "static"
        private const val PATH_AVATAR_CONVERSATION = "images/avatars/conversations"
        private const val PATH_AVATAR_USER = "images/avatars/users"

        private val (MultipartFile).format: String
            get() = when (contentType) {
                "image/png" -> "png"
                "image/jpeg" -> "jpg"
                else -> throw IllegalArgumentException("Only png and jpeg images are supported")
            }

        private fun (MultipartFile).save(path: String) = transferTo(File(path).absoluteFile)
    }

    /**
     * @return path to image in static resources
     */
    fun saveConversationImage(file: MultipartFile, conversationId: Long): String {
        val path = "$PATH_AVATAR_CONVERSATION/$conversationId.${file.format}"
        file.save("$PATH_RESOURCES/$path")
        return path
    }

    fun saveUserImage(file: MultipartFile, userId: Int): String {
        val path = "$PATH_AVATAR_USER/$userId.${file.format}"
        file.save("$PATH_RESOURCES/$path")
        return path
    }
}