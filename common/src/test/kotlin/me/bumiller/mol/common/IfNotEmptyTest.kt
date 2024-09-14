package me.bumiller.mol.common

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class IfNotEmptyTest {

    @Test
    fun `executes lambda if it is not empty`() {
        val list = listOf(1, 2, 3)

        assertThrows<IllegalStateException> {
            list.ifNotEmpty {
                throw IllegalStateException("Success!")
            }
        }
    }

    @Test
    fun `doesnt lambda if it is empty`() {
        val list = listOf<Int>()

        assertDoesNotThrow {
            list.ifNotEmpty {
                throw IllegalStateException("Success!")
            }
        }
    }

}