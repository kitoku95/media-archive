package io.github.kitoku95.mediaarchive.data.mapper

import io.github.kitoku95.mediaarchive.data.local.entity.CategoryEntity
import io.github.kitoku95.mediaarchive.data.local.entity.MediaItemEntity
import io.github.kitoku95.mediaarchive.domain.model.AccessMode
import io.github.kitoku95.mediaarchive.domain.model.MediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EntityMapperTest {

    @Test
    fun `CategoryEntity maps PUBLIC and PRIVATE modes accurately`() {
        val publicEntity = CategoryEntity(
            id = 1L,
            name = "Main",
            accessMode = "PUBLIC",
            passwordHash = null,
            passwordSalt = null,
            isDefault = true,
            createdAt = 1000L
        )
        val privateEntity = CategoryEntity(
            id = 2L,
            name = "Vault",
            accessMode = "PRIVATE",
            passwordHash = "hash",
            passwordSalt = "salt",
            isDefault = false,
            createdAt = 2000L
        )

        val publicDomain = publicEntity.toDomain()
        assertEquals(1L, publicDomain.id)
        assertEquals("Main", publicDomain.name)
        assertEquals(AccessMode.PUBLIC, publicDomain.accessMode)
        assertEquals(true, publicDomain.isDefault)

        val privateDomain = privateEntity.toDomain()
        assertEquals(2L, privateDomain.id)
        assertEquals("Vault", privateDomain.name)
        assertEquals(AccessMode.PRIVATE, privateDomain.accessMode)
        assertEquals(false, privateDomain.isDefault)
    }

    @Test
    fun `MediaItemEntity maps IMAGE, VIDEO, and DOCUMENT items accurately`() {
        val imageEntity = MediaItemEntity(
            id = 10L,
            categoryId = 1L,
            mediaType = "IMAGE",
            contentUri = "content://media/10",
            canonicalId = "provider::10",
            thumbnailPath = "/data/files/thumbnails/a.webp",
            displayName = "photo.jpg",
            mimeType = "image/jpeg",
            durationMs = null,
            sizeBytes = 2048L,
            addedAt = 5000L
        )
        val videoEntity = MediaItemEntity(
            id = 11L,
            categoryId = 1L,
            mediaType = "VIDEO",
            contentUri = "content://media/11",
            canonicalId = "provider::11",
            thumbnailPath = "/data/files/thumbnails/b.webp",
            displayName = "clip.mp4",
            mimeType = "video/mp4",
            durationMs = 15_000L,
            sizeBytes = 102_400L,
            addedAt = 6000L
        )
        val documentEntity = MediaItemEntity(
            id = 12L,
            categoryId = 1L,
            mediaType = "DOCUMENT",
            contentUri = "content://media/12",
            canonicalId = "provider::12",
            thumbnailPath = "",
            displayName = "report.pdf",
            mimeType = "application/pdf",
            durationMs = null,
            sizeBytes = 50_000L,
            addedAt = 7000L
        )

        val imageDomain = imageEntity.toDomain()
        assertEquals(MediaType.IMAGE, imageDomain.mediaType)
        assertNull(imageDomain.durationMs)
        assertEquals("photo.jpg", imageDomain.displayName)

        val videoDomain = videoEntity.toDomain()
        assertEquals(MediaType.VIDEO, videoDomain.mediaType)
        assertEquals(15_000L, videoDomain.durationMs)

        val documentDomain = documentEntity.toDomain()
        assertEquals(MediaType.DOCUMENT, documentDomain.mediaType)
        assertEquals("", documentDomain.thumbnailPath)
        assertEquals("report.pdf", documentDomain.displayName)
    }
}
