import fs from "fs";
import zlib from "zlib";

const w = 16;
const h = 16;

function crc32(buf) {
  let c = ~0 >>> 0;
  for (let i = 0; i < buf.length; i++) {
    c ^= buf[i];
    for (let k = 0; k < 8; k++) {
      c = (c >>> 1) ^ (0xedb88320 & -(c & 1));
    }
  }
  return (~c) >>> 0;
}

function chunk(type, data) {
  const len = Buffer.alloc(4);
  len.writeUInt32BE(data.length, 0);
  const typeBuf = Buffer.from(type, "ascii");
  const crc = crc32(Buffer.concat([typeBuf, data]));
  const crcBuf = Buffer.alloc(4);
  crcBuf.writeUInt32BE(crc, 0);
  return Buffer.concat([len, typeBuf, data, crcBuf]);
}

const rowSize = 1 + w * 3;
const raw = Buffer.alloc(h * rowSize);
for (let y = 0; y < h; y++) {
  raw[y * rowSize] = 0;
  for (let x = 0; x < w; x++) {
    const cellR = (y / 2) | 0;
    const cellC = (x / 2) | 0;
    const dark = (cellR + cellC) % 2 === 0;
    const r = dark ? 0x4a : 0xe8;
    const g = dark ? 0x37 : 0xd4;
    const b = dark ? 0x28 : 0xb8;
    const o = y * rowSize + 1 + x * 3;
    raw[o] = r;
    raw[o + 1] = g;
    raw[o + 2] = b;
  }
}

const idat = zlib.deflateSync(raw);

const sig = Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]);
const ihdr = Buffer.alloc(13);
ihdr.writeUInt32BE(w, 0);
ihdr.writeUInt32BE(h, 4);
ihdr[8] = 8;
ihdr[9] = 2;
ihdr[10] = 0;
ihdr[11] = 0;
ihdr[12] = 0;

const png = Buffer.concat([
  sig,
  chunk("IHDR", ihdr),
  chunk("IDAT", idat),
  chunk("IEND", Buffer.alloc(0)),
]);

const out = new URL("../src/main/resources/assets/futuremod/textures/block/checkers_board.png", import.meta.url);
fs.writeFileSync(out, png);
console.log("Wrote", out.pathname || out);
