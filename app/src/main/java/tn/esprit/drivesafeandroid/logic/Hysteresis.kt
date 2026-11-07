package tn.esprit.drivesafeandroid.logic

sealed class HysteresisState {
    object Idle : HysteresisState()
    data class Active(val severity: Severity) : HysteresisState()
}

class Hysteresis(private val config: LwmsConfig = LwmsConfig.DEFAULT) {
    private var currentState: HysteresisState = HysteresisState.Idle
    private var candidateSeverity: Severity? = null
    private var candidateCount: Int = 0
    private var offCount: Int = 0

    fun process(decision: Decision): HysteresisState {
        val tier = decision.adjusted

        if (tier == Severity.NONE) {
            candidateSeverity = null
            candidateCount = 0
            when (currentState) {
                is HysteresisState.Idle -> {
                    // stays idle
                }
                is HysteresisState.Active -> {
                    offCount++
                    if (offCount >= config.nOff) {
                        currentState = HysteresisState.Idle
                        offCount = 0
                    }
                }
            }
        } else {
            offCount = 0
            if (candidateSeverity == tier) {
                candidateCount++
            } else {
                candidateSeverity = tier
                candidateCount = 1
            }

            if (candidateCount >= config.nOn) {
                currentState = HysteresisState.Active(tier)
            }
        }

        return currentState
    }

    fun getCurrentState(): HysteresisState = currentState

    fun reset() {
        currentState = HysteresisState.Idle
        candidateSeverity = null
        candidateCount = 0
        offCount = 0
    }
}
