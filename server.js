const http = require('http');

const PORT = process.env.DEFAULT_APP_PORT || 3000;

const html = `<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <title>Triple Chart Browser - 看盘三屏多窗浏览器</title>
  <style>
    :root {
      --bg: #0b0e14;
      --surface: #141821;
      --surface-variant: #1d2330;
      --border: #2b3345;
      --cyan: #00e5ff;
      --green: #00e676;
      --red: #ff5252;
      --gold: #ffd600;
      --text: #f1f5f9;
      --text-muted: #8b949e;
    }
    * {
      box-sizing: border-box;
      margin: 0;
      padding: 0;
      user-select: none;
    }
    body {
      background-color: var(--bg);
      color: var(--text);
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
      overflow: hidden;
      width: 100vw;
      height: 100vh;
      display: flex;
      flex-direction: column;
    }

    /* Top Workspace Header Bar */
    header {
      background: var(--surface);
      height: 44px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 12px;
      border-bottom: 1px solid var(--border);
      flex-shrink: 0;
      z-index: 100;
    }
    .brand {
      display: flex;
      align-items: center;
      gap: 8px;
    }
    .brand-logo {
      width: 20px;
      height: 20px;
      background: linear-gradient(135deg, var(--cyan), #2979ff);
      border-radius: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 900;
      font-size: 11px;
      color: #000;
    }
    .brand-title {
      font-weight: 800;
      letter-spacing: 1px;
      font-size: 13px;
      color: var(--cyan);
      font-family: monospace;
    }
    .status-badge {
      font-size: 10px;
      padding: 2px 8px;
      border-radius: 4px;
      font-weight: 700;
      background: rgba(0, 229, 255, 0.15);
      color: var(--cyan);
      border: 1px solid rgba(0, 229, 255, 0.4);
    }
    .restore-all-btn {
      background: transparent;
      border: 1px solid var(--cyan);
      color: var(--cyan);
      padding: 2px 8px;
      border-radius: 4px;
      font-size: 10px;
      font-weight: 700;
      cursor: pointer;
      display: none;
    }
    .restore-all-btn.show {
      display: inline-block;
    }

    .workspaces {
      display: flex;
      align-items: center;
      gap: 6px;
      overflow-x: auto;
      padding: 0 8px;
      scrollbar-width: none;
    }
    .workspaces::-webkit-scrollbar {
      display: none;
    }
    .ws-chip {
      background: var(--surface-variant);
      border: 1px solid var(--border);
      color: var(--text-muted);
      padding: 4px 10px;
      border-radius: 6px;
      font-size: 11px;
      cursor: pointer;
      white-space: nowrap;
      transition: all 0.2s;
      display: flex;
      align-items: center;
      gap: 4px;
    }
    .ws-chip:hover {
      border-color: var(--cyan);
      color: var(--text);
    }
    .ws-chip.active {
      background: rgba(0, 229, 255, 0.15);
      border-color: var(--cyan);
      color: var(--cyan);
      font-weight: 600;
    }
    .ws-chip .del-btn {
      color: var(--red);
      font-size: 12px;
      margin-left: 2px;
    }

    .header-actions {
      display: flex;
      align-items: center;
      gap: 8px;
    }
    .action-btn {
      background: transparent;
      border: 1px solid var(--border);
      color: var(--text);
      padding: 4px 10px;
      border-radius: 4px;
      font-size: 11px;
      cursor: pointer;
      display: flex;
      align-items: center;
      gap: 4px;
      transition: all 0.2s;
    }
    .action-btn.gold {
      border-color: rgba(255, 214, 0, 0.6);
      color: var(--gold);
    }
    .action-btn:hover {
      background: var(--surface-variant);
    }

    /* Main Multi-Window Layout Grid (1:1:1 side by side) */
    .windows-container {
      flex: 1;
      display: flex;
      width: 100%;
      height: calc(100vh - 44px);
      background: var(--bg);
      position: relative;
    }

    .window-pane {
      flex: 1;
      height: 100%;
      display: flex;
      flex-direction: column;
      border-right: 1px solid var(--border);
      position: relative;
      overflow: hidden;
      transition: flex 0.25s cubic-bezier(0.4, 0, 0.2, 1), width 0.25s;
    }
    .window-pane:last-child {
      border-right: none;
    }
    .window-pane.hidden {
      flex: 0 0 0px !important;
      width: 0 !important;
      opacity: 0;
      pointer-events: none;
      border-right: none;
    }
    .window-pane.maximized {
      flex: 1 0 100% !important;
      z-index: 10;
    }

    /* Window Control Bar */
    .pane-header {
      background: var(--surface);
      height: 38px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 8px;
      border-bottom: 1px solid var(--border);
      flex-shrink: 0;
    }
    .pane-badge {
      font-family: monospace;
      font-weight: 800;
      font-size: 10px;
      padding: 2px 6px;
      border-radius: 4px;
      text-transform: uppercase;
    }
    .badge-0 { background: rgba(0, 229, 255, 0.15); color: var(--cyan); border: 1px solid rgba(0, 229, 255, 0.5); }
    .badge-1 { background: rgba(0, 230, 118, 0.15); color: var(--green); border: 1px solid rgba(0, 230, 118, 0.5); }
    .badge-2 { background: rgba(255, 214, 0, 0.15); color: var(--gold); border: 1px solid rgba(255, 214, 0, 0.5); }

    .url-chip {
      flex: 1;
      margin: 0 8px;
      background: var(--surface-variant);
      border-radius: 4px;
      padding: 2px 8px;
      font-size: 11px;
      color: var(--text-muted);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      cursor: pointer;
      border: 1px solid transparent;
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
    .url-chip:hover {
      border-color: var(--border);
      color: var(--text);
    }

    .pane-ctrls {
      display: flex;
      align-items: center;
      gap: 2px;
    }
    .icon-btn {
      width: 28px;
      height: 28px;
      background: transparent;
      border: none;
      border-radius: 4px;
      color: var(--text);
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      font-size: 13px;
      transition: background 0.15s;
    }
    .icon-btn:hover {
      background: var(--surface-variant);
    }
    .icon-btn.active {
      color: var(--cyan);
      background: rgba(0, 229, 255, 0.15);
    }
    .icon-btn.gold-active {
      color: var(--gold);
      background: rgba(255, 214, 0, 0.15);
    }

    /* Web Content Frame */
    .pane-webview {
      flex: 1;
      width: 100%;
      height: calc(100% - 38px);
      position: relative;
      background: #000;
    }
    iframe {
      width: 100%;
      height: 100%;
      border: none;
      background: #131722;
    }

    /* Draggable Tampermonkey Shortcut Assist Floating Bar */
    .tv-floating-bar {
      position: absolute;
      top: 12px;
      left: 12px;
      background: rgba(20, 24, 33, 0.92);
      backdrop-filter: blur(8px);
      border: 1px solid var(--border);
      border-radius: 20px;
      padding: 3px 8px;
      display: flex;
      align-items: center;
      gap: 6px;
      box-shadow: 0 4px 12px rgba(0,0,0,0.5);
      cursor: grab;
      z-index: 20;
      transition: opacity 0.2s;
    }
    .tv-floating-bar:active {
      cursor: grabbing;
    }
    .tv-floating-btn {
      background: transparent;
      border: none;
      color: var(--cyan);
      font-size: 10px;
      font-weight: 600;
      padding: 3px 6px;
      border-radius: 12px;
      cursor: pointer;
      display: flex;
      align-items: center;
      gap: 3px;
    }
    .tv-floating-btn:hover {
      background: rgba(0, 229, 255, 0.2);
    }

    /* Loupe Magnifier Lens */
    .loupe-lens {
      position: absolute;
      width: 180px;
      height: 180px;
      border-radius: 50%;
      border: 3px solid var(--gold);
      box-shadow: 0 0 16px rgba(255, 214, 0, 0.4), inset 0 0 20px rgba(0,0,0,0.6);
      pointer-events: none;
      display: none;
      z-index: 30;
      overflow: hidden;
      background-repeat: no-repeat;
    }
    .loupe-crosshair {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      width: 30px;
      height: 30px;
      pointer-events: none;
    }
    .loupe-crosshair::before, .loupe-crosshair::after {
      content: '';
      position: absolute;
      background: var(--gold);
    }
    .loupe-crosshair::before {
      top: 14px; left: 0; width: 30px; height: 2px;
    }
    .loupe-crosshair::after {
      left: 14px; top: 0; width: 2px; height: 30px;
    }

    /* Modal dialog */
    .modal-overlay {
      position: fixed;
      inset: 0;
      background: rgba(0,0,0,0.7);
      display: none;
      align-items: center;
      justify-content: center;
      z-index: 999;
    }
    .modal-overlay.open {
      display: flex;
    }
    .modal-box {
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: 8px;
      width: 90%;
      max-width: 480px;
      padding: 20px;
      box-shadow: 0 8px 32px rgba(0,0,0,0.6);
    }
    .modal-title {
      font-size: 16px;
      font-weight: 700;
      margin-bottom: 12px;
      color: var(--text);
    }
    .modal-input {
      width: 100%;
      background: var(--surface-variant);
      border: 1px solid var(--border);
      border-radius: 6px;
      color: var(--text);
      padding: 10px 12px;
      font-size: 13px;
      outline: none;
      margin-bottom: 12px;
    }
    .modal-input:focus {
      border-color: var(--cyan);
    }
    .quick-chips {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
      margin-bottom: 16px;
    }
    .q-chip {
      background: var(--surface-variant);
      border: 1px solid var(--border);
      color: var(--text-muted);
      padding: 4px 8px;
      border-radius: 4px;
      font-size: 11px;
      cursor: pointer;
    }
    .q-chip:hover {
      color: var(--cyan);
      border-color: var(--cyan);
    }
    .modal-btns {
      display: flex;
      justify-content: flex-end;
      gap: 8px;
    }
    .btn-cancel {
      background: transparent;
      border: 1px solid var(--border);
      color: var(--text-muted);
      padding: 8px 16px;
      border-radius: 4px;
      cursor: pointer;
    }
    .btn-confirm {
      background: var(--cyan);
      border: none;
      color: #000;
      font-weight: 700;
      padding: 8px 16px;
      border-radius: 4px;
      cursor: pointer;
    }
  </style>
</head>
<body>

  <!-- Top App Navigation & Workspaces Bar -->
  <header>
    <div class="brand">
      <div class="brand-logo">3W</div>
      <div class="brand-title">TRIPLE CHART</div>
      <div class="status-badge" id="layoutStatusBadge">3窗并列 (1:1:1)</div>
      <button class="restore-all-btn" id="restoreAllBtn" onclick="restoreAllWindows()">恢复 3 窗 (1:1:1)</button>
    </div>

    <!-- Workspace Presets -->
    <div class="workspaces" id="workspaceList">
      <!-- Populated via JS -->
    </div>

    <div class="header-actions">
      <button class="action-btn gold" onclick="openSaveWorkspaceModal()">
        <span>⭐</span> 存为新分组
      </button>
    </div>
  </header>

  <!-- Main 3-Window Side-by-Side Canvas -->
  <div class="windows-container" id="windowsContainer">

    <!-- Window 0 -->
    <div class="window-pane" id="pane-0">
      <div class="pane-header">
        <div class="pane-badge badge-0">WIN 1</div>
        <div class="url-chip" onclick="openUrlModal(0)" id="url-title-0">加载中... ✏️</div>
        <div class="pane-ctrls">
          <button class="icon-btn" onclick="reloadPane(0)" title="刷新">↻</button>
          <button class="icon-btn active" id="ua-btn-0" onclick="toggleUA(0)" title="切换PC/手机UA">💻</button>
          <button class="icon-btn" id="mag-btn-0" onclick="toggleMagnifier(0)" title="局部放大镜">🔍</button>
          <button class="icon-btn active" id="ast-btn-0" onclick="toggleAssist(0)" title="油猴辅助快捷键">⚡</button>
          <button class="icon-btn" id="max-btn-0" onclick="toggleMaximize(0)" title="全屏最大化/还原">⛶</button>
          <button class="icon-btn" onclick="toggleHide(0)" title="隐藏窗口">👁️</button>
        </div>
      </div>
      <div class="pane-webview" id="wv-container-0">
        <iframe id="iframe-0" src="about:blank"></iframe>
        <!-- Injected Floating Shortcuts -->
        <div class="tv-floating-bar" id="tv-bar-0">
          <span style="font-size:10px; opacity:0.5;">⋮⋮</span>
          <button class="tv-floating-btn" onclick="triggerTVAction(0, 'hide')">👁️ 藏图</button>
          <button class="tv-floating-btn" onclick="triggerTVAction(0, 'magnet')">🧲 磁吸</button>
          <button class="tv-floating-btn" onclick="triggerTVAction(0, 'invert')">🔄 反转</button>
          <button class="tv-floating-btn" onclick="triggerTVAction(0, 'fullscreen')">⛶ 全图</button>
          <button class="tv-floating-btn" onclick="triggerTVAction(0, 'reset')">↺ 自适</button>
        </div>
        <div class="loupe-lens" id="loupe-0"><div class="loupe-crosshair"></div></div>
      </div>
    </div>

    <!-- Window 1 -->
    <div class="window-pane" id="pane-1">
      <div class="pane-header">
        <div class="pane-badge badge-1">WIN 2</div>
        <div class="url-chip" onclick="openUrlModal(1)" id="url-title-1">加载中... ✏️</div>
        <div class="pane-ctrls">
          <button class="icon-btn" onclick="reloadPane(1)" title="刷新">↻</button>
          <button class="icon-btn active" id="ua-btn-1" onclick="toggleUA(1)" title="切换PC/手机UA">💻</button>
          <button class="icon-btn" id="mag-btn-1" onclick="toggleMagnifier(1)" title="局部放大镜">🔍</button>
          <button class="icon-btn active" id="ast-btn-1" onclick="toggleAssist(1)" title="油猴辅助快捷键">⚡</button>
          <button class="icon-btn" id="max-btn-1" onclick="toggleMaximize(1)" title="全屏最大化/还原">⛶</button>
          <button class="icon-btn" onclick="toggleHide(1)" title="隐藏窗口">👁️</button>
        </div>
      </div>
      <div class="pane-webview" id="wv-container-1">
        <iframe id="iframe-1" src="about:blank"></iframe>
        <div class="tv-floating-bar" id="tv-bar-1">
          <span style="font-size:10px; opacity:0.5;">⋮⋮</span>
          <button class="tv-floating-btn" onclick="triggerTVAction(1, 'hide')">👁️ 藏图</button>
          <button class="tv-floating-btn" onclick="triggerTVAction(1, 'magnet')">🧲 磁吸</button>
          <button class="tv-floating-btn" onclick="triggerTVAction(1, 'invert')">🔄 反转</button>
          <button class="tv-floating-btn" onclick="triggerTVAction(1, 'fullscreen')">⛶ 全图</button>
          <button class="tv-floating-btn" onclick="triggerTVAction(1, 'reset')">↺ 自适</button>
        </div>
        <div class="loupe-lens" id="loupe-1"><div class="loupe-crosshair"></div></div>
      </div>
    </div>

    <!-- Window 2 -->
    <div class="window-pane" id="pane-2">
      <div class="pane-header">
        <div class="pane-badge badge-2">WIN 3</div>
        <div class="url-chip" onclick="openUrlModal(2)" id="url-title-2">加载中... ✏️</div>
        <div class="pane-ctrls">
          <button class="icon-btn" onclick="reloadPane(2)" title="刷新">↻</button>
          <button class="icon-btn active" id="ua-btn-2" onclick="toggleUA(2)" title="切换PC/手机UA">💻</button>
          <button class="icon-btn" id="mag-btn-2" onclick="toggleMagnifier(2)" title="局部放大镜">🔍</button>
          <button class="icon-btn active" id="ast-btn-2" onclick="toggleAssist(2)" title="油猴辅助快捷键">⚡</button>
          <button class="icon-btn" id="max-btn-2" onclick="toggleMaximize(2)" title="全屏最大化/还原">⛶</button>
          <button class="icon-btn" onclick="toggleHide(2)" title="隐藏窗口">👁️</button>
        </div>
      </div>
      <div class="pane-webview" id="wv-container-2">
        <iframe id="iframe-2" src="about:blank"></iframe>
        <div class="tv-floating-bar" id="tv-bar-2">
          <span style="font-size:10px; opacity:0.5;">⋮⋮</span>
          <button class="tv-floating-btn" onclick="triggerTVAction(2, 'hide')">👁️ 藏图</button>
          <button class="tv-floating-btn" onclick="triggerTVAction(2, 'magnet')">🧲 磁吸</button>
          <button class="tv-floating-btn" onclick="triggerTVAction(2, 'invert')">🔄 反转</button>
          <button class="tv-floating-btn" onclick="triggerTVAction(2, 'fullscreen')">⛶ 全图</button>
          <button class="tv-floating-btn" onclick="triggerTVAction(2, 'reset')">↺ 自适</button>
        </div>
        <div class="loupe-lens" id="loupe-2"><div class="loupe-crosshair"></div></div>
      </div>
    </div>

  </div>

  <!-- URL Setting Modal -->
  <div class="modal-overlay" id="urlModal">
    <div class="modal-box">
      <div class="modal-title" id="urlModalTitle">窗口网址设置</div>
      <input type="text" class="modal-input" id="urlInput" placeholder="输入网址">
      <div style="font-size:11px; color:var(--text-muted); margin-bottom:6px;">热门标的快速直达：</div>
      <div class="quick-chips">
        <span class="q-chip" onclick="setModalUrl('https://s.tradingview.com/widgetembed/?symbol=BINANCE%3ABTCUSDT&theme=dark')">BTC/USDT</span>
        <span class="q-chip" onclick="setModalUrl('https://s.tradingview.com/widgetembed/?symbol=BINANCE%3AETHUSDT&theme=dark')">ETH/USDT</span>
        <span class="q-chip" onclick="setModalUrl('https://s.tradingview.com/widgetembed/?symbol=BINANCE%3ASOLUSDT&theme=dark')">SOL/USDT</span>
        <span class="q-chip" onclick="setModalUrl('https://s.tradingview.com/widgetembed/?symbol=OANDA%3AXAUUSD&theme=dark')">现货黄金</span>
        <span class="q-chip" onclick="setModalUrl('https://s.tradingview.com/widgetembed/?symbol=OANDA%3AXAGUSD&theme=dark')">现货白银</span>
        <span class="q-chip" onclick="setModalUrl('https://s.tradingview.com/widgetembed/?symbol=TVC%3AUSOIL&theme=dark')">美原油</span>
        <span class="q-chip" onclick="setModalUrl('https://s.tradingview.com/widgetembed/?symbol=CAPITALCOM%3ADXY&theme=dark')">美元指数</span>
        <span class="q-chip" onclick="setModalUrl('https://s.tradingview.com/widgetembed/?symbol=FOREXCOM%3ASPXUSD&theme=dark')">标普500</span>
      </div>
      <div class="modal-btns">
        <button class="btn-cancel" onclick="closeUrlModal()">取消</button>
        <button class="btn-confirm" onclick="applyModalUrl()">加载网址</button>
      </div>
    </div>
  </div>

  <!-- Save Workspace Modal -->
  <div class="modal-overlay" id="saveWsModal">
    <div class="modal-box">
      <div class="modal-title">保存当前 3 窗口为新工作区分组</div>
      <input type="text" class="modal-input" id="wsNameInput" placeholder="请输入分组名称 (如: 主力看盘组)">
      <div style="font-size:11px; color:var(--text-muted); margin-bottom:12px;" id="saveWsDetails"></div>
      <div class="modal-btns">
        <button class="btn-cancel" onclick="closeSaveWsModal()">取消</button>
        <button class="btn-confirm" onclick="confirmSaveWorkspace()">确认保存</button>
      </div>
    </div>
  </div>

  <script>
    // TradingView embed widget URLs for seamless iframe viewing without X-Frame-Options blocking
    function getEmbedUrl(symbol) {
      return 'https://s.tradingview.com/widgetembed/?symbol=' + encodeURIComponent(symbol) + '&interval=D&hidesidetoolbar=0&symboledit=1&saveimage=1&toolbarbg=f1f3f6&studies=%5B%5D&theme=dark&style=1&timezone=Asia%2FShanghai';
    }

    const defaultWorkspaces = [
      {
        id: 'crypto',
        name: '加密货币 (Crypto)',
        icon: '🪙',
        urls: [
          getEmbedUrl('BINANCE:BTCUSDT'),
          getEmbedUrl('BINANCE:ETHUSDT'),
          getEmbedUrl('BINANCE:SOLUSDT')
        ]
      },
      {
        id: 'metals',
        name: '贵金属外汇 (Metals)',
        icon: '🥇',
        urls: [
          getEmbedUrl('OANDA:XAUUSD'),
          getEmbedUrl('OANDA:XAGUSD'),
          getEmbedUrl('CAPITALCOM:DXY')
        ]
      },
      {
        id: 'commodities',
        name: '能源大宗 (Energy)',
        icon: '🛢️',
        urls: [
          getEmbedUrl('TVC:USOIL'),
          getEmbedUrl('TVC:UKOIL'),
          getEmbedUrl('NYMEX:NG1!')
        ]
      },
      {
        id: 'indices',
        name: '全球股指 (Indices)',
        icon: '📈',
        urls: [
          getEmbedUrl('FOREXCOM:SPXUSD'),
          getEmbedUrl('FOREXCOM:NAS100'),
          getEmbedUrl('INDEX:HSI')
        ]
      },
      {
        id: 'macro',
        name: '宏观国债 (Macro)',
        icon: '📊',
        urls: [
          getEmbedUrl('TVC:US10Y'),
          getEmbedUrl('TVC:US02Y'),
          getEmbedUrl('TVC:VIX')
        ]
      }
    ];

    let workspaces = [...defaultWorkspaces];
    try {
      const saved = localStorage.getItem('custom_workspaces');
      if (saved) {
        const parsed = JSON.parse(saved);
        if (Array.isArray(parsed)) workspaces = [...defaultWorkspaces, ...parsed];
      }
    } catch(e) {}

    let currentWorkspaceId = 'crypto';
    let hiddenWindows = new Set();
    let maximizedWindow = null;
    let editingWindowIndex = 0;

    let windowUrls = [
      getEmbedUrl('BINANCE:BTCUSDT'),
      getEmbedUrl('BINANCE:ETHUSDT'),
      getEmbedUrl('BINANCE:SOLUSDT')
    ];

    function init() {
      renderWorkspaces();
      windowUrls.forEach((url, i) => {
        setWindowUrl(i, url);
      });
      updateLayout();
      setupDraggableBars();
    }

    function renderWorkspaces() {
      const container = document.getElementById('workspaceList');
      container.innerHTML = workspaces.map(ws => {
        const isCustom = ws.id.startsWith('custom_');
        return \`
          <div class="ws-chip \${ws.id === currentWorkspaceId ? 'active' : ''}" onclick="selectWorkspace('\${ws.id}')">
            <span>\${ws.icon} \${ws.name}</span>
            \${isCustom ? \`<span class="del-btn" onclick="event.stopPropagation(); deleteWorkspace('\${ws.id}')">✕</span>\` : ''}
          </div>
        \`;
      }).join('');
    }

    function selectWorkspace(id) {
      currentWorkspaceId = id;
      const ws = workspaces.find(w => w.id === id);
      if (!ws) return;
      renderWorkspaces();
      ws.urls.forEach((url, i) => {
        setWindowUrl(i, url);
      });
    }

    function setWindowUrl(index, url) {
      windowUrls[index] = url;
      const iframe = document.getElementById('iframe-' + index);
      if (iframe.src !== url) {
        iframe.src = url;
      }
      // Extract symbol name for readable title
      let displayTitle = url;
      try {
        const match = url.match(/symbol=([^&]+)/);
        if (match && match[1]) {
          displayTitle = decodeURIComponent(match[1]);
        }
      } catch(e) {}
      document.getElementById('url-title-' + index).innerText = displayTitle + ' ✏️';
    }

    function toggleHide(index) {
      if (maximizedWindow === index) {
        maximizedWindow = null;
      }
      if (hiddenWindows.has(index)) {
        hiddenWindows.delete(index);
      } else {
        if (hiddenWindows.size >= 2) return; // Keep at least 1 visible
        hiddenWindows.add(index);
      }
      updateLayout();
    }

    function toggleMaximize(index) {
      if (maximizedWindow === index) {
        maximizedWindow = null;
      } else {
        hiddenWindows.delete(index);
        maximizedWindow = index;
      }
      updateLayout();
    }

    function restoreAllWindows() {
      maximizedWindow = null;
      hiddenWindows.clear();
      updateLayout();
    }

    function updateLayout() {
      const statusBadge = document.getElementById('layoutStatusBadge');
      const restoreBtn = document.getElementById('restoreAllBtn');

      for (let i = 0; i < 3; i++) {
        const pane = document.getElementById('pane-' + i);
        const maxBtn = document.getElementById('max-btn-' + i);

        if (maximizedWindow !== null) {
          if (maximizedWindow === i) {
            pane.classList.remove('hidden');
            pane.classList.add('maximized');
            maxBtn.innerText = '⊡';
          } else {
            pane.classList.add('hidden');
            pane.classList.remove('maximized');
            maxBtn.innerText = '⛶';
          }
        } else {
          pane.classList.remove('maximized');
          maxBtn.innerText = '⛶';
          if (hiddenWindows.has(i)) {
            pane.classList.add('hidden');
          } else {
            pane.classList.remove('hidden');
          }
        }
      }

      // Update Header Status Badge
      const visibleCount = 3 - hiddenWindows.size;
      if (maximizedWindow !== null) {
        statusBadge.innerText = 'WIN ' + (maximizedWindow + 1) + ' 全屏独占 (100%)';
        restoreBtn.classList.add('show');
      } else if (visibleCount === 2) {
        statusBadge.innerText = '2 窗并列均分 (50% : 50%)';
        restoreBtn.classList.add('show');
      } else if (visibleCount === 1) {
        statusBadge.innerText = '单窗独占 (100%)';
        restoreBtn.classList.add('show');
      } else {
        statusBadge.innerText = '3 窗并列 (1:1:1 均分)';
        restoreBtn.classList.remove('show');
      }
    }

    function reloadPane(index) {
      const iframe = document.getElementById('iframe-' + index);
      iframe.src = iframe.src;
    }

    function toggleUA(index) {
      const btn = document.getElementById('ua-btn-' + index);
      const isPC = btn.innerText === '💻';
      btn.innerText = isPC ? '📱' : '💻';
      btn.title = isPC ? '当前移动端UA' : '当前桌面端UA';
    }

    function toggleAssist(index) {
      const bar = document.getElementById('tv-bar-' + index);
      const btn = document.getElementById('ast-btn-' + index);
      const isVisible = bar.style.display !== 'none';
      bar.style.display = isVisible ? 'none' : 'flex';
      btn.classList.toggle('active', !isVisible);
    }

    function toggleMagnifier(index) {
      const loupe = document.getElementById('loupe-' + index);
      const btn = document.getElementById('mag-btn-' + index);
      const isActive = loupe.style.display === 'block';
      loupe.style.display = isActive ? 'none' : 'block';
      btn.classList.toggle('gold-active', !isActive);

      const container = document.getElementById('wv-container-' + index);
      if (!isActive) {
        container.onmousemove = function(e) {
          const rect = container.getBoundingClientRect();
          const x = e.clientX - rect.left;
          const y = e.clientY - rect.top;
          loupe.style.left = (x - 90) + 'px';
          loupe.style.top = (y - 120) + 'px';
        };
      } else {
        container.onmousemove = null;
      }
    }

    function triggerTVAction(index, action) {
      // Send shortcut message or visual confirmation
      const bar = document.getElementById('tv-bar-' + index);
      bar.style.transform = 'scale(0.96)';
      setTimeout(() => bar.style.transform = 'scale(1)', 150);

      const iframe = document.getElementById('iframe-' + index);
      try {
        iframe.contentWindow.postMessage({ type: 'tv_shortcut', action: action }, '*');
      } catch(e) {}
    }

    // Modal dialogs
    function openUrlModal(index) {
      editingWindowIndex = index;
      document.getElementById('urlModalTitle').innerText = '窗口 ' + (index + 1) + ' 网址设置';
      document.getElementById('urlInput').value = windowUrls[index];
      document.getElementById('urlModal').classList.add('open');
    }
    function closeUrlModal() {
      document.getElementById('urlModal').classList.remove('open');
    }
    function setModalUrl(url) {
      document.getElementById('urlInput').value = url;
    }
    function applyModalUrl() {
      const val = document.getElementById('urlInput').value.trim();
      if (val) {
        setWindowUrl(editingWindowIndex, val);
      }
      closeUrlModal();
    }

    function openSaveWorkspaceModal() {
      const details = windowUrls.map((u, i) => '窗口 ' + (i + 1) + ': ' + u).join('<br>');
      document.getElementById('saveWsDetails').innerHTML = '即将保存当前 3 窗口：<br>' + details;
      document.getElementById('wsNameInput').value = '自定义自选组 ' + Math.floor(Math.random() * 90 + 10);
      document.getElementById('saveWsModal').classList.add('open');
    }
    function closeSaveWsModal() {
      document.getElementById('saveWsModal').classList.remove('open');
    }
    function confirmSaveWorkspace() {
      const name = document.getElementById('wsNameInput').value.trim();
      if (!name) return;
      const newWs = {
        id: 'custom_' + Date.now(),
        name: name,
        icon: '⭐',
        urls: [...windowUrls]
      };
      workspaces.push(newWs);
      currentWorkspaceId = newWs.id;
      try {
        const customOnly = workspaces.filter(w => w.id.startsWith('custom_'));
        localStorage.setItem('custom_workspaces', JSON.stringify(customOnly));
      } catch(e) {}
      renderWorkspaces();
      closeSaveWsModal();
    }
    function deleteWorkspace(id) {
      workspaces = workspaces.filter(w => w.id !== id);
      if (currentWorkspaceId === id) currentWorkspaceId = 'crypto';
      try {
        const customOnly = workspaces.filter(w => w.id.startsWith('custom_'));
        localStorage.setItem('custom_workspaces', JSON.stringify(customOnly));
      } catch(e) {}
      renderWorkspaces();
      selectWorkspace(currentWorkspaceId);
    }

    // Draggable Shortcut Floating Bars
    function setupDraggableBars() {
      [0, 1, 2].forEach(i => {
        const bar = document.getElementById('tv-bar-' + i);
        let isDragging = false, startX, startY, origX, origY;
        bar.addEventListener('mousedown', e => {
          if (e.target.tagName.toLowerCase() === 'button') return;
          isDragging = true;
          startX = e.clientX;
          startY = e.clientY;
          origX = bar.offsetLeft;
          origY = bar.offsetTop;
        });
        window.addEventListener('mousemove', e => {
          if (!isDragging) return;
          const dx = e.clientX - startX;
          const dy = e.clientY - startY;
          bar.style.left = Math.max(0, origX + dx) + 'px';
          bar.style.top = Math.max(0, origY + dy) + 'px';
        });
        window.addEventListener('mouseup', () => isDragging = false);
      });
    }

    window.onload = init;
  </script>
</body>
</html>`;

const server = http.createServer((req, res) => {
  res.writeHead(200, {
    'Content-Type': 'text/html; charset=utf-8',
    'Cache-Control': 'no-cache'
  });
  res.end(html);
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`Triple Chart Browser preview server running on port ${PORT}`);
});
