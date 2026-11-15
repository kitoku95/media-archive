# ADR-005: Multi-Theme Palettes, 3-Language Localization (`en`, `vi`, `ja`) & Concise `About` Screen

- **Status**: Approved
- **Date**: 2026-11-15
- **Related Rules**: HR-07, HR-08

---

## 1. Context

The user specified the following design and localization requirements for **MediaArchive**:
1. **Default Neutral Theme & Additional Deep Themes**:
   - Default theme uses a neutral palette of **White, Black, and Taupe (Brownish Gray)**.
   - Additional selectable themes use rich, deep tones such as **Deep Crimson Red**, **Deep Forest Green**, and **Deep Sapphire Blue**.
2. **Settings Screen**:
   - Users can change both the **Color Theme** and **App Language** from the **Settings** screen.
3. **3 Supported Languages**:
   - **English (`en`)**, **Vietnamese (`vi`)**, and **Japanese (`ja`)**.
4. **Concise `About` Screen**:
   - The `About` screen only briefly introduces the app's user-facing functionality and **must not** advertise or mention internal architecture or technologies.

---

## 2. Decision

1. **Custom Material 3 Theme Engine (`MediaArchiveTheme`)**:
   - Define `AppThemePalette` enum with 4 options:
     - `NEUTRAL_TAUPE` (Default — White, Black, Taupe / Brownish Gray)
     - `DEEP_CRIMSON` (Deep Crimson / Burgundy Red)
     - `DEEP_FOREST` (Deep Forest / Emerald Green)
     - `DEEP_SAPPHIRE` (Deep Navy / Sapphire Blue)
   - Persist the user's selected theme palette and dark/light mode preference in **Jetpack DataStore (`Preferences`)**.
2. **Per-App Localization for `en`, `vi`, and `ja`**:
   - Maintain complete string resources in:
     - `res/values/strings.xml` (English)
     - `res/values-vi/strings.xml` (Vietnamese)
     - `res/values-ja/strings.xml` (Japanese)
   - Use `AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))` triggered from `SettingsScreen` so language changes take effect immediately without restarting the app.
3. **Strict Content Policy for `AboutScreen`**:
   - Display only the app icon, name, version, and a short 2–3 sentence user-focused description of what the app does.
   - Prohibit any mention of frameworks, libraries, or architecture patterns on `AboutScreen`.

---

## 3. Consequences

### Positive:
- Gives the app a refined, gallery-grade default look (Neutral Taupe) while offering expressive deep-toned alternatives in Settings.
- Provides first-class localization across English, Vietnamese, and Japanese.
- Keeps the `About` screen clean and focused on end-user value.
