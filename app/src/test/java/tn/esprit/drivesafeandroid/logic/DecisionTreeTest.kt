package tn.esprit.drivesafeandroid.logic

import org.junit.Assert.assertEquals
import org.junit.Test

class DecisionTreeTest {
    private val dt = DecisionTree()

    private fun baseFeatureVector(
        vMean: Double = 40.0,
        phoneHandled: Handled = Handled.NO,
        tapRate: Double = 0.0,
        scrollCount: Int = 0,
        isTyping: Boolean = false,
        onCall: Boolean = false,
        appCat: AppCat = AppCat.OTHER
    ) = FeatureVector(
        vMean = vMean,
        sigmaAcc = 0.1,
        sigmaGyr = 0.1,
        phoneHandled = phoneHandled,
        tapRate = tapRate,
        scrollCount = scrollCount,
        isTyping = isTyping,
        onCall = onCall,
        appCat = appCat
    )

    @Test
    fun testStationaryOverridesEverything() {
        val fv = baseFeatureVector(vMean = 2.0, isTyping = true)
        val decision = dt.evaluate(fv)
        assertEquals(Severity.NONE, decision.adjusted)
        assertEquals("R0_STATIONARY", decision.firedRule)
    }

    @Test
    fun testNavExemptionHandledNo() {
        val fv = baseFeatureVector(vMean = 50.0, appCat = AppCat.NAVIGATION, phoneHandled = Handled.NO, scrollCount = 5)
        val decision = dt.evaluate(fv)
        assertEquals(Severity.NONE, decision.adjusted)
        assertEquals("R1_NAV_EXEMPT", decision.firedRule)
    }

    @Test
    fun testNavExemptionHandledYesNotExempt() {
        val fv = baseFeatureVector(vMean = 20.0, appCat = AppCat.NAVIGATION, phoneHandled = Handled.YES, scrollCount = 0)
        val decision = dt.evaluate(fv)
        assertEquals(Severity.MODERATE, decision.adjusted)
        assertEquals("R5_UNSPECIFIED", decision.firedRule)
    }

    @Test
    fun testInactivity() {
        val fv = baseFeatureVector(vMean = 40.0, phoneHandled = Handled.NO, tapRate = 0.0, scrollCount = 0, isTyping = false, onCall = false)
        val decision = dt.evaluate(fv)
        assertEquals(Severity.NONE, decision.adjusted)
        assertEquals("R2_INACTIVE", decision.firedRule)
    }

    @Test
    fun testTextingHighWithSpeedEscalation() {
        val fv = baseFeatureVector(vMean = 60.0, isTyping = true)
        val decision = dt.evaluate(fv)
        assertEquals(Interaction.TEXTING, decision.interaction)
        assertEquals(Severity.HIGH, decision.base)
        assertEquals(Severity.HIGH, decision.adjusted)
        assertEquals("R3_TEXTING", decision.firedRule)
    }

    @Test
    fun testCallingHandledYes() {
        val fv = baseFeatureVector(vMean = 20.0, onCall = true, phoneHandled = Handled.YES)
        val decision = dt.evaluate(fv)
        assertEquals(Interaction.HANDLED_CALL, decision.interaction)
        assertEquals(Severity.HIGH, decision.base)
        assertEquals(Severity.HIGH, decision.adjusted)
        assertEquals("R4_CALL_HANDLED", decision.firedRule)
    }

    @Test
    fun testCallingHandsFree() {
        val fv = baseFeatureVector(vMean = 20.0, onCall = true, phoneHandled = Handled.NO)
        val decision = dt.evaluate(fv)
        assertEquals(Interaction.HANDS_FREE_CALL, decision.interaction)
        assertEquals(Severity.MODERATE, decision.base)
        assertEquals(Severity.MODERATE, decision.adjusted)
        assertEquals("R4_CALL_HANDSFREE", decision.firedRule)
    }

    @Test
    fun testTappingBeatsScrolling() {
        val fv = baseFeatureVector(vMean = 20.0, tapRate = 2.0, scrollCount = 5)
        val decision = dt.evaluate(fv)
        assertEquals(Interaction.TAPPING, decision.interaction)
        assertEquals(Severity.MODERATE, decision.base)
        assertEquals("R5_TAPPING", decision.firedRule)
    }

    @Test
    fun testScrolling() {
        val fv = baseFeatureVector(vMean = 20.0, tapRate = 0.5, scrollCount = 3)
        val decision = dt.evaluate(fv)
        assertEquals(Interaction.SCROLLING, decision.interaction)
        assertEquals(Severity.LOW, decision.base)
        assertEquals("R5_SCROLLING", decision.firedRule)
    }

    @Test
    fun testSpeedBoundariesAndEscalation() {
        val fv1 = baseFeatureVector(vMean = 29.9, scrollCount = 3)
        val dec1 = dt.evaluate(fv1)
        assertEquals(Severity.LOW, dec1.base)
        assertEquals(Severity.LOW, dec1.adjusted)

        val fv2 = baseFeatureVector(vMean = 30.0, scrollCount = 3)
        val dec2 = dt.evaluate(fv2)
        assertEquals(Severity.LOW, dec2.base)
        assertEquals(Severity.MODERATE, dec2.adjusted)

        val fv3 = baseFeatureVector(vMean = 59.9, scrollCount = 3)
        val dec3 = dt.evaluate(fv3)
        assertEquals(Severity.MODERATE, dec3.adjusted)

        val fv4 = baseFeatureVector(vMean = 60.0, scrollCount = 3)
        val dec4 = dt.evaluate(fv4)
        assertEquals(Severity.HIGH, dec4.adjusted)
    }

    @Test
    fun testMovingBoundary() {
        val fv5 = baseFeatureVector(vMean = 5.0, scrollCount = 3)
        assertEquals("R0_STATIONARY", dt.evaluate(fv5).firedRule)

        val fv6 = baseFeatureVector(vMean = 5.1, scrollCount = 3)
        assertEquals("R5_SCROLLING", dt.evaluate(fv6).firedRule)
    }
}
