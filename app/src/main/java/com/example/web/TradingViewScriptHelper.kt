package com.example.web

object TradingViewScriptHelper {
  const val DESKTOP_USER_AGENT =
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"

  /**
   * Custom userscript injected into TradingView pages.
   * Dispatches authentic keyboard events (Alt+H, Alt+I, Alt+F, Alt+R) and triggers DOM buttons.
   */
  fun getInjectedUserScript(): String {
    return """
      (function() {
        if (window.__tv_assist_injected__) return;
        window.__tv_assist_injected__ = true;

        function triggerKey(key, alt = false, ctrl = false, shift = false) {
          const keyUpper = key.toUpperCase();
          const target = document.activeElement || document.querySelector('iframe') || document.body;
          const opts = {
            key: key,
            code: 'Key' + keyUpper,
            keyCode: keyUpper.charCodeAt(0),
            which: keyUpper.charCodeAt(0),
            altKey: alt,
            ctrlKey: ctrl,
            shiftKey: shift,
            bubbles: true,
            cancelable: true
          };
          ['keydown', 'keypress', 'keyup'].forEach(type => {
            const ev = new KeyboardEvent(type, opts);
            target.dispatchEvent(ev);
            window.dispatchEvent(ev);
            document.dispatchEvent(ev);
          });
        }

        function clickButtonWithTextOrAria(terms) {
          const elements = Array.from(document.querySelectorAll('button, [role="button"], div[data-name]'));
          for (const el of elements) {
            const attr = (el.getAttribute('title') || el.getAttribute('aria-label') || el.getAttribute('data-name') || el.innerText || '').toLowerCase();
            if (terms.some(t => attr.includes(t.toLowerCase()))) {
              el.click();
              return true;
            }
          }
          return false;
        }

        window.TradingViewAssist = {
          // 隐藏/显示所有绘图 (Alt + H)
          hideDrawings: function() {
            triggerKey('h', true, false, false);
            clickButtonWithTextOrAria(['hide drawings', 'hide all', '隐藏绘图']);
          },
          // 磁吸锁定模式开关
          toggleMagnet: function() {
            triggerKey('m', false, true, false);
            clickButtonWithTextOrAria(['magnet', '磁吸']);
          },
          // 坐标轴反转 (Alt + I)
          invertScale: function() {
            triggerKey('i', true, false, false);
            clickButtonWithTextOrAria(['invert scale', '反转坐标']);
          },
          // 全屏图表 (Alt + F)
          fullscreenChart: function() {
            triggerKey('f', true, false, false);
            clickButtonWithTextOrAria(['fullscreen', '全屏']);
          },
          // 重置价格坐标 (Alt + R)
          resetScale: function() {
            triggerKey('r', true, false, false);
            clickButtonWithTextOrAria(['reset price scale', 'auto', '自适应']);
          }
        };

        // Notify Android bridge if symbol is detected
        try {
          if (window.AndroidBridge && typeof window.AndroidBridge.onScriptReady === 'function') {
            window.AndroidBridge.onScriptReady(window.location.href);
          }
        } catch (e) {}
      })();
    """.trimIndent()
  }

  enum class AssistAction {
    HIDE_DRAWINGS,
    TOGGLE_MAGNET,
    INVERT_SCALE,
    FULLSCREEN_CHART,
    RESET_SCALE
  }

  fun getScriptForAction(action: AssistAction): String {
    return when (action) {
      AssistAction.HIDE_DRAWINGS -> "if (window.TradingViewAssist) { window.TradingViewAssist.hideDrawings(); }"
      AssistAction.TOGGLE_MAGNET -> "if (window.TradingViewAssist) { window.TradingViewAssist.toggleMagnet(); }"
      AssistAction.INVERT_SCALE -> "if (window.TradingViewAssist) { window.TradingViewAssist.invertScale(); }"
      AssistAction.FULLSCREEN_CHART -> "if (window.TradingViewAssist) { window.TradingViewAssist.fullscreenChart(); }"
      AssistAction.RESET_SCALE -> "if (window.TradingViewAssist) { window.TradingViewAssist.resetScale(); }"
    }
  }
}
