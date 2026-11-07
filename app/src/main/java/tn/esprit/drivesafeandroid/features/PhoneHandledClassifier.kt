package tn.esprit.drivesafeandroid.features

import tn.esprit.drivesafeandroid.logic.Handled

data class CalibrationProfile(
    val muAcc0: Double = 0.1,
    val sigmaAcc0: Double = 0.05,
    val muGyr0: Double = 0.1,
    val sigmaGyr0: Double = 0.05,
    val tauL: Double = 1.0,
    val tauH: Double = 2.0,
    val isCalibrated: Boolean = false
)

class PhoneHandledClassifier(
    private var profile: CalibrationProfile = CalibrationProfile()
) {
    fun updateProfile(newProfile: CalibrationProfile) {
        profile = newProfile
    }

    fun classify(sigmaAcc: Double, sigmaGyr: Double, validSampleRatio: Double = 1.0): Handled {
        if (validSampleRatio < 0.8) {
            return Handled.UNKNOWN
        }
        if (!profile.isCalibrated) {
            return Handled.UNKNOWN
        }
        if (profile.tauL >= profile.tauH) {
            return Handled.UNKNOWN
        }

        val sigmaAccNorm = if (profile.sigmaAcc0 > 0.0) (sigmaAcc - profile.muAcc0) / profile.sigmaAcc0 else 0.0
        val sigmaGyrNorm = if (profile.sigmaGyr0 > 0.0) (sigmaGyr - profile.muGyr0) / profile.sigmaGyr0 else 0.0
        val s = sigmaAccNorm + sigmaGyrNorm

        return when {
            s < profile.tauL -> Handled.NO
            s < profile.tauH -> Handled.UNKNOWN
            else -> Handled.YES
        }
    }
}
