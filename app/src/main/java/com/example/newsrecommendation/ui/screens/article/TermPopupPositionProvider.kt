package com.example.newsrecommendation.ui.screens.article

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider

/** Anchor uses window pixels from WebView. Placement never changes the reader size. */
internal class TermPopupPositionProvider(private val selection: IntRect, private val margin: Int, private val gap: Int) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect, windowSize: IntSize,
        layoutDirection: LayoutDirection, popupContentSize: IntSize
    ): IntOffset {
        val maxX = (windowSize.width - popupContentSize.width - margin).coerceAtLeast(margin)
        val maxY = (windowSize.height - popupContentSize.height - margin).coerceAtLeast(margin)
        val x = (selection.left + (selection.width - popupContentSize.width) / 2).coerceIn(margin, maxX)
        val below = selection.bottom + gap
        val above = selection.top - popupContentSize.height - gap
        val y = if (below <= maxY) below else above
        return IntOffset(x, y.coerceIn(margin, maxY))
    }
}
