// GameBox Electron 主进程
// 打包后前端以本地 HTTP 方式加载：静态文件来自 vite 构建产物 dist/，
// /api/** 请求反向代理到网关（默认 127.0.0.1:8080），因此前端代码无需任何改动。
const { app, BrowserWindow, Menu, Tray, nativeImage } = require('electron');
const http = require('http');
const fs = require('fs');
const path = require('path');

const GATEWAY_HOST = '127.0.0.1';
const GATEWAY_PORT = Number(process.env.GAMEBOX_GATEWAY_PORT || 8080);
const DIST_DIR = path.join(__dirname, '..', 'dist');

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

function showWindow() {
  if (!win) return;
  win.show();
  win.focus();
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

function getArg(name) {
  const hit = process.argv.find((a) => a.startsWith(`--${name}=`));
  return hit ? hit.slice(name.length + 3) : null;
}

if (!app.requestSingleInstanceLock()) {
  app.quit();
} else {
  app.on('second-instance', showWindow);

  app.whenReady().then(async () => {
    createTray();
    const devUrl = getArg('dev-url') || process.env.GAMEBOX_DEV_URL;
    if (devUrl) {
      baseUrl = devUrl;
      createWindow(devUrl);
      return;
    }
    if (!fs.existsSync(path.join(DIST_DIR, 'index.html'))) {
      console.error('未找到 dist/index.html，请先执行: npm run build');
      app.quit();
      return;
    }
    const { server, port } = await startServer();
    app.on('before-quit', () => server.close());
    baseUrl = `http://127.0.0.1:${port}`;
    createWindow(`${baseUrl}/`);
  });

  app.on('window-all-closed', () => app.quit());
}
