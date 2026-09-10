package com.example.web

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.model.WindowState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class WebEngineManager(private val context: Context) {

  private val _windowStates = MutableStateFlow(
    listOf(
      WindowState(id = 0, currentUrl = "https://www.tradingview.com/chart/?symbol=BINANCE:BTCUSDT"),
      WindowState(id = 1, currentUrl = "https://www.tradingview.com/chart/?symbol=BINANCE:ETHUSDT"),
      WindowState(id = 2, currentUrl = "https://www.tradingview.com/chart/?symbol=BINANCE:SOLUSDT")
    )
  )
  val windowStates: StateFlow<List<WindowState>> = _windowStates.asStateFlow()

  // 3 persistent WebViews that live through the app session
  val webViews = ArrayList<WebView>(3)

  init {
    for (i in 0 until 3) {
      val webView = createConfiguredWebView(i)
      webViews.add(webView)
    }
  }

  @SuppressLint("SetJavaScriptEnabled")
  private fun createConfiguredWebView(index: Int): WebView {
    val wv = WebView(context).apply {
      isFocusable = true
      isFocusableInTouchMode = true
      setLayerType(View.LAYER_TYPE_HARDWARE, null)
      scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY

      settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        databaseEnabled = true
        useWideViewPort = true
        loadWithOverviewMode = true
        mediaPlaybackRequiresUserGesture = false
        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        allowFileAccess = true
        allowContentAccess = true
        setSupportZoom(true)
        builtInZoomControls = true
        displayZoomControls = false
        cacheMode = WebSettings.LOAD_DEFAULT
        userAgentString = TradingViewScriptHelper.DESKTOP_USER_AGENT
      }

      webChromeClient = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
          updateWindowState(index) {
            it.copy(
              progress = newProgress,
              isLoading = newProgress < 100
            )
          }
          if (newProgress > 60) {
            view?.evaluateJavascript(TradingViewScriptHelper.getInjectedUserScript(), null)
          }
        }

        override fun onReceivedTitle(view: WebView?, title: String?) {
          if (!title.isNullOrBlank()) {
            updateWindowState(index) { it.copy(title = title) }
          }
        }
      }

      webViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
          // Keep all navigation inside this WebView
          return false
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
          updateWindowState(index) {
            it.copy(
              isLoading = true,
              progress = 10,
              currentUrl = url ?: it.currentUrl,
              canGoBack = view?.canGoBack() == true,
              canGoForward = view?.canGoForward() == true
            )
          }
        }

        override fun onPageFinished(view: WebView?, url: String?) {
          updateWindowState(index) {
            it.copy(
              isLoading = false,
              progress = 100,
              currentUrl = url ?: it.currentUrl,
              title = view?.title ?: it.title,
              canGoBack = view?.canGoBack() == true,
              canGoForward = view?.canGoForward() == true
            )
          }
          // Inject custom TradingView helper script
          view?.evaluateJavascript(TradingViewScriptHelper.getInjectedUserScript(), null)
        }
      }

      addJavascriptInterface(object {
        @JavascriptInterface
        fun onScriptReady(url: String?) {
          // JS-to-native callback confirmation
        }
      }, "AndroidBridge")
    }

    // Load initial URL
    val initialUrl = _windowStates.value.getOrNull(index)?.currentUrl
    if (!initialUrl.isNullOrEmpty()) {
      wv.loadUrl(initialUrl)
    }

    return wv
  }

  private fun updateWindowState(index: Int, update: (WindowState) -> WindowState) {
    _windowStates.update { list ->
      list.mapIndexed { i, state ->
        if (i == index) update(state) else state
      }
    }
  }

  fun loadUrl(index: Int, url: String) {
    val formattedUrl = when {
      url.startsWith("http://") || url.startsWith("https://") -> url
      url.contains(".") -> "https://$url"
      else -> "https://www.google.com/search?q=$url"
    }
    webViews.getOrNull(index)?.loadUrl(formattedUrl)
    updateWindowState(index) { it.copy(currentUrl = formattedUrl) }
  }

  fun reload(index: Int) {
    webViews.getOrNull(index)?.reload()
  }

  fun goBack(index: Int) {
    webViews.getOrNull(index)?.let { wv ->
      if (wv.canGoBack()) {
        wv.goBack()
      }
    }
  }

  fun goForward(index: Int) {
    webViews.getOrNull(index)?.let { wv ->
      if (wv.canGoForward()) {
        wv.goForward()
      }
    }
  }

  fun toggleDesktopMode(index: Int) {
    val currentState = _windowStates.value.getOrNull(index) ?: return
    val newMode = !currentState.isDesktopUA
    webViews.getOrNull(index)?.let { wv ->
      wv.settings.userAgentString = if (newMode) {
        TradingViewScriptHelper.DESKTOP_USER_AGENT
      } else {
        WebSettings.getDefaultUserAgent(context)
      }
      wv.reload()
    }
    updateWindowState(index) { it.copy(isDesktopUA = newMode) }
  }

  fun executeAssistAction(index: Int, action: TradingViewScriptHelper.AssistAction) {
    val script = TradingViewScriptHelper.getScriptForAction(action)
    webViews.getOrNull(index)?.evaluateJavascript(script, null)
  }

  fun toggleMagnifier(index: Int) {
    updateWindowState(index) { it.copy(isMagnifierActive = !it.isMagnifierActive) }
  }

  fun setMagnifier(index: Int, active: Boolean) {
    updateWindowState(index) { it.copy(isMagnifierActive = active) }
  }

  fun toggleAssistBar(index: Int) {
    updateWindowState(index) { it.copy(isAssistBarVisible = !it.isAssistBarVisible) }
  }

  fun switchWorkspaceUrls(urls: List<String>) {
    urls.forEachIndexed { index, url ->
      if (index < webViews.size && url.isNotBlank()) {
        val current = _windowStates.value.getOrNull(index)?.currentUrl
        if (current != url) {
          loadUrl(index, url)
        }
      }
    }
  }

  fun destroyAll() {
    webViews.forEach { wv ->
      wv.stopLoading()
      wv.clearHistory()
      wv.destroy()
    }
    webViews.clear()
  }
}
