package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WindowState
import com.example.ui.theme.ChartBlue
import com.example.ui.theme.ChartCyan
import com.example.ui.theme.ChartGold
import com.example.ui.theme.ChartGreen
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalSurfaceVariant

@Composable
fun WindowControlBar(
  state: WindowState,
  isMaximized: Boolean,
  onNavigateBack: () -> Unit,
  onNavigateForward: () -> Unit,
  onReload: () -> Unit,
  onToggleDesktopUA: () -> Unit,
  onToggleMagnifier: () -> Unit,
  onToggleAssistBar: () -> Unit,
  onToggleMaximize: () -> Unit,
  onHideWindow: () -> Unit,
  onEditUrlClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val accentColor = when (state.id) {
    0 -> ChartCyan
    1 -> ChartGreen
    else -> ChartGold
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(TerminalSurface)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(38.dp)
        .padding(horizontal = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // Left: Window Badge & Title / URL
      Row(
        modifier = Modifier
          .weight(1f)
          .padding(end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Window Badge #1, #2, #3
        Surface(
          shape = RoundedCornerShape(4.dp),
          color = accentColor.copy(alpha = 0.2f),
          border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.6f))
        ) {
          Text(
            text = "WIN ${state.id + 1}",
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace,
              color = accentColor
            )
          )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Title and URL button (clickable to edit)
        Row(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(4.dp))
            .background(TerminalSurfaceVariant)
            .clickable { onEditUrlClick() }
            .padding(horizontal = 6.dp, vertical = 3.dp)
            .testTag("url_badge_${state.id}"),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (state.title.isNotBlank()) state.title else state.currentUrl,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall.copy(
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.weight(1f)
          )
          Spacer(modifier = Modifier.width(3.dp))
          Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "编辑网址",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(11.dp)
          )
        }
      }

      // Center/Right: Navigation & Window Controls
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(1.dp)
      ) {
        // Back
        IconButton(
          onClick = onNavigateBack,
          enabled = state.canGoBack,
          modifier = Modifier
            .size(32.dp)
            .testTag("back_button_${state.id}")
        ) {
          Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "后退",
            tint = if (state.canGoBack) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            modifier = Modifier.size(15.dp)
          )
        }

        // Forward
        IconButton(
          onClick = onNavigateForward,
          enabled = state.canGoForward,
          modifier = Modifier
            .size(32.dp)
            .testTag("forward_button_${state.id}")
        ) {
          Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = "前进",
            tint = if (state.canGoForward) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            modifier = Modifier.size(15.dp)
          )
        }

        // Refresh
        IconButton(
          onClick = onReload,
          modifier = Modifier
            .size(32.dp)
            .testTag("reload_button_${state.id}")
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "刷新",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(15.dp)
          )
        }

        // Desktop UA Switch
        IconButton(
          onClick = onToggleDesktopUA,
          modifier = Modifier
            .size(32.dp)
            .testTag("ua_button_${state.id}")
        ) {
          Icon(
            imageVector = if (state.isDesktopUA) Icons.Default.Computer else Icons.Default.PhoneAndroid,
            contentDescription = if (state.isDesktopUA) "切换为移动UA" else "切换为桌面UA",
            tint = if (state.isDesktopUA) ChartCyan else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(15.dp)
          )
        }

        // Loupe Magnifier Toggle
        IconButton(
          onClick = onToggleMagnifier,
          modifier = Modifier
            .size(32.dp)
            .testTag("magnifier_button_${state.id}")
        ) {
          Icon(
            imageVector = Icons.Default.ZoomIn,
            contentDescription = "局部放大镜",
            tint = if (state.isMagnifierActive) ChartGold else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
          )
        }

        // TradingView Shortcut Assist Bar Toggle
        IconButton(
          onClick = onToggleAssistBar,
          modifier = Modifier
            .size(32.dp)
            .testTag("assist_button_${state.id}")
        ) {
          Icon(
            imageVector = Icons.Default.FlashOn,
            contentDescription = "看盘快捷键浮钮",
            tint = if (state.isAssistBarVisible) ChartGreen else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(15.dp)
          )
        }

        // Maximize / Restore Button
        IconButton(
          onClick = onToggleMaximize,
          modifier = Modifier
            .size(32.dp)
            .testTag("maximize_button_${state.id}")
        ) {
          Icon(
            imageVector = if (isMaximized) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
            contentDescription = if (isMaximized) "还原窗口" else "全屏最大化",
            tint = if (isMaximized) ChartCyan else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(16.dp)
          )
        }

        // Hide Window Button
        IconButton(
          onClick = onHideWindow,
          modifier = Modifier
            .size(32.dp)
            .testTag("hide_button_${state.id}")
        ) {
          Icon(
            imageVector = Icons.Default.VisibilityOff,
            contentDescription = "隐藏此窗口",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(15.dp)
          )
        }
      }
    }

    // Thin loading progress bar
    if (state.isLoading && state.progress < 100) {
      LinearProgressIndicator(
        progress = { state.progress / 100f },
        modifier = Modifier
          .fillMaxWidth()
          .height(2.dp),
        color = accentColor,
        trackColor = Color.Transparent,
      )
    } else {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(1.dp)
          .background(TerminalBorder)
      )
    }
  }
}
