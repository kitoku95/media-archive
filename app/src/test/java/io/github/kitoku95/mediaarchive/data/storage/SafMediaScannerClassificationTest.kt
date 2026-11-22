package io.github.kitoku95.mediaarchive.data.storage

import android.content.Context
import io.github.kitoku95.mediaarchive.domain.model.CategoryType
import io.github.kitoku95.mediaarchive.domain.model.MediaType
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SafMediaScannerClassificationTest {

    private val context: Context = mockk(relaxed = true)
    private val canonicalIdResolver: SafCanonicalIdResolver = mockk(relaxed = true)
    private val scanner = SafMediaScanner(context, canonicalIdResolver)

    @Test
    fun `resolveMediaType classifies images, videos, and office documents accurately`() {
        assertEquals(MediaType.IMAGE, scanner.resolveMediaType("image/jpeg", "photo.jpg"))
        assertEquals(MediaType.VIDEO, scanner.resolveMediaType("video/mp4", "movie.mp4"))
        assertEquals(MediaType.DOCUMENT, scanner.resolveMediaType("application/pdf", "report.pdf"))
        assertEquals(MediaType.DOCUMENT, scanner.resolveMediaType("text/plain", "notes.txt"))
        assertEquals(
            MediaType.DOCUMENT,
            scanner.resolveMediaType(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "resume.docx"
            )
        )
        assertEquals(
            MediaType.DOCUMENT,
            scanner.resolveMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "budget.xlsx"
            )
        )
        assertEquals(
            MediaType.DOCUMENT,
            scanner.resolveMediaType(
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "slides.pptx"
            )
        )
    }

    @Test
    fun `resolveMediaType falls back to lowercase file extension for octet-stream`() {
        assertEquals(MediaType.DOCUMENT, scanner.resolveMediaType("application/octet-stream", "README.MD"))
        assertEquals(MediaType.DOCUMENT, scanner.resolveMediaType("application/octet-stream", "data.csv"))
        assertEquals(MediaType.DOCUMENT, scanner.resolveMediaType("application/octet-stream", "deck.PPTX"))
        assertNull(scanner.resolveMediaType("application/octet-stream", "archive.zip"))
    }

    @Test
    fun `shouldSkipDirectory skips thumbnails and dot-prefixed hidden folders`() {
        assertTrue(scanner.shouldSkipDirectory(".thumbnails"))
        assertTrue(scanner.shouldSkipDirectory(".Thumbnails"))
        assertTrue(scanner.shouldSkipDirectory("thumbnails"))
        assertTrue(scanner.shouldSkipDirectory(".trash"))
        assertTrue(scanner.shouldSkipDirectory(".cache"))
        assertFalse(scanner.shouldSkipDirectory("Camera"))
        assertFalse(scanner.shouldSkipDirectory("Vacation 2026"))
    }

    @Test
    fun `shouldSkipFile skips hidden dot-prefixed files`() {
        assertTrue(scanner.shouldSkipFile(".nomedia"))
        assertTrue(scanner.shouldSkipFile(".trashed-12345.jpg"))
        assertFalse(scanner.shouldSkipFile("IMG_001.jpg"))
        assertFalse(scanner.shouldSkipFile("report.pdf"))
    }

    @Test
    fun `isAllowedForCategoryType separates MEDIA and DOCUMENT types strictly`() {
        assertTrue(scanner.isAllowedForCategoryType(MediaType.IMAGE, CategoryType.MEDIA))
        assertTrue(scanner.isAllowedForCategoryType(MediaType.VIDEO, CategoryType.MEDIA))
        assertFalse(scanner.isAllowedForCategoryType(MediaType.DOCUMENT, CategoryType.MEDIA))

        assertTrue(scanner.isAllowedForCategoryType(MediaType.DOCUMENT, CategoryType.DOCUMENT))
        assertFalse(scanner.isAllowedForCategoryType(MediaType.IMAGE, CategoryType.DOCUMENT))
        assertFalse(scanner.isAllowedForCategoryType(MediaType.VIDEO, CategoryType.DOCUMENT))
    }

    @Test
    fun `resolveMediaType classifies json and xml mime types as DOCUMENT`() {
        assertEquals(MediaType.DOCUMENT, scanner.resolveMediaType("application/json", "config"))
        assertEquals(MediaType.DOCUMENT, scanner.resolveMediaType("application/xml", "layout"))
    }
}
