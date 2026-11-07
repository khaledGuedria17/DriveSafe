package tn.esprit.drivesafeandroid.sensor

import kotlinx.coroutines.flow.Flow

interface SpeedSource {
    val speedKmh: Flow<Double>
}
