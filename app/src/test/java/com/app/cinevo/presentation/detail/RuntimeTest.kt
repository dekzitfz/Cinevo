package com.app.cinevo.presentation.detail

import org.junit.Assert.assertEquals
import org.junit.Test

class RuntimeTest {
    @Test
    fun `splits a runtime into hours and minutes`() {
        assertEquals(Runtime(hours = 2, minutes = 15), runtimeOf(135))
        assertEquals(Runtime(hours = 1, minutes = 1), runtimeOf(61))
    }

    @Test
    fun `runtime under an hour has zero hours`() {
        assertEquals(Runtime(hours = 0, minutes = 45), runtimeOf(45))
    }

    @Test
    fun `whole hours have zero minutes`() {
        assertEquals(Runtime(hours = 2, minutes = 0), runtimeOf(120))
    }
}
