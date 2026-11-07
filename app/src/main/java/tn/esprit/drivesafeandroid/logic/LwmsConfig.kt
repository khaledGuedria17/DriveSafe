package tn.esprit.drivesafeandroid.logic

data class LwmsConfig(
    val windowSeconds: Double = 5.0,
    val strideSeconds: Double = 5.0,
    val vMin: Double = 5.0,
    val vLow: Double = 30.0,
    val vHigh: Double = 60.0,
    val tauTap: Double = 1.5,
    val nOn: Int = 1,
    val nOff: Int = 1,
    val alertRepeatSeconds: Int = 15,
    val imuHzMoving: Int = 50,
    val imuHzStationary: Int = 10,
    val obdPollMovingSeconds: Int = 1,
    val obdPollStationarySeconds: Int = 5,
    val calStationarySeconds: Int = 300,
    val calHandledSeconds: Int = 300,
    val pctLow: Double = 0.95,
    val pctHigh: Double = 0.05,
    val demoFast: Boolean = false
) {
    companion object {
        val DEFAULT = LwmsConfig()
    }
}
