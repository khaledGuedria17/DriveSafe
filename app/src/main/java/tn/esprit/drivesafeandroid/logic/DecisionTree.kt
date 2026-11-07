package tn.esprit.drivesafeandroid.logic

import tn.esprit.drivesafeandroid.features.FeatureExtractor

class DecisionTree(private val config: LwmsConfig = LwmsConfig.DEFAULT) {
    private val featureExtractor = FeatureExtractor(config)

    fun evaluate(fv: FeatureVector): Decision {
        val composites = featureExtractor.extractComposites(fv)

        // R0: Stationary
        if (!composites.isMoving) {
            return Decision(
                interaction = null,
                base = Severity.NONE,
                adjusted = Severity.NONE,
                appCat = fv.appCat,
                firedRule = "R0_STATIONARY"
            )
        }

        // R1: Navigation Exemption (requires handled == NO exactly per spec section 12 item 5)
        if (fv.appCat == AppCat.NAVIGATION && fv.phoneHandled == Handled.NO) {
            return Decision(
                interaction = null,
                base = Severity.NONE,
                adjusted = Severity.NONE,
                appCat = fv.appCat,
                firedRule = "R1_NAV_EXEMPT"
            )
        }

        // R2: Inactivity
        if (!composites.isActive) {
            return Decision(
                interaction = null,
                base = Severity.NONE,
                adjusted = Severity.NONE,
                appCat = fv.appCat,
                firedRule = "R2_INACTIVE"
            )
        }

        // R3: Texting
        if (fv.isTyping) {
            val base = Severity.HIGH
            val adjusted = escalateSpeed(base, fv.vMean)
            return Decision(
                interaction = Interaction.TEXTING,
                base = base,
                adjusted = adjusted,
                appCat = fv.appCat,
                firedRule = "R3_TEXTING"
            )
        }

        // R4: Calling
        if (fv.onCall) {
            val (interaction, base) = when (fv.phoneHandled) {
                Handled.YES -> Interaction.HANDLED_CALL to Severity.HIGH
                Handled.NO -> Interaction.HANDS_FREE_CALL to Severity.MODERATE
                Handled.UNKNOWN -> Interaction.UNKNOWN_HANDLING_CALL to Severity.MODERATE
            }
            val adjusted = escalateSpeed(base, fv.vMean)
            val ruleName = when (fv.phoneHandled) {
                Handled.YES -> "R4_CALL_HANDLED"
                Handled.NO -> "R4_CALL_HANDSFREE"
                Handled.UNKNOWN -> "R4_CALL_UNKNOWN"
            }
            return Decision(
                interaction = interaction,
                base = base,
                adjusted = adjusted,
                appCat = fv.appCat,
                firedRule = ruleName
            )
        }

        // R5: Scroll / Tap / Unspecified handling
        if (composites.isTapping || composites.isScrolling) {
            val (interaction, base) = if (composites.isTapping) {
                Interaction.TAPPING to Severity.MODERATE
            } else {
                Interaction.SCROLLING to Severity.LOW
            }
            val adjusted = escalateSpeed(base, fv.vMean)
            val ruleName = if (composites.isTapping) "R5_TAPPING" else "R5_SCROLLING"
            return Decision(
                interaction = interaction,
                base = base,
                adjusted = adjusted,
                appCat = fv.appCat,
                firedRule = ruleName
            )
        }

        if (fv.phoneHandled == Handled.YES) {
            val base = Severity.MODERATE
            val adjusted = escalateSpeed(base, fv.vMean)
            return Decision(
                interaction = Interaction.UNSPECIFIED_HANDLING,
                base = base,
                adjusted = adjusted,
                appCat = fv.appCat,
                firedRule = "R5_UNSPECIFIED"
            )
        }

        // Fallback if none matched
        return Decision(
            interaction = null,
            base = Severity.NONE,
            adjusted = Severity.NONE,
            appCat = fv.appCat,
            firedRule = "R5_NONE"
        )
    }

    private fun escalateSpeed(base: Severity, vMean: Double): Severity {
        if (base == Severity.NONE) return Severity.NONE

        val increment = when {
            vMean >= config.vHigh -> 2
            vMean >= config.vLow -> 1
            else -> 0
        }

        val tiers = listOf(Severity.NONE, Severity.LOW, Severity.MODERATE, Severity.HIGH)
        val baseIndex = tiers.indexOf(base)
        val newIndex = (baseIndex + increment).coerceAtMost(Severity.HIGH.ordinal)
        return tiers[newIndex]
    }
}
