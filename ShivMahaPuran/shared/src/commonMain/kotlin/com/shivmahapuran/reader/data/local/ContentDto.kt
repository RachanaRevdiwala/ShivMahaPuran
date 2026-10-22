package com.shivmahapuran.reader.data.local

import kotlinx.serialization.Serializable

/** Mirrors `composeResources/files/shivpuran_core.json` produced by tools/build_content.py. */
@Serializable
internal data class CoreDto(
    val contentVersion: Int,
    val parts: List<PartDto>,
    val chapters: List<ChapterDto>,
    val books: List<BookDto> = emptyList(),
)

@Serializable
internal data class LocalizedDto(val en: String, val hi: String, val gu: String)

@Serializable
internal data class PartialLocalizedDto(val en: String? = null, val hi: String? = null, val gu: String? = null)

@Serializable
internal data class PartDto(
    val id: Int,
    val samhita: Int,
    val khanda: Int,
    val nameSanskrit: String,
    val name: LocalizedDto,
    val description: LocalizedDto,
    val chapterCount: Int,
)

@Serializable
internal data class ChapterDto(
    val id: Int,
    val part: Int,
    val number: Int,
    val titleSanskrit: String,
    val synopsisSanskrit: String,
    val title: PartialLocalizedDto = PartialLocalizedDto(),
    val summary: PartialLocalizedDto = PartialLocalizedDto(),
    val verseCount: Int,
    val colophon: String,
    val hasMeanings: Boolean = false,
)

/** One file per part: files/verses/part_XX.json */
@Serializable
internal data class VerseFileDto(val contentVersion: Int, val part: Int, val verses: List<VerseDto>)

@Serializable
internal data class VerseDto(
    val id: Long,
    val chapter: Int,
    val number: Int,
    val sanskrit: String,
    val speaker: String? = null,
    val meaning: PartialLocalizedDto? = null,
)

@Serializable
internal data class BookDto(
    val id: String,
    val language: String,
    val title: LocalizedDto,
    val subtitle: LocalizedDto,
    val author: LocalizedDto,
    val source: String,
    val sections: List<BookSectionDto>,
)

@Serializable
internal data class BookSectionDto(
    val title: PartialLocalizedDto? = null,
    val sanskrit: String? = null,
    val text: PartialLocalizedDto,
)
