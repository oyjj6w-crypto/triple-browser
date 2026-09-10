package com.example.model

data class Workspace(
  val id: String,
  val name: String,
  val iconEmoji: String,
  val urls: List<String>,
  val isCustom: Boolean = false
) {
  companion object {
    val PRESETS = listOf(
      Workspace(
        id = "crypto",
        name = "加密货币 (Crypto)",
        iconEmoji = "🪙",
        urls = listOf(
          "https://www.tradingview.com/chart/?symbol=BINANCE:BTCUSDT",
          "https://www.tradingview.com/chart/?symbol=BINANCE:ETHUSDT",
          "https://www.tradingview.com/chart/?symbol=BINANCE:SOLUSDT"
        )
      ),
      Workspace(
        id = "metals",
        name = "贵金属外汇 (Metals)",
        iconEmoji = "🥇",
        urls = listOf(
          "https://www.tradingview.com/chart/?symbol=OANDA:XAUUSD",
          "https://www.tradingview.com/chart/?symbol=OANDA:XAGUSD",
          "https://www.tradingview.com/chart/?symbol=CAPITALCOM:DXY"
        )
      ),
      Workspace(
        id = "commodities",
        name = "能源大宗 (Energy)",
        iconEmoji = "🛢️",
        urls = listOf(
          "https://www.tradingview.com/chart/?symbol=TVC:USOIL",
          "https://www.tradingview.com/chart/?symbol=TVC:UKOIL",
          "https://www.tradingview.com/chart/?symbol=NYMEX:NG1!"
        )
      ),
      Workspace(
        id = "indices",
        name = "全球股指 (Indices)",
        iconEmoji = "📈",
        urls = listOf(
          "https://www.tradingview.com/chart/?symbol=FOREXCOM:SPXUSD",
          "https://www.tradingview.com/chart/?symbol=FOREXCOM:NAS100",
          "https://www.tradingview.com/chart/?symbol=INDEX:HSI"
        )
      ),
      Workspace(
        id = "macro",
        name = "宏观国债 (Macro)",
        iconEmoji = "📊",
        urls = listOf(
          "https://www.tradingview.com/chart/?symbol=TVC:US10Y",
          "https://www.tradingview.com/chart/?symbol=TVC:US02Y",
          "https://www.tradingview.com/chart/?symbol=TVC:VIX"
        )
      )
    )
  }
}
