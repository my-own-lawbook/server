package me.bumiller.mol.core.mapping

import me.bumiller.mol.model.Gender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UserMappingTest {

    @Test
    fun `mapGender correctly maps gender`() {
        val male = mapGender("male")
        val female = mapGender("female")
        val disclosed = mapGender("disclosed")
        val other = mapGender("other")

        assertEquals(Gender.Male, male)
        assertEquals(Gender.Female, female)
        assertEquals(Gender.Disclosed, disclosed)
        assertEquals(Gender.Other, other)
    }

    @Test
    fun `mapGenderString correctly maps gender`() {
        val male = mapGenderString(Gender.Male)
        val female = mapGenderString(Gender.Female)
        val disclosed = mapGenderString(Gender.Disclosed)
        val other = mapGenderString(Gender.Other)

        assertEquals("male", male)
        assertEquals("female", female)
        assertEquals("disclosed", disclosed)
        assertEquals("other", other)
    }

}