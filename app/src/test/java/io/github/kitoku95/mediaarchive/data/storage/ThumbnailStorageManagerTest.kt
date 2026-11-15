package io.github.kitoku95.mediaarchive.data.storage

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ThumbnailStorageManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `deleteThumbnail removes existing webp file inside thumbnails dir and ignores blank or missing paths`() = runTest {
        val context = mockk<Context> {
            every { filesDir } returns tempFolder.root
        }
        val manager = ThumbnailStorageManager(context)
        val thumbsDir = File(tempFolder.root, "thumbnails").apply { mkdirs() }
        val thumbFile = File(thumbsDir, "sample.webp").apply { writeText("webp") }
        assertTrue(thumbFile.exists())

        manager.deleteThumbnail(thumbFile.absolutePath)
        assertFalse(thumbFile.exists())

        // Should not throw on blank or non-existent path
        manager.deleteThumbnail("")
        manager.deleteThumbnail(File(thumbsDir, "missing.webp").absolutePath)
    }

    @Test
    fun `deleteThumbnail refuses to delete files outside thumbnails directory`() = runTest {
        val context = mockk<Context> {
            every { filesDir } returns tempFolder.root
        }
        val manager = ThumbnailStorageManager(context)
        val outsideFile = File(tempFolder.root, "outside.webp").apply { writeText("keep") }
        assertTrue(outsideFile.exists())

        manager.deleteThumbnail(outsideFile.absolutePath)
        assertTrue(outsideFile.exists())
    }

    @Test
    fun `deleteThumbnails removes all listed files inside thumbnails dir`() = runTest {
        val context = mockk<Context> {
            every { filesDir } returns tempFolder.root
        }
        val manager = ThumbnailStorageManager(context)
        val thumbsDir = File(tempFolder.root, "thumbnails").apply { mkdirs() }
        val file1 = File(thumbsDir, "1.webp").apply { writeText("1") }
        val file2 = File(thumbsDir, "2.webp").apply { writeText("2") }

        manager.deleteThumbnails(listOf(file1.absolutePath, file2.absolutePath, ""))

        assertFalse(file1.exists())
        assertFalse(file2.exists())
    }
}
