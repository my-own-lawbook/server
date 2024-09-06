package me.bumiller.mol.core

/**
 * Service to perform simple encryption and hashing actions
 */
interface EncryptionService {

    /**
     * Encrypts the raw password
     *
     * @param password The password
     * @return The encrypted password
     */
    fun encrypt(password: String): String

    /**
     * Verifies a raw password against an encrypted version
     *
     * @param raw The user entered password
     * @param encrypted restore, encrypted password
     * @return Whether the password was correct
     */
    fun verify(raw: String, encrypted: String): Boolean

}