package tn.esprit.drivesafeandroid.logic

enum class Handled {
    NO, YES, UNKNOWN
}

enum class Severity {
    NONE, LOW, MODERATE, HIGH
}

enum class Interaction {
    TEXTING,
    HANDLED_CALL,
    HANDS_FREE_CALL,
    UNKNOWN_HANDLING_CALL,
    TAPPING,
    SCROLLING,
    UNSPECIFIED_HANDLING
}

enum class AppCat {
    NAVIGATION,
    MESSAGING,
    PHONE_CALL,
    SOCIAL_MEDIA,
    WEB_BROWSING,
    VIDEO,
    AUDIO,
    UNKNOWN,
    OTHER
}

data class FeatureVector(
    val vMean: Double,                // km/h
    val sigmaAcc: Double,
    val sigmaGyr: Double,
    val phoneHandled: Handled,
    val tapRate: Double,              // taps/s
    val scrollCount: Int,
    val isTyping: Boolean,
    val onCall: Boolean,
    val appCat: AppCat
)

data class Decision(
    val interaction: Interaction?,
    val base: Severity,
    val adjusted: Severity,
    val appCat: AppCat,
    val firedRule: String
)
