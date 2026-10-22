package com.shivmahapuran.reader.domain.model

/** UI + content languages supported by the app. [code] is the BCP-47 tag used for resources. */
enum class AppLanguage(val code: String, val nativeName: String) {
    ENGLISH("en", "English"),
    HINDI("hi", "हिन्दी"),
    GUJARATI("gu", "ગુજરાતી");

    companion object {
        fun fromCode(code: String?): AppLanguage? =
            entries.firstOrNull { code != null && code.lowercase().startsWith(it.code) }
    }
}

/** Which script the Sanskrit shlokas are rendered in. */
enum class ShlokaScript { AUTO, DEVANAGARI, GUJARATI }

fun ShlokaScript.resolve(language: AppLanguage): ShlokaScript = when (this) {
    ShlokaScript.AUTO -> if (language == AppLanguage.GUJARATI) ShlokaScript.GUJARATI else ShlokaScript.DEVANAGARI
    else -> this
}

/** A string available in all three app languages. */
data class LocalizedText(val en: String, val hi: String, val gu: String) {
    operator fun get(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> en
        AppLanguage.HINDI -> hi
        AppLanguage.GUJARATI -> gu
    }
}

/** Name of a content language ("en", "hi", "gu", "sa") written in the reader's [uiLanguage]. */
fun languageName(code: String, uiLanguage: AppLanguage): String {
    val names = when (uiLanguage) {
        AppLanguage.ENGLISH -> mapOf("en" to "English", "hi" to "Hindi", "gu" to "Gujarati", "sa" to "Sanskrit")
        AppLanguage.HINDI -> mapOf("en" to "अंग्रेज़ी", "hi" to "हिन्दी", "gu" to "गुजराती", "sa" to "संस्कृत")
        AppLanguage.GUJARATI -> mapOf("en" to "અંગ્રેજી", "hi" to "હિન્દી", "gu" to "ગુજરાતી", "sa" to "સંસ્કૃત")
    }
    return names[code] ?: code
}
