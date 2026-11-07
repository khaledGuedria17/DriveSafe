package tn.esprit.drivesafeandroid

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import tn.esprit.drivesafeandroid.logic.LwmsMonitor

class LwmsAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                LwmsMonitor.reportScroll()
            }
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                LwmsMonitor.reportTap()
            }
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                LwmsMonitor.reportTyping(true)
            }
        }
    }

    override fun onInterrupt() {}
}
