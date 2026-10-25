# Nang cap ton kho theo don vi nho nhat

## 0. Lich su cap nhat

### 2026-04-27

- hoan tat logic ton kho theo don vi nho nhat
- da chay migration du lieu ton kho that tren `QuayThuocDB`
- bo sung hien thi `Ton quy doi` tren man hinh ton kho
- cap nhat ban hang de tu dong tinh `Can thu`
- xu ly loi trung `thuoc + so lo` khi luu hoa don
- dong bo format tien tren ban hang, nhap kho, ton kho, bao cao va khach hang

## 1. Muc tieu

Dot nang cap nay chuyen logic ton kho cua he thong sang nguyen tac luu tru va tinh toan theo don vi nho nhat, khong thay doi cau truc bang du lieu.

Muc tieu chinh:

- tru ton kho chinh xac theo tung don vi nho
- giu nguyen gia tri tien hang trong kho sau khi quy doi
- khong them xoa cot trong database
- dong bo logic ban hang, nhap kho, ton kho, bao cao, canh bao

## 2. Nguyen tac sau nang cap

- `TonKho.SoLuongTon` duoc hieu la so luong theo don vi nho nhat
- `Thuoc.GiaBan` van duoc hieu la gia theo `DonViTinh`
- neu thuoc co `TyLeQuyDoi > 1` thi cac man hinh tinh gia tri ton kho phai dung:

`Gia theo DV nho = GiaBan / TyLeQuyDoi`

- cac nghiep vu luu chi tiet ban hang va nhap kho phai quy doi ve don vi nho truoc khi ghi xuong database

## 3. Cac file code da duoc cap nhat

### Ban hang

- `QuanLyQuayThuoc/GUI/FormBanHang.cs`
- `QuanLyQuayThuoc/DTO/ChiTietHoaDonDTO.cs`
- `QuanLyQuayThuoc/DAO/HoaDonDAO.cs`

Noi dung da cap nhat:

- hien thi ton kho theo huong `Ton (DV nho)`
- quy doi `SoLuongBan` va `DonGiaBan` ve don vi nho truoc khi luu
- luu them thong tin hien thi de phuc hoi `don tam` dung don vi, so luong va don gia
- bo sung thong tin `DonViTinh` cho lich su chi tiet hoa don
- tu dong gop cac dong trung `thuoc + so lo` truoc khi luu de tranh loi khoa chinh trong `ChiTietHoaDon`
- tu dong tinh `Can thu = Tong tien - Giam gia`, khong can nhap tay
- doi nhan `Thanh toan` thanh `Can thu` de phu hop nghiep vu
- bo sung format tien co dau phan cach hang nghin o phan ban hang va lich su hoa don

### Nhap kho

- `QuanLyQuayThuoc/GUI/FormNhapKho.cs`
- `QuanLyQuayThuoc/GUI/FormNhapKho.Designer.cs`
- `QuanLyQuayThuoc/DAO/PhieuNhapDAO.cs`
- `QuanLyQuayThuoc/DAO/ThuocDAO.cs`

Noi dung da cap nhat:

- bo sung thong tin `DonViTinh`, `DonViQuyDoi`, `TyLeQuyDoi` khi chon thuoc nhap
- them cot don vi nhap tren luoi
- quy doi `SoLuongNhap` va `DonGiaNhap` ve don vi nho truoc khi luu
- bo sung thong tin don vi cho lich su chi tiet phieu nhap
- bo sung format tien cho tong tien, da thanh toan, lich su phieu nhap va chi tiet phieu nhap

### Ton kho va canh bao

- `QuanLyQuayThuoc/DAO/TonKhoDAO.cs`
- `QuanLyQuayThuoc/GUI/FormTonKho.cs`
- `QuanLyQuayThuoc/DAO/CanhBaoHanDungDAO.cs`
- `QuanLyQuayThuoc/BUS/CanhBaoHanDungBUS.cs`

Noi dung da cap nhat:

- bo sung `TyLeQuyDoi` cho cac truy van ton kho chi tiet, tong hop, canh bao
- hien thi don vi uu tien `DonViQuyDoi` khi co quy doi
- doi nhan cot ton kho thanh `Tong ton (DV nho)` / `Ton (DV nho)`
- bo sung cot `Ton quy doi` de hien thi theo kieu de doc, vi du `10 Hop 2 Vi`
- tinh tong gia tri ton kho theo cong thuc:

`SoLuongTon * (GiaBan / TyLeQuyDoi)`

- thong bao canh bao han dung ghi ro ton theo don vi nho
- bo sung format tien cho tong gia tri ton kho va cot gia ban

### Bao cao

- `QuanLyQuayThuoc/DAO/BaoCaoDAO.cs`
- `QuanLyQuayThuoc/GUI/FormBaoCaoDoanhThu.cs`

Noi dung da cap nhat:

- bo sung cot `DonViTinh` cho bao cao thuoc ban chay
- giup doc dung y nghia `TongSoLuongBan` sau khi chuyen sang don vi nho
- bo sung format tien cho tong doanh thu va cac cot tien trong bao cao

### Khach hang

- `QuanLyQuayThuoc/GUI/FormKhachHang.cs`

Noi dung da cap nhat:

- bo sung format tien cho thong ke mua hang va lich su mua hang

## 4. Script migration du lieu

Da tao file:

- `migrate_tonkho_to_smallest_unit.sql`

Vai tro cua file:

- preview cac dong ton kho bi anh huong
- tao backup tam bang `#TonKhoBackup`
- cap nhat so luong ton kho sang don vi nho
- verify ket qua sau cap nhat
- so sanh tong gia tri ton truoc va sau quy doi

## 5. Du lieu database da duoc cap nhat that

Database dang ket noi:

- `localhost`
- `QuayThuocDB`

Da cap nhat du lieu ton kho that tren database:

- `Berberin`, so lo `2`: `10 -> 50`
- `betadine`, so lo `3`: `12 -> 24`

Ket qua kiem tra gia tri ton kho:

- tong gia tri truoc quy doi: `5180000`
- tong gia tri sau quy doi: `5180000`

Ket luan:

- viec quy doi du lieu khong lam thay doi tong gia tri ton kho

## 6. Kiem tra ky thuat da thuc hien

Da build lai solution:

- `QuanLyQuayThuoc.slnx`

Ket qua:

- build thanh cong
- `0 Warning(s)`
- `0 Error(s)`

Luu y:

- trong mot lan build truoc do, file `QuanLyQuayThuoc.exe` tung bi khoa do ung dung dang mo
- sau khi dong app va build lai, ket qua da thanh cong

## 7. Checklist nghiep vu can test lai

- ban 1 don vi nho cua thuoc co quy doi, ton kho giam dung 1
- ban 1 don vi lon cua thuoc co quy doi, ton kho giam dung theo ty le
- nhap kho theo don vi lon, du lieu luu xuong database duoc quy doi ve don vi nho
- kiem tra tong gia tri ton kho khong bi doi phong sai
- kiem tra cot `Ton quy doi` hien thi dung theo quy tac lon + le
- kiem tra lich su hoa don va lich su phieu nhap hien thi de hieu
- kiem tra bao cao thuoc ban chay doc dung don vi
- kiem tra thong bao canh bao han dung khong gay nham lan don vi
- kiem tra khi chon trung cung mot `thuoc + so lo` trong hoa don thi he thong van luu duoc va khong bao loi trung khoa
- kiem tra `Giam gia` va `Can thu` tu dong cap nhat va hien thi dung format tien

## 8. Ghi chu quan trong

- Dot nang cap nay chua tao `git commit` vi thu muc lam viec hien tai khong co `.git` va shell khong goi duoc `git`
- File Markdown nay duoc tao de dong vai tro moc tai lieu cho dot thay doi
- Neu sau nay dua du an vao repo git, nen tao mot commit rieng cho dot nang cap ton kho nay
