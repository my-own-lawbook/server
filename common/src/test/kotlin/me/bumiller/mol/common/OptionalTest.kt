package me.bumiller.mol.common

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class OptionalTest {

    @Test
    fun `empty returns empty optional`() {
        val opt = empty<String>()

        assertFalse(opt.isPresent)
    }

    @Test
    fun `present returns empty optional`() {
        val opt1 = present<String?>(null)
        val opt2 = present(5)

        assertTrue(opt1.isPresent)
        assertEquals(null, opt1.get())

        assertTrue(opt2.isPresent)
        assertEquals(5, opt2.get())
    }

    @Test
    fun `presentWhenNotNull returns empty optional for null parameters`() {
        val opt1 = presentWhenNotNull(null)
        val opt2 = presentWhenNotNull("value")

        assertFalse(opt1.isPresent)

        assertTrue(opt2.isPresent)
    }

    @Test
    fun `getOrNull returns null if the optional is not present`() {
        val opt1 = present(1)
        val opt2 = empty<Int>()

        assertNotNull(opt1.getOrNull())
        assertNull(opt2.getOrNull())
    }

    @Test
    fun `map returns optional with mapped value if not empty`() {
        val opt1 = present(1).map { "string-$it" }
        val opt2 = empty<Int>().map { "string-$it" }

        assertEquals("string-1", opt1.get())
        assertFalse(opt2.isPresent)
    }

}