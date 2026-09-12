package com.dotnotes.app.sync.supabase

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object NoteCrypto {
    private const val PREFIX = "enc:v1:"
    private const val GCM_TAG_LENGTH = 128
    private const val IV_LENGTH = 12
    private const val APP_SALT = "dotnotes_e2ee_salt_2026"

    private fun deriveKey(userId: String): SecretKeySpec {
        val md = MessageDigest.getInstance("SHA-256")
        val keyBytes = md.digest("$userId:$APP_SALT".toByteArray(Charsets.UTF_8))
        return SecretKeySpec(keyBytes, "AES")
    }

    fun encrypt(plaintext: String, userId: String): String {
        if (plaintext.isEmpty()) return ""
        return try {
            val key = deriveKey(userId)
            val iv = ByteArray(IV_LENGTH).also { SecureRandom().nextBytes(it) }
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
            val cipherBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
            val combined = ByteArray(iv.size + cipherBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherBytes, 0, combined, iv.size, cipherBytes.size)
            PREFIX + Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (_: Exception) {
            plaintext
        }
    }

    fun decrypt(cipherText: String, userId: String): String {
        if (!cipherText.startsWith(PREFIX)) {
            return cipherText
        }
        return try {
            val key = deriveKey(userId)
            val payload = cipherText.removePrefix(PREFIX)
            val combined = Base64.decode(payload, Base64.NO_WRAP)
            if (combined.size <= IV_LENGTH) return cipherText
            val iv = ByteArray(IV_LENGTH)
            val cipherBytes = ByteArray(combined.size - IV_LENGTH)
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH)
            System.arraycopy(combined, IV_LENGTH, cipherBytes, 0, cipherBytes.size)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
            val plainBytes = cipher.doFinal(cipherBytes)
            String(plainBytes, Charsets.UTF_8)
        } catch (_: Exception) {
            cipherText
        }
    }
}
