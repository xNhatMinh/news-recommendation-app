package com.example.newsrecommendation.ui.screens.article

import android.content.Context
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import org.json.JSONTokener
import org.json.JSONObject
import androidx.compose.ui.unit.IntRect

/** Reads selection while the native text selection toolbar is active. */
class SelectionWebView(
    context: Context,
    private val onAnchorChanged: (IntRect) -> Unit,
    private val onSelectionCleared: () -> Unit,
    private val onTermSelected: (String) -> Unit
) : WebView(context) {
    private var selecting = false
    private var candidate = ""
    private var delivered = ""
    private var stableSince = 0L
    private val selectionPoll = object : Runnable {
        override fun run() {
            if (!selecting) return
            evaluateJavascript("""
                (function() {
                    var s = window.getSelection();
                    if (!s || !s.rangeCount || s.isCollapsed) return JSON.stringify({text: ''});
                    var r = s.getRangeAt(0).getBoundingClientRect();
                    return JSON.stringify({text: s.toString(), left: r.left, top: r.top,
                        right: r.right, bottom: r.bottom, viewport: window.innerWidth});
                })()
            """.trimIndent()) { raw ->
                if (selecting) {
                    val selection = runCatching {
                        JSONObject(JSONTokener(raw).nextValue() as String)
                    }.getOrNull()
                    val value = selection?.optString("text").orEmpty().trim().replace(Regex("\\s+"), " ")
                    if (value.isNotEmpty() && selection != null) {
                        val scale = width / selection.optDouble("viewport", width.toDouble()).coerceAtLeast(1.0)
                        val location = IntArray(2)
                        getLocationInWindow(location)
                        onAnchorChanged(IntRect(
                            location[0] + (selection.optDouble("left") * scale).toInt(),
                            location[1] + (selection.optDouble("top") * scale).toInt(),
                            location[0] + (selection.optDouble("right") * scale).toInt(),
                            location[1] + (selection.optDouble("bottom") * scale).toInt()
                        ))
                    } else if (delivered.isNotEmpty()) {
                        delivered = ""
                        onSelectionCleared()
                    }
                    val now = android.os.SystemClock.uptimeMillis()
                    if (value != candidate) {
                        if (delivered.isNotEmpty()) {
                            delivered = ""
                            onSelectionCleared()
                        }
                        candidate = value; stableSince = now
                    }
                    if (value.isNotEmpty() && value.length <= 120 && value.split(' ').size <= 15 &&
                        value != delivered && now - stableSince >= 600) {
                        delivered = value
                        onTermSelected(value)
                    }
                    postDelayed(this, 250)
                }
            }
        }
    }

    override fun startActionMode(callback: ActionMode.Callback, type: Int): ActionMode? {
        val wrapper = object : ActionMode.Callback2() {
            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                val created = callback.onCreateActionMode(mode, menu)
                if (created) {
                    selecting = true; candidate = ""; delivered = ""
                    removeCallbacks(selectionPoll); post(selectionPoll)
                }
                return created
            }
            override fun onPrepareActionMode(mode: ActionMode, menu: Menu) = callback.onPrepareActionMode(mode, menu)
            override fun onActionItemClicked(mode: ActionMode, item: MenuItem) = callback.onActionItemClicked(mode, item)
            override fun onDestroyActionMode(mode: ActionMode) {
                selecting = false; removeCallbacks(selectionPoll)
                onSelectionCleared()
                callback.onDestroyActionMode(mode)
            }
            override fun onGetContentRect(mode: ActionMode, view: android.view.View, outRect: android.graphics.Rect) {
                if (callback is ActionMode.Callback2) callback.onGetContentRect(mode, view, outRect)
                else super.onGetContentRect(mode, view, outRect)
            }
        }
        return super.startActionMode(wrapper, type)
    }

    override fun destroy() {
        selecting = false; removeCallbacks(selectionPoll)
        super.destroy()
    }
}
