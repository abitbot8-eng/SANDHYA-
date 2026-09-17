package com.sandhya.ai.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class SandhyaAccessibilityService : AccessibilityService() {
    override fun onServiceConnected() {
        super.onServiceConnected()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    fun home(): Boolean = performGlobalAction(GLOBAL_ACTION_HOME)
    fun back(): Boolean = performGlobalAction(GLOBAL_ACTION_BACK)
    fun recents(): Boolean = performGlobalAction(GLOBAL_ACTION_RECENTS)

    fun scrollDown(): Boolean = rootInActiveWindow?.let { scrollNode(it, true) } ?: false
    fun scrollUp(): Boolean = rootInActiveWindow?.let { scrollNode(it, false) } ?: false

    private fun scrollNode(node: AccessibilityNodeInfo, down: Boolean): Boolean {
        if (node.isScrollable) {
            return node.performAction(
                if (down) AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
                else AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
            )
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (scrollNode(child, down)) return true
        }
        return false
    }

    fun tap(x: Float, y: Float): Boolean {
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
            .build()
        return dispatchGesture(gesture, null, null)
    }

    fun readVisibleText(): List<String> {
        val root = rootInActiveWindow ?: return emptyList()
        val out = mutableListOf<String>()
        collectText(root, out)
        return out
    }

    private fun collectText(node: AccessibilityNodeInfo, out: MutableList<String>) {
        node.text?.toString()?.takeIf { it.isNotBlank() }?.let(out::add)
        node.contentDescription?.toString()?.takeIf { it.isNotBlank() }?.let(out::add)
        for (i in 0 until node.childCount) node.getChild(i)?.let { collectText(it, out) }
    }
}
