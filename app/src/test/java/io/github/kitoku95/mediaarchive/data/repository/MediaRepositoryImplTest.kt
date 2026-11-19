package io.github.kitoku95.mediaarchive.data.repository

import android.net.Uri
import io.github.kitoku95.mediaarchive.data.local.dao.MediaItemDao
import io.github.kitoku95.mediaarchive.data.local.entity.MediaItemEntity
import io.github.kitoku95.mediaarchive.data.storage.SafCanonicalIdResolver
import io.github.kitoku95.mediaarchive.data.storage.SafMediaScanner
import io.github.kitoku95.mediaarchive.data.storage.ScannedMediaFile
import io.github.kitoku95.mediaarchive.data.storage.ThumbnailStorageManager
import io.github.kitoku95.mediaarchive.domain.model.ImportResult
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
    private val canonicalIdResolver: SafCanonicalIdResolver = mockk(relaxed = true)
    private val thumbnailStorageManager: ThumbnailStorageManager = mockk(relaxed = true)

    private val repository = MediaRepositoryImpl(
        mediaItemDao = mediaItemDao,
        safMediaScanner = safMediaScanner,
        canonicalIdResolver = canonicalIdResolver,
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
    fun `importSelectedMedia deduplicates by canonicalId and skips thumbnail for DOCUMENT`() = runTest {
        val existingUriStr = "content://media/1"
        val newDocUriStr = "content://media/2"
        val existingUri: Uri = mockk { every { this@mockk.toString() } returns existingUriStr }
        val newDocUri: Uri = mockk { every { this@mockk.toString() } returns newDocUriStr }
        every { Uri.parse(existingUriStr) } returns existingUri
        every { Uri.parse(newDocUriStr) } returns newDocUri
        every { canonicalIdResolver.resolve(existingUri) } returns "provider::1"
        every { canonicalIdResolver.resolve(newDocUri) } returns "provider::2"

        coEvery { mediaItemDao.existsByCanonicalId(1L, "provider::1") } returns true
        coEvery { mediaItemDao.existsByCanonicalId(1L, "provider::2") } returns false
        coEvery { safMediaScanner.inspectSingleFile(newDocUri) } returns ScannedMediaFile(
            contentUri = newDocUri,
            canonicalId = "provider::2",
            mediaType = MediaType.DOCUMENT,
            displayName = "contract.pdf",
            mimeType = "application/pdf",
            durationMs = null,
            sizeBytes = 4096L
        )
        coEvery { mediaItemDao.insertMediaItems(any()) } returns listOf(10L)

        val result = repository.importSelectedMedia(
            categoryId = 1L,
            uriStrings = listOf(existingUriStr, newDocUriStr)
        )

        assertTrue(result.isSuccess)
        assertEquals(ImportResult(importedCount = 1, skippedDuplicateCount = 1), result.getOrNull())
        coVerify(exactly = 0) { thumbnailStorageManager.generateAndSaveThumbnail(any(), any()) }
        coVerify(exactly = 1) {
            mediaItemDao.insertMediaItems(
                match { items ->
                    items.size == 1 &&
                        items.first().canonicalId == "provider::2" &&
                        items.first().thumbnailPath == "" &&
                        items.first().mediaType == "DOCUMENT"
                }
            )
        }
    }

    @Test
    fun `importSelectedMedia deletes orphan thumbnail when insert returns -1L`() = runTest {
        val uriStr = "content://media/3"
        val uri: Uri = mockk { every { this@mockk.toString() } returns uriStr }
        every { Uri.parse(uriStr) } returns uri
        every { canonicalIdResolver.resolve(uri) } returns "provider::3"

        coEvery { mediaItemDao.existsByCanonicalId(1L, "provider::3") } returns false
        coEvery { safMediaScanner.inspectSingleFile(uri) } returns ScannedMediaFile(
            contentUri = uri,
            canonicalId = "provider::3",
            mediaType = MediaType.IMAGE,
            displayName = "photo.jpg",
            mimeType = "image/jpeg",
            durationMs = null,
            sizeBytes = 2048L
        )
        coEvery {
            thumbnailStorageManager.generateAndSaveThumbnail(uri, MediaType.IMAGE)
        } returns "/files/thumbnails/orphan.webp"
        coEvery { mediaItemDao.insertMediaItems(any()) } returns listOf(-1L)

        val result = repository.importSelectedMedia(1L, listOf(uriStr))

        assertTrue(result.isSuccess)
        assertEquals(ImportResult(importedCount = 0, skippedDuplicateCount = 1), result.getOrNull())
        coVerify(exactly = 1) { thumbnailStorageManager.deleteThumbnail("/files/thumbnails/orphan.webp") }
    }

    @Test
    fun `importFolderMedia flattens scanned files into category and deduplicates by canonicalId`() = runTest {
        val rootTreeStr = "content://tree/root"
        val rootTreeUri: Uri = mockk { every { this@mockk.toString() } returns rootTreeStr }
        val fileUri1: Uri = mockk { every { this@mockk.toString() } returns "content://tree/root/sub/doc.txt" }
        val fileUri2: Uri = mockk { every { this@mockk.toString() } returns "content://tree/root/sub/dup.txt" }
        every { Uri.parse(rootTreeStr) } returns rootTreeUri

        val scannedFiles = listOf(
            ScannedMediaFile(
                contentUri = fileUri1,
                canonicalId = "provider::doc1",
                mediaType = MediaType.DOCUMENT,
                displayName = "todo.txt",
                mimeType = "text/plain",
                durationMs = null,
                sizeBytes = 128L
            ),
            ScannedMediaFile(
                contentUri = fileUri2,
                canonicalId = "provider::doc1",
                mediaType = MediaType.DOCUMENT,
                displayName = "todo.txt",
                mimeType = "text/plain",
                durationMs = null,
                sizeBytes = 128L
            )
        )

        coEvery { safMediaScanner.scanFolder(rootTreeUri) } returns scannedFiles
        coEvery { mediaItemDao.existsByCanonicalId(1L, "provider::doc1") } returns false
        coEvery { mediaItemDao.insertMediaItems(any()) } returns listOf(100L)

        val result = repository.importFolderMedia(1L, treeUriString = rootTreeStr)

        assertTrue(result.isSuccess)
        assertEquals(ImportResult(importedCount = 1, skippedDuplicateCount = 1), result.getOrNull())
        coVerify(exactly = 1) {
            mediaItemDao.insertMediaItems(
                match { it.size == 1 && it.first().categoryId == 1L }
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
            canonicalId = "provider::5",
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
