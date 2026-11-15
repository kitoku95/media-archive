package io.github.kitoku95.mediaarchive.core.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result of hashing a password with a per-category random salt.
 */
data class HashedPassword(
    val hashBase64: String,
    val saltBase64: String
)

/**
 * Salted PBKDF2-HMAC-SHA256 password hasher.
 *
 * Accepts any non-empty password (`password.isNotEmpty()`), including single-character
 * or single-digit passwords, without enforcing minimum length or complexity rules.
 */
@Singleton
class PasswordHasher @Inject constructor() {

    private val secureRandom = SecureRandom()

    fun hash(password: String): HashedPassword {
        require(password.isNotEmpty()) { "Password must not be empty." }
        val salt = ByteArray(SALT_BYTES).also(secureRandom::nextBytes)
        val digest = deriveKey(password, salt)
        val encoder = Base64.getEncoder()
        return HashedPassword(
            hashBase64 = encoder.encodeToString(digest),
            saltBase64 = encoder.encodeToString(salt)
        )
    }

    fun verify(password: String, expectedHashBase64: String, saltBase64: String): Boolean {
        if (password.isEmpty() || expectedHashBase64.isEmpty() || saltBase64.isEmpty()) {
            return false
        }
        return runCatching {
            val decoder = Base64.getDecoder()
            val expectedHash = decoder.decode(expectedHashBase64)
            val salt = decoder.decode(saltBase64)
            val actualHash = deriveKey(password, salt)
            MessageDigest.isEqual(actualHash, expectedHash)
        }.getOrDefault(false)
    }

    private fun deriveKey(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        return try {
            SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private companion object {
        const val ALGORITHM = "PBKDF2WithHmacSHA256"
        const val ITERATIONS = 100_000
        const val KEY_LENGTH_BITS = 256
        const val SALT_BYTES = 16
    }
}
