package com.vl.messenger.domain.usecase

import com.vl.messenger.domain.boundary.FileStorageAccessor
import com.vl.messenger.domain.boundary.MessengerRestApi
import com.vl.messenger.domain.boundary.SessionStore
import java.io.BufferedInputStream
import java.io.BufferedOutputStream

private const val MIME_TYPE_PDF = "application/pdf"
private const val DIRECTORY = "Messenger"
private const val BASE_NAME = "report"
private const val CHUNK_BYTES = 4096

class DownloadConversationReportUseCase(
    private val sessionStore: SessionStore,
    private val api: MessengerRestApi,
    private val fileStorageAccessor: FileStorageAccessor
): SuspendedUseCase<String, Unit> {
    /**
     * @param param dialog id of conversation
     */
    override suspend fun invoke(param: String) {
        val input = BufferedInputStream(
            api.getConversationReport(sessionStore.getToken()!!.token, param)
        )
        val output = BufferedOutputStream(
            fileStorageAccessor.createFileAtDownloads(DIRECTORY, BASE_NAME, MIME_TYPE_PDF)
        )

        input.use {
            output.use {
                val buf = ByteArray(CHUNK_BYTES)

                while (true) {
                    val size = input.read(buf)
                        .takeIf { it > 0 }
                        ?: break

                    output.write(buf, 0, size)
                }
            }
        }
    }
}