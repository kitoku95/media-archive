package io.github.kitoku95.mediaarchive.domain.usecase.media

import io.github.kitoku95.mediaarchive.domain.model.CategoryType
import io.github.kitoku95.mediaarchive.domain.model.ImportResult
import io.github.kitoku95.mediaarchive.domain.model.MediaItem
import io.github.kitoku95.mediaarchive.domain.model.MediaType
import io.github.kitoku95.mediaarchive.domain.repository.MediaRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MediaUseCasesTest {

    private lateinit var mediaRepository: MediaRepository
    private lateinit var getMediaByCategoryUseCase: GetMediaByCategoryUseCase
    private lateinit var importSelectedMediaUseCase: ImportSelectedMediaUseCase
    private lateinit var importFolderMediaUseCase: ImportFolderMediaUseCase
    private lateinit var deleteMediaItemUseCase: DeleteMediaItemUseCase
    private lateinit var isMediaAccessibleUseCase: IsMediaAccessibleUseCase

    @Before
    fun setUp() {
        mediaRepository = mockk()
        getMediaByCategoryUseCase = GetMediaByCategoryUseCase(mediaRepository)
        importSelectedMediaUseCase = ImportSelectedMediaUseCase(mediaRepository)
        importFolderMediaUseCase = ImportFolderMediaUseCase(mediaRepository)
        deleteMediaItemUseCase = DeleteMediaItemUseCase(mediaRepository)
        isMediaAccessibleUseCase = IsMediaAccessibleUseCase(mediaRepository)
    }

    @Test
    fun `getMediaByCategoryUseCase delegates to repository for given category and type`() = runTest {
        val sampleImage = MediaItem(
            id = 10L,
            categoryId = 1L,
            mediaType = MediaType.IMAGE,
            contentUri = "content://media/10",
            thumbnailPath = "/data/user/0/app/files/thumbnails/a.webp",
            displayName = "photo.jpg",
            mimeType = "image/jpeg",
            durationMs = null,
            sizeBytes = 1024L,
            addedAt = 1000L
        )
        every {
            mediaRepository.observeMediaByCategoryAndType(1L, MediaType.IMAGE)
        } returns flowOf(listOf(sampleImage))

        val result = getMediaByCategoryUseCase(1L, MediaType.IMAGE).first()

        assertEquals(listOf(sampleImage), result)
    }

    @Test
    fun `importSelectedMediaUseCase returns zero without calling repository when list is empty`() = runTest {
        val result = importSelectedMediaUseCase(
            categoryId = 1L,
            categoryType = CategoryType.MEDIA,
            uriStrings = emptyList()
        )

        assertTrue(result.isSuccess)
        assertEquals(ImportResult(0, 0), result.getOrNull())
        coVerify(exactly = 0) { mediaRepository.importSelectedMedia(any(), any(), any()) }
    }

    @Test
    fun `importSelectedMediaUseCase delegates non-empty uri list to repository`() = runTest {
        val uris = listOf("content://doc/1", "content://doc/2")
        coEvery {
            mediaRepository.importSelectedMedia(1L, CategoryType.DOCUMENT, uris)
        } returns Result.success(ImportResult(2, 0))

        val result = importSelectedMediaUseCase(
            categoryId = 1L,
            categoryType = CategoryType.DOCUMENT,
            uriStrings = uris
        )

        assertTrue(result.isSuccess)
        assertEquals(ImportResult(2, 0), result.getOrNull())
        coVerify(exactly = 1) { mediaRepository.importSelectedMedia(1L, CategoryType.DOCUMENT, uris) }
    }

    @Test
    fun `importFolderMediaUseCase returns zero without calling repository when uri is blank`() = runTest {
        val result = importFolderMediaUseCase(
            categoryId = 1L,
            categoryType = CategoryType.MEDIA,
            treeUriString = "   "
        )

        assertTrue(result.isSuccess)
        assertEquals(ImportResult(0, 0), result.getOrNull())
        coVerify(exactly = 0) { mediaRepository.importFolderMedia(any(), any(), any()) }
    }

    @Test
    fun `importFolderMediaUseCase delegates valid tree uri to repository`() = runTest {
        val treeUri = "content://com.android.externalstorage.documents/tree/primary%3APictures"
        coEvery {
            mediaRepository.importFolderMedia(2L, CategoryType.MEDIA, treeUri)
        } returns Result.success(ImportResult(5, 1))

        val result = importFolderMediaUseCase(
            categoryId = 2L,
            categoryType = CategoryType.MEDIA,
            treeUriString = treeUri
        )

        assertTrue(result.isSuccess)
        assertEquals(ImportResult(5, 1), result.getOrNull())
        coVerify(exactly = 1) { mediaRepository.importFolderMedia(2L, CategoryType.MEDIA, treeUri) }
    }

    @Test
    fun `deleteMediaItemUseCase delegates to repository`() = runTest {
        coEvery { mediaRepository.deleteMediaItem(7L) } returns Result.success(Unit)

        val result = deleteMediaItemUseCase(7L)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { mediaRepository.deleteMediaItem(7L) }
    }

    @Test
    fun `isMediaAccessibleUseCase returns false for blank uri without calling repository`() = runTest {
        val result = isMediaAccessibleUseCase("   ")

        assertEquals(false, result)
        coVerify(exactly = 0) { mediaRepository.isMediaAccessible(any()) }
    }

    @Test
    fun `isMediaAccessibleUseCase delegates non-blank uri to repository`() = runTest {
        coEvery { mediaRepository.isMediaAccessible("content://media/1") } returns true

        val result = isMediaAccessibleUseCase("content://media/1")

        assertTrue(result)
        coVerify(exactly = 1) { mediaRepository.isMediaAccessible("content://media/1") }
    }
}
