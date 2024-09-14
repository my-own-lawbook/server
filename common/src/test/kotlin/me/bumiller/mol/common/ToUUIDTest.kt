package me.bumiller.mol.common

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class ToUUIDTest {

    @Test
    fun `toUUID throws if is not uuid`() {
        val str1 = "2bce2b33-0c2e-41e9-905b-ed48e37bf559"
        val str2 = "PMA=)A(§WUJ(A=D"

        assertDoesNotThrow {
            str1.toUUID()
        }
        assertThrows<IllegalArgumentException> {
            str2.toUUID()
        }
    }

    @Test
    fun `toUUIDSafe returns null if is not uuid`() {
        val str1 = "2bce2b33-0c2e-41e9-905b-ed48e37bf559"
        val str2 = "PMA=)A(§WUJ(A=D"

        assertNotNull(str1.toUUIDSafe())
        assertNull(str2.toUUIDSafe())
    }

}