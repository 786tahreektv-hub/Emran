package com.example.core

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object CryptoManager {
    private const val KEY_ALIAS = "amar_jibon_diary_aes_key"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val PREFIX_ENC = "ENC_V1:"

    private fun getOrCreateKey(): SecretKey? {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            val existing = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
            if (existing != null) return existing

            val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            keyGen.init(spec)
            keyGen.generateKey()
        } catch (_: Throwable) {
            null
        }
    }

    fun encrypt(plainText: String): String {
        if (plainText.isEmpty()) return ""
        val key = getOrCreateKey() ?: return plainText
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val iv = cipher.iv
            val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            val ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP)
            val dataB64 = Base64.encodeToString(cipherBytes, Base64.NO_WRAP)
            "$PREFIX_ENC$ivB64:$dataB64"
        } catch (_: Throwable) {
            plainText
        }
    }

    fun decrypt(storedText: String): String {
        if (!storedText.startsWith(PREFIX_ENC)) return storedText
        val key = getOrCreateKey() ?: return storedText
        return try {
            val payload = storedText.removePrefix(PREFIX_ENC)
            val splitIdx = payload.indexOf(':')
            if (splitIdx <= 0) return storedText
            val iv = Base64.decode(payload.substring(0, splitIdx), Base64.NO_WRAP)
            val cipherBytes = Base64.decode(payload.substring(splitIdx + 1), Base64.NO_WRAP)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
            String(cipher.doFinal(cipherBytes), Charsets.UTF_8)
        } catch (_: Throwable) {
            storedText
        }
    }

    fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(("amar_jibon_salt_$pin").toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
