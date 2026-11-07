package tn.esprit.drivesafeandroid.logic

import org.junit.Assert.assertEquals
import org.junit.Test

class HysteresisTest {
    private val hysteresis = Hysteresis(LwmsConfig(nOn = 3, nOff = 3))

    private fun decision(severity: Severity) = Decision(
        interaction = null,
        base = severity,
        adjusted = severity,
        appCat = AppCat.OTHER,
        firedRule = "TEST"
    )

    @Test
    fun testHysteresisOnAndOff() {
        assertEquals(HysteresisState.Idle, hysteresis.getCurrentState())

        hysteresis.process(decision(Severity.LOW))
        assertEquals(HysteresisState.Idle, hysteresis.getCurrentState())

        hysteresis.process(decision(Severity.LOW))
        assertEquals(HysteresisState.Idle, hysteresis.getCurrentState())

        hysteresis.process(decision(Severity.LOW))
        assertEquals(HysteresisState.Active(Severity.LOW), hysteresis.getCurrentState())

        hysteresis.process(decision(Severity.NONE))
        assertEquals(HysteresisState.Active(Severity.LOW), hysteresis.getCurrentState())

        hysteresis.process(decision(Severity.NONE))
        assertEquals(HysteresisState.Active(Severity.LOW), hysteresis.getCurrentState())

        hysteresis.process(decision(Severity.NONE))
        assertEquals(HysteresisState.Idle, hysteresis.getCurrentState())
    }

    @Test
    fun testFlipFlopResetsCandidateCount() {
        hysteresis.reset()
        hysteresis.process(decision(Severity.LOW))
        hysteresis.process(decision(Severity.LOW))
        hysteresis.process(decision(Severity.HIGH))
        assertEquals(HysteresisState.Idle, hysteresis.getCurrentState())

        hysteresis.process(decision(Severity.HIGH))
        hysteresis.process(decision(Severity.HIGH))
        assertEquals(HysteresisState.Active(Severity.HIGH), hysteresis.getCurrentState())
    }
}
