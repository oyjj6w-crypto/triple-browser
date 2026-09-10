package com.example

import com.example.model.WindowState
import com.example.model.Workspace
import com.example.web.TradingViewScriptHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkspaceAndScriptTest {

  @Test
  fun presetWorkspaces_eachHasThreeUrls() {
    val presets = Workspace.PRESETS
    assertTrue("At least 5 preset categories should exist", presets.size >= 5)

    presets.forEach { ws ->
      assertEquals("Each workspace must contain exactly 3 URLs for the 3 windows", 3, ws.urls.size)
      ws.urls.forEach { url ->
        assertTrue("URL must be valid TradingView chart link", url.startsWith("https://www.tradingview.com/chart/"))
      }
    }
  }

  @Test
  fun assistActions_generateCorrectJavaScript() {
    val hideScript = TradingViewScriptHelper.getScriptForAction(TradingViewScriptHelper.AssistAction.HIDE_DRAWINGS)
    assertTrue(hideScript.contains("hideDrawings"))

    val magnetScript = TradingViewScriptHelper.getScriptForAction(TradingViewScriptHelper.AssistAction.TOGGLE_MAGNET)
    assertTrue(magnetScript.contains("toggleMagnet"))

    val invertScript = TradingViewScriptHelper.getScriptForAction(TradingViewScriptHelper.AssistAction.INVERT_SCALE)
    assertTrue(invertScript.contains("invertScale"))

    val fullscreenScript = TradingViewScriptHelper.getScriptForAction(TradingViewScriptHelper.AssistAction.FULLSCREEN_CHART)
    assertTrue(fullscreenScript.contains("fullscreenChart"))

    val resetScript = TradingViewScriptHelper.getScriptForAction(TradingViewScriptHelper.AssistAction.RESET_SCALE)
    assertTrue(resetScript.contains("resetScale"))
  }

  @Test
  fun windowState_defaultsAndCopy() {
    val initial = WindowState(id = 0, currentUrl = "https://example.com")
    assertTrue(initial.isDesktopUA)
    assertFalse(initial.isMagnifierActive)
    assertTrue(initial.isAssistBarVisible)

    val updated = initial.copy(isMagnifierActive = true, isDesktopUA = false)
    assertTrue(updated.isMagnifierActive)
    assertFalse(updated.isDesktopUA)
  }

  @Test
  fun windowDistribution_ratioLogic() {
    // 3 windows total: IDs 0, 1, 2
    // Case 1: 0 hidden -> 3 visible -> 1:1:1
    val allVisible = listOf(0, 1, 2)
    assertEquals(3, allVisible.size)

    // Case 2: 1 hidden (e.g. 1) -> 2 visible -> 50% : 50%
    val hiddenOne = setOf(1)
    val remainingTwo = listOf(0, 1, 2).filterNot { hiddenOne.contains(it) }
    assertEquals(2, remainingTwo.size)
    assertEquals(listOf(0, 2), remainingTwo)

    // Case 3: 2 hidden (e.g. 0, 1) -> 1 visible -> 100%
    val hiddenTwo = setOf(0, 1)
    val remainingOne = listOf(0, 1, 2).filterNot { hiddenTwo.contains(it) }
    assertEquals(1, remainingOne.size)
    assertEquals(listOf(2), remainingOne)
  }
}
