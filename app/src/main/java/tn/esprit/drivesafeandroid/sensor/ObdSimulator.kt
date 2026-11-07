package tn.esprit.drivesafeandroid.sensor

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ObdSimulator : SpeedSource {
    private val _speedKmh = MutableStateFlow(0.0)
    override val speedKmh: StateFlow<Double> = _speedKmh.asStateFlow()

    fun setSpeed(speed: Double) {
        _speedKmh.value = speed.coerceAtLeast(0.0)
    }

    enum class Preset(val speed: Double) {
        PARKED(0.0),
        RESIDENTIAL(20.0),
        URBAN(45.0),
        HIGHWAY(90.0)
    }

    fun applyPreset(preset: Preset) {
        setSpeed(preset.speed)
    }
}
