package com.shivmahapuran.reader

import com.shivmahapuran.reader.ads.InterstitialPolicy
import com.shivmahapuran.reader.domain.model.AppLanguage
import com.shivmahapuran.reader.domain.model.AppSettings
import com.shivmahapuran.reader.domain.model.BookSection
import com.shivmahapuran.reader.domain.model.Chapter
import com.shivmahapuran.reader.domain.model.LocalizedText
import com.shivmahapuran.reader.domain.model.Part
import com.shivmahapuran.reader.domain.model.ShlokaScript
import com.shivmahapuran.reader.domain.model.Verse
import com.shivmahapuran.reader.domain.model.isSpeakerLine
import com.shivmahapuran.reader.domain.model.resolve
import com.shivmahapuran.reader.navigation.DeepLink
import com.shivmahapuran.reader.util.TextNormalizer
import com.shivmahapuran.reader.util.Transliteration
import com.shivmahapuran.reader.util.formatVerseForSharing
import com.shivmahapuran.reader.util.localized
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DeepLinkTest {
    @Test fun parsesReaderLinks() {
        assertEquals(DeepLink.Read(120, 5), DeepLink.parse("shivmahapuran://read/120/5"))
        assertEquals(DeepLink.Read(457, 1), DeepLink.parse("shivmahapuran://read/457"))
    }

    @Test fun parsesOtherDestinations() {
        assertEquals(DeepLink.OpenPart(12), DeepLink.parse("shivmahapuran://part/12/"))
        assertEquals(DeepLink.Search, DeepLink.parse("shivmahapuran://search"))
        assertEquals(DeepLink.OpenBook("lingashtakam"), DeepLink.parse("shivmahapuran://book/lingashtakam"))
        assertEquals(DeepLink.Tab("stotras"), DeepLink.parse("shivmahapuran://tab/stotras"))
    }

    @Test fun rejectsInvalidLinks() {
        assertNull(DeepLink.parse("shivmahapuran://read/458/1"))
        assertNull(DeepLink.parse("shivmahapuran://read/0/1"))
        assertNull(DeepLink.parse("shivmahapuran://part/13"))
        assertNull(DeepLink.parse("https://example.com/read/2/47"))
        assertNull(DeepLink.parse("shivmahapuran://unknown"))
    }
}

class TextTest {
    @Test fun plainStripsIastDiacritics() {
        assertEquals("namah shivaya", TextNormalizer.plain("namaḥ śivāya"))
        assertEquals("tryambakam yajamahe", TextNormalizer.plain("tryambakaṃ yajāmahe"))
    }

    @Test fun escapesLikeWildcards() {
        assertEquals("50\\% \\_x\\\\", TextNormalizer.escapeLike("50% _x\\"))
    }

    @Test fun localizesDigits() {
        assertEquals("457", 457.localized(AppLanguage.ENGLISH))
        assertEquals("४५७", 457.localized(AppLanguage.HINDI))
        assertEquals("૪૫૭", 457.localized(AppLanguage.GUJARATI))
    }

    @Test fun transliterates() {
        assertEquals("ૐ નમઃ શિવાય ॥", Transliteration.toGujarati("ॐ नमः शिवाय ॥"))
        assertEquals("oṃ namaḥ śivāya", Transliteration.toIast("ॐ नमः शिवाय"))
    }

    @Test fun detectsSpeakerLines() {
        assertTrue("सूत उवाच".isSpeakerLine())
        assertTrue("ऋषय ऊचुः".isSpeakerLine())
        assertTrue("સૂત ઉવાચ".isSpeakerLine())
        assertFalse("सौराष्ट्रे सोमनाथं च श्रीशैले मल्लिकार्जुनम् ।".isSpeakerLine())
    }

    @Test fun autoScriptFollowsLanguage() {
        assertEquals(ShlokaScript.GUJARATI, ShlokaScript.AUTO.resolve(AppLanguage.GUJARATI))
        assertEquals(ShlokaScript.DEVANAGARI, ShlokaScript.AUTO.resolve(AppLanguage.HINDI))
        assertEquals(ShlokaScript.DEVANAGARI, ShlokaScript.DEVANAGARI.resolve(AppLanguage.GUJARATI))
    }
}

class ModelTest {
    private fun chapter(title: Map<AppLanguage, String>, titleSanskrit: String, synopsis: String) =
        Chapter(1, 1, 1, titleSanskrit, synopsis, title, emptyMap(), 10, "", false)

    @Test fun chapterTitleFallsBackToSanskrit() {
        val translated = chapter(mapOf(AppLanguage.ENGLISH to "The Glory of the Lingas"), "लिंगमाहात्म्यम्", "x")
        assertEquals("The Glory of the Lingas", translated.displayTitle(AppLanguage.ENGLISH))
        assertEquals("लिंगमाहात्म्यम्", translated.displayTitle(AppLanguage.HINDI))

        val untitled = chapter(emptyMap(), "", "उमाप्रादुर्भाववर्णनम्")
        assertEquals("उमाप्रादुर्भाववर्णनम्", untitled.displayTitle(AppLanguage.GUJARATI))
        assertTrue(untitled.synopsisIsTitle(AppLanguage.GUJARATI))
    }

    @Test fun bookSectionFallsBackToAvailableLanguage() {
        val section = BookSection(0, emptyMap(), "ॐ नमः शिवाय ॥", mapOf(AppLanguage.HINDI to "शिव को नमस्कार"))
        assertEquals(AppLanguage.HINDI to "शिव को नमस्कार", section.textFor(AppLanguage.GUJARATI))
        assertNull(section.titleFor(AppLanguage.ENGLISH))
    }
}

class SharingTest {
    private val part = Part(
        id = 8, samhita = 4, khanda = 0, nameSanskrit = "कोटिरुद्रसंहिता",
        name = LocalizedText("Kotirudra Samhita", "कोटिरुद्र संहिता", "કોટિરુદ્ર સંહિતા"),
        description = LocalizedText("", "", ""), chapterCount = 43,
    )
    private val chapter = Chapter(297, 8, 1, "", "", emptyMap(), emptyMap(), 44, "", true)
    private val verse = Verse(
        id = 1, chapterId = 297, number = 21, speaker = null,
        sanskrit = "सौराष्ट्रे सोमनाथं च श्रीशैले मल्लिकार्जुनम् ।",
        meaning = mapOf(AppLanguage.GUJARATI to "સૌરાષ્ટ્રમાં સોમનાથ, શ્રીશૈલ પર મલ્લિકાર્જુન"),
    )

    @Test fun formatsWithReferenceInReaderNumerals() {
        val text = formatVerseForSharing(verse, chapter, part, AppSettings(language = AppLanguage.GUJARATI), "— શિવમહાપુરાણ, {ref}")
        assertTrue(text.startsWith("સૌરાષ્ટ્રે સોમનાથં"))
        assertTrue(text.contains("સૌરાષ્ટ્રમાં સોમનાથ"))
        assertTrue(text.endsWith("— શિવમહાપુરાણ, કોટિરુદ્ર સંહિતા ૧.૨૧"))
    }

    @Test fun omitsMeaningWhenMissing() {
        val text = formatVerseForSharing(verse, chapter, part, AppSettings(language = AppLanguage.ENGLISH), "{ref}")
        assertEquals("सौराष्ट्रे सोमनाथं च श्रीशैले मल्लिकार्जुनम् ।\n\nKotirudra Samhita 1.21", text)
    }
}

class InterstitialPolicyTest {
    @Test fun respectsGraceAndInterval() {
        val start = 1_000_000L
        InterstitialPolicy.onSessionStart(start)
        assertFalse(InterstitialPolicy.shouldShow(start + 60_000))
        assertTrue(InterstitialPolicy.shouldShow(start + 3 * 60_000))
        InterstitialPolicy.recordShown(start + 3 * 60_000)
        assertFalse(InterstitialPolicy.shouldShow(start + 5 * 60_000))
        assertTrue(InterstitialPolicy.shouldShow(start + 8 * 60_000))
    }
}
