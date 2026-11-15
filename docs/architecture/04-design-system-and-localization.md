# 04. Design System, Multi-Theme Palettes, Localization & `About` Screen

This document defines the visual design system, color palettes, multi-language support (`en`, `vi`, `ja`), `Settings` screen behavior, and content rules for the `About` screen in **MediaArchive**.

---

## 1. Multi-Theme Color Palette System

**MediaArchive** provides **4 curated color palettes** selectable in the **Settings** screen, alongside Light / Dark / System mode support.

### 1.1. Default Theme: `Neutral Taupe` (White, Black & Brownish Gray)
A refined, distraction-free gallery aesthetic built around pure white, deep charcoal black, and warm taupe (brownish gray) accents so media thumbnails stand out naturally.

| Role | Light Scheme | Dark Scheme | Description |
| :--- | :--- | :--- | :--- |
| `Primary` | `#5C5346` (Deep Taupe) | `#C8BEB2` (Warm Taupe) | Primary buttons, active tab indicator, FAB |
| `OnPrimary` | `#FFFFFF` (Pure White) | `#1C1917` (Charcoal Black) | Text/icons on primary elements |
| `Secondary` | `#8D8278` (Brownish Gray) | `#9E9388` (Muted Taupe) | Secondary chips, badges, lock icons |
| `Background` | `#FAF8F5` (Warm White) | `#121212` (True Dark Black) | App screen background |
| `Surface` | `#FFFFFF` (Pure White) | `#1E1C1A` (Dark Espresso Gray) | Cards, dialogs, bottom sheets, top bars |
| `SurfaceVariant` | `#EDEAE4` (Light Taupe Gray) | `#2C2825` (Taupe Charcoal) | Grid placeholders, text fields |

### 1.2. Deep Accent Themes (Selectable in Settings)

For users who prefer richer, deep-toned colors instead of neutral monochrome/taupe, the app offers three deep jewel-toned palettes:

| Theme Palette ID | Display Name | Primary (Light / Dark) | Secondary / Container | Surface Tint Character |
| :--- | :--- | :--- | :--- | :--- |
| `NEUTRAL_TAUPE` *(Default)* | **Neutral Taupe** | `#5C5346` / `#C8BEB2` | `#8D8278` / `#3D362E` | White, Black & Brownish Gray |
| `DEEP_CRIMSON` | **Deep Crimson** | `#8B1E24` / `#FFB3B4` | `#5C1116` / `#681319` | Rich Dark Crimson / Burgundy Red |
| `DEEP_FOREST` | **Deep Forest Green** | `#1B4D3E` / `#8BD6B9` | `#12352A` / `#143D30` | Deep Emerald / Pine Green |
| `DEEP_SAPPHIRE` | **Deep Sapphire Blue** | `#1A365D` / `#A6C8FF` | `#102440` / `#152E50` | Deep Navy / Midnight Blue |

### Theme Persistence Flow:
- Stored in Jetpack DataStore (`PreferencesKey: "app_theme_palette"` and `"app_dark_mode"`).
- `MainActivity` collects `ObserveUserPreferencesUseCase` and passes the selected `AppThemePalette` into `MediaArchiveTheme(palette = ..., darkTheme = ...)`.
- Switching themes in `SettingsScreen` updates the entire app UI instantaneously without restarting.

---

## 2. Multi-Language Localization (`en`, `vi`, `ja`)

**MediaArchive** natively supports **3 languages**:
1. **English (`en`)** — `app/src/main/res/values/strings.xml` (Default fallback)
2. **Vietnamese (`vi`)** — `app/src/main/res/values-vi/strings.xml`
3. **Japanese (`ja`)** — `app/src/main/res/values-ja/strings.xml`

### Runtime Language Switching in `SettingsScreen`:
- Uses AndroidX Official Per-App Language API (`AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTag))`) combined with `localeConfig` in `AndroidManifest.xml`.
- `MainActivity` extends `AppCompatActivity` so changing the language in `SettingsScreen` immediately updates all Compose `stringResource(R.string.*)` texts across the app without losing navigation state.

### Core Terminology Translation Matrix:

| Resource Key Concept | English (`en`) | Vietnamese (`vi`) | Japanese (`ja`) |
| :--- | :--- | :--- | :--- |
| Categories Screen Title | `Categories` | `Danh mục` | `カテゴリー` |
| Default Category Name | `Main` | `Main` | `Main` |
| Public Mode | `Public` | `Công khai (Public)` | `公開 (Public)` |
| Private Mode | `Private` | `Riêng tư (Private)` | `非公開 (Private)` |
| Images Tab | `Images` | `Ảnh` | `画像` |
| Videos Tab | `Videos` | `Video` | `動画` |
| Browse Storage Button | `Browse Storage` | `Duyệt bộ nhớ` | `ストレージを参照` |
| Select Files Option | `Select Images / Videos` | `Chọn Ảnh / Video` | `画像・動画を選択` |
| Select Folder Option | `Select Folder` | `Chọn Thư mục` | `フォルダを選択` |
| Settings Screen Title | `Settings` | `Cài đặt` | `設定` |
| Theme Setting Label | `Color Theme` | `Tông màu giao diện` | `カラーテーマ` |
| Language Setting Label | `Language` | `Ngôn ngữ` | `言語` |
| About Screen Title | `About` | `Giới thiệu` | `アプリについて` |

---

## 3. `Settings` Screen & `About` Screen Design Rules

### 3.1. `SettingsScreen`
Accessible from the top bar of `CategoryListScreen`:
- **Color Theme Section**: Visual color swatch cards allowing the user to pick between:
  - *Neutral Taupe (Default)*
  - *Deep Crimson*
  - *Deep Forest Green*
  - *Deep Sapphire Blue*
- **Appearance Mode Section**: *System Default*, *Light*, *Dark*.
- **Language Section**: Radio selector for *English*, *Tiếng Việt*, *日本語*.
- **About Link**: Navigates to `AboutScreen`.

### 3.2. `AboutScreen` (Strict Content Rule — HR-07)
The `About` screen must remain clean, user-friendly, and strictly focused on **what the app does for the user**:
- **Included on `AboutScreen`**:
  - App Icon, App Name (**MediaArchive**), and Version Name (e.g., `v1.0.0`).
  - A concise 2–3 sentence summary of the app's features:
    - Organize and watch your personal photos and videos by custom categories.
    - Add only the files or folders you choose without automatic device scanning.
    - Protect sensitive categories with a private password.
- **Strictly Excluded from `AboutScreen`**:
  - **NO mention** of internal software architecture, design patterns, databases, or libraries (do not mention Kotlin, Jetpack Compose, Material 3, Clean Architecture, MVVM, Room, SQLite, SAF, or ExoPlayer).
