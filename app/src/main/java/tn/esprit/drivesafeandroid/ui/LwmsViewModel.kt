package tn.esprit.drivesafeandroid.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import tn.esprit.drivesafeandroid.features.CalibrationProfile
import tn.esprit.drivesafeandroid.features.FeatureExtractor
import tn.esprit.drivesafeandroid.features.PhoneHandledClassifier
import tn.esprit.drivesafeandroid.logic.AppCat
import tn.esprit.drivesafeandroid.logic.DecisionTree
import tn.esprit.drivesafeandroid.logic.FeatureVector
import tn.esprit.drivesafeandroid.logic.Handled
import tn.esprit.drivesafeandroid.logic.Hysteresis
import tn.esprit.drivesafeandroid.logic.HysteresisState
import tn.esprit.drivesafeandroid.logic.LwmsConfig
import tn.esprit.drivesafeandroid.logic.Severity
import tn.esprit.drivesafeandroid.sensor.ObdSimulator

data class DashboardState(
    val speed: Double = 0.0,
    val sigmaAcc: Double = 0.1,
    val sigmaGyr: Double = 0.1,
    val phoneHandled: Handled = Handled.NO,
    val sScore: Double = 0.0,
    val tapRate: Double = 0.0,
    val scrollCount: Int = 0,
    val isTyping: Boolean = false,
    val onCall: Boolean = false,
    val appCat: AppCat = AppCat.OTHER,
    val isMoving: Boolean = false,
    val isScrolling: Boolean = false,
    val isTapping: Boolean = false,
    val isActive: Boolean = false,
    val firedRule: String = "R0_STATIONARY",
    val baseSeverity: Severity = Severity.NONE,
    val adjustedSeverity: Severity = Severity.NONE,
    val hysteresisState: HysteresisState = HysteresisState.Idle,
    val alertTimeline: List<String> = emptyList()
)

class LwmsViewModel : ViewModel() {
    private val config = LwmsConfig.DEFAULT
    private val obdSimulator = ObdSimulator()
    private val classifier = PhoneHandledClassifier(CalibrationProfile(isCalibrated = true, tauL = 1.0, tauH = 2.0))
    private val featureExtractor = FeatureExtractor(config)
    private val decisionTree = DecisionTree(config)
    private val hysteresis = Hysteresis(config)

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private val _isSimulationActive = MutableStateFlow(false)
    val isSimulationActive: StateFlow<Boolean> = _isSimulationActive.asStateFlow()

    private val _isConnectedToObd = MutableStateFlow(false)
    val isConnectedToObd: StateFlow<Boolean> = _isConnectedToObd.asStateFlow()

    val speedSourceText: StateFlow<String> = combine(_isConnectedToObd, _isSimulationActive) { connected, simulating ->
        when {
            connected -> "In-car OBD2"
            simulating -> "Virtual OBD2 Simulator"
            else -> " - "
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), " - ")

    fun toggleObdConnection() {
        val next = !_isConnectedToObd.value
        _isConnectedToObd.value = next
        if (next) {
            _isSimulationActive.value = false
        }
    }

    fun setSimulationActive(active: Boolean) {
        _isSimulationActive.value = active
        if (active) {
            _isConnectedToObd.value = false
        }
    }

    fun updateSpeed(speed: Double) {
        obdSimulator.setSpeed(speed)
        tn.esprit.drivesafeandroid.logic.LwmsMonitor.updateSpeed(speed)
        recompute()
    }

    fun applyPreset(preset: ObdSimulator.Preset) {
        obdSimulator.applyPreset(preset)
        tn.esprit.drivesafeandroid.logic.LwmsMonitor.updateSpeed(preset.speed)
        recompute()
    }

    fun updateOverrides(
        sigmaAcc: Double? = null,
        sigmaGyr: Double? = null,
        tapRate: Double? = null,
        scrollCount: Int? = null,
        isTyping: Boolean? = null,
        onCall: Boolean? = null,
        appCat: AppCat? = null,
        phoneHandled: Handled? = null
    ) {
        val current = _state.value
        _state.value = current.copy(
            sigmaAcc = sigmaAcc ?: current.sigmaAcc,
            sigmaGyr = sigmaGyr ?: current.sigmaGyr,
            tapRate = tapRate ?: current.tapRate,
            scrollCount = scrollCount ?: current.scrollCount,
            isTyping = isTyping ?: current.isTyping,
            onCall = onCall ?: current.onCall,
            appCat = appCat ?: current.appCat,
            phoneHandled = phoneHandled ?: current.phoneHandled
        )
        recompute()
    }

    fun runScenario(scenarioKey: String) {
        when (scenarioKey) {
            "parked_texting" -> {
                obdSimulator.setSpeed(0.0)
                updateOverrides(isTyping = true, appCat = AppCat.MESSAGING)
            }
            "nav_cradle_50" -> {
                obdSimulator.setSpeed(50.0)
                updateOverrides(isTyping = false, onCall = false, scrollCount = 0, tapRate = 0.0, phoneHandled = Handled.NO, appCat = AppCat.NAVIGATION)
            }
            "scroll_20" -> {
                obdSimulator.setSpeed(20.0)
                updateOverrides(scrollCount = 3, tapRate = 0.0, isTyping = false, onCall = false, phoneHandled = Handled.UNKNOWN, appCat = AppCat.SOCIAL_MEDIA)
            }
            "scroll_90" -> {
                obdSimulator.setSpeed(90.0)
                updateOverrides(scrollCount = 3, tapRate = 0.0, isTyping = false, onCall = false, phoneHandled = Handled.UNKNOWN, appCat = AppCat.SOCIAL_MEDIA)
            }
            "texting_45" -> {
                obdSimulator.setSpeed(45.0)
                updateOverrides(isTyping = true, appCat = AppCat.MESSAGING)
            }
            "handheld_call" -> {
                obdSimulator.setSpeed(40.0)
                updateOverrides(onCall = true, phoneHandled = Handled.YES, appCat = AppCat.PHONE_CALL)
            }
            "handsfree_call" -> {
                obdSimulator.setSpeed(40.0)
                updateOverrides(onCall = true, phoneHandled = Handled.NO, appCat = AppCat.PHONE_CALL)
            }
        }
    }

    private fun recompute() {
        val speed = obdSimulator.speedKmh.value
        val current = _state.value
        val phoneHandled = current.phoneHandled

        val sScore = ((current.sigmaAcc - 0.1) / 0.05) + ((current.sigmaGyr - 0.1) / 0.05)

        val fv = FeatureVector(
            vMean = speed,
            sigmaAcc = current.sigmaAcc,
            sigmaGyr = current.sigmaGyr,
            phoneHandled = phoneHandled,
            tapRate = current.tapRate,
            scrollCount = current.scrollCount,
            isTyping = current.isTyping,
            onCall = current.onCall,
            appCat = current.appCat
        )

        val composites = featureExtractor.extractComposites(fv)
        val decision = decisionTree.evaluate(fv)
        val hState = hysteresis.process(decision)

        val timelineEntry = "[Rule: ${decision.firedRule}] Base: ${decision.base}, Adj: ${decision.adjusted}, HState: $hState"
        val newTimeline = (listOf(timelineEntry) + current.alertTimeline).take(20)

        if (decision.adjusted != Severity.NONE) {
            tn.esprit.drivesafeandroid.data.EventRepository.logEvent("EVAL_EVENT", decision.adjusted, decision.firedRule, speed, "Rule ${decision.firedRule} fired with adjusted severity ${decision.adjusted}")
        }

        _state.value = current.copy(
            speed = speed,
            phoneHandled = phoneHandled,
            sScore = sScore,
            isMoving = composites.isMoving,
            isScrolling = composites.isScrolling,
            isTapping = composites.isTapping,
            isActive = composites.isActive,
            firedRule = decision.firedRule,
            baseSeverity = decision.base,
            adjustedSeverity = decision.adjusted,
            hysteresisState = hState,
            alertTimeline = newTimeline
        )
    }
}
