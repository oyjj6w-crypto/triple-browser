package com.example

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.WindowState
import com.example.ui.components.SaveWorkspaceDialog
import com.example.ui.components.UrlEditDialog
import com.example.ui.components.WindowItemContainer
import com.example.ui.components.WorkspaceBar
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TerminalBackground
import com.example.viewmodel.BrowserViewModel

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      MyApplicationTheme {
        TripleChartBrowserApp()
      }
    }
  }
}

@Composable
fun TripleChartBrowserApp(
  viewModel: BrowserViewModel = viewModel()
) {
  val windowStates by viewModel.webEngine.windowStates.collectAsStateWithLifecycle()
  val hiddenWindows by viewModel.hiddenWindows.collectAsStateWithLifecycle()
  val maximizedWindowId by viewModel.maximizedWindowId.collectAsStateWithLifecycle()
  val workspaces by viewModel.workspaces.collectAsStateWithLifecycle()
  val activeWorkspaceId by viewModel.activeWorkspaceId.collectAsStateWithLifecycle()
  val editingWindowId by viewModel.editingWindowId.collectAsStateWithLifecycle()
  val isSaveWorkspaceDialogOpen by viewModel.isSaveWorkspaceDialogOpen.collectAsStateWithLifecycle()
  val magnifierZoom by viewModel.magnifierZoom.collectAsStateWithLifecycle()

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .background(TerminalBackground)
      .statusBarsPadding()
      .navigationBarsPadding(),
    topBar = {
      WorkspaceBar(
        workspaces = workspaces,
        activeWorkspaceId = activeWorkspaceId,
        hiddenCount = hiddenWindows.size,
        maximizedWindowId = maximizedWindowId,
        onSelectWorkspace = { viewModel.selectWorkspace(it) },
        onSaveCurrentWorkspaceClick = { viewModel.openSaveWorkspaceDialog() },
        onDeleteCustomWorkspace = { viewModel.deleteCustomWorkspace(it) },
        onRestoreAllWindows = { viewModel.restoreAllWindows() }
      )
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .background(TerminalBackground)
    ) {
      Row(
        modifier = Modifier.fillMaxSize()
      ) {
        for (i in 0 until 3) {
          val isHidden = hiddenWindows.contains(i)
          val isMaximized = maximizedWindowId == i
          val shouldShow = if (maximizedWindowId != null) isMaximized else !isHidden

          // Crucial zero-reload pattern: keep all 3 WebViews mounted continuously.
          // Hidden or collapsed windows take width 0.dp with alpha 0f so that
          // WebSockets, WebGL, and JavaScript timers continue operating seamlessly.
          val windowModifier = if (shouldShow) {
            Modifier
              .weight(1f)
              .fillMaxHeight()
          } else {
            Modifier
              .width(0.dp)
              .fillMaxHeight()
              .alpha(0f)
          }

          val state = windowStates.getOrElse(i) { WindowState(id = i) }

          WindowItemContainer(
            windowIndex = i,
            state = state,
            isMaximized = isMaximized,
            webEngine = viewModel.webEngine,
            onToggleMaximize = { viewModel.toggleMaximizeWindow(i) },
            onHideWindow = { viewModel.toggleHideWindow(i) },
            onEditUrlClick = { viewModel.openUrlEditDialog(i) },
            onZoomChange = { viewModel.setMagnifierZoom(it) },
            currentZoom = magnifierZoom,
            modifier = windowModifier
          )
        }
      }
    }
  }

  // Dialog to change URL for a specific window
  editingWindowId?.let { targetWindowId ->
    val currentUrl = windowStates.getOrNull(targetWindowId)?.currentUrl.orEmpty()
    UrlEditDialog(
      windowId = targetWindowId,
      initialUrl = currentUrl,
      onConfirm = { newUrl ->
        viewModel.webEngine.loadUrl(targetWindowId, newUrl)
        viewModel.closeUrlEditDialog()
      },
      onDismiss = { viewModel.closeUrlEditDialog() }
    )
  }

  // Dialog to save current 3 URLs as a workspace group
  if (isSaveWorkspaceDialogOpen) {
    val currentUrls = windowStates.map { it.currentUrl }
    SaveWorkspaceDialog(
      currentUrls = currentUrls,
      onConfirm = { name ->
        viewModel.saveCurrentWorkspace(name)
      },
      onDismiss = { viewModel.closeSaveWorkspaceDialog() }
    )
  }
}
