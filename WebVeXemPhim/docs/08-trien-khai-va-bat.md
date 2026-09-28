# 08. Triển khai local và file `.bat`

Không Docker Compose (trừ khi giảng viên bắt). `.bat` chỉ mở process.

SOS01: JDK **17 hoặc 21** — BTL dùng **21**. MySQL. Postman **đăng nhập tài khoản cá nhân** mỗi lần test (ảnh nộp phải thấy user góc phải).

## 1. Thứ tự chạy

MySQL 3306 **Running** (`MySQL80` trên Windows).

1. Tạo schema `vexemphim` (hoặc `createDatabaseIfNotExist=true`).
2. `vexemphim-api` (`mvnw.cmd spring-boot:run`) — cổng **8080**
3. `web-client` `npm run dev` — **5173**

Frontend chỉ gọi **8080**.

Nếu CRS/lab SOS khác đang chiếm 8080: tắt app đó hoặc đổi `server.port` BTL (đổi luôn CORS + `VITE_API_BASE_URL`).

## 2. Database

Một schema: `vexemphim`.

`application.properties` **local** (không commit mật khẩu máy):

```
spring.datasource.url=jdbc:mysql://localhost:3306/vexemphim?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
app.api.key=SECRET_KEY_BTL
jwt.secret=...
jwt.expiration-ms=86400000
spring.web.resources.static-locations=classpath:/static/,file:uploads/
```

Không copy password mẫu trong PDF SOS04 vào git.

## 3. `start-all.bat`

Hai chế độ:

| Lệnh | Database | Khi nào dùng |
|---|---|---|
| `start-all.bat` | MySQL 3306, schema `vexemphim` | Bản chính, dữ liệu còn sau khi tắt |
| `start-all.bat h2` | H2 in-memory | MySQL chưa bật / chạy thử nhanh; dữ liệu **reset** mỗi lần chạy |

Script mở 2 cửa sổ (API rồi client) và tự mở http://localhost:5173.

Đợi log `Started VexemphimApplication`. Đóng cửa sổ để dừng. Không cần stop.bat (BTL).

Chạy tay nếu không dùng `.bat`:

```
cd vexemphim-api && mvn spring-boot:run                                   (MySQL)
cd vexemphim-api && mvn spring-boot:run -Dspring-boot.run.profiles=h2     (H2)
cd web-client && npm run dev
```

Cổng 8080 bị chiếm (lab CRS/SOS còn chạy): tìm PID bằng `netstat -ano | findstr :8080` rồi `taskkill /PID <pid> /F`.

## 4. Frontend env

```
VITE_API_BASE_URL=http://localhost:8080
VITE_API_KEY=SECRET_KEY_BTL
```

## 5. JDK / Node

- JDK 21
- Node LTS (Vite + React)

## 6. Ổ đĩa

Tài liệu: `c:\Projects\WebVeXemPhim`. Source nhẹ. Ổ C đầy: chuyển workspace sang `D:\`. MySQL thường vẫn 3306 trên C. Không cài Docker lên C vì BTL.

## 7. Git

Repo **private**. Không nhét CRS/SOS lab vào cùng repo.

Không commit: `.idea`, `node_modules`, `target`, `uploads/*` (ảnh), `.env` có secret.

`.gitignore` khi Pha 0.

## 8. Kiểm tra API sống

Trình duyệt: `GET http://localhost:8080/api/movies` → JSON page.  
Hoặc `/actuator/health` nếu bật.

## 9. Package / groupId (SOS01)

Khi `start.spring.io`: Group = `ten_lop.tenkhongdauXXX.vexemphim`. Ảnh MainApplication phải thấy package này nếu thầy yêu cầu nộp screenshot kiểu lab.
