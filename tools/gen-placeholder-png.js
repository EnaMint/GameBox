const zlib = require('zlib');
const fs = require('fs');
const path = require('path');

function crc32(buf) {
  let table = crc32.table;
  if (!table) {
    table = crc32.table = [];
    for (let n = 0; n < 256; n++) {
      let c = n;
      for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
      table[n] = c >>> 0;
    }
  }
  let c = 0xffffffff;
  for (const b of buf) c = table[(c ^ b) & 0xff] ^ (c >>> 8);
  return (c ^ 0xffffffff) >>> 0;
}

function chunk(type, data) {
  const len = Buffer.alloc(4);
  len.writeUInt32BE(data.length);
  const td = Buffer.concat([Buffer.from(type, 'ascii'), data]);
  const crc = Buffer.alloc(4);
  crc.writeUInt32BE(crc32(td));
  return Buffer.concat([len, td, crc]);
}

function makePng(w, h, top, bottom) {
  const sig = Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]);
  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(w, 0);
  ihdr.writeUInt32BE(h, 4);
  ihdr[8] = 8; ihdr[9] = 2; // 8-bit RGB
  const rows = [];
  for (let y = 0; y < h; y++) {
    const row = Buffer.alloc(1 + w * 3);
    row[0] = 0;
    const t = y / (h - 1);
    const r = Math.round(top[0] + (bottom[0] - top[0]) * t);
    const g = Math.round(top[1] + (bottom[1] - top[1]) * t);
    const b = Math.round(top[2] + (bottom[2] - top[2]) * t);
    for (let x = 0; x < w; x++) {
      row[1 + x * 3] = r; row[2 + x * 3] = g; row[3 + x * 3] = b;
    }
    rows.push(row);
  }
  const idat = zlib.deflateSync(Buffer.concat(rows), { level: 9 });
  return Buffer.concat([sig, chunk('IHDR', ihdr), chunk('IDAT', idat), chunk('IEND', Buffer.alloc(0))]);
}

const outDir = process.argv[2];
const jobs = [
  ['cover/20260901/demo-elden.png', 960, 540, [26, 58, 92], [214, 171, 66]],
  ['cover/20260901/demo-wukong.png', 960, 540, [64, 24, 16], [230, 126, 34]],
  ['cover/20260901/demo-bg3.png', 960, 540, [46, 20, 64], [142, 68, 173]],
  ['cover/20260901/demo-cyber.png', 960, 540, [12, 32, 48], [245, 214, 61]],
  ['record/20260901/demo-r1.png', 800, 450, [20, 30, 48], [88, 166, 255]],
  ['record/20260901/demo-r2.png', 800, 450, [48, 20, 20], [255, 122, 89]],
  ['record/20260901/demo-r3.png', 800, 450, [16, 44, 32], [163, 207, 6]],
  ['avatar/20260901/demo-a.png', 240, 240, [102, 192, 244], [30, 66, 102]],
  ['avatar/20260901/demo-b.png', 240, 240, [163, 207, 6], [44, 62, 18]],
];
for (const [rel, w, h, top, bottom] of jobs) {
  const p = path.join(outDir, rel);
  fs.mkdirSync(path.dirname(p), { recursive: true });
  fs.writeFileSync(p, makePng(w, h, top, bottom));
  console.log('wrote', p);
}
