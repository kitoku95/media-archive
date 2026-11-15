# Kotlin & Android Naming and Coding Conventions

This document defines the naming and coding standards across the **MediaArchive** codebase, aligned with the **Official Kotlin Coding Conventions** and **Google Android Developer Guidelines**.

---

## 1. General Naming Matrix

| Element | Case Style | Mandatory Rule / Suffix | Good Example | Bad Example |
| :--- | :--- | :--- | :--- | :--- |
| **Package** | `lowercase` | Lowercase alphanumeric, no underscores | `io.github.kitoku95.mediaarchive.feature.categorydetail` | `io.github.kitoku95.mediaarchive.feature.category_detail` |
| **Class / Interface / Enum** | `PascalCase` | Noun or noun phrase | `Category`, `MediaItem`, `AppThemePalette` | `category`, `Media_Item` |
| **Kotlin Class File** | `PascalCase.kt` | Matches the primary class name | `CategoryDetailViewModel.kt` | `categoryDetailVM.kt` |
| **Extension Function File** | `PascalCaseExt.kt` | `<ReceiverType>Ext.kt` | `UriExt.kt`, `ContextExt.kt` | `Utils.kt`, `Helper.kt` |
| **Regular Function** | `camelCase` | Verb or verb phrase | `importFolderMedia()`, `verifyPassword()` | `FolderMedia()`, `check_pass()` |
| **`@Composable` UI Function** | `PascalCase` | Noun describing the UI component | `CategoryListScreen()`, `MediaSquareGridItem()` | `drawCategoryList()`, `mediaGridItem()` |
| **Property / Variable** | `camelCase` | Descriptive noun, no Hungarian notation | `selectedCategoryId`, `thumbnailPath` | `catId`, `thPath`, `mCategory` |
| **Boolean Property** | `camelCase` | Prefix with `is`, `has`, `can`, `should` | `isDefault`, `isPasswordVisible`, `hasNextVideo` | `default`, `passwordFlag` |
| **Compile-time Constant (`const val`)** | `UPPER_SNAKE_CASE` | Uppercase words separated by `_` | `DEFAULT_CATEGORY_NAME`, `THUMBNAIL_SIZE_PX` | `defaultCategoryName`, `ThumbnailSize` |

---

## 2. Layer-Specific Naming Conventions

### 2.1. Domain Layer (`domain/`)
- **Domain Models (`domain/model/`)**:
  - Clean business nouns without `Model` or `Dto` suffixes: `Category.kt`, `MediaItem.kt`, `AccessMode.kt`, `MediaType.kt`, `AppThemePalette.kt`, `AppLanguage.kt`.
- **Repository Interfaces (`domain/repository/`)**:
  - Named `<Entity>Repository` without an `I` prefix: `CategoryRepository.kt`, `MediaRepository.kt`, `UserPreferencesRepository.kt`.
  - Method prefixes:
    - Reactive streams (`Flow<T>`): `observe...` (e.g., `observeCategories()`, `observeMediaByCategory()`).
    - One-shot `suspend` operations: `get...`, `create...`, `insert...`, `delete...`, `verify...`, `update...`.
- **Use Cases (`domain/usecase/`)**:
  - Single-responsibility classes named `<Verb><Noun>UseCase` with an `operator fun invoke(...)` method:
    - `GetCategoriesUseCase.kt`
    - `CreateCategoryUseCase.kt`
    - `VerifyCategoryPasswordUseCase.kt`
    - `ImportSelectedMediaUseCase.kt`
    - `ImportFolderMediaUseCase.kt`
    - `UpdateAppThemePaletteUseCase.kt`
    - `UpdateAppLanguageUseCase.kt`

### 2.2. Data Layer (`data/`)
- **Room Entities (`data/local/entity/`)**:
  - Class suffix `Entity`: `CategoryEntity`, `MediaItemEntity`.
  - Plural `snake_case` table names: `categories`, `media_items`.
  - Explicit `snake_case` `@ColumnInfo(name = "...")` names: `category_id`, `access_mode`, `password_hash`, `content_uri`, `thumbnail_path`.
- **Room DAOs (`data/local/dao/`)**:
  - Interface suffix `Dao`: `CategoryDao`, `MediaItemDao`.
- **Mappers (`data/mapper/`)**:
  - Extension functions in `<Name>Mapper.kt`: `CategoryEntity.toDomain()`, `MediaItemEntity.toDomain()`.
- **Repository Implementations (`data/repository/`)**:
  - Class suffix `Impl`: `CategoryRepositoryImpl`, `MediaRepositoryImpl`, `UserPreferencesRepositoryImpl`.

### 2.3. Presentation Layer (`feature/`)
Each screen package under `feature/<screenname>/` follows a standardized 3-file pattern:

1. **`<Feature>Contract.kt`**:
   ```kotlin
   @Immutable
   data class CategoryDetailUiState(
       val categoryName: String = "",
       val selectedTab: MediaType = MediaType.IMAGE,
       val images: List<MediaItem> = emptyList(),
       val videos: List<MediaItem> = emptyList(),
       val isImporting: Boolean = false
   )

   sealed interface CategoryDetailUiEvent {
       data class OnTabSelected(val mediaType: MediaType) : CategoryDetailUiEvent
       data class OnMediaFilesSelected(val uris: List<String>) : CategoryDetailUiEvent
       data class OnFolderSelected(val treeUri: String) : CategoryDetailUiEvent
       data class OnImageClicked(val mediaId: Long, val index: Int) : CategoryDetailUiEvent
       data class OnVideoClicked(val mediaId: Long, val index: Int) : CategoryDetailUiEvent
   }

   sealed interface CategoryDetailUiEffect {
       data class NavigateToImageViewer(val categoryId: Long, val initialIndex: Int) : CategoryDetailUiEffect
       data class NavigateToVideoPlayer(val categoryId: Long, val initialIndex: Int) : CategoryDetailUiEffect
       data class ShowSnackbar(val messageResId: Int) : CategoryDetailUiEffect
   }
   ```
2. **`<Feature>ViewModel.kt`**:
   - Holds private `_uiState: MutableStateFlow<UiState>` and exposes `uiState: StateFlow<UiState>`.
   - Single public entry point for user actions: `fun onEvent(event: <Feature>UiEvent)`.
3. **`<Feature>Screen.kt`**:
   - Splits into `<Feature>Route` (Stateful: injects `hiltViewModel()`, collects state, handles SAF launchers and navigation effects) and `<Feature>Screen` (Stateless: takes `uiState` and `onEvent`, easy to preview and test).

---

## 3. Resource Naming (`res/`)

All XML resources use `snake_case` with the pattern `<feature_or_common>_<element>_<description>` across all 3 locale directories (`values/`, `values-vi/`, `values-ja/`):

- **String Resources (`strings.xml`)**:
  - `category_list_title`
  - `category_create_dialog_title`
  - `category_password_prompt_hint`
  - `category_detail_tab_images`
  - `category_detail_tab_videos`
  - `category_detail_btn_browse_storage`
  - `video_player_btn_previous`
  - `video_player_btn_next`
  - `settings_title`
  - `settings_theme_label`
  - `settings_language_label`
  - `about_title`
  - `about_description`
- **Drawable Resources (`res/drawable/`)**:
  - `ic_category_lock.xml`
  - `ic_category_public.xml`
  - `ic_player_skip_previous.xml`
  - `ic_player_skip_next.xml`
