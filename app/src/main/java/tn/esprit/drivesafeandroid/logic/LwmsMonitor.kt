package tn.esprit.drivesafeandroid.logic

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import tn.esprit.drivesafeandroid.data.EventRepository

object LwmsMonitor {
    private val config = LwmsConfig.DEFAULT
    private val decisionTree = DecisionTree(config)
    private val hysteresis = Hysteresis(config)

    private val _currentSpeed = MutableStateFlow(0.0) // Start at parked (0.0) by default
    val currentSpeed: StateFlow<Double> = _currentSpeed.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    private val _scrollCount = MutableStateFlow(0)
    val scrollCount: StateFlow<Int> = _scrollCount.asStateFlow()

    private val _tapRate = MutableStateFlow(0.0)
    val tapRate: StateFlow<Double> = _tapRate.asStateFlow()

    private val _onCall = MutableStateFlow(false)
    val onCall: StateFlow<Boolean> = _onCall.asStateFlow()

    private val _appCat = MutableStateFlow(AppCat.OTHER)
    val appCat: StateFlow<AppCat> = _appCat.asStateFlow()

    var alertCallback: ((Severity, String, Double) -> Unit)? = null

    fun updateSpeed(speed: Double) {
        _currentSpeed.value = speed.coerceAtLeast(0.0)
        evaluate()
    }

    fun reportTyping(isTyping: Boolean) {
        _isTyping.value = isTyping
        evaluate()
    }

    fun reportScroll() {
        _scrollCount.value = _scrollCount.value + 1
        evaluate()
    }

    fun reportTap() {
        _tapRate.value = 2.5
        evaluate()
    }

    fun reportCall(onCall: Boolean) {
        _onCall.value = onCall
        evaluate()
    }

    fun updateBehaviors(isTyping: Boolean, scrollCount: Int, tapRate: Double, onCall: Boolean, appCat: AppCat) {
        _isTyping.value = isTyping
        _scrollCount.value = scrollCount
        _tapRate.value = tapRate
        _onCall.value = onCall
        _appCat.value = appCat
        evaluate()
    }

    fun evaluate() {
        val fv = FeatureVector(
            vMean = _currentSpeed.value,
            sigmaAcc = 0.1,
            sigmaGyr = 0.1,
            phoneHandled = Handled.YES,
            tapRate = _tapRate.value,
            scrollCount = _scrollCount.value,
            isTyping = _isTyping.value,
            onCall = _onCall.value,
            appCat = _appCat.value
        )

        val decision = decisionTree.evaluate(fv)
        val hState = hysteresis.process(decision)

        val activeSeverity = when (hState) {
            is HysteresisState.Idle -> Severity.NONE
            is HysteresisState.Active -> hState.severity
        }

        if (activeSeverity != Severity.NONE) {
            EventRepository.logEvent("ALERT_TRIGGERED", activeSeverity, decision.firedRule, fv.vMean, "Distraction detected: ${decision.interaction ?: decision.firedRule}")
            alertCallback?.invoke(activeSeverity, decision.firedRule, fv.vMean)
        }

        // Reset transient indicators after evaluation window
        _isTyping.value = false
        _scrollCount.value = 0
        _tapRate.value = 0.0
    }
}
