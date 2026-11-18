package za.co.skinnercafe.app.data.api

import android.content.Context
import android.util.Base64
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecureCookieJar(context: Context) : CookieJar {

    companion object {
        private const val KEY_ALIAS = "SkinnerCafeAuthCookieKey"
        private const val PREFS_NAME = "secure_auth_cookies"
        private const val COOKIE_KEY = "auth_cookie"
        private const val COOKIE_NAME = "SkinnerCafe.Auth"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }

    private val preferences = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    private fun getOrCreateKey(): SecretKey {
        val existingKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existingKey != null) return existingKey

        val keyGenerator = KeyGenerator.getInstance(
            "AES",
            ANDROID_KEYSTORE
        )

        keyGenerator.init(
            android.security.keystore.KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or
                        android.security.keystore.KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(
                    android.security.keystore.KeyProperties.BLOCK_MODE_GCM
                )
                .setEncryptionPaddings(
                    android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE
                )
                .setRandomizedEncryptionRequired(true)
                .build()
        )

        return keyGenerator.generateKey()
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())

        val encrypted = cipher.doFinal(
            value.toByteArray(StandardCharsets.UTF_8)
        )

        val combined = ByteBuffer.allocate(
            cipher.iv.size + encrypted.size
        )
            .put(cipher.iv)
            .put(encrypted)
            .array()

        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    private fun decrypt(value: String): String? {
        return try {
            val combined = Base64.decode(value, Base64.NO_WRAP)
            val buffer = ByteBuffer.wrap(combined)

            val iv = ByteArray(12)
            buffer.get(iv)

            val encrypted = ByteArray(buffer.remaining())
            buffer.get(encrypted)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                getOrCreateKey(),
                GCMParameterSpec(128, iv)
            )

            String(
                cipher.doFinal(encrypted),
                StandardCharsets.UTF_8
            )
        } catch (exception: Exception) {
            clear()
            null
        }
    }

    @Synchronized
    override fun saveFromResponse(
        url: HttpUrl,
        cookies: List<Cookie>
    ) {
        val authCookie = cookies.firstOrNull {
            it.name == COOKIE_NAME
        } ?: return

        if (authCookie.expiresAt <= System.currentTimeMillis()) {
            clear()
            return
        }

        val cookieData = listOf(
            authCookie.name,
            authCookie.value,
            authCookie.expiresAt.toString(),
            authCookie.domain,
            authCookie.path,
            authCookie.secure.toString(),
            authCookie.httpOnly.toString(),
            authCookie.hostOnly.toString()
        ).joinToString("\n")

        val encryptedCookie = encrypt(cookieData)

        preferences.edit()
            .putString(COOKIE_KEY, encryptedCookie)
            .apply()
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val encryptedCookie = preferences.getString(COOKIE_KEY, null)
            ?: return emptyList()

        val cookieData = decrypt(encryptedCookie)
            ?: return emptyList()

        val fields = cookieData.split("\n")

        if (fields.size != 8) {
            clear()
            return emptyList()
        }

        val cookie = try {
            val builder = Cookie.Builder()
                .name(fields[0])
                .value(fields[1])
                .expiresAt(fields[2].toLong())
                .path(fields[4])

            if (fields[7].toBoolean()) {
                builder.hostOnlyDomain(fields[3])
            } else {
                builder.domain(fields[3])
            }

            if (fields[5].toBoolean()) builder.secure()
            if (fields[6].toBoolean()) builder.httpOnly()

            builder.build()
        } catch (exception: Exception) {
            clear()
            return emptyList()
        }

        if (cookie.name != COOKIE_NAME ||
            cookie.expiresAt <= System.currentTimeMillis() ||
            !cookie.matches(url)
        ) {
            if (cookie.expiresAt <= System.currentTimeMillis()) {
                clear()
            }
            return emptyList()
        }

        return listOf(cookie)
    }

    @Synchronized
    fun clear() {
        preferences.edit()
            .remove(COOKIE_KEY)
            .apply()
    }
}

