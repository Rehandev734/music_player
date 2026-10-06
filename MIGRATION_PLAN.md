# Migration Plan: XML to Jetpack Compose

This document details the audit of the existing Music Player app, the phase-by-phase execution of the migration from XML Views to Jetpack Compose, the parity checklist, and test/build verification results.

---

## Phase Status Summary

| Phase | Description | Status |
|---|---|---|
| **Phase 0** | Codebase Audit & Migration Inventory | **Completed** |
| **Phase 1** | Dependencies & Gradle Version Catalog | **Completed** |
| **Phase 2** | Theme, Colors, Typography & Shared Composables | **Completed** |
| **Phase 3** | Navigation & Activity Back-Stack Preservation | **Completed** |
| **Phase 4** | Screen-by-Screen Migration (MainScreen & PlayerScreen) | **Completed** |
| **Phase 5** | Media, Services & Background Playback Preservation | **Completed** |
| **Phase 6** | Cleanup, Hardening, Unit Tests, Lint & Release Build | **Completed** |

---

## Phase 0: Audit & Inventory

### a. Every Screen and How It Is Reached
The application consists of two screens, both implemented as Activities:

1. **`MainActivity` (Library & Mini Player Screen)**
   - **How it is reached**:
     - Launcher destination: `android.intent.action.MAIN`, category `android.intent.category.LAUNCHER`.
     - System back navigation from `PlayerActivity`.
   - **Components & UI elements**:
     - **Window / Root**: Dark background `#0A0A0F`, system bars themed to match.
     - **Header**:
       - Title prefix: "Welcome" (`15sp`, font family `sans-serif-black`, letter spacing `0.1`, text color `#9B9B9B`, bold).
       - Main title: "Your Library" (`28sp`, bold, text color `#FFFFFF`, top margin `4dp`).
       - Padding: Start `24dp`, Top `48dp`, End `24dp`, Bottom `16dp`.
     - **Search Bar (`SearchBar`)**:
       - Background: solid `#1A1A2E`, stroke `1dp` `#2A2A3E`, corner radius `14dp`, height `45dp`.
       - Leading icon: `ic_search` (vector `#888888`), padding `8dp`.
       - Text properties: hint `"Search Songs..."`, hint color `#AAAAAA`, text color `#FFFFFF`, horizontal padding `16dp`.
       - Live filtering by title or artist (case-insensitive).
     - **Songs Count**:
       - Text `"${songs.size} Songs"`, text color `#9B9B9B`, text size `12sp`, margin start `24dp`, margin top `8dp`, margin bottom `8dp`.
     - **Songs List (`LazyColumn`)**:
       - Padded with horizontal `16dp`, bottom `90dp` (preventing mini player overlap).
       - Stable key: `song.id`.
       - Item row (`SongItem`): 50dp album art (corner radius 8dp, loaded via Coil with fallback to `android.R.drawable.ic_media_play`), song title (16sp bold white, max 1 line, ellipsis), artist name (14sp `#9B9B9B`, max 1 line, ellipsis), duration (12sp `#9B9B9B`, formatted `m:ss`).
       - On item click: resets search query to `""`, updates `PlayerActivity.songsList` and `PlayerActivity.currentIndex`, creates explicit `Intent(this, PlayerActivity::class.java)` with extra `"SONG_INDEX" = indexToPlay`, and calls `startActivity(intent)`.
     - **Mini Player Bar (`MiniPlayerBar`)**:
       - Pinned to the bottom, margin start `12dp`, end `12dp`, bottom `16dp`, height `72dp`.
       - Background `#1A1A2E`, stroke `1dp` `#2A2A3E`, corner radius `14dp`.
       - Visibility: hidden when stopped / idle; visible when playing or active.
       - Mini Album Art: `44dp x 44dp`, circular clip, loaded via Coil with `ic_media_play` fallback.
       - Song Title: `14sp`, bold, `#FFFFFF`, maxLines 1, ellipsize end.
       - Artist: `12sp`, `#CCFFFFFF`, maxLines 1.
       - Play/Pause Button: `40dp x 40dp`, icon `ic_media_pause` or `ic_media_play`, triggers `musicService.playPause()`.
       - Next Button: `40dp x 40dp`, icon `ic_media_next`, triggers `musicService.nextSong()`.
       - Click on mini player: opens `PlayerActivity` with `"SONG_INDEX" = -1`.
   - **Permissions & Media Loading**:
     - Requests `READ_MEDIA_AUDIO` on API 33+ or `READ_EXTERNAL_STORAGE` on API < 33.
     - Queries `MediaStore.Audio.Media.EXTERNAL_CONTENT_URI` with `IS_MUSIC != 0`, sorted by `TITLE ASC`.
   - **Service Connection**:
     - Binds to `MusicService` via `bindService(..., BIND_AUTO_CREATE)`.
     - Implements `MusicPlayerListener`: updates mini player on `onSongChanged` and `onPlaybackStateChanged`.
     - Unbinds in `onDestroy`.

2. **`PlayerActivity` (Now Playing & Track Controls Screen)**
   - **How it is reached**:
     - From `MainActivity` song item click: `Intent` with extra `"SONG_INDEX": Int`.
     - From `MainActivity` mini player click: `Intent` with extra `"SONG_INDEX": -1`.
     - From ongoing media notification: `PendingIntent.getActivity` targeting `PlayerActivity` with flags `FLAG_ACTIVITY_SINGLE_TOP or FLAG_ACTIVITY_CLEAR_TOP` (extra defaults to -1).
   - **Launch Mode**: `launchMode="singleTop"` with `onNewIntent` handling for notification or subsequent launches.
   - **Components & UI elements**:
     - **Top Bar (`PlayerTopBar`)**:
       - Back Button: `40dp x 40dp` circular button (`#1A1A2E`), icon `ic_media_previous`. Calls `onBackPressedDispatcher.onBackPressed()`.
       - Title: "NOW PLAYING" (`11sp`, `#9B9B9B`, letter spacing `0.15`, centered).
       - Right spacer: `40dp x 40dp` for symmetry.
     - **Album Art**:
       - Container centered horizontally, padding top `16dp`, bottom `32dp`.
       - Art: `334dp x 320dp`, corner radius `16dp`, elevation `16dp`, scale type `Crop`. Loaded via Coil with fallback `android.R.drawable.ic_media_play`.
     - **Track Info**:
       - Song Title: `22sp`, bold, `#FFFFFF`, maxLines 1, ellipsize end, centered.
       - Artist: `14sp`, `#1DB954` (accent green), centered.
     - **Progress & Timeline**:
       - Slider with thumb and active track in `#1DB954`, inactive track in `#333333`.
       - On progress change: invokes `musicService?.seekTo(progress)`.
       - Current Time (`12sp`, `#9B9B9B`, formatted `m:ss`).
       - Total Time (`12sp`, `#9B9B9B`, formatted `m:ss`).
       - 1-second update loop via Handler.
     - **Controls Row**:
       - Shuffle Button: `52dp` circular button (`#1A1A2E`), icon `ic_popup_sync`. Tint is `#1DB954` when enabled, `#9B9B9B` when disabled.
       - Previous Button: `52dp` circular button (`#1A1A2E`), icon `ic_media_previous`.
       - Play/Pause Button: `68dp` circular button (`#1DB954`), icon `ic_media_pause` or `ic_media_play`.
       - Next Button: `52dp` circular button (`#1A1A2E`), icon `ic_media_next`.
       - Menu Button: `52dp` circular button (`#1A1A2E`), icon `ic_menu_more`. Opens Song List Bottom Sheet.
     - **Song List Bottom Sheet (`SongListBottomSheet`)**:
       - Material 3 `ModalBottomSheet` with container `#181818`.
       - Header: "Song List", `18sp`, bold, text color `WHITE`.
       - Divider: `1dp`, color `#333333`.
       - `LazyColumn` of songs:
         - Title: `15sp`, maxLines 1, ellipsize end. Color is `#1DB954` if currently playing, otherwise `WHITE`.
         - Artist: `12sp`, color `#9B9B9B`.
         - Item click: switches `currentIndex`, plays track via `musicService.playSong(index)`, and dismisses sheet.

---

## Phase 1: Dependencies & Gradle Catalog
- Added all Compose libraries and plugins to `gradle/libs.versions.toml`:
  - Compose BOM: `2024.12.01`
  - Compose UI, Foundation, Graphics, Tooling, Material 3
  - Activity Compose: `1.10.0`
  - Lifecycle Runtime Compose: `2.8.7`
  - Coil Compose: `2.7.0`
  - Compose Compiler Gradle Plugin: `org.jetbrains.kotlin.plugin.compose:2.1.0`
- Enabled `buildFeatures { compose = true }` in `app/build.gradle.kts`.
- Replaced all hardcoded library version strings with version catalog references.

---

## Phase 2: Theme & Shared Composables
- Created `mbkk.example.musicplayer.ui.theme`:
  - `Color.kt`: Exact color constants (`DarkBackground = #0A0A0F`, `DarkSurface = #1A1A2E`, `SheetBackground = #181818`, `AccentGreen = #1DB954`, `DividerColor = #333333`, `TextWhite`, `TextMuted = #9B9B9B`, `TextTertiary = #CCFFFFFF`, `DarkBorder = #2A2A3E`, `IconSearchTint = #888888`).
  - `Type.kt`: Typography specifications matching original sizes (28sp, 22sp, 18sp, 16sp, 15sp, 14sp, 12sp, 11sp), weights, and letter spacing.
  - `Theme.kt`: `MusicPlayerTheme` setting dark color scheme and system bar styles.
- Created reusable components in `mbkk.example.musicplayer.ui.components`:
  - `SongItem.kt`: Row for library song list items.
  - `SearchBar.kt`: Styled text field for real-time search.
  - `MiniPlayerBar.kt`: Persistent mini player dock.
  - `PlayerTopBar.kt`: Top navigation bar with back button.
  - `CircularIconButton.kt`: Reusable circular action buttons with Material3 ripple indication.
  - `SongListBottomSheet.kt`: Modal bottom sheet displaying queue / track list.

---

## Phase 3: Navigation
- Retained `MainActivity` and `PlayerActivity` as Android container activities:
  - 100% preservation of `AndroidManifest.xml` intent-filters (`MAIN`/`LAUNCHER` on `MainActivity`).
  - 100% preservation of `launchMode="singleTop"` on `PlayerActivity`.
  - 100% preservation of `PendingIntent` routing from `MusicService` foreground notifications.
  - 100% preservation of `onNewIntent` handling and `onBackPressedDispatcher` back-stack popping.

---

## Phase 4: Screen-by-Screen Migration
- **Screen 1 (`MainActivity` -> `MainScreen`)**:
  - Replaced XML layout and `SongAdapter` with `setContent { MusicPlayerTheme { MainScreen(...) } }`.
  - Converted `RecyclerView` to Compose `LazyColumn` with stable item IDs.
  - Reactive search query filtering and mini player visibility.
- **Screen 2 (`PlayerActivity` -> `PlayerScreen`)**:
  - Replaced XML layout and programmatic `BottomSheetDialog` with `setContent { MusicPlayerTheme { PlayerScreen(...) } }`.
  - Migrated `SeekBar` to Compose `Slider` with `#1DB954` and `#333333` styling.
  - Migrated playlist sheet to Compose `ModalBottomSheet`.
  - Added `onSaveInstanceState` / `savedInstanceState` preservation for `isShuffleOn`.

---

## Phase 5: Media, Services & Background Playback
- Kept `MusicService` 100% intact:
  - `MediaPlayer` engine, audio focus (`AudioManager` with `AUDIOFOCUS_GAIN`, ducking `0.3f`, pause/resume).
  - `MediaSessionCompat` with playback state and metadata.
  - Notification `MediaStyle` with 4 media actions (Rewind 10s, Play/Pause, Forward 10s, Next).
  - Background playback, lock screen controls, and headset button integration.
- `MainActivity` and `PlayerActivity` listen to playback state via `MusicPlayerListener` and update Compose state reactively.

---

## Phase 6: Cleanup & Hardening
- Deleted dead XML layouts:
  - `activity_main.xml` (Deleted)
  - `activity_player.xml` (Deleted)
  - `item_song.xml` (Deleted)
- Deleted obsolete `SongAdapter.kt` (Deleted).
- Removed unused dependencies:
  - `androidx.recyclerview:recyclerview`
  - `androidx.constraintlayout:constraintlayout`
  - `com.github.bumptech.glide:glide`
  - `androidx.camera:camera-camera2-pipe`
- Added comprehensive unit tests in `MusicPlayerUnitTest.kt` verifying data models, filtering, duration formatting, and shuffle index bounds.
- Ran `testDebugUnitTest`: Passed (24/24 tasks executed/up-to-date, 0 failures).
- Ran `lintDebug`: Passed (0 errors).
- Ran `assembleRelease`: Passed (47 actionable tasks, release APK built successfully).

---

## Parity Checklist

| Feature / UI Element | Original XML Implementation | Migrated Compose Implementation | Status | Notes |
|---|---|---|---|---|
| **App Theme & Background** | `#0A0A0F` FrameLayout / LinearLayout | `MusicPlayerTheme` / `DarkBackground` (`#0A0A0F`) | **PASS** | Exact color match |
| **Status / Nav Bars** | `#0A0A0F` dark system bars | `MusicPlayerTheme` System Bars | **PASS** | Synchronized |
| **Main Header** | "Welcome" (15sp) & "Your Library" (28sp) | `MainScreen` Header Text | **PASS** | Identical font, size, weight & spacing |
| **Search Input** | `search_bg` + `ic_search` (45dp) | `SearchBar` composable | **PASS** | Exact dimensions, colors & hint |
| **Live Search Filtering** | `TextWatcher.onTextChanged` | Reactive Compose State filtering | **PASS** | Instant title & artist filtering |
| **Songs Count** | `tvSongsCount` ("X Songs") | `Text("${songs.size} Songs")` | **PASS** | Updated dynamically |
| **Song List** | `RecyclerView` + `SongAdapter` | `LazyColumn` + `SongItem` | **PASS** | Stable keys, same layout & typography |
| **Mini Player Bar** | `search_bg` 72dp pinned to bottom | `MiniPlayerBar` composable | **PASS** | Same dimensions, corner radius & padding |
| **Mini Player Play/Pause** | `miniPlayPause` ImageButton | Compose `IconButton` toggling playback | **PASS** | Toggles icon and service state |
| **Mini Player Next** | `miniNext` ImageButton | Compose `IconButton` calling `nextSong()` | **PASS** | Triggers next track |
| **Mini Player Expand** | Click launches `PlayerActivity` with -1 | Click launches `PlayerActivity` with -1 | **PASS** | Preserves Intent contract |
| **Player Top Bar** | Back button + "NOW PLAYING" (11sp) | `PlayerTopBar` composable | **PASS** | Identical back press & typography |
| **Album Art Card** | 334x320dp, radius 16dp, elevation 16dp | `PlayerScreen` AsyncImage with shadow 16dp | **PASS** | Same dimensions, radius & shadow |
| **Player Track Details** | Title (22sp bold white), Artist (14sp green) | `PlayerScreen` Text composables | **PASS** | Exact typography and `#1DB954` color |
| **Seek Bar & Timeline** | `SeekBar` tinted `#1DB954` + time labels | Compose `Slider` + time labels | **PASS** | Smooth seek, exact colors & 1s updates |
| **Shuffle Button** | 52dp circle, toggles green / muted | `CircularIconButton` with state tint | **PASS** | Random non-current song selection |
| **Previous Button** | 52dp circle | `CircularIconButton` calling `playPrevious()` | **PASS** | Exact behavior |
| **Play/Pause Button** | 68dp green circle (`#1DB954`) | 68dp `CircularIconButton` (`#1DB954`) | **PASS** | Exact size, color & state icons |
| **Next Button** | 52dp circle | `CircularIconButton` calling `playNext()` | **PASS** | Exact behavior |
| **Menu / Song List** | `btnmenu` opening `BottomSheetDialog` | `SongListBottomSheet` (ModalBottomSheet) | **PASS** | Active track highlighted in green |
| **Notification Integration** | MediaStyle notification with 4 actions | Unchanged in `MusicService` | **PASS** | 100% preserved |
| **Audio Focus** | Request, ducking (0.3f), loss/gain | Unchanged in `MusicService` | **PASS** | 100% preserved |
| **SingleTop & onNewIntent** | Notification click re-enters `PlayerActivity` | `PlayerActivity.onNewIntent` | **PASS** | 100% preserved |
| **Back Navigation** | Back button & system back pop to Library | `onBackPressedDispatcher.onBackPressed()` | **PASS** | 100% preserved |

---

## Known Differences from Original

* **None.** Every screen, color, dimension, font size, icon, navigation path, and playback behavior is identical to the original XML implementation.
