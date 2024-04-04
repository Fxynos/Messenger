package com.vl.messenger.data.entity

data class User(
    val id: Int,
    val login: String,
    val imageUrl: String?
) {
    /*private val scope = CoroutineScope(Dispatchers.Default)
    @Volatile private var bitmap: Bitmap? = null
    @Volatile private var state = State.CLEARED

    fun clear() {
        bitmap = null
        state = State.CLEARED
    }

    private fun loadImageAsync(loader: (String) -> Bitmap, onLoadListener: () -> Unit) {
        scope.launch {
            bitmap = withContext(Dispatchers.IO) {
                downloadManager.downloadBitmap(imageUrl)
            }
            withContext(Dispatchers.Main) { image.setImageBitmap(bitmap) }
            state = State.CACHED
        }
    }

    enum class State {
        LOADING,
        CACHED,
        CLEARED
    }*/
}