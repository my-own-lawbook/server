package me.bumiller.mol.common

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AllNonNullOrNullTest {

    @Test
    fun `returns null if an entry is null`() {
        val list = listOf(1, 2, null, 4, 6)

        val returned = list.allNonNullOrNull()
        assertNull(returned)
    }

    @Test
    fun `returns itself if no entries are null`() {
        val list = listOf(1, 2, 3, 4, 5, 6)

        val returned = list.allNonNullOrNull()
        assertNotNull(returned)
        assertArrayEquals(list.toTypedArray(), returned?.toTypedArray())
    }

}