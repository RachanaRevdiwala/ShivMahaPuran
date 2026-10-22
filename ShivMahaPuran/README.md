# Shri Shiv Maha Puran — KMP reader app

An offline, book-style reader of the **Shiva Mahapurana** for Android and iOS, built with
**Kotlin Multiplatform + Compose Multiplatform** (one shared UI), **MVVM** and a local **SQLite**
database (SQLDelight). The UI is available in **English, Hindi (हिन्दी) and Gujarati (ગુજરાતી)**.
The parchment background carries a Neelkantha (poison-blue) tint on every screen.

## Features

| Area | What's included |
|---|---|
| Scripture | The complete Sanskrit Shiva Mahapurana: 7 samhitas in 12 parts, **457 adhyayas, 24,278 shlokas**, with speaker lines (सूत उवाच …) and the closing colophon of each adhyaya. Shlokas can be shown in Devanagari or Gujarati script |
| Chapter guides | Every adhyaya has a title and a short summary in English, Hindi and Gujarati. Each part has an introduction |
| Verse meanings | Verse-by-verse meanings in all three languages for selected passages. So far this covers Kotirudra Samhita 1 (the twelve Jyotirlingas). Adhyayas with meanings are marked in the chapter list |
| Stotras (Library) | Shiva Panchakshara Stotram · Lingashtakam · Shri Rudrashtakam (Tulsidas) · Dvadasha Jyotirlinga Stotram · Shiva Mantras (Panchakshari, Mahamrityunjaya, Karpuragauram). Each has the Sanskrit text and meanings in all three languages |
| Reading | Book-like reader with page turns, framed pages, adjustable text size, verses per page, meanings toggle, choice of shloka script, night reading and keep-screen-on |
| Devotee comforts | Adhyaya of the Day, Continue Reading, per-part and overall progress, bookmarks, share or copy a shloka, and search (words, or a reference) |
| Localization | In-app language switch (independent of the phone's language), localized numerals (४७ / ૪૭), and a trilingual first-run language picker |
| Ads | Google Mobile Ads: anchored adaptive banners, plus a frequency-capped interstitial shown only when an adhyaya is completed. Includes UMP consent and ATT on iOS |
| Deep links | `shivmahapuran://read/265/21` (global adhyaya 1–457, verse), `shivmahapuran://part/8`, `shivmahapuran://book/lingashtakam`, `shivmahapuran://search`, `shivmahapuran://tab/stotras` (home, samhitas, stotras, bookmarks, settings) |
| Backup | Bookmarks, progress and preferences are included in Android Auto Backup |

## Architecture (MVVM)

```
shared/src/commonMain/kotlin/com/shivmahapuran/reader
├── domain/        models (Part, Chapter, Verse, Book, AppSettings…) + repository interfaces
├── data/
│   ├── local/     SQLDelight driver (expect/actual), JSON DTOs, ContentSeeder, mappers
│   └── repository PuranRepositoryImpl, UserDataRepositoryImpl, SettingsRepositoryImpl
├── presentation/  ViewModels exposing immutable StateFlow<UiState>
├── ui/            theme (palette + fonts), components (parchment, ornaments), screens
├── navigation/    NavHost routes + deep links
├── ads/           AdBanner / InterstitialAds (expect) + consent state and frequency policy
├── platform/      locale override, share, keep-screen-on, time (expect/actual)
└── di/            Koin modules
shared/src/commonMain/sqldelight/…/Puran.sq      schema + all queries
shared/src/commonMain/composeResources/          strings (en/hi/gu), fonts, bg image, files/
androidApp/        Application + MainActivity (Compose host), manifest, icons
iosApp/            SwiftUI host, GoogleAdsBridge.swift (AdMob + UMP), project.yml (XcodeGen)
tools/             content pipeline (see below)
```

**Data flow:** on first launch, and again whenever the content version changes, `ContentSeeder`
writes `files/shivpuran_core.json` (parts, adhyaya metadata and summaries, stotras) into SQLite.
The shlokas (`files/verses/part_XX.json`, about 7 MB) are seeded one part at a time in the
background, or straight away when a part is opened first. User tables (bookmarks, progress,
preferences) are kept separate from content tables, so a content update never wipes a devotee's
data.

## Building

Requirements: JDK 17, Android SDK 36, Xcode 16+ (tested with 26.x), XcodeGen (`brew install xcodegen`).

```bash
./gradlew :androidApp:assembleDebug          # androidApp/build/outputs/apk/debug/
./gradlew :androidApp:installDebug           # with a device or emulator connected

cd iosApp && xcodegen generate && open iosApp.xcodeproj   # then run the iosApp scheme

./gradlew :shared:testDebugUnitTest :shared:iosSimulatorArm64Test   # shared-logic tests
```

## Content pipeline

```bash
python3 tools/build_content.py     # regenerates composeResources/files/
python3 tools/dump_part.py 9       # digest of a part (synopses, opening verses) for writing summaries
```

Sources in `tools/source/`:
- `shivpuran_wikitext.json`: the Sanskrit Shiva Mahapurana from Sanskrit Wikisource (public
  domain), parsed by `tools/parse_shivpuran.py`.
- `parts_i18n.json`: names and descriptions of the 12 parts (in-house).
- `summaries/part_XX.json`: adhyaya titles and summaries in en/hi/gu (in-house; all 457 done).
- `meanings/part_XX.json`: verse meanings for selected adhyayas, as
  `{"<adhyaya>": {"<verse>": {en, hi, gu}}}` (in-house).
- `library/*.json`: stotras (traditional Sanskrit with in-house meanings), ordered by `order`.

The build **fails** if the adhyaya count is not 457, if any adhyaya has no verses, or if a
summary is missing a language. **When content changes, bump `CONTENT_VERSION`** in
`tools/build_content.py` **and** `ContentSeeder.BUNDLED_CONTENT_VERSION` (both are currently 2).
Installed apps then re-seed on their next launch. `tools/make_icons.py` regenerates the app icons.

## Before publishing

1. **AdMob IDs.** Google's public *test* IDs are in place. Replace the Android app id
   (`androidApp/build.gradle.kts`, `admobAppId`), the iOS app id (`iosApp/iosApp/Info.plist`,
   `GADApplicationIdentifier`) and the ad unit ids (`shared/src/{android,ios}Main/…/ads/`).
   Set up the GDPR and IDFA consent messages in AdMob.
2. **Content review.** Have the summaries, part descriptions, verse meanings and stotra meanings
   proofread by a Shaiva scholar, especially the Hindi and Gujarati. The Sanskrit text comes from
   Wikisource and still contains some OCR-style typos from its source.
3. **Store compliance.** Add a privacy-policy URL. Fill in Play *Data safety* and the App Store
   privacy label (AdMob uses the advertising ID / IDFA). Refresh `SKAdNetworkItems` from
   Google's current list.
4. **Signing.** Create an Android upload keystore and an iOS team and bundle id.

## Ideas for v1.1

More verse meanings (for example Shiva Tattva, the Panchakshara chapters of the Vayaviya Samhita,
and Rudra Samhita highlights). More stotras (Shiva Tandava, Shiva Mahimna, Bilvashtakam).
A daily adhyaya notification (the deep link is already supported), audio recitation, a "Remove ads"
purchase, and full-text search with SQLite FTS.

Debug-only: launching on iOS with `SIMCTL_CHILD_PURAN_DISABLE_ADS=1` skips the consent dialog
(useful for screenshots).
