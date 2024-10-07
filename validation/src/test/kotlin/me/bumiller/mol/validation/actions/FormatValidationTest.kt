package me.bumiller.mol.validation.actions

import me.bumiller.mol.model.http.RequestException
import me.bumiller.mol.validation.validateThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class FormatValidationTest {

    @Test
    fun `isEmail allows only emails`() {
        assertDoesNotThrow {
            validateThat("test@domain.com").isEmail()
        }
        assertDoesNotThrow {
            validateThat("test.dak@domain.com").isEmail()
        }
        assertDoesNotThrow {
            validateThat("test.dak+3@domain.com").isEmail()
        }

        val ex1 = assertThrows<RequestException> {
            validateThat("test@com").isEmail()
        }
        assertEquals(400, ex1.code)

        val ex2 = assertThrows<RequestException> {
            validateThat("testcom.de").isEmail()
        }
        assertEquals(400, ex2.code)

        val ex3 = assertThrows<RequestException> {
            validateThat("dadwad32").isEmail()
        }
        assertEquals(400, ex3.code)
    }

    @Test
    fun `isUsername allows only letters, digits, dashes and underscore and checks for length`() {
        assertDoesNotThrow {
            validateThat("fsdfsfsdfs").isUsername()
        }
        assertDoesNotThrow {
            validateThat("dfs68sdfsdf-__").isUsername()
        }
        assertDoesNotThrow {
            validateThat("sdfsdf772222-__").isUsername()
        }

        val ex1 = assertThrows<RequestException> {
            validateThat("asdasd").isUsername()
        }
        assertEquals(400, ex1.code)

        val ex2 = assertThrows<RequestException> {
            validateThat("asdadasdadadadsadadad").isUsername()
        }
        assertEquals(400, ex2.code)

        val ex3 = assertThrows<RequestException> {
            validateThat("asdda@@¼(=)2").isUsername()
        }
        assertEquals(400, ex3.code)

        val ex4 = assertThrows<RequestException> {
            validateThat("asd3 asd8aa").isUsername()
        }
        assertEquals(400, ex4.code)
    }

    @Test
    fun `isPassword allows only only minimun six letters`() {
        assertDoesNotThrow {
            validateThat("fsdfsfsdfs").isPassword()
        }
        assertDoesNotThrow {
            validateThat("asd()922@€ø@@„.ð-__").isPassword()
        }
        assertDoesNotThrow {
            validateThat("sdfsdf772222-__").isPassword()
        }

        val ex1 = assertThrows<RequestException> {
            validateThat("123").isPassword()
        }
        assertEquals(400, ex1.code)

        val ex2 = assertThrows<RequestException> {
            validateThat("676g").isPassword()
        }
        assertEquals(400, ex2.code)
    }

    @Test
    fun `isProfileName allows only names with letters and spaces`() {
        assertDoesNotThrow {
            validateThat("fsdfsfsdfs").isProfileName()
        }
        assertDoesNotThrow {
            validateThat("John Doe").isProfileName()
        }
        assertDoesNotThrow {
            validateThat("John Micheal Doe").isProfileName()
        }
        assertDoesNotThrow {
            validateThat("Ed").isProfileName()
        }

        val ex1 = assertThrows<RequestException> {
            validateThat("John 12").isProfileName()
        }
        assertEquals(400, ex1.code)

        val ex2 = assertThrows<RequestException> {
            validateThat("John_Doe").isProfileName()
        }
        assertEquals(400, ex2.code)

        val ex3 = assertThrows<RequestException> {
            validateThat("F").isProfileName()
        }
        assertEquals(400, ex3.code)
    }

}