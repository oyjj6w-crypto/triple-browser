package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ChartCyan
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalSurfaceVariant

@Composable
fun UrlEditDialog(
  windowId: Int,
  initialUrl: String,
  onConfirm: (String) -> Unit,
  onDismiss: () -> Unit
) {
  var urlText by remember { mutableStateOf(initialUrl) }

  val quickSymbols = listOf(
    "BTC/USDT" to "https://www.tradingview.com/chart/?symbol=BINANCE:BTCUSDT",
    "ETH/USDT" to "https://www.tradingview.com/chart/?symbol=BINANCE:ETHUSDT",
    "SOL/USDT" to "https://www.tradingview.com/chart/?symbol=BINANCE:SOLUSDT",
    "现货黄金" to "https://www.tradingview.com/chart/?symbol=OANDA:XAUUSD",
    "现货白银" to "https://www.tradingview.com/chart/?symbol=OANDA:XAGUSD",
    "美原油" to "https://www.tradingview.com/chart/?symbol=TVC:USOIL",
    "美元指数" to "https://www.tradingview.com/chart/?symbol=CAPITALCOM:DXY",
    "标普500" to "https://www.tradingview.com/chart/?symbol=FOREXCOM:SPXUSD",
    "纳指100" to "https://www.tradingview.com/chart/?symbol=FOREXCOM:NAS100"
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = TerminalSurface,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Language,
          contentDescription = null,
          tint = ChartCyan,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "窗口 ${windowId + 1} 网址设置",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        )
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = urlText,
          onValueChange = { urlText = it },
          label = { Text("输入网址或搜索内容") },
          singleLine = true,
          trailingIcon = {
            if (urlText.isNotEmpty()) {
              IconButton(onClick = { urlText = "" }) {
                Icon(
                  imageVector = Icons.Default.Clear,
                  contentDescription = "清空",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ChartCyan,
            unfocusedBorderColor = TerminalBorder,
            focusedContainerColor = TerminalSurfaceVariant,
            unfocusedContainerColor = TerminalSurfaceVariant
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("url_input_field")
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "快捷标的行情直达：",
          style = MaterialTheme.typography.labelSmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
          )
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Quick symbol chips
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          quickSymbols.forEach { (label, url) ->
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = TerminalSurfaceVariant,
              border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
              onClick = { urlText = url },
              modifier = Modifier.height(28.dp)
            ) {
              Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(urlText) },
        colors = ButtonDefaults.buttonColors(containerColor = ChartCyan, contentColor = Color.Black),
        modifier = Modifier.testTag("confirm_url_button")
      ) {
        Text("前往加载", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }
  )
}
