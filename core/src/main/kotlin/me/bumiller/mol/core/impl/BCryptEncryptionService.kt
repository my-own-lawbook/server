package me.bumiller.mol.core.impl

import at.favre.lib.crypto.bcrypt.BCrypt
import me.bumiller.mol.core.EncryptionService

internal class BCryptEncryptionService : EncryptionService {

    companion object {

        const val BCRYPT_ROUNDS = 10

        val CHARSET = Charsets.UTF_8

    }

    override fun encrypt(password: String): String {
        return BCrypt.withDefaults().hash(BCRYPT_ROUNDS, password.encodeToByteArray()).toString(CHARSET)
    }

    override fun verify(raw: String, encrypted: String): Boolean {
        return BCrypt.verifyer().verify(raw.encodeToByteArray(), encrypted.encodeToByteArray()).verified
    }
}