package tn.esprit.drivesafeandroid

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import tn.esprit.drivesafeandroid.logic.LwmsMonitor

class KeyboardUsageService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            LwmsMonitor.reportTyping(true)
        }
    }

    override fun onInterrupt() {}
}
