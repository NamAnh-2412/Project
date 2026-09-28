# 01. Kiến trúc Modular Monolith (hướng dịch vụ)

## 1. Vì sao không làm một website độc lập

Môn học yêu cầu **thiết kế, xây dựng, triển khai và tích hợp dịch vụ**, không phải ứng dụng nhét HTML + SQL trong cùng một luồng render.

Client (React) **không** ghi MySQL. Mọi thao tác đi qua RESTful JSON. Đó là mô hình Client–Server đã làm từ SOS01–SOS02.

## 2. Vì sao Modular Monolith, không tách 4 process

Bản kế hoạch trước bám lab CRS (gateway + 3 service + 3 DB). Bộ **Hướng dẫn Modular Monolith** (SOS01–SOS10) của thầy đi theo hướng khác:

- **Một** ứng dụng Spring Boot, **một** MySQL.
- Tổ chức **package theo nghiệp vụ**, bên trong mỗi nhóm là 3 tầng Controller → Service → Repository → Entity (SOS03, SOS05).
- Client riêng gọi API (SOS08–SOS09), CORS, Postman, API Key (SOS10).

**Modular monolith** = một process triển khai, nhưng **ranh giới module rõ** (auth / catalog / booking). Đủ để báo cáo “hướng dịch vụ”: hợp đồng REST, tách bounded context, hai client một API. Nhẹ hơn microservice (không Gateway, không RestTemplate, không 4 cửa sổ Java).

Hướng mở (chương hạn chế báo cáo): tách module thành service + Gateway khi quy mô tăng — không làm trong BTL.

## 3. Nguyên tắc áp dụng

1. **Tách theo nghiệp vụ, không tách theo tầng kỹ thuật toàn app**  
   Không gom mọi Controller vào một package rồi mọi Entity vào một package kiểu “layer toàn cục” cho dự án lớn. SOS05 cho phép cấu trúc đơn giản với app nhỏ; BTL **gom 3 tầng bên trong từng module**.

2. **Một database, quan hệ JPA trong cùng DB**  
   SOS04–SOS05: `@ManyToOne` / `@OneToMany`, FK MySQL. BTL dùng được vì không còn “database per service”.

3. **Hợp đồng REST + JSON**  
   Client chỉ biết `http://localhost:8080`. Không JDBC từ trình duyệt.

4. **Hai kênh, một nguồn ghế**  
   POS và đặt online là hai client. Trạng thái ghế chỉ module booking quyết.

5. **Cách chạy không phải kiến trúc**  
   `.bat` chỉ mở process. Một JAR monolith vẫn là SOA về mặt hợp đồng API.

6. **Nâng lab, không copy phần “tạm thời”**  
   SOS06 dùng `?role=ADMIN` và mật khẩu plaintext — lab ghi *tạm thời, chưa Spring Security*. BTL dùng **JWT + BCrypt**. SOS10 API Key **giữ** làm lớp bổ sung khi ghi catalog.

## 4. So với SOS và với CRS

| SOS (lab Modular Monolith) | CRS (lab microservice) | BTL WebVeXemPhim |
|---|---|---|
| 1 Spring Boot `:8080` | Gateway + 3 service | **1 Spring Boot `:8080`** (theo SOS) |
| 1 MySQL (`ptpmhdv`) | DB per service | **1 MySQL `vexemphim`** |
| Category 1-N Product | Course / Registration tách DB | Genre 1-N Movie; Room 1-N Seat; Reservation 1-N ghế |
| 3 tầng C–S–R | Mỗi service tự 3 tầng | 3 tầng **trong từng module** |
| `?role=` (tạm) | JWT + filter | **JWT** (nâng SOS06) |
| API Key header | Partner key tùy lab | **X-API-KEY** khi ghi catalog (SOS10) |
| HTML + fetch `:5500` | React + Axios | **React** (hai kênh + SeatGrid; SOS08 không đủ POS) |
| ADMIN / CUSTOMER | ADMIN / STUDENT | ADMIN / CASHIER / CUSTOMER |
| Search, Page, upload | — | Search/page phim, upload poster |
| Product in-memory rồi JPA | — | JPA từ đầu (SOS04) |

Khác SOS: nghiệp vụ **hold ghế timeout** + **unique (showtime, seat)** — tiêu chí “không bán trùng”, nhấn trong báo cáo.

## 5. Sơ đồ hệ thống

```
[ Trình duyệt khách :5173 /          ]
[ Trình duyệt POS    :5173 /pos      ]
[ Trình duyệt Admin  :5173 /admin    ]
                    |
                    | JSON, Bearer JWT
                    | X-API-KEY (ghi catalog)
                    v
            [ vexemphim-api :8080 ]
            |     |        |
            v     v        v
         auth  catalog  booking     ← package/module, cùng process
            \     |        /
             \    |       /
              v   v      v
           MySQL :3306  (schema vexemphim)
           file  uploads/  (poster)
```

Luồng tóm tắt:

- Client gọi `/api/...` trên `:8080`.
- `JwtAuthFilter` đọc Bearer; interceptor API Key trên path ghi catalog.
- Booking **gọi CatalogService** (bean), không HTTP nội bộ.
- Catalog không biết vé. Auth không biết ghế. Booking lưu snapshot tên phim/suất khi confirm để in vé nếu admin sửa phim sau.

## 6. Phân tầng trách nhiệm

### 6.1. Backend (cung cấp dịch vụ)

Một MainApplication. Mỗi module:

- **Controller** — HTTP, mã 200/201/204/400/403/404/409/503, không SQL.
- **Service** — nghiệp vụ (hold, overlap suất, phân quyền nghiệp vụ).
- **Repository** — `JpaRepository`.
- **Entity** — bảng.

`common`: CORS (SOS08), JWT, API Key (SOS10), exception handler, cấu hình upload.

### 6.2. Client (tiêu thụ dịch vụ)

Vite + React, `axios` `baseURL = http://localhost:8080`. Interceptor gắn JWT và `X-API-KEY` khi gọi API ghi catalog. 401 → login.

### 6.3. Tích hợp

- Đồng bộ ghế: cả hai UI `GET` seat-map từ booking.
- Xung đột: request sau **409 `SEAT_TAKEN`**.

## 7. Quyết định phạm vi kiến trúc (BTL)

**Làm**

- 1 process Java + 1 frontend.
- Thanh toán giả lập **trong** module booking.
- Một rạp, vài phòng.
- Thể loại 1-N phim (tương đương Category–Product SOS05).
- Tìm kiếm / sắp xếp / phân trang danh sách phim (SOS07).
- Upload poster (SOS07).

**Không làm trong BTL**

- API Gateway, Eureka, Kafka, config server.
- Payment-service riêng, VNPay thật.
- Docker Compose (trừ khi giảng viên bắt).
- Tách process theo module.

## 8. Cổng cố định (local)

| Process | Cổng |
|---|---|
| vexemphim-api | 8080 |
| React (Vite) | 5173 |
| MySQL | 3306 |

SOS08 dùng Live Server `:5500`. BTL dùng Vite `:5173` — CORS chỉ cần origin này (cộng `127.0.0.1:5173`). Không chạy CRS và BTL cùng lúc nếu CRS đang chiếm 8080.

## 9. Ánh xạ SOS → chỗ làm trong BTL

| Lab | Áp vào BTL |
|---|---|
| SOS01 JDK 17/21, Postman đăng nhập, package MSSV | JDK 21, Postman cá nhân, đổi package khi Pha 0 |
| SOS02 GET/POST, 200/201/404 | Toàn bộ API |
| SOS03 3 tầng, PUT/DELETE, 204 | Mọi module |
| SOS04 JPA, MySQL, ddl-auto=update | Một schema `vexemphim` |
| SOS05 1-N, `@JsonManagedReference` / Back | Genre–Movie, Room–Seat, Reservation–ghế |
| SOS06 role ADMIN ghi, CUSTOMER đọc | JWT + role; thêm CASHIER cho POS |
| SOS07 name/page/size/sort, multipart | GET `/api/movies`, POST upload poster |
| SOS08 CORS, login, list + page | React khách |
| SOS09 admin CRUD + upload | `/admin` React |
| SOS10 `X-API-KEY` | Ghi `/api/genres/**`, `/api/movies` (POST/PUT/DELETE/upload), `/api/showtimes` ghi |
