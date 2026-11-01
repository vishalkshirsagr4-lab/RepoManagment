package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecureStorage(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("repo_manager_secure_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ALIAS = "RepoManagerProKey"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128

        private const val KEY_GITHUB_TOKEN = "enc_github_token"
        private const val KEY_GITHUB_USER = "enc_github_user"
        private const val KEY_CLOUDFLARE_TOKEN = "enc_cf_token"
        private const val KEY_CLOUDFLARE_ACCOUNT_ID = "enc_cf_account_id"
        private const val KEY_HUGGINGFACE_TOKEN = "enc_hf_token"

        private const val PREF_DEMO_MODE = "pref_demo_mode"
        private const val PREF_EDITOR_FONT_SIZE = "pref_editor_font_size"
        private const val PREF_EDITOR_WORD_WRAP = "pref_editor_word_wrap"
        private const val PREF_EDITOR_LINE_NUMBERS = "pref_editor_line_numbers"

        @Volatile
        private var INSTANCE: SecureStorage? = null

        fun getInstance(context: Context): SecureStorage {
            return INSTANCE ?: synchronized(this) {
                val instance = SecureStorage(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    init {
        ensureKeyStoreKey()
    }

    private fun ensureKeyStoreKey() {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyGen = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEYSTORE
                )
                val spec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .build()
                keyGen.init(spec)
                keyGen.generateKey()
            }
        } catch (_: Exception) {
            // Key store fallback or initialization
        }
    }

    private fun getSecretKey(): SecretKey? {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        } catch (_: Exception) {
            null
        }
    }

    private fun encrypt(plainText: String): String? {
        return try {
            val key = getSecretKey() ?: return null
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            val combined = ByteArray(iv.size + encryptedBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)
            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (_: Exception) {
            null
        }
    }

    private fun decrypt(cipherText: String?): String? {
        if (cipherText.isNullOrEmpty()) return null
        return try {
            val key = getSecretKey() ?: return null
            val combined = Base64.decode(cipherText, Base64.NO_WRAP)
            if (combined.size < 12) return null
            val iv = combined.copyOfRange(0, 12)
            val encrypted = combined.copyOfRange(12, combined.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)
            val decryptedBytes = cipher.doFinal(encrypted)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }
    }

    // GitHub Token
    fun setGitHubToken(token: String?) {
        if (token.isNullOrBlank()) {
            prefs.edit().remove(KEY_GITHUB_TOKEN).apply()
        } else {
            val encrypted = encrypt(token)
            prefs.edit().putString(KEY_GITHUB_TOKEN, encrypted).apply()
        }
    }

    fun getGitHubToken(): String? {
        val encrypted = prefs.getString(KEY_GITHUB_TOKEN, null)
        return decrypt(encrypted)
    }

    // GitHub User Name / Login
    fun setGitHubUsername(username: String?) {
        if (username.isNullOrBlank()) {
            prefs.edit().remove(KEY_GITHUB_USER).apply()
        } else {
            val enc = encrypt(username)
            prefs.edit().putString(KEY_GITHUB_USER, enc).apply()
        }
    }

    fun getGitHubUsername(): String? {
        return decrypt(prefs.getString(KEY_GITHUB_USER, null))
    }

    // Cloudflare Token
    fun setCloudflareToken(token: String?) {
        if (token.isNullOrBlank()) {
            prefs.edit().remove(KEY_CLOUDFLARE_TOKEN).apply()
        } else {
            prefs.edit().putString(KEY_CLOUDFLARE_TOKEN, encrypt(token)).apply()
        }
    }

    fun getCloudflareToken(): String? {
        return decrypt(prefs.getString(KEY_CLOUDFLARE_TOKEN, null))
    }

    fun setCloudflareAccountId(accountId: String?) {
        if (accountId.isNullOrBlank()) {
            prefs.edit().remove(KEY_CLOUDFLARE_ACCOUNT_ID).apply()
        } else {
            prefs.edit().putString(KEY_CLOUDFLARE_ACCOUNT_ID, encrypt(accountId)).apply()
        }
    }

    fun getCloudflareAccountId(): String? {
        return decrypt(prefs.getString(KEY_CLOUDFLARE_ACCOUNT_ID, null))
    }

    // Hugging Face Token
    fun setHuggingFaceToken(token: String?) {
        if (token.isNullOrBlank()) {
            prefs.edit().remove(KEY_HUGGINGFACE_TOKEN).apply()
        } else {
            prefs.edit().putString(KEY_HUGGINGFACE_TOKEN, encrypt(token)).apply()
        }
    }

    fun getHuggingFaceToken(): String? {
        return decrypt(prefs.getString(KEY_HUGGINGFACE_TOKEN, null))
    }

    // Live connection is the default mode
    fun isDemoMode(): Boolean {
        return prefs.getBoolean(PREF_DEMO_MODE, false)
    }

    fun setDemoMode(enabled: Boolean) {
        prefs.edit().putBoolean(PREF_DEMO_MODE, enabled).apply()
    }

    // Editor Preferences
    fun getEditorFontSize(): Float {
        return prefs.getFloat(PREF_EDITOR_FONT_SIZE, 13f)
    }

    fun setEditorFontSize(size: Float) {
        prefs.edit().putFloat(PREF_EDITOR_FONT_SIZE, size).apply()
    }

    fun isWordWrapEnabled(): Boolean {
        return prefs.getBoolean(PREF_EDITOR_WORD_WRAP, false)
    }

    fun setWordWrapEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(PREF_EDITOR_WORD_WRAP, enabled).apply()
    }

    fun isLineNumbersEnabled(): Boolean {
        return prefs.getBoolean(PREF_EDITOR_LINE_NUMBERS, true)
    }

    fun setLineNumbersEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(PREF_EDITOR_LINE_NUMBERS, enabled).apply()
    }

    fun getRepoSecretsRaw(repoFullName: String): String? {
        val key = "repo_sec_${repoFullName.replace('/', '_')}"
        return decrypt(prefs.getString(key, null))
    }

    fun saveRepoSecretsRaw(repoFullName: String, json: String) {
        val key = "repo_sec_${repoFullName.replace('/', '_')}"
        val encrypted = encrypt(json)
        prefs.edit().putString(key, encrypted).apply()
    }

    // Persist staged source changes using the same Android Keystore AES-GCM encryption
    // used for credentials. Staged files can contain private source code.
    fun getStagedChangesRaw(): String? {
        return decrypt(prefs.getString("enc_staged_changes_v1", null))
    }

    fun saveStagedChangesRaw(json: String) {
        val encrypted = encrypt(json)
            ?: throw IllegalStateException("Unable to encrypt staged changes with Android Keystore.")
        prefs.edit().putString("enc_staged_changes_v1", encrypted).apply()
    }

    fun clearStagedChangesRaw() {
        prefs.edit().remove("enc_staged_changes_v1").apply()
    }

    fun clearAllCredentials() {
        prefs.edit()
            .remove(KEY_GITHUB_TOKEN)
            .remove(KEY_GITHUB_USER)
            .remove(KEY_CLOUDFLARE_TOKEN)
            .remove(KEY_CLOUDFLARE_ACCOUNT_ID)
            .remove(KEY_HUGGINGFACE_TOKEN)
            .apply()
    }
}
