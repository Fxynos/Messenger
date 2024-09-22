package com.vl.messenger.domain.usecase

import com.vl.messenger.domain.boundary.MessengerRestApi
import java.io.InputStream

class DownloadFileUseCase(
    private val messengerApi: MessengerRestApi
): GetIsLoggedInUseCase<String, InputStream> {
    override suspend fun invoke(param: String) = messengerApi.downloadFile(param)
}