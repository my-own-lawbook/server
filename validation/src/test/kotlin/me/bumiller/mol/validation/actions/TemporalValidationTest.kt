package me.bumiller.mol.validation.actions

import kotlinx.datetime.*
import me.bumiller.mol.model.http.RequestException
import me.bumiller.mol.validation.validateThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class TemporalValidationTest {

    @Test
    fun `isInPast return true if in past and false if note`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        val inPast = today.minus(DatePeriod(months = 5))
        val inFuture = today.plus(DatePeriod(months = 5))

        val ex1 = assertThrows<RequestException> {
            validateThat(inFuture).isInPast()
        }
        assertEquals(400, ex1.code)

        val ex2 = assertThrows<RequestException> {
            validateThat(today).isInPast()
        }
        assertEquals(400, ex2.code)

        assertDoesNotThrow {
            validateThat(inPast).isInPast()
        }
    }

}