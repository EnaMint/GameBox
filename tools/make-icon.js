// 生成 GameBox 图标 (PNG-in-ICO，多尺寸 64/256)
// 输出: tools/gamebox.ico 与 gamebox-web/electron/gamebox.ico
const zlib = require('zlib');
const fs = require('fs');
const path = require('path');

const NAVY = [27, 40, 56];      // #1b2838
const BLUE = [102, 192, 244];   // #66c0f4

function render(S) {
  const px = Buffer.alloc(S * S * 4);
  const k = S / 64;
  const set = (x, y, [r, g, b, a = 255]) => {
    if (x < 0 || y < 0 || x >= S || y >= S) return;
    const i = (y * S + x) * 4;
    px[i] = r; px[i + 1] = g; px[i + 2] = b; px[i + 3] = a;
  };
  const circle = (cx, cy, rad, c) => {
    for (let y = Math.floor(cy - rad) - 1; y <= Math.ceil(cy + rad) + 1; y++)
      for (let x = Math.floor(cx - rad) - 1; x <= Math.ceil(cx + rad) + 1; x++)
        if ((x - cx) ** 2 + (y - cy) ** 2 <= rad * rad) set(x, y, c);
  };
  const rect = (x0, y0, x1, y1, c) => {
    for (let y = y0; y <= y1; y++) for (let x = x0; x <= x1; x++) set(x, y, c);
  };

  // 圆角背景
  rect(6 * k, 0, 57 * k, S - 1, NAVY);
  rect(0, 6 * k, S - 1, 57 * k, NAVY);
  circle(6 * k, 6 * k, 6 * k, NAVY); circle(57 * k, 6 * k, 6 * k, NAVY);
  circle(6 * k, 57 * k, 6 * k, NAVY); circle(57 * k, 57 * k, 6 * k, NAVY);

  // 手柄主体
  rect(14 * k, 24 * k, 49 * k, 40 * k, BLUE);
  circle(14 * k, 32 * k, 8 * k, BLUE);
  circle(49 * k, 32 * k, 8 * k, BLUE);

  // 十字键
  rect(17 * k, 30 * k, 27 * k, 34 * k, NAVY);
  rect(20 * k, 27 * k, 24 * k, 37 * k, NAVY);

  // 按键
  circle(42 * k, 29 * k, 2.6 * k, NAVY);
  circle(47 * k, 34 * k, 2.6 * k, NAVY);
  return px;
}

function crc32(buf) {
  let t = crc32.t;
  if (!t) {
    t = crc32.t = [];
    for (let n = 0; n < 256; n++) {
      let c = n;
      for (let i = 0; i < 8; i++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
      t[n] = c >>> 0;
    }
  }
  let c = 0xffffffff;
  for (const b of buf) c = t[(c ^ b) & 0xff] ^ (c >>> 8);
  return (c ^ 0xffffffff) >>> 0;
}

function chunk(type, data) {
  const len = Buffer.alloc(4); len.writeUInt32BE(data.length);
  const td = Buffer.concat([Buffer.from(type, 'ascii'), data]);
  const crc = Buffer.alloc(4); crc.writeUInt32BE(crc32(td));
  return Buffer.concat([len, td, crc]);
}

function encodePng(S, px) {
  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(S, 0); ihdr.writeUInt32BE(S, 4);
  ihdr[8] = 8; ihdr[9] = 6; // 8-bit RGBA
  const rows = [];
  for (let y = 0; y < S; y++) {
    const row = Buffer.alloc(1 + S * 4);
    row[0] = 0;
    px.copy(row, 1, y * S * 4, (y + 1) * S * 4);
    rows.push(row);
  }
  return Buffer.concat([
    Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]),
    chunk('IHDR', ihdr),
    chunk('IDAT', zlib.deflateSync(Buffer.concat(rows), { level: 9 })),
    chunk('IEND', Buffer.alloc(0)),
  ]);
}

const sizes = [64, 256];
const pngs = sizes.map((s) => encodePng(s, render(s)));

const header = Buffer.alloc(6);
header.writeUInt16LE(0, 0); header.writeUInt16LE(1, 2); header.writeUInt16LE(sizes.length, 4);
let offset = 6 + 16 * sizes.length;
const entries = [];
sizes.forEach((s, i) => {
  const e = Buffer.alloc(16);
  e[0] = s >= 256 ? 0 : s; e[1] = s >= 256 ? 0 : s;
  e.writeUInt16LE(1, 4); e.writeUInt16LE(32, 6);
  e.writeUInt32LE(pngs[i].length, 8); e.writeUInt32LE(offset, 12);
  offset += pngs[i].length;
  entries.push(e);
});
const ico = Buffer.concat([header, ...entries, ...pngs]);

const targets = [
  path.join(__dirname, 'gamebox.ico'),
  path.join(__dirname, '..', 'gamebox-web', 'electron', 'gamebox.ico'),
];
for (const t of targets) {
  fs.mkdirSync(path.dirname(t), { recursive: true });
  fs.writeFileSync(t, ico);
  console.log('wrote', t);
}
