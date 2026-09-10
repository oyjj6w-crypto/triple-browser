package com.example.ui.components

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.magnifier
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ChartCyan
import com.example.ui.theme.ChartGold
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalSurface
import kotlin.math.roundToInt

@Composable
fun LoupeMagnifierOverlay(
  windowId: Int,
  zoom: Float,
  onZoomChange: (Float) -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier
) {
  val density = LocalDensity.current
  var touchPos by remember { mutableStateOf(Offset(200f, 300f)) }

  // Check if system magnifier is supported (API 28+)
  val isMagnifierSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

  Box(
    modifier = modifier
      .fillMaxSize()
      .pointerInput(Unit) {
        detectDragGestures(
          onDragStart = { offset -> touchPos = offset },
          onDrag = { change, _ ->
            change.consume()
            touchPos = change.position
          }
        )
      }
      .then(
        if (isMagnifierSupported) {
          Modifier.magnifier(
            sourceCenter = { touchPos },
            magnifierCenter = {
              // Position the magnifier lens right above touch point
              val offsetY = touchPos.y - with(density) { 130.dp.toPx() }
              Offset(touchPos.x, offsetY.coerceAtLeast(with(density) { 70.dp.toPx() }))
            },
            zoom = zoom,
            size = DpSize(180.dp, 180.dp),
            cornerRadius = 90.dp,
            elevation = 10.dp
          )
        } else {
          Modifier
        }
      )
  ) {
    // Crosshair Reticle Indicator following touch position
    Canvas(
      modifier = Modifier
        .size(40.dp)
        .offset {
          IntOffset(
            (touchPos.x - with(density) { 20.dp.toPx() }).roundToInt(),
            (touchPos.y - with(density) { 20.dp.toPx() }).roundToInt()
          )
        }
    ) {
      val center = Offset(size.width / 2, size.height / 2)
      drawCircle(
        color = ChartGold,
        radius = size.width / 2 - 2f,
        style = Stroke(width = 2f)
      )
      // Horizontal crosshair
      drawLine(
        color = ChartGold.copy(alpha = 0.8f),
        start = Offset(0f, center.y),
        end = Offset(size.width, center.y),
        strokeWidth = 1.5f
      )
      // Vertical crosshair
      drawLine(
        color = ChartGold.copy(alpha = 0.8f),
        start = Offset(center.x, 0f),
        end = Offset(center.x, size.height),
        strokeWidth = 1.5f
      )
    }

    // Top control bar for Magnifier controls (zoom level, prompt, exit)
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = TerminalSurface.copy(alpha = 0.95f),
      border = androidx.compose.foundation.BorderStroke(1.dp, ChartGold.copy(alpha = 0.5f)),
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 16.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
      ) {
        Text(
          text = "🔍 放大镜模式: 拖动屏幕精细看盘",
          style = MaterialTheme.typography.labelSmall.copy(
            color = ChartGold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
          )
        )

        // Zoom out
        IconButton(
          onClick = { onZoomChange((zoom - 0.5f).coerceAtLeast(1.5f)) },
          modifier = Modifier.size(26.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Remove,
            contentDescription = "缩小倍率",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(14.dp)
          )
        }

        // Current Zoom badge
        Text(
          text = String.format("%.1fx", zoom),
          style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = ChartCyan,
            fontSize = 11.sp
          )
        )

        // Zoom in
        IconButton(
          onClick = { onZoomChange((zoom + 0.5f).coerceAtMost(4.0f)) },
          modifier = Modifier.size(26.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "放大倍率",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(14.dp)
          )
        }

        // Close magnifier
        IconButton(
          onClick = onClose,
          modifier = Modifier.size(26.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "关闭放大镜",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(14.dp)
          )
        }
      }
    }
  }
}
