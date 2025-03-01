package me.bumiller.civoris.core.impl

import me.bumiller.civoris.core.EncryptionService
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BCryptEncryptionServiceTest {

    private val encryptionService: EncryptionService = BCryptEncryptionService()

    @Test
    fun `encryption and decryption works`() {
        val password = "JD§(Q)JD=)"

        val hash = encryptionService.encrypt(password)
        val verified1 = encryptionService.verify("ddq98329", hash)
        val verified2 = encryptionService.verify("ao0UDJQ=)dq0D", hash)
        val verified3 = encryptionService.verify(password, hash)

        assertFalse(verified1)
        assertFalse(verified2)
        assertTrue(verified3)
    }

}