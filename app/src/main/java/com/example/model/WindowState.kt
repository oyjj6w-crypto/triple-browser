package com.example.model

data class WindowState(
  val id: Int, // 0, 1, 2
  val title: String = "加载中...",
  val currentUrl: String = "",
  val isLoading: Boolean = false,
  val progress: Int = 0,
  val isDesktopUA: Boolean = true,
  val isMagnifierActive: Boolean = false,
  val isAssistBarVisible: Boolean = true,
  val canGoBack: Boolean = false,
  val canGoForward: Boolean = false
)
