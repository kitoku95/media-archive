package io.github.kitoku95.mediaarchive.core.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordHasherTest {

    private val hasher = PasswordHasher()

    @Test
    fun `hash and verify accept single character and single digit passwords`() {
        val singleChar = hasher.hash("a")
        val singleDigit = hasher.hash("7")
        val singleSpace = hasher.hash(" ")

        assertTrue(hasher.verify("a", singleChar.hashBase64, singleChar.saltBase64))
        assertTrue(hasher.verify("7", singleDigit.hashBase64, singleDigit.saltBase64))
        assertTrue(hasher.verify(" ", singleSpace.hashBase64, singleSpace.saltBase64))

        assertFalse(hasher.verify("b", singleChar.hashBase64, singleChar.saltBase64))
        assertFalse(hasher.verify("8", singleDigit.hashBase64, singleDigit.saltBase64))
        assertFalse(hasher.verify("", singleChar.hashBase64, singleChar.saltBase64))
    }

    @Test
    fun `hash generates unique salt for identical passwords`() {
        val first = hasher.hash("secret")
        val second = hasher.hash("secret")

        assertNotEquals(first.saltBase64, second.saltBase64)
        assertNotEquals(first.hashBase64, second.hashBase64)
        assertTrue(hasher.verify("secret", first.hashBase64, first.saltBase64))
        assertTrue(hasher.verify("secret", second.hashBase64, second.saltBase64))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `hash rejects empty password`() {
        hasher.hash("")
    }

    @Test
    fun `verify returns false on malformed base64`() {
        assertFalse(hasher.verify("1", "not-valid-base64!!!", "also-invalid!!!"))
        assertEquals(false, hasher.verify("1", "", ""))
    }
}
