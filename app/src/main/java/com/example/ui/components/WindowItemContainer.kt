package com.example.ui.components

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.WindowState
import com.example.ui.theme.TerminalBackground
import com.example.ui.theme.TerminalBorder
import com.example.web.TradingViewScriptHelper
import com.example.web.WebEngineManager

@Composable
fun WindowItemContainer(
  windowIndex: Int,
  state: WindowState,
  isMaximized: Boolean,
  webEngine: WebEngineManager,
  onToggleMaximize: () -> Unit,
  onHideWindow: () -> Unit,
  onEditUrlClick: () -> Unit,
  onZoomChange: (Float) -> Unit,
  currentZoom: Float,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(TerminalBackground)
      .border(0.5.dp, TerminalBorder)
      .testTag("window_container_$windowIndex")
  ) {
    // Window Top Control Bar
    WindowControlBar(
      state = state,
      isMaximized = isMaximized,
      onNavigateBack = { webEngine.goBack(windowIndex) },
      onNavigateForward = { webEngine.goForward(windowIndex) },
      onReload = { webEngine.reload(windowIndex) },
      onToggleDesktopUA = { webEngine.toggleDesktopMode(windowIndex) },
      onToggleMagnifier = { webEngine.toggleMagnifier(windowIndex) },
      onToggleAssistBar = { webEngine.toggleAssistBar(windowIndex) },
      onToggleMaximize = onToggleMaximize,
      onHideWindow = onHideWindow,
      onEditUrlClick = onEditUrlClick
    )

    // Main Web View and overlays
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxSize()
    ) {
      // Direct Native WebView View
      AndroidView(
        factory = {
          val wv = webEngine.webViews[windowIndex]
          (wv.parent as? ViewGroup)?.removeView(wv)
          wv
        },
        modifier = Modifier
          .fillMaxSize()
          .testTag("native_webview_$windowIndex")
      )

      // Injected Tampermonkey Floating Action Bar for TradingView Shortcuts
      if (state.isAssistBarVisible) {
        TradingViewFloatingBar(
          windowId = windowIndex,
          onActionClick = { action ->
            webEngine.executeAssistAction(windowIndex, action)
          }
        )
      }

      // Local High-Definition Loupe Magnifier
      if (state.isMagnifierActive) {
        LoupeMagnifierOverlay(
          windowId = windowIndex,
          zoom = currentZoom,
          onZoomChange = onZoomChange,
          onClose = { webEngine.setMagnifier(windowIndex, false) }
        )
      }
    }
  }
}
