// GameBox Electron 主进程
// 打包后前端以本地 HTTP 方式加载：静态文件来自 vite 构建产物 dist/，
// /api/** 请求反向代理到网关（默认 127.0.0.1:8080），因此前端代码无需任何改动。
// 双击本程序即为完整一键启动：后端未就绪时先显示加载窗口，由它在后台隐藏执行 start-all.sh。
const { app, BrowserWindow, Menu, Tray, nativeImage, ipcMain, shell } = require('electron');
const http = require('http');
const fs = require('fs');
const path = require('path');
const boot = require('./boot.cjs');

const GATEWAY_HOST = '127.0.0.1';
const GATEWAY_PORT = Number(process.env.GAMEBOX_GATEWAY_PORT || 8080);
const DIST_DIR = path.join(__dirname, '..', 'dist');
const SPLASH_FILE = path.join(__dirname, 'splash.html');
const SPLASH_PRELOAD = path.join(__dirname, 'splash-preload.cjs');
const SPLASH_WIDTH = 480;
const SPLASH_HEIGHT = 300;
const SPLASH_HEIGHT_ERROR = 430;
const SPLASH_MIN_MS = 1200;

const MIME = {
  '.html': 'text/html; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.svg': 'image/svg+xml',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.gif': 'image/gif',
  '.ico': 'image/x-icon',
  '.json': 'application/json; charset=utf-8',
  '.woff': 'font/woff',
  '.woff2': 'font/woff2',
};

function proxyToGateway(req, res) {
  const opts = {
    method: req.method,
    hostname: GATEWAY_HOST,
    port: GATEWAY_PORT,
    path: req.url,
    headers: { ...req.headers, host: `${GATEWAY_HOST}:${GATEWAY_PORT}` },
  };
  const upstream = http.request(opts, (up) => {
    res.writeHead(up.statusCode, up.headers);
    up.pipe(res);
  });
  upstream.on('error', () => {
    res.writeHead(503, { 'Content-Type': 'application/json; charset=utf-8' });
    res.end(JSON.stringify({ code: 503, message: '后端服务不可用，请先运行 start-all.sh 启动 GameBox 后端', data: null }));
  });
  req.pipe(upstream);
}

function serveStatic(req, res) {
  const reqPath = decodeURIComponent((req.url || '/').split('?')[0]);
  let filePath = path.normalize(path.join(DIST_DIR, reqPath));
  if (!filePath.startsWith(DIST_DIR)) {
    res.writeHead(403); res.end(); return;
  }
  const send = (file) => {
    res.writeHead(200, { 'Content-Type': MIME[path.extname(file).toLowerCase()] || 'application/octet-stream' });
    fs.createReadStream(file).pipe(res);
  };
  fs.stat(filePath, (err, st) => {
    if (!err && st.isFile()) return send(filePath);
    // SPA 路由回退
    send(path.join(DIST_DIR, 'index.html'));
  });
}

function startServer() {
  return new Promise((resolve, reject) => {
    const server = http.createServer((req, res) => {
      const url = req.url || '/';
      if (url === '/api' || url.startsWith('/api/')) return proxyToGateway(req, res);
      serveStatic(req, res);
    });
    const tryListen = (port) => {
      server.once('error', (e) => {
        if (e.code === 'EADDRINUSE' && port < 5219) tryListen(port + 1);
        else reject(e);
      });
      server.listen(port, '127.0.0.1', () => resolve({ server, port }));
    };
    tryListen(5199);
  });
}

let win = null;
let tray = null;
let baseUrl = null;
let isQuitting = false;
let trayHinted = false;
let splash = null;
let booting = false;
let splashClosedByUs = false;
let bootCtx = null;
let bootEnv = { root: null, bash: null };
let bootStartedAt = 0;
let ipcRegistered = false;

function showWindow() {
  if (win) {
    win.show();
    win.focus();
    return;
  }
  if (splash && !splash.isDestroyed()) {
    splash.show();
    splash.focus();
  }
}

function navigateTo(route) {
  if (!win || !baseUrl) return;
  showWindow();
  win.loadURL(baseUrl.replace(/\/$/, '') + route);
}

function createTray() {
  tray = new Tray(nativeImage.createFromPath(path.join(__dirname, 'gamebox.ico')));
  tray.setToolTip('GameBox - 游戏攻略与玩家社区');
  tray.setContextMenu(Menu.buildFromTemplate([
    { label: '显示主窗口', click: showWindow },
    { type: 'separator' },
    { label: '游戏库', click: () => navigateTo('/games') },
    { label: '攻略社区', click: () => navigateTo('/strategy') },
    { label: '组队大厅', click: () => navigateTo('/team') },
    { label: '战绩动态', click: () => navigateTo('/record') },
    { type: 'separator' },
    { label: '退出', click: () => { isQuitting = true; app.quit(); } },
  ]));
  tray.on('click', showWindow);
  tray.on('double-click', showWindow);
}

function createWindow(url) {
  win = new BrowserWindow({
    width: 1280,
    height: 820,
    minWidth: 1000,
    minHeight: 640,
    backgroundColor: '#1b2838',
    autoHideMenuBar: true,
    icon: path.join(__dirname, 'gamebox.ico'),
    webPreferences: { contextIsolation: true, nodeIntegration: false },
  });
  win.on('close', (e) => {
    if (!isQuitting) {
      e.preventDefault();
      win.hide();
      if (!trayHinted) {
        trayHinted = true;
        tray.displayBalloon({ title: 'GameBox', content: '程序仍在后台运行，右键托盘图标可彻底退出' });
      }
    }
  });
  win.on('closed', () => { win = null; });
  win.loadURL(url);
  return win;
}

let lastProgress = null;
let lastState = null;

function sendToSplash(channel, payload) {
  if (splash && !splash.isDestroyed()) splash.webContents.send(channel, payload);
}

function sendProgress(p) {
  lastProgress = p;
  sendToSplash('boot:progress', p);
}

function sendState(s) {
  lastState = s;
  sendToSplash('boot:state', s);
  if (!splash || splash.isDestroyed()) return;
  const failed = s.state === 'error' || s.state === 'timeout';
  splash.setSize(SPLASH_WIDTH, failed ? SPLASH_HEIGHT_ERROR : SPLASH_HEIGHT);
}

function closeSplash() {
  if (!splash) return;
  splashClosedByUs = true;
  const s = splash;
  splash = null;
  s.close();
}

function createSplash() {
  splashClosedByUs = false;
  splash = new BrowserWindow({
    width: SPLASH_WIDTH,
    height: SPLASH_HEIGHT,
    frame: false,
    resizable: false,
    minimizable: false,
    maximizable: false,
    fullscreenable: false,
    alwaysOnTop: true,
    show: false,
    backgroundColor: '#1b2838',
    icon: path.join(__dirname, 'gamebox.ico'),
    webPreferences: {
      preload: SPLASH_PRELOAD,
      contextIsolation: true,
      nodeIntegration: false,
    },
  });
  splash.setMenuBarVisibility(false);
  // 页面加载完成前发出的进度会丢，这里补发最近一次
  splash.webContents.once('did-finish-load', () => {
    if (lastProgress) sendToSplash('boot:progress', lastProgress);
    if (lastState) sendToSplash('boot:state', lastState);
  });
  splash.on('close', () => {
    // 加载阶段用 Alt+F4 关掉窗口视为放弃启动
    if (booting && !splashClosedByUs && !isQuitting) {
      if (bootCtx) bootCtx.cancel();
      bootCtx = null;
      isQuitting = true;
      app.quit();
    }
  });
  splash.on('closed', () => { splash = null; });
  splash.loadFile(SPLASH_FILE);
  splash.once('ready-to-show', () => { if (splash) splash.show(); });
  return splash;
}

function registerBootIpc() {
  if (ipcRegistered) return;
  ipcRegistered = true;
  ipcMain.on('boot:cancel', () => {
    if (bootCtx) bootCtx.cancel();
    bootCtx = null;
    isQuitting = true;
    app.quit();
  });
  ipcMain.on('boot:retry', () => {
    if (bootCtx) { bootCtx.cancel(); bootCtx = null; }
    lastProgress = null;
    lastState = null;
    startBootAttempt();
  });
  ipcMain.on('boot:open-logs', () => {
    shell.openPath(path.join(bootEnv.root || process.cwd(), 'logs'));
  });
  ipcMain.on('boot:quit', () => {
    isQuitting = true;
    app.quit();
  });
}

async function openMainWindow() {
  if (win) { showWindow(); return; }
  if (!fs.existsSync(path.join(DIST_DIR, 'index.html'))) {
    console.error('未找到 dist/index.html，请先执行: npm run build');
    app.quit();
    return;
  }
  const { server, port } = await startServer();
  app.on('before-quit', () => server.close());
  baseUrl = `http://127.0.0.1:${port}`;
  createWindow(`${baseUrl}/`);
}

async function startBootAttempt() {
  bootStartedAt = Date.now();
  sendState({ state: 'running', message: '', tail: [] });
  bootCtx = boot.startBackend({
    root: bootEnv.root,
    bash: bootEnv.bash,
    onProgress: sendProgress,
    onState: sendState,
  });
  const code = await bootCtx.finished;
  bootCtx = null;
  // 错误/超时/取消：留在加载窗口等用户重试或退出
  if (code !== 0) return;
  const rest = SPLASH_MIN_MS - (Date.now() - bootStartedAt);
  if (rest > 0) await new Promise((r) => setTimeout(r, rest));
  closeSplash();
  await openMainWindow();
  booting = false;
}

async function runBoot(root, bash) {
  bootEnv = { root, bash };
  booting = true;
  createSplash();
  registerBootIpc();
  await startBootAttempt();
}

function getArg(name) {
  const hit = process.argv.find((a) => a.startsWith(`--${name}=`));
  return hit ? hit.slice(name.length + 3) : null;
}

if (!app.requestSingleInstanceLock()) {
  app.quit();
} else {
  app.on('second-instance', showWindow);
  app.on('before-quit', () => {
    if (bootCtx) { bootCtx.cancel(); bootCtx = null; }
  });

  app.whenReady().then(async () => {
    createTray();
    const devUrl = getArg('dev-url') || process.env.GAMEBOX_DEV_URL;
    if (devUrl) {
      baseUrl = devUrl;
      createWindow(devUrl);
      return;
    }
    if (await boot.isBackendReady()) {
      await openMainWindow();
      return;
    }
    const env = await boot.resolveEnvironment();
    if (!env.root || !env.bash) {
      console.warn(`未找到 start-all.sh 或 bash（root=${env.root}, bash=${env.bash}），跳过加载窗口直接打开应用`);
      await openMainWindow();
      return;
    }
    await runBoot(env.root, env.bash);
  });

  // 加载窗口关闭到主窗口创建之间有无窗口空档，booting 期间不能退出
  app.on('window-all-closed', () => { if (!booting) app.quit(); });
}
