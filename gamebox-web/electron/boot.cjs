// GameBox 启动编排：加载窗口通过它在后台隐藏执行 start-all.sh --no-browser，
// 并把脚本的真实输出翻译成进度百分比与步骤文字。
const { spawn, execFile } = require('child_process');
const fs = require('fs');
const net = require('net');
const path = require('path');

const GATEWAY_PORT = Number(process.env.GAMEBOX_GATEWAY_PORT || 8080);
// start-all.sh 最坏情况约 15 分钟（Nacos 90s + 6×120s + 前端 60s），8 分钟未结束视为卡死
const BOOT_TIMEOUT_MS = Number(process.env.GAMEBOX_BOOT_TIMEOUT || 480000);

const SERVICE_ORDER = [
  'gamebox-user',
  'gamebox-auth',
  'gamebox-strategy',
  'gamebox-team',
  'gamebox-record',
  'gamebox-gateway',
];

const SERVICE_META = {
  'gamebox-user': { name: '用户服务', port: 8082 },
  'gamebox-auth': { name: '认证服务', port: 8081 },
  'gamebox-strategy': { name: '攻略服务', port: 8083 },
  'gamebox-team': { name: '组队服务', port: 8084 },
  'gamebox-record': { name: '战绩服务', port: 8085 },
  'gamebox-gateway': { name: '网关', port: 8080 },
};

const SERVICE_BASE = 34;
const SERVICE_STEP = 9;

// 打包后 __dirname 位于 release/win-unpacked/resources/app.asar/electron，上溯 5 层才是仓库根；
// 开发模式（electron .）位于 gamebox-web/electron，上溯 2 层。逐层找 start-all.sh 同时覆盖两种情况。
function findProjectRoot() {
  const fromEnv = process.env.GAMEBOX_ROOT;
  if (fromEnv && fs.existsSync(path.join(fromEnv, 'start-all.sh'))) return fromEnv;
  let dir = __dirname;
  for (let i = 0; i < 10; i += 1) {
    if (fs.existsSync(path.join(dir, 'start-all.sh'))) return dir;
    const parent = path.dirname(dir);
    if (parent === dir) break;
    dir = parent;
  }
  return null;
}

function findBashSync() {
  const fromEnv = process.env.GAMEBOX_BASH;
  if (fromEnv && fs.existsSync(fromEnv)) return fromEnv;
  const candidates = [
    'D:\\Git\\bin\\bash.exe',
    'C:\\Program Files\\Git\\bin\\bash.exe',
    'C:\\Program Files (x86)\\Git\\bin\\bash.exe',
    process.env.LOCALAPPDATA ? path.join(process.env.LOCALAPPDATA, 'Programs\\Git\\bin\\bash.exe') : null,
    process.env.ProgramFiles ? path.join(process.env.ProgramFiles, 'Git\\usr\\bin\\bash.exe') : null,
  ].filter(Boolean);
  return candidates.find((c) => fs.existsSync(c)) || null;
}

function whereBash() {
  return new Promise((resolve) => {
    execFile('where', ['bash'], { windowsHide: true }, (err, stdout) => {
      if (err) return resolve(null);
      const hit = String(stdout)
        .split(/\r?\n/)
        .map((s) => s.trim())
        .find((s) => /bash\.exe$/i.test(s));
      resolve(hit && fs.existsSync(hit) ? hit : null);
    });
  });
}

async function findBash() {
  return findBashSync() || (await whereBash());
}

async function resolveEnvironment() {
  return { root: findProjectRoot(), bash: await findBash() };
}

function probePort(port, timeoutMs = 600) {
  return new Promise((resolve) => {
    const sock = new net.Socket();
    let done = false;
    const finish = (ok) => {
      if (done) return;
      done = true;
      clearTimeout(timer);
      sock.destroy();
      resolve(ok);
    };
    const timer = setTimeout(() => finish(false), timeoutMs);
    sock.setTimeout(timeoutMs);
    sock.once('connect', () => finish(true));
    sock.once('timeout', () => finish(false));
    sock.once('error', () => finish(false));
    sock.connect(port, '127.0.0.1');
  });
}

// 打包应用自己提供前端静态资源，只有网关是必须的（脚本最后启动它，通即代表整链路可用）
function isBackendReady() {
  return probePort(GATEWAY_PORT);
}

function killTree(pid) {
  if (!pid) return;
  try {
    spawn('taskkill', ['/pid', String(pid), '/T', '/F'], { windowsHide: true }).on('error', () => {});
  } catch (e) {
    // 杀不掉就交给 stop-all.sh 清理
  }
}

function serviceProgress(line) {
  const start = line.match(/启动 (gamebox-[a-z]+) \.\.\./);
  const skip = line.match(/\[SKIP\] (gamebox-[a-z]+) 已在运行/);
  const ready = line.match(/\[OK\] (gamebox-[a-z]+) 就绪/);
  const hit = start || skip || ready;
  if (!hit) return null;
  const idx = SERVICE_ORDER.indexOf(hit[1]);
  if (idx < 0) return null;
  const meta = SERVICE_META[hit[1]];
  if (ready) {
    return { percent: SERVICE_BASE + (idx + 1) * SERVICE_STEP, step: `${meta.name}已就绪`, level: 'ok' };
  }
  if (skip) {
    return { percent: SERVICE_BASE + (idx + 1) * SERVICE_STEP, step: `${meta.name}已在运行`, level: 'ok' };
  }
  return { percent: SERVICE_BASE + idx * SERVICE_STEP, step: `正在启动${meta.name} (${meta.port})…` };
}

function applyRules(line) {
  if (/^\s*\[FAIL\]/.test(line)) {
    return { percent: 0, step: line.replace(/^\s*\[FAIL\]\s*/, ''), level: 'error' };
  }
  const hit = serviceProgress(line);
  if (hit) return hit;
  if (/==\s*\[1\/4\]/.test(line)) return { percent: 8, step: '正在检查 MySQL 数据库…' };
  if (/\[OK\] MySQL 正在运行/.test(line)) return { percent: 12, step: 'MySQL 已就绪', level: 'ok' };
  if (/==\s*\[2\/4\]/.test(line)) return { percent: 16, step: '正在检查注册中心 Nacos…' };
  if (/启动 Nacos/.test(line)) return { percent: 20, step: '正在启动 Nacos（约需 30-60 秒）…' };
  if (/\[SKIP\] Nacos 已在运行/.test(line)) return { percent: 28, step: 'Nacos 已在运行', level: 'ok' };
  if (/\[OK\] Nacos 就绪/.test(line)) return { percent: 28, step: 'Nacos 已就绪', level: 'ok' };
  if (/==\s*\[3\/4\]/.test(line)) return { percent: 32, step: '正在启动后端微服务…' };
  if (/==\s*\[4\/4\]/.test(line)) return { percent: 90, step: '正在启动前端…' };
  if (/首次运行，安装前端依赖/.test(line)) return { percent: 90, step: '正在安装前端依赖（首次运行，耗时较久）…' };
  if (/\[OK\] 前端 就绪/.test(line) || /\[SKIP\] 前端已在运行/.test(line)) {
    return { percent: 95, step: '前端已就绪', level: 'ok' };
  }
  if (/全部就绪/.test(line)) return { percent: 96, step: '服务全部就绪', level: 'ok' };
  if (/启动未全部成功/.test(line)) return { percent: 0, step: '部分组件未启动成功', level: 'error' };
  return null;
}

// 返回 { finished: Promise<number|null>, cancel() }；finished 解析为脚本退出码，取消/超时为 null
function startBackend({ root, bash, onProgress, onState }) {
  let percent = 0;
  let settled = false;
  let child = null;
  let watchdog = null;
  let timedOut = false;
  let buf = '';
  let lastError = null;
  const tail = [];
  let resolveFinished = () => {};
  const finished = new Promise((r) => { resolveFinished = r; });

  const emit = (p, step, level = 'info') => {
    percent = Math.max(percent, p);
    if (onProgress) onProgress({ percent, step, level });
  };

  const settle = (code, state, message) => {
    if (settled) return;
    settled = true;
    if (watchdog) clearTimeout(watchdog);
    if (onState) onState({ state, message: message || '', tail: tail.slice(-8) });
    resolveFinished(code);
  };

  const handleLine = (raw) => {
    const line = String(raw).replace(/\s+$/, '');
    if (!line.trim()) return;
    tail.push(line);
    if (tail.length > 40) tail.shift();
    const hit = applyRules(line);
    if (hit) {
      if (hit.level === 'error') lastError = hit.step;
      emit(hit.percent, hit.step, hit.level);
    }
  };

  emit(3, '正在准备启动…');

  try {
    child = spawn(bash, ['start-all.sh', '--no-browser'], { cwd: root, windowsHide: true, shell: false });
  } catch (e) {
    settle(null, 'error', `无法执行启动脚本：${e.message}`);
    return { finished, cancel() {} };
  }

  child.stdout.setEncoding('utf8');
  child.stdout.on('data', (chunk) => {
    buf += chunk;
    const lines = buf.split('\n');
    buf = lines.pop();
    lines.forEach(handleLine);
  });

  child.stderr.setEncoding('utf8');
  child.stderr.on('data', (chunk) => {
    String(chunk).split(/\r?\n/).forEach((l) => { if (l.trim()) tail.push(l); });
    if (tail.length > 40) tail.splice(0, tail.length - 40);
  });

  child.on('error', (e) => settle(null, 'error', `启动脚本执行失败：${e.message}`));

  child.on('exit', (code, signal) => {
    if (timedOut) return;
    if (buf.trim()) { handleLine(buf); buf = ''; }
    if (code === 0) {
      emit(100, '正在打开应用窗口…', 'ok');
      settle(0, 'done', '');
      return;
    }
    settle(code, 'error', lastError || `启动未完成（退出码 ${code === null ? signal : code}），可查看详情或重试`);
  });

  watchdog = setTimeout(() => {
    timedOut = true;
    killTree(child.pid);
    settle(null, 'timeout', `启动超过 ${Math.round(BOOT_TIMEOUT_MS / 1000)} 秒仍未完成`);
  }, BOOT_TIMEOUT_MS);

  return {
    finished,
    cancel() {
      if (settled) { resolveFinished(null); return; }
      settled = true;
      if (watchdog) clearTimeout(watchdog);
      killTree(child.pid);
      try { child.kill(); } catch (e) { /* 已退出 */ }
      resolveFinished(null);
    },
  };
}

module.exports = {
  GATEWAY_PORT,
  BOOT_TIMEOUT_MS,
  findProjectRoot,
  findBash,
  resolveEnvironment,
  probePort,
  isBackendReady,
  killTree,
  startBackend,
};
