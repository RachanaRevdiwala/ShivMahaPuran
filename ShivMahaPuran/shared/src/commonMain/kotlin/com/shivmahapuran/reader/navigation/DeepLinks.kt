package com.shivmahapuran.reader.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Deep links of the form:
 *   shivmahapuran://read/120/5     open the reader at global adhyaya 120, verse 5
 *   shivmahapuran://part/3         open a samhita / khanda overview (1..12)
 *   shivmahapuran://search
 *   shivmahapuran://book/lingashtakam   open a stotra
 *   shivmahapuran://tab/bookmarks  (home | samhitas | stotras | bookmarks | settings)
 */
sealed interface DeepLink {
    data class Read(val chapter: Int, val verse: Int) : DeepLink
    data class OpenPart(val part: Int) : DeepLink
    data object Search : DeepLink
    data class OpenBook(val id: String) : DeepLink
    data class Tab(val name: String) : DeepLink

    companion object {
        const val SCHEME = "shivmahapuran"
        const val CHAPTER_COUNT = 457
        const val PART_COUNT = 12

        fun parse(url: String): DeepLink? {
            val parts = url.substringAfter("$SCHEME://", missingDelimiterValue = "")
                .trim('/').split('/').filter { it.isNotBlank() }
            return when (parts.firstOrNull()) {
                "read" -> {
                    val chapter = parts.getOrNull(1)?.toIntOrNull() ?: return null
                    Read(chapter, parts.getOrNull(2)?.toIntOrNull() ?: 1)
                }
                "part" -> parts.getOrNull(1)?.toIntOrNull()?.let { OpenPart(it) }
                "search" -> Search
                "book" -> parts.getOrNull(1)?.let { OpenBook(it) }
                "tab" -> parts.getOrNull(1)?.let { Tab(it) }
                else -> null
            }?.takeIf { it.isValid() }
        }

        private fun DeepLink.isValid(): Boolean = when (this) {
            is Read -> chapter in 1..CHAPTER_COUNT && verse >= 1
            is OpenPart -> part in 1..PART_COUNT
            else -> true
        }
    }
}

/** Platform entry points push links here; the nav host consumes them once the UI is ready. */
object DeepLinkBus {
    private val _pending = MutableStateFlow<DeepLink?>(null)
    val pending: StateFlow<DeepLink?> = _pending.asStateFlow()

    fun handle(url: String): Boolean {
        val link = DeepLink.parse(url) ?: return false
        _pending.value = link
        return true
    }

    fun consume() {
        _pending.value = null
    }
}
