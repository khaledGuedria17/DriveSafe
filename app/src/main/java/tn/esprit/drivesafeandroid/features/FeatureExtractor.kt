package tn.esprit.drivesafeandroid.features

import tn.esprit.drivesafeandroid.logic.FeatureVector
import tn.esprit.drivesafeandroid.logic.Handled
import tn.esprit.drivesafeandroid.logic.LwmsConfig

data class CompositeFeatures(
    val isMoving: Boolean,
    val isScrolling: Boolean,
    val isTapping: Boolean,
    val isActive: Boolean
)

class FeatureExtractor(private val config: LwmsConfig = LwmsConfig.DEFAULT) {
    fun extractComposites(fv: FeatureVector): CompositeFeatures {
        val isMoving = fv.vMean > config.vMin
        val isScrolling = fv.scrollCount > 0
        val isTapping = fv.tapRate > config.tauTap
        val isActive = fv.phoneHandled == Handled.YES || fv.isTyping || fv.onCall || isTapping || isScrolling
        return CompositeFeatures(isMoving, isScrolling, isTapping, isActive)
    }
}
