package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.FilterDrama
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material.icons.filled.LineAxis
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ChartCyan
import com.example.ui.theme.ChartGold
import com.example.ui.theme.ChartGreen
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalSurface
import com.example.web.TradingViewScriptHelper
import kotlin.math.roundToInt

@Composable
fun TradingViewFloatingBar(
  windowId: Int,
  onActionClick: (TradingViewScriptHelper.AssistAction) -> Unit,
  modifier: Modifier = Modifier
) {
  var offsetX by remember { mutableFloatStateOf(16f) }
  var offsetY by remember { mutableFloatStateOf(60f) }

  Box(
    modifier = modifier
      .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
      .shadow(8.dp, RoundedCornerShape(24.dp))
      .clip(RoundedCornerShape(24.dp))
      .background(TerminalSurface.copy(alpha = 0.92f))
      .border(1.dp, TerminalBorder, RoundedCornerShape(24.dp))
      .padding(horizontal = 8.dp, vertical = 4.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      // Drag Handle
      Box(
        modifier = Modifier
          .size(24.dp)
          .pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
              change.consume()
              offsetX = (offsetX + dragAmount.x).coerceAtLeast(0f)
              offsetY = (offsetY + dragAmount.y).coerceAtLeast(0f)
            }
          },
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.DragHandle,
          contentDescription = "拖拽把手",
          tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
          modifier = Modifier.size(16.dp)
        )
      }

      // Action 1: 隐藏绘图 (Alt+H)
      ShortcutActionButton(
        icon = Icons.Default.VisibilityOff,
        label = "藏图",
        shortcutKey = "Alt+H",
        tag = "tv_hide_drawings_$windowId",
        onClick = { onActionClick(TradingViewScriptHelper.AssistAction.HIDE_DRAWINGS) }
      )

      // Action 2: 磁吸锁定 (Magnet)
      ShortcutActionButton(
        icon = Icons.Default.Hardware,
        label = "磁吸",
        shortcutKey = "Magnet",
        tag = "tv_magnet_$windowId",
        onClick = { onActionClick(TradingViewScriptHelper.AssistAction.TOGGLE_MAGNET) }
      )

      // Action 3: 坐标轴反转 (Alt+I)
      ShortcutActionButton(
        icon = Icons.Default.SwapVert,
        label = "反转",
        shortcutKey = "Alt+I",
        tag = "tv_invert_scale_$windowId",
        onClick = { onActionClick(TradingViewScriptHelper.AssistAction.INVERT_SCALE) }
      )

      // Action 4: 全屏图表 (Alt+F)
      ShortcutActionButton(
        icon = Icons.Default.AspectRatio,
        label = "全图",
        shortcutKey = "Alt+F",
        tag = "tv_fullscreen_chart_$windowId",
        onClick = { onActionClick(TradingViewScriptHelper.AssistAction.FULLSCREEN_CHART) }
      )

      // Action 5: 重置坐标 (Alt+R)
      ShortcutActionButton(
        icon = Icons.Default.RestartAlt,
        label = "自适",
        shortcutKey = "Alt+R",
        tag = "tv_reset_scale_$windowId",
        onClick = { onActionClick(TradingViewScriptHelper.AssistAction.RESET_SCALE) }
      )
    }
  }
}

@Composable
private fun ShortcutActionButton(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  shortcutKey: String,
  tag: String,
  onClick: () -> Unit
) {
  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(16.dp),
    color = Color.Transparent,
    modifier = Modifier.testTag(tag)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = "$label ($shortcutKey)",
        tint = ChartCyan,
        modifier = Modifier.size(13.dp)
      )
      Spacer(modifier = Modifier.width(3.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
          fontSize = 10.sp,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurface
        )
      )
    }
  }
}
