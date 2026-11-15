package io.github.kitoku95.mediaarchive.data.repository

import android.net.Uri
import io.github.kitoku95.mediaarchive.data.local.dao.MediaItemDao
import io.github.kitoku95.mediaarchive.data.local.entity.MediaItemEntity
import io.github.kitoku95.mediaarchive.data.storage.SafMediaScanner
import io.github.kitoku95.mediaarchive.data.storage.ScannedMediaFile
import io.github.kitoku95.mediaarchive.data.storage.ThumbnailStorageManager
import io.github.kitoku95.mediaarchive.domain.model.MediaType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MediaRepositoryImplTest {

    private val mediaItemDao: MediaItemDao = mockk(relaxed = true)
    private val safMediaScanner: SafMediaScanner = mockk(relaxed = true)
    private val thumbnailStorageManager: ThumbnailStorageManager = mockk(relaxed = true)

    private val repository = MediaRepositoryImpl(
        mediaItemDao = mediaItemDao,
        safMediaScanner = safMediaScanner,
        thumbnailStorageManager = thumbnailStorageManager
    )

    @Before
    fun setUp() {
        mockkStatic(Uri::class)
    }

    @After
    fun tearDown() {
        unmockkStatic(Uri::class)
    }

    @Test
    fun `importSelectedMedia skips existing URIs and saves new thumbnails to SQLite`() = runTest {
        val existingUri = "content://media/1"
        val newUriString = "content://media/2"
        val newUri: Uri = mockk {
            every { this@mockk.toString() } returns newUriString
        }
        every { Uri.parse(newUriString) } returns newUri

        coEvery { mediaItemDao.existsInCategory(1L, existingUri) } returns true
        coEvery { mediaItemDao.existsInCategory(1L, newUriString) } returns false
        coEvery { safMediaScanner.inspectSingleFile(newUri) } returns ScannedMediaFile(
            contentUri = newUri,
            mediaType = MediaType.IMAGE,
            displayName = "sunset.jpg",
            mimeType = "image/jpeg",
            durationMs = null,
            sizeBytes = 4096L
        )
        coEvery {
            thumbnailStorageManager.generateAndSaveThumbnail(newUri, MediaType.IMAGE)
        } returns "/files/thumbnails/uuid.webp"
        coEvery { mediaItemDao.insertMediaItems(any()) } returns listOf(10L)

        val result = repository.importSelectedMedia(1L, listOf(existingUri, newUriString))

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull())
        coVerify(exactly = 1) {
            mediaItemDao.insertMediaItems(
                match { items ->
                    items.size == 1 &&
                        items.first().contentUri == newUriString &&
                        items.first().thumbnailPath == "/files/thumbnails/uuid.webp"
                }
            )
        }
    }

    @Test
    fun `deleteMediaItem removes database record and thumbnail file`() = runTest {
        val entity = MediaItemEntity(
            id = 5L,
            categoryId = 1L,
            mediaType = "VIDEO",
            contentUri = "content://media/5",
            thumbnailPath = "/files/thumbnails/video.webp",
            displayName = "clip.mp4",
            mimeType = "video/mp4",
            durationMs = 5000L,
            sizeBytes = 10_000L,
            addedAt = 1000L
        )
        coEvery { mediaItemDao.getMediaItemById(5L) } returns entity

        val result = repository.deleteMediaItem(5L)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { mediaItemDao.deleteMediaItemById(5L) }
        coVerify(exactly = 1) { thumbnailStorageManager.deleteThumbnail("/files/thumbnails/video.webp") }
    }
}
