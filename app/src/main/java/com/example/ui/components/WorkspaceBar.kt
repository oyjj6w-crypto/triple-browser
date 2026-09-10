package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Workspace
import com.example.ui.theme.ChartCyan
import com.example.ui.theme.ChartGold
import com.example.ui.theme.ChartGreen
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalSurfaceVariant

@Composable
fun WorkspaceBar(
  workspaces: List<Workspace>,
  activeWorkspaceId: String,
  hiddenCount: Int,
  maximizedWindowId: Int?,
  onSelectWorkspace: (Workspace) -> Unit,
  onSaveCurrentWorkspaceClick: () -> Unit,
  onDeleteCustomWorkspace: (String) -> Unit,
  onRestoreAllWindows: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    color = TerminalSurface,
    tonalElevation = 2.dp,
    modifier = modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(44.dp)
        .padding(horizontal = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // Left: App Logo/Title + Layout state indicator
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.ViewColumn,
          contentDescription = "看盘三屏",
          tint = ChartCyan,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "TRIPLE CHART",
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = ChartCyan
          )
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Status badge
        val statusText = when {
          maximizedWindowId != null -> "WIN ${maximizedWindowId + 1} 全屏独占"
          hiddenCount == 1 -> "2窗并列 (50%:50%)"
          hiddenCount == 2 -> "单窗独占 (100%)"
          else -> "3窗并列 (1:1:1)"
        }
        val statusColor = when {
          maximizedWindowId != null -> ChartGold
          hiddenCount > 0 -> ChartGreen
          else -> ChartCyan
        }

        Surface(
          shape = RoundedCornerShape(4.dp),
          color = statusColor.copy(alpha = 0.15f),
          border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
        ) {
          Text(
            text = statusText,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = 10.sp,
              color = statusColor,
              fontWeight = FontWeight.Bold
            )
          )
        }

        // Restore button if anything is hidden or maximized
        AnimatedVisibility(visible = hiddenCount > 0 || maximizedWindowId != null) {
          Row {
            Spacer(modifier = Modifier.width(6.dp))
            OutlinedButton(
              onClick = onRestoreAllWindows,
              shape = RoundedCornerShape(4.dp),
              contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
              modifier = Modifier
                .height(26.dp)
                .testTag("restore_all_button"),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = ChartCyan),
              border = androidx.compose.foundation.BorderStroke(1.dp, ChartCyan.copy(alpha = 0.7f))
            ) {
              Icon(
                imageVector = Icons.Default.GridView,
                contentDescription = null,
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(text = "恢复 3 窗 (1:1:1)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      // Middle: Scrollable Workspace Presets
      Row(
        modifier = Modifier
          .weight(1f)
          .padding(horizontal = 12.dp)
          .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        workspaces.forEach { ws ->
          val isSelected = ws.id == activeWorkspaceId
          FilterChip(
            selected = isSelected,
            onClick = { onSelectWorkspace(ws) },
            label = {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "${ws.iconEmoji} ${ws.name}", fontSize = 11.sp)
                if (ws.isCustom) {
                  Spacer(modifier = Modifier.width(4.dp))
                  Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "删除自定义分组",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier
                      .size(12.dp)
                      .padding(1.dp)
                  )
                }
              }
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = ChartCyan.copy(alpha = 0.2f),
              selectedLabelColor = ChartCyan,
              containerColor = TerminalSurfaceVariant,
              labelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            border = FilterChipDefaults.filterChipBorder(
              enabled = true,
              selected = isSelected,
              borderColor = if (isSelected) ChartCyan else TerminalBorder
            ),
            modifier = Modifier
              .height(28.dp)
              .testTag("workspace_chip_${ws.id}")
          )
        }
      }

      // Right: Save current workspace button
      OutlinedButton(
        onClick = onSaveCurrentWorkspaceClick,
        shape = RoundedCornerShape(4.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
        modifier = Modifier
          .height(28.dp)
          .testTag("save_workspace_button"),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = ChartGold),
        border = androidx.compose.foundation.BorderStroke(1.dp, ChartGold.copy(alpha = 0.6f))
      ) {
        Icon(
          imageVector = Icons.Default.BookmarkAdd,
          contentDescription = null,
          modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "存为分组", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
      }
    }
  }
}
