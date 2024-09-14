package me.bumiller.mol.validation.actions

import io.mockk.mockk
import me.bumiller.mol.model.http.RequestException
import me.bumiller.mol.validation.ValidationScope
import me.bumiller.mol.validation.validateThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class FormatValidationTest {

    val scope: ValidationScope = mockk()

    @Test
    fun `isEmail allows only emails`() {
        assertDoesNotThrow {
            scope.validateThat("test@domain.com").isEmail()
        }
        assertDoesNotThrow {
            scope.validateThat("test.dak@domain.com").isEmail()
        }
        assertDoesNotThrow {
            scope.validateThat("test.dak+3@domain.com").isEmail()
        }

        val ex1 = assertThrows<RequestException> {
            scope.validateThat("test@com").isEmail()
        }
        assertEquals(400, ex1.code)

        val ex2 = assertThrows<RequestException> {
            scope.validateThat("testcom.de").isEmail()
        }
        assertEquals(400, ex2.code)

        val ex3 = assertThrows<RequestException> {
            scope.validateThat("dadwad32").isEmail()
        }
        assertEquals(400, ex3.code)
    }

    @Test
    fun `isUsername allows only letters, digits, dashes and underscore and checks for length`() {
        assertDoesNotThrow {
            scope.validateThat("fsdfsfsdfs").isUsername()
        }
        assertDoesNotThrow {
            scope.validateThat("dfs68sdfsdf-__").isUsername()
        }
        assertDoesNotThrow {
            scope.validateThat("sdfsdf772222-__").isUsername()
        }

        val ex1 = assertThrows<RequestException> {
            scope.validateThat("asdasd").isUsername()
        }
        assertEquals(400, ex1.code)

        val ex2 = assertThrows<RequestException> {
            scope.validateThat("asdadasdadadadsadadad").isUsername()
        }
        assertEquals(400, ex2.code)

        val ex3 = assertThrows<RequestException> {
            scope.validateThat("asdda@@¼(=)2").isUsername()
        }
        assertEquals(400, ex3.code)

        val ex4 = assertThrows<RequestException> {
            scope.validateThat("asd3 asd8aa").isUsername()
        }
        assertEquals(400, ex4.code)
    }

    @Test
    fun `isPassword allows only only minimun six letters`() {
        assertDoesNotThrow {
            scope.validateThat("fsdfsfsdfs").isPassword()
        }
        assertDoesNotThrow {
            scope.validateThat("asd()922@€ø@@„.ð-__").isPassword()
        }
        assertDoesNotThrow {
            scope.validateThat("sdfsdf772222-__").isPassword()
        }

        val ex1 = assertThrows<RequestException> {
            scope.validateThat("123").isPassword()
        }
        assertEquals(400, ex1.code)

        val ex2 = assertThrows<RequestException> {
            scope.validateThat("676g").isPassword()
        }
        assertEquals(400, ex2.code)
    }

    @Test
    fun `isProfileName allows only names with letters and spaces`() {
        assertDoesNotThrow {
            scope.validateThat("fsdfsfsdfs").isProfileName()
        }
        assertDoesNotThrow {
            scope.validateThat("John Doe").isProfileName()
        }
        assertDoesNotThrow {
            scope.validateThat("John Micheal Doe").isProfileName()
        }
        assertDoesNotThrow {
            scope.validateThat("Ed").isProfileName()
        }

        val ex1 = assertThrows<RequestException> {
            scope.validateThat("John 12").isProfileName()
        }
        assertEquals(400, ex1.code)

        val ex2 = assertThrows<RequestException> {
            scope.validateThat("John_Doe").isProfileName()
        }
        assertEquals(400, ex2.code)

        val ex3 = assertThrows<RequestException> {
            scope.validateThat("F").isProfileName()
        }
        assertEquals(400, ex3.code)
    }

}