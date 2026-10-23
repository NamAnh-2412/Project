/**
 * Sinh CineVe.postman_collection.json từ inventory API hiện tại.
 * Chạy: node postman/generate-collection.mjs
 */
import { writeFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = dirname(fileURLToPath(import.meta.url));

const bearer = (tokenVar) => ({
  type: 'bearer',
  bearer: [{ key: 'token', value: `{{${tokenVar}}}`, type: 'string' }],
});

const jsonHeader = [{ key: 'Content-Type', value: 'application/json' }];
const adminWriteHeaders = [
  ...jsonHeader,
  { key: 'X-API-KEY', value: '{{apiKey}}' },
];

const test = (lines) => [
  {
    listen: 'test',
    script: { type: 'text/javascript', exec: lines },
  },
];

const prerequest = (lines) => [
  {
    listen: 'prerequest',
    script: { type: 'text/javascript', exec: lines },
  },
];

const tomorrowScript = (hours = 19, minutes = 0, varName = 'startAtTomorrow') => [
  'const d = new Date();',
  'd.setDate(d.getDate() + 1);',
  `d.setHours(${hours}, ${minutes}, 0, 0);`,
  "const pad = n => String(n).padStart(2, '0');",
  `pm.variables.set('${varName}', \`\${d.getFullYear()}-\${pad(d.getMonth()+1)}-\${pad(d.getDate())}T\${pad(d.getHours())}:\${pad(d.getMinutes())}:00\`);`,
];

function req({ name, method, url, auth, headers, body, events, description }) {
  const r = {
    name,
    request: {
      method,
      header: headers || [],
      url,
    },
  };
  if (auth) r.request.auth = auth;
  if (body !== undefined) {
    r.request.body = typeof body === 'string'
      ? { mode: 'raw', raw: body }
      : body;
  }
  if (description) r.request.description = description;
  if (events) r.event = events;
  return r;
}

function url(raw, query) {
  const path = raw.replace('{{baseUrl}}/', '').split('?')[0].split('/');
  const u = {
    raw,
    host: ['{{baseUrl}}'],
    path,
  };
  if (query) u.query = query;
  return u;
}

const collection = {
  info: {
    _postman_id: 'cineve-btl-collection',
    name: 'CineVe API — BTL',
    description: [
      'Bộ request test toàn bộ API CineVe cho báo cáo.',
      '',
      'Cách dùng:',
      '1. Import file này + CineVe.local.postman_environment.json',
      '2. Chọn environment **CineVe Local**',
      '3. Bật API (start-all.bat hoặc mvn spring-boot:run ở vexemphim-api)',
      '4. Chạy folder **05 Demo báo cáo** từ trên xuống để chụp ảnh',
      '',
      'Auth:',
      '- JWT: Authorization Bearer {{adminToken|customerToken|cashierToken}}',
      '- Ghi catalog (genres/movies/showtimes/rooms/concessions/settings): thêm X-API-KEY={{apiKey}}',
      '',
      'Chi tiết: docs/06-postman.md',
    ].join('\n'),
    schema: 'https://schema.getpostman.com/json/collection/v2.1.0/collection.json',
  },
  variable: [{ key: 'baseUrl', value: 'http://localhost:8080' }],
  item: [],
};

// ─── 00 Auth ───────────────────────────────────────────────
collection.item.push({
  name: '00 Auth',
  item: [
    req({
      name: 'Login Admin',
      method: 'POST',
      headers: jsonHeader,
      body: '{\n  "username": "{{adminUser}}",\n  "password": "{{adminPass}}"\n}',
      url: '{{baseUrl}}/api/auth/login',
      events: test([
        "pm.test('200 login admin', () => pm.response.to.have.status(200));",
        'const j = pm.response.json();',
        "pm.environment.set('adminToken', j.token);",
        "pm.test('có token', () => pm.expect(j.token).to.be.a('string'));",
        "pm.test('role ADMIN', () => pm.expect(j.role).to.eql('ADMIN'));",
      ]),
    }),
    req({
      name: 'Login Customer',
      method: 'POST',
      headers: jsonHeader,
      body: '{\n  "username": "{{customerUser}}",\n  "password": "{{customerPass}}"\n}',
      url: '{{baseUrl}}/api/auth/login',
      events: test([
        "pm.test('200 login customer', () => pm.response.to.have.status(200));",
        "pm.environment.set('customerToken', pm.response.json().token);",
      ]),
    }),
    req({
      name: 'Login Cashier',
      method: 'POST',
      headers: jsonHeader,
      body: '{\n  "username": "{{cashierUser}}",\n  "password": "{{cashierPass}}"\n}',
      url: '{{baseUrl}}/api/auth/login',
      events: test([
        "pm.test('200 login cashier', () => pm.response.to.have.status(200));",
        "pm.environment.set('cashierToken', pm.response.json().token);",
      ]),
    }),
    req({
      name: 'Me (Customer)',
      method: 'GET',
      auth: bearer('customerToken'),
      url: '{{baseUrl}}/api/auth/me',
      events: test([
        "pm.test('200 me', () => pm.response.to.have.status(200));",
        "pm.expect(pm.response.json().role).to.eql('CUSTOMER');",
      ]),
    }),
    req({
      name: 'Register Customer (đổi username/email trước khi gửi)',
      method: 'POST',
      headers: jsonHeader,
      body: '{\n  "username": "demo_postman",\n  "password": "demo1234",\n  "email": "demo_postman@example.com"\n}',
      url: '{{baseUrl}}/api/auth/register',
      description: '201 Created. Username/email trùng → 409. Chỉ tạo role CUSTOMER.',
    }),
    req({
      name: 'Login sai mật khẩu (chụp 401)',
      method: 'POST',
      headers: jsonHeader,
      body: '{\n  "username": "{{customerUser}}",\n  "password": "sai-mat-khau"\n}',
      url: '{{baseUrl}}/api/auth/login',
      events: test([
        'pm.test("401 AUTH_FAILED", () => {',
        '  pm.response.to.have.status(401);',
        "  pm.expect(pm.response.json().code).to.eql('AUTH_FAILED');",
        '});',
      ]),
    }),
  ],
});

// ─── 01 Catalog GET ────────────────────────────────────────
collection.item.push({
  name: '01 Catalog (GET công khai)',
  description: 'GET catalog không cần JWT. Seat-map / booking không nằm ở đây.',
  item: [
    req({
      name: 'List movies SHOWING',
      method: 'GET',
      url: url('{{baseUrl}}/api/movies?status=SHOWING&size=10', [
        { key: 'status', value: 'SHOWING' },
        { key: 'size', value: '10' },
      ]),
      events: test([
        "pm.test('200 movies', () => pm.response.to.have.status(200));",
        'const j = pm.response.json();',
        'const first = (j.content || j)[0];',
        "if (first && first.id) pm.environment.set('movieId', String(first.id));",
        "if (first && first.genreId) pm.environment.set('genreId', String(first.genreId));",
      ]),
    }),
    req({
      name: 'Movie detail',
      method: 'GET',
      url: '{{baseUrl}}/api/movies/{{movieId}}',
    }),
    req({
      name: 'List genres',
      method: 'GET',
      url: '{{baseUrl}}/api/genres',
      events: test([
        "pm.test('200 genres', () => pm.response.to.have.status(200));",
        'const g = pm.response.json()[0];',
        "if (g) pm.environment.set('genreId', String(g.id));",
      ]),
    }),
    req({
      name: 'Genre detail',
      method: 'GET',
      url: '{{baseUrl}}/api/genres/{{genreId}}',
    }),
    req({
      name: 'Movies of genre',
      method: 'GET',
      url: '{{baseUrl}}/api/genres/{{genreId}}/movies',
    }),
    req({
      name: 'List rooms',
      method: 'GET',
      url: '{{baseUrl}}/api/rooms',
      events: test([
        "pm.test('200 rooms', () => pm.response.to.have.status(200));",
        "const rooms = pm.response.json();",
        "const active = rooms.find(r => r.status === 'ACTIVE') || rooms[0];",
        "if (active) pm.environment.set('roomId', String(active.id));",
      ]),
    }),
    req({
      name: 'Room seats layout',
      method: 'GET',
      url: '{{baseUrl}}/api/rooms/{{roomId}}/seats',
    }),
    req({
      name: 'List showtimes (public — ẩn suất đã qua)',
      method: 'GET',
      url: url('{{baseUrl}}/api/showtimes?movieId={{movieId}}', [
        { key: 'movieId', value: '{{movieId}}' },
        { key: 'date', value: '', description: 'yyyy-MM-dd — để trống = hôm nay', disabled: true },
      ]),
      description: 'Khách/anonymous: chỉ suất tương lai, phòng ACTIVE. Folder 05 tự tạo suất nên không bắt buộc request này.',
    }),
    req({
      name: 'Showtime detail',
      method: 'GET',
      url: '{{baseUrl}}/api/showtimes/{{showtimeId}}',
      description: 'Cần showtimeId (từ Create showtime hoặc seed).',
    }),
    req({
      name: 'List concessions',
      method: 'GET',
      url: '{{baseUrl}}/api/concessions',
      events: test([
        "pm.test('200 concessions', () => pm.response.to.have.status(200));",
        'const p = pm.response.json().find(x => x.status === "ACTIVE") || pm.response.json()[0];',
        "if (p) pm.environment.set('productId', String(p.id));",
      ]),
    }),
    req({
      name: 'Cinema settings',
      method: 'GET',
      url: '{{baseUrl}}/api/settings',
    }),
  ],
});

// ─── 02 Admin ──────────────────────────────────────────────
collection.item.push({
  name: '02 Admin (JWT + API Key)',
  description: 'Cần Login Admin trước. Mọi POST/PUT/DELETE catalog bắt buộc X-API-KEY.',
  item: [
    req({
      name: 'Create genre',
      method: 'POST',
      auth: bearer('adminToken'),
      headers: adminWriteHeaders,
      body: '{\n  "name": "Postman Genre"\n}',
      url: '{{baseUrl}}/api/genres',
      events: test([
        'if (pm.response.code === 201) {',
        "  pm.environment.set('genreId', String(pm.response.json().id));",
        '}',
      ]),
    }),
    req({
      name: 'Create movie',
      method: 'POST',
      auth: bearer('adminToken'),
      headers: adminWriteHeaders,
      body: [
        '{',
        '  "title": "Phim Postman Demo",',
        '  "durationMinutes": 110,',
        '  "rated": "C13",',
        '  "description": "Phim tạo từ Postman để test API.",',
        '  "genre": { "id": {{genreId}} },',
        '  "status": "SHOWING",',
        '  "trailerUrl": null,',
        '  "director": "Demo",',
        '  "castNames": "A, B",',
        '  "releaseDate": "2026-01-01"',
        '}',
      ].join('\n'),
      url: '{{baseUrl}}/api/movies',
      events: test([
        "pm.test('201 movie', () => pm.response.to.have.status(201));",
        "pm.environment.set('movieId', String(pm.response.json().id));",
      ]),
      description: 'Có thể dùng movieId seed (1) thay vì tạo mới. Upload poster cần form-data file riêng.',
    }),
    req({
      name: 'Update movie',
      method: 'PUT',
      auth: bearer('adminToken'),
      headers: adminWriteHeaders,
      body: [
        '{',
        '  "title": "Phim Postman Demo",',
        '  "durationMinutes": 112,',
        '  "rated": "C13",',
        '  "description": "Đã cập nhật từ Postman.",',
        '  "genre": { "id": {{genreId}} },',
        '  "status": "SHOWING",',
        '  "trailerUrl": null,',
        '  "director": "Demo",',
        '  "castNames": "A, B",',
        '  "releaseDate": "2026-01-01"',
        '}',
      ].join('\n'),
      url: '{{baseUrl}}/api/movies/{{movieId}}',
    }),
    req({
      name: 'Create room (tuỳ chọn)',
      method: 'POST',
      auth: bearer('adminToken'),
      headers: adminWriteHeaders,
      body: '{\n  "name": "Phòng Postman",\n  "rowCount": 6,\n  "seatsPerRow": 8,\n  "vipRowCount": 1\n}',
      url: '{{baseUrl}}/api/rooms',
      description: 'Seed đã có Phòng 1/2. Chỉ chạy nếu cần phòng mới. Tự sinh ghế.',
      events: test([
        'if (pm.response.code === 201) {',
        "  pm.environment.set('roomId', String(pm.response.json().id));",
        '}',
      ]),
    }),
    req({
      name: 'Update room status',
      method: 'PUT',
      auth: bearer('adminToken'),
      headers: adminWriteHeaders,
      body: '{\n  "name": null,\n  "status": "ACTIVE"\n}',
      url: '{{baseUrl}}/api/rooms/{{roomId}}',
    }),
    req({
      name: 'Create concession',
      method: 'POST',
      auth: bearer('adminToken'),
      headers: adminWriteHeaders,
      body: '{\n  "name": "Combo Postman",\n  "price": 99000,\n  "type": "COMBO",\n  "status": "ACTIVE"\n}',
      url: '{{baseUrl}}/api/concessions',
      events: test([
        'if (pm.response.code === 201) {',
        "  pm.environment.set('productId', String(pm.response.json().id));",
        '}',
      ]),
    }),
    req({
      name: 'Update settings name',
      method: 'PUT',
      auth: bearer('adminToken'),
      headers: adminWriteHeaders,
      body: '{\n  "name": "CineVe"\n}',
      url: '{{baseUrl}}/api/settings',
    }),
    req({
      name: 'Create showtime (ngày mai 19:00)',
      method: 'POST',
      auth: bearer('adminToken'),
      headers: adminWriteHeaders,
      body: '{\n  "movieId": {{movieId}},\n  "roomId": {{roomId}},\n  "startAt": "{{startAtTomorrow}}",\n  "basePrice": 90000\n}',
      url: '{{baseUrl}}/api/showtimes',
      description: 'Cần movie SHOWING + room ACTIVE. 409 SHOWTIME_OVERLAP → đổi roomId hoặc giờ.',
      events: [
        ...prerequest(tomorrowScript(19, 0)),
        ...test([
          "pm.test('201 tạo suất', () => pm.response.to.have.status(201));",
          "pm.environment.set('showtimeId', String(pm.response.json().id));",
        ]),
      ],
    }),
    req({
      name: 'Create showtime thiếu API Key (chụp 401)',
      method: 'POST',
      auth: bearer('adminToken'),
      headers: jsonHeader,
      body: '{\n  "movieId": {{movieId}},\n  "roomId": {{roomId}},\n  "startAt": "{{startAtDayAfter}}",\n  "basePrice": 90000\n}',
      url: '{{baseUrl}}/api/showtimes',
      events: [
        ...prerequest(tomorrowScript(20, 0, 'startAtDayAfter').map((l) =>
          l.includes('getDate() + 1') ? l.replace('+ 1', '+ 2') : l,
        )),
        ...test([
          'pm.test("401 thiếu API key", () => {',
          '  pm.response.to.have.status(401);',
          "  pm.expect(pm.response.json().code).to.eql('API_KEY_INVALID');",
          '});',
        ]),
      ],
    }),
    req({
      name: 'Admin showtimes includePast',
      method: 'GET',
      auth: bearer('adminToken'),
      url: url('{{baseUrl}}/api/showtimes?movieId={{movieId}}&includePast=true', [
        { key: 'movieId', value: '{{movieId}}' },
        { key: 'includePast', value: 'true' },
      ]),
      description: 'Chỉ ADMIN + includePast=true mới thấy suất đã bắt đầu trong ngày, kèm occupancy.',
    }),
    req({
      name: 'Update showtime',
      method: 'PUT',
      auth: bearer('adminToken'),
      headers: adminWriteHeaders,
      body: '{\n  "movieId": {{movieId}},\n  "roomId": {{roomId}},\n  "startAt": "{{startAtTomorrow}}",\n  "basePrice": 95000\n}',
      url: '{{baseUrl}}/api/showtimes/{{showtimeId}}',
      description: 'Đã có vé/hold → 409 HAS_TICKETS / HAS_HOLDS. Chạy trước khi hold.',
      events: prerequest(tomorrowScript(19, 15)),
    }),
    req({
      name: 'Cancel showtime (soft CANCELLED)',
      method: 'DELETE',
      auth: bearer('adminToken'),
      headers: [{ key: 'X-API-KEY', value: '{{apiKey}}' }],
      url: '{{baseUrl}}/api/showtimes/{{showtimeId}}',
      description: 'Không xóa cứng. status=CANCELLED. Có vé → 409 HAS_TICKETS. Chỉ chạy trên suất demo không còn dùng.',
    }),
    req({
      name: 'Create voucher',
      method: 'POST',
      auth: bearer('adminToken'),
      headers: jsonHeader,
      body: [
        '{',
        '  "code": "{{voucherCode}}",',
        '  "discountType": "AMOUNT",',
        '  "discountValue": 20000,',
        '  "minOrderAmount": 50000,',
        '  "maxDiscountAmount": null,',
        '  "usageLimit": 50,',
        '  "startAt": null,',
        '  "endAt": null',
        '}',
      ].join('\n'),
      url: '{{baseUrl}}/api/vouchers',
      description: 'Không cần X-API-KEY. Code trùng → 409 — đổi voucherCode trong environment.',
      events: test([
        'if (pm.response.code === 201) {',
        '  const j = pm.response.json();',
        "  if (j.code) pm.environment.set('voucherCode', j.code);",
        '}',
      ]),
    }),
    req({
      name: 'List vouchers',
      method: 'GET',
      auth: bearer('adminToken'),
      url: '{{baseUrl}}/api/vouchers',
    }),
    req({
      name: 'Revenue report',
      method: 'GET',
      auth: bearer('adminToken'),
      url: url('{{baseUrl}}/api/reports/revenue?grain=DAY', [
        { key: 'grain', value: 'DAY' },
        { key: 'from', value: '', disabled: true },
        { key: 'to', value: '', disabled: true },
      ]),
      events: test(["pm.test('200 revenue', () => pm.response.to.have.status(200));"]),
    }),
  ],
});

// ─── 03 Booking ONLINE ─────────────────────────────────────
collection.item.push({
  name: '03 Booking ONLINE',
  description: 'Luồng khách: seat-map → hold → (voucher/bắp) → confirm ONLINE_MOCK → my tickets.',
  item: [
    req({
      name: 'Seat map',
      method: 'GET',
      auth: bearer('customerToken'),
      url: '{{baseUrl}}/api/bookings/showtimes/{{showtimeId}}/seat-map',
      description: 'Cần JWT. Script lưu ghế AVAILABLE đầu tiên vào seatId.',
      events: test([
        "pm.test('200 seat-map', () => pm.response.to.have.status(200));",
        'const seats = pm.response.json().seats || [];',
        "const free = seats.find(s => s.status === 'AVAILABLE');",
        "pm.test('còn ghế trống', () => pm.expect(free, 'hết ghế AVAILABLE').to.be.ok);",
        "if (free) pm.environment.set('seatId', String(free.seatId));",
      ]),
    }),
    req({
      name: 'Hold seats ONLINE',
      method: 'POST',
      auth: bearer('customerToken'),
      headers: jsonHeader,
      body: '{\n  "showtimeId": {{showtimeId}},\n  "seatIds": [{{seatId}}],\n  "channel": "ONLINE"\n}',
      url: '{{baseUrl}}/api/bookings/hold',
      events: test([
        "pm.test('201 hold', () => pm.response.to.have.status(201));",
        'const j = pm.response.json();',
        "pm.environment.set('reservationId', String(j.reservationId));",
      ]),
    }),
    req({
      name: 'Hold trùng ghế (chụp 409 SEAT_TAKEN)',
      method: 'POST',
      auth: bearer('cashierToken'),
      headers: jsonHeader,
      body: '{\n  "showtimeId": {{showtimeId}},\n  "seatIds": [{{seatId}}],\n  "channel": "POS",\n  "guestName": "Khách walk-in"\n}',
      url: '{{baseUrl}}/api/bookings/hold',
      description: 'Chạy sau Hold ONLINE cùng ghế. Login Cashier trước.',
      events: test([
        'pm.test("409 SEAT_TAKEN", () => {',
        '  pm.response.to.have.status(409);',
        "  pm.expect(pm.response.json().code).to.eql('SEAT_TAKEN');",
        '});',
      ]),
    }),
    req({
      name: 'Set concessions (tuỳ chọn)',
      method: 'PUT',
      auth: bearer('customerToken'),
      headers: jsonHeader,
      body: '{\n  "items": [\n    { "productId": {{productId}}, "qty": 1 }\n  ]\n}',
      url: '{{baseUrl}}/api/bookings/{{reservationId}}/concessions',
      description: 'Chạy List concessions trước để có productId. qty=0 để gỡ món.',
    }),
    req({
      name: 'Apply voucher (tuỳ chọn)',
      method: 'POST',
      auth: bearer('customerToken'),
      headers: jsonHeader,
      body: '{\n  "code": "{{voucherCode}}"\n}',
      url: '{{baseUrl}}/api/bookings/{{reservationId}}/apply-voucher',
    }),
    req({
      name: 'Mock QR PNG',
      method: 'GET',
      auth: bearer('customerToken'),
      url: '{{baseUrl}}/api/bookings/{{reservationId}}/mock-qr',
      description: 'Trả image/png. Postman → Body → Preview.',
    }),
    req({
      name: 'Create payment link PayOS (tuỳ chọn)',
      method: 'POST',
      auth: bearer('customerToken'),
      url: '{{baseUrl}}/api/bookings/{{reservationId}}/create-payment-link',
      description: 'Cần cấu hình PayOS. Báo cáo demo dùng ONLINE_MOCK — có thể bỏ qua.',
    }),
    req({
      name: 'Confirm ONLINE_MOCK',
      method: 'POST',
      auth: bearer('customerToken'),
      headers: jsonHeader,
      body: '{\n  "paymentMethod": "ONLINE_MOCK"\n}',
      url: '{{baseUrl}}/api/bookings/{{reservationId}}/confirm',
      events: test([
        "pm.test('200 confirm', () => pm.response.to.have.status(200));",
        'const j = pm.response.json();',
        "pm.expect(j.status).to.eql('CONFIRMED');",
        'const t = (j.tickets || [])[0];',
        "if (t) pm.environment.set('ticketCode', t.ticketCode);",
      ]),
    }),
    req({
      name: 'Get booking by id',
      method: 'GET',
      auth: bearer('customerToken'),
      url: '{{baseUrl}}/api/bookings/{{reservationId}}',
    }),
    req({
      name: 'My tickets',
      method: 'GET',
      auth: bearer('customerToken'),
      url: '{{baseUrl}}/api/bookings/my',
    }),
    req({
      name: 'Cancel hold (chỉ khi còn HOLD)',
      method: 'DELETE',
      auth: bearer('customerToken'),
      url: '{{baseUrl}}/api/bookings/{{reservationId}}',
      description: '204 khi còn HOLD. Đã confirm rồi thì bỏ qua.',
    }),
  ],
});

// ─── 04 POS + Soát vé ──────────────────────────────────────
collection.item.push({
  name: '04 POS + Soát vé',
  description: 'Thu ngân: hold POS → confirm-cash → lookup → check-in. Cần ghế AVAILABLE khác ghế đã bán ở folder 03.',
  item: [
    req({
      name: 'Hold seats POS (tự lấy ghế trống)',
      method: 'POST',
      auth: bearer('cashierToken'),
      headers: jsonHeader,
      body: '{\n  "showtimeId": {{showtimeId}},\n  "seatIds": [{{seatId}}],\n  "channel": "POS",\n  "guestName": "Khách quầy"\n}',
      url: '{{baseUrl}}/api/bookings/hold',
      description: 'Prerequest gọi seat-map và chọn ghế AVAILABLE mới.',
      events: [
        ...prerequest([
          "const base = pm.environment.get('baseUrl') || pm.collectionVariables.get('baseUrl');",
          "const showtimeId = pm.environment.get('showtimeId');",
          "const token = pm.environment.get('cashierToken');",
          'const res = await pm.sendRequest({',
          "  url: `${base}/api/bookings/showtimes/${showtimeId}/seat-map`,",
          '  method: "GET",',
          '  header: { Authorization: `Bearer ${token}` }',
          '});',
          'const seats = res.json().seats || [];',
          "const free = seats.find(s => s.status === 'AVAILABLE');",
          "if (!free) throw new Error('Không còn ghế AVAILABLE cho POS');",
          "pm.environment.set('seatId', String(free.seatId));",
        ]),
        ...test([
          "pm.test('201 hold POS', () => pm.response.to.have.status(201));",
          "pm.environment.set('reservationId', String(pm.response.json().reservationId));",
        ]),
      ],
    }),
    req({
      name: 'Confirm cash',
      method: 'POST',
      auth: bearer('cashierToken'),
      headers: jsonHeader,
      body: '{}',
      url: '{{baseUrl}}/api/pos/bookings/{{reservationId}}/confirm-cash',
      events: test([
        "pm.test('200 cash', () => pm.response.to.have.status(200));",
        'const t = (pm.response.json().tickets || [])[0];',
        "if (t) pm.environment.set('ticketCode', t.ticketCode);",
      ]),
    }),
    req({
      name: 'Lookup ticket',
      method: 'GET',
      auth: bearer('cashierToken'),
      url: '{{baseUrl}}/api/tickets/{{ticketCode}}',
    }),
    req({
      name: 'Check-in ticket',
      method: 'POST',
      auth: bearer('cashierToken'),
      url: '{{baseUrl}}/api/tickets/{{ticketCode}}/check-in',
      events: test([
        "pm.test('200 check-in', () => pm.response.to.have.status(200));",
        "pm.expect(pm.response.json().status).to.eql('CHECKED_IN');",
      ]),
    }),
    req({
      name: 'Check-in lần 2 (chụp 409 ALREADY_CHECKED_IN)',
      method: 'POST',
      auth: bearer('cashierToken'),
      url: '{{baseUrl}}/api/tickets/{{ticketCode}}/check-in',
      events: test([
        'pm.test("409 ALREADY_CHECKED_IN", () => {',
        '  pm.response.to.have.status(409);',
        "  pm.expect(pm.response.json().code).to.eql('ALREADY_CHECKED_IN');",
        '});',
      ]),
    }),
    req({
      name: 'Occupancy',
      method: 'GET',
      auth: bearer('cashierToken'),
      url: url('{{baseUrl}}/api/bookings/occupancy?showtimeId={{showtimeId}}', [
        { key: 'showtimeId', value: '{{showtimeId}}' },
      ]),
    }),
  ],
});

// ─── 05 Demo báo cáo ───────────────────────────────────────
collection.item.push({
  name: '05 Demo báo cáo (chạy lần lượt)',
  description: 'Thứ tự chụp ảnh báo cáo. Mỗi request xanh ≈ một screenshot. Chạy A0→A8.',
  item: [
    req({
      name: 'A0 List movies + rooms (chuẩn bị id)',
      method: 'GET',
      url: url('{{baseUrl}}/api/movies?status=SHOWING&size=5', [
        { key: 'status', value: 'SHOWING' },
        { key: 'size', value: '5' },
      ]),
      events: test([
        "pm.test('200 movies', () => pm.response.to.have.status(200));",
        'const first = (pm.response.json().content || [])[0];',
        "pm.test('có phim SHOWING', () => pm.expect(first).to.be.ok);",
        "pm.environment.set('movieId', String(first.id));",
        "if (first.genreId) pm.environment.set('genreId', String(first.genreId));",
        "const base = pm.environment.get('baseUrl') || 'http://localhost:8080';",
        'const res = await pm.sendRequest(`${base}/api/rooms`);',
        "pm.test('200 rooms', () => pm.expect(res.code).to.eql(200));",
        "const active = res.json().find(r => r.status === 'ACTIVE') || res.json()[0];",
        "pm.environment.set('roomId', String(active.id));",
      ]),
    }),
    req({
      name: 'A1 Login Admin',
      method: 'POST',
      headers: jsonHeader,
      body: '{\n  "username": "{{adminUser}}",\n  "password": "{{adminPass}}"\n}',
      url: '{{baseUrl}}/api/auth/login',
      events: test([
        "pm.test('200', () => pm.response.to.have.status(200));",
        "pm.environment.set('adminToken', pm.response.json().token);",
      ]),
    }),
    req({
      name: 'A2 Create showtime ngày mai',
      method: 'POST',
      auth: bearer('adminToken'),
      headers: adminWriteHeaders,
      body: '{\n  "movieId": {{movieId}},\n  "roomId": {{roomId}},\n  "startAt": "{{startAtTomorrow}}",\n  "basePrice": 90000\n}',
      url: '{{baseUrl}}/api/showtimes',
      events: [
        ...prerequest(tomorrowScript(19, 30)),
        ...test([
          "pm.test('201', () => pm.response.to.have.status(201));",
          "pm.environment.set('showtimeId', String(pm.response.json().id));",
        ]),
      ],
    }),
    req({
      name: 'A3 Login Customer',
      method: 'POST',
      headers: jsonHeader,
      body: '{\n  "username": "{{customerUser}}",\n  "password": "{{customerPass}}"\n}',
      url: '{{baseUrl}}/api/auth/login',
      events: test([
        "pm.test('200', () => pm.response.to.have.status(200));",
        "pm.environment.set('customerToken', pm.response.json().token);",
      ]),
    }),
    req({
      name: 'A4 Seat map + lấy ghế trống',
      method: 'GET',
      auth: bearer('customerToken'),
      url: '{{baseUrl}}/api/bookings/showtimes/{{showtimeId}}/seat-map',
      events: test([
        'const seats = pm.response.json().seats || [];',
        "const free = seats.find(s => s.status === 'AVAILABLE');",
        'pm.test("còn ghế", () => pm.expect(free).to.be.ok);',
        "pm.environment.set('seatId', String(free.seatId));",
      ]),
    }),
    req({
      name: 'A5 Hold ONLINE',
      method: 'POST',
      auth: bearer('customerToken'),
      headers: jsonHeader,
      body: '{\n  "showtimeId": {{showtimeId}},\n  "seatIds": [{{seatId}}],\n  "channel": "ONLINE"\n}',
      url: '{{baseUrl}}/api/bookings/hold',
      events: test([
        "pm.test('201', () => pm.response.to.have.status(201));",
        "pm.environment.set('reservationId', String(pm.response.json().reservationId));",
      ]),
    }),
    req({
      name: 'A6 Confirm ONLINE_MOCK',
      method: 'POST',
      auth: bearer('customerToken'),
      headers: jsonHeader,
      body: '{\n  "paymentMethod": "ONLINE_MOCK"\n}',
      url: '{{baseUrl}}/api/bookings/{{reservationId}}/confirm',
      events: test([
        'pm.test("CONFIRMED", () => {',
        '  pm.response.to.have.status(200);',
        '  const j = pm.response.json();',
        "  pm.expect(j.status).to.eql('CONFIRMED');",
        "  pm.environment.set('ticketCode', j.tickets[0].ticketCode);",
        '});',
      ]),
    }),
    req({
      name: 'A7 Login Cashier',
      method: 'POST',
      headers: jsonHeader,
      body: '{\n  "username": "{{cashierUser}}",\n  "password": "{{cashierPass}}"\n}',
      url: '{{baseUrl}}/api/auth/login',
      events: test([
        "pm.test('200', () => pm.response.to.have.status(200));",
        "pm.environment.set('cashierToken', pm.response.json().token);",
      ]),
    }),
    req({
      name: 'A8 Check-in',
      method: 'POST',
      auth: bearer('cashierToken'),
      url: '{{baseUrl}}/api/tickets/{{ticketCode}}/check-in',
      events: test([
        'pm.test("CHECKED_IN", () => {',
        '  pm.response.to.have.status(200);',
        "  pm.expect(pm.response.json().status).to.eql('CHECKED_IN');",
        '});',
      ]),
    }),
  ],
});

// ─── 06 Ảnh lỗi gợi ý ──────────────────────────────────────
collection.item.push({
  name: '06 Ảnh lỗi (chụp thêm)',
  description: 'Các case âm tính nên có trong báo cáo. Chạy sau khi đã login đủ role / có dữ liệu từ folder 03–05.',
  item: [
    req({
      name: 'E1 Login sai mật khẩu → 401 AUTH_FAILED',
      method: 'POST',
      headers: jsonHeader,
      body: '{\n  "username": "{{customerUser}}",\n  "password": "sai-mat-khau"\n}',
      url: '{{baseUrl}}/api/auth/login',
      events: test([
        'pm.test("401 AUTH_FAILED", () => {',
        '  pm.response.to.have.status(401);',
        "  pm.expect(pm.response.json().code).to.eql('AUTH_FAILED');",
        '});',
      ]),
    }),
    req({
      name: 'E2 Create showtime thiếu API Key → 401',
      method: 'POST',
      auth: bearer('adminToken'),
      headers: jsonHeader,
      body: '{\n  "movieId": {{movieId}},\n  "roomId": {{roomId}},\n  "startAt": "{{startAtDayAfter}}",\n  "basePrice": 90000\n}',
      url: '{{baseUrl}}/api/showtimes',
      events: [
        ...prerequest(tomorrowScript(21, 0, 'startAtDayAfter').map((l) =>
          l.includes('getDate() + 1') ? l.replace('+ 1', '+ 3') : l,
        )),
        ...test([
          'pm.test("401 API_KEY_INVALID", () => {',
          '  pm.response.to.have.status(401);',
          "  pm.expect(pm.response.json().code).to.eql('API_KEY_INVALID');",
          '});',
        ]),
      ],
    }),
    req({
      name: 'E3 Hold trùng ghế → 409 SEAT_TAKEN',
      method: 'POST',
      auth: bearer('cashierToken'),
      headers: jsonHeader,
      body: '{\n  "showtimeId": {{showtimeId}},\n  "seatIds": [{{seatId}}],\n  "channel": "POS",\n  "guestName": "Trùng ghế"\n}',
      url: '{{baseUrl}}/api/bookings/hold',
      description: 'Cần ghế đang HOLD/BOOKED (sau A5 hoặc Hold ONLINE).',
      events: test([
        'pm.test("409 SEAT_TAKEN", () => {',
        '  pm.response.to.have.status(409);',
        "  pm.expect(pm.response.json().code).to.eql('SEAT_TAKEN');",
        '});',
      ]),
    }),
    req({
      name: 'E4 Check-in lần 2 → 409 ALREADY_CHECKED_IN',
      method: 'POST',
      auth: bearer('cashierToken'),
      url: '{{baseUrl}}/api/tickets/{{ticketCode}}/check-in',
      description: 'Chạy sau A8 Check-in.',
      events: test([
        'pm.test("409 ALREADY_CHECKED_IN", () => {',
        '  pm.response.to.have.status(409);',
        "  pm.expect(pm.response.json().code).to.eql('ALREADY_CHECKED_IN');",
        '});',
      ]),
    }),
  ],
});

const out = join(__dirname, 'CineVe.postman_collection.json');
writeFileSync(out, JSON.stringify(collection, null, 2) + '\n', 'utf8');

function countReqs(items) {
  let n = 0;
  for (const it of items) {
    if (it.item) n += countReqs(it.item);
    else n += 1;
  }
  return n;
}
console.log(`Wrote ${out}`);
console.log(`Folders: ${collection.item.length}, Requests: ${countReqs(collection.item)}`);
