SET NOCOUNT ON;
SET XACT_ABORT ON;

/*
    Migrate TonKho.SoLuongTon to the smallest unit without changing schema.

    Assumption:
    - Thuoc.GiaBan is still stored per DonViTinh (the larger/base selling unit).
    - Thuoc.DonViQuyDoi / Thuoc.TyLeQuyDoi already exist in the live database.
    - Application code has been updated to:
      + store ChiTietHoaDon.SoLuongBan / ChiTietNhap.SoLuongNhap in the smallest unit
      + display TonKho as smallest unit
      + value stock as GiaBan / TyLeQuyDoi * SoLuongTon

    Safe usage:
    1. Run the PREVIEW section first.
    2. Run the TRANSACTION section.
    3. Review the verification result set.
    4. Replace ROLLBACK with COMMIT only when satisfied.
*/

PRINT '=== PREVIEW AFFECTED ROWS ===';

SELECT COUNT(*) AS SoLoBiAnhHuong
FROM TonKho TK
INNER JOIN Thuoc T ON TK.MaThuoc = T.MaThuoc
WHERE ISNULL(T.TyLeQuyDoi, 1) > 1;

SELECT
    TK.MaThuoc,
    T.TenThuoc,
    TK.SoLo,
    TK.SoLuongTon AS TonHienTai,
    T.DonViTinh,
    T.DonViQuyDoi,
    T.TyLeQuyDoi,
    TK.SoLuongTon * T.TyLeQuyDoi AS TonSauQuyDoi
FROM TonKho TK
INNER JOIN Thuoc T ON TK.MaThuoc = T.MaThuoc
WHERE ISNULL(T.TyLeQuyDoi, 1) > 1
ORDER BY T.TenThuoc, TK.SoLo;

PRINT '=== START TRANSACTION ===';

BEGIN TRAN;

IF OBJECT_ID('tempdb..#TonKhoBackup') IS NOT NULL
    DROP TABLE #TonKhoBackup;

SELECT
    TK.MaThuoc,
    TK.SoLo,
    TK.SoLuongTon AS SoLuongTonCu,
    T.TyLeQuyDoi,
    TK.SoLuongTon * T.TyLeQuyDoi AS SoLuongTonMoi
INTO #TonKhoBackup
FROM TonKho TK
INNER JOIN Thuoc T ON TK.MaThuoc = T.MaThuoc
WHERE ISNULL(T.TyLeQuyDoi, 1) > 1;

UPDATE TK
SET TK.SoLuongTon = B.SoLuongTonMoi
FROM TonKho TK
INNER JOIN #TonKhoBackup B
    ON TK.MaThuoc = B.MaThuoc
   AND TK.SoLo = B.SoLo;

PRINT '=== VERIFY UPDATED ROWS ===';

SELECT
    TK.MaThuoc,
    T.TenThuoc,
    TK.SoLo,
    B.SoLuongTonCu,
    TK.SoLuongTon AS SoLuongTonSauCapNhat,
    T.DonViQuyDoi,
    B.TyLeQuyDoi
FROM TonKho TK
INNER JOIN #TonKhoBackup B
    ON TK.MaThuoc = B.MaThuoc
   AND TK.SoLo = B.SoLo
INNER JOIN Thuoc T
    ON TK.MaThuoc = T.MaThuoc
ORDER BY T.TenThuoc, TK.SoLo;

PRINT '=== VERIFY STOCK VALUE STILL MATCHES ===';

SELECT
    SUM(CAST(B.SoLuongTonCu AS DECIMAL(18, 2)) * CAST(T.GiaBan AS DECIMAL(18, 2))) AS TongGiaTriTruocQuyDoi,
    SUM(CAST(TK.SoLuongTon AS DECIMAL(18, 2)) * (CAST(T.GiaBan AS DECIMAL(18, 2)) / NULLIF(CAST(ISNULL(T.TyLeQuyDoi, 1) AS DECIMAL(18, 2)), 0))) AS TongGiaTriSauQuyDoi
FROM TonKho TK
INNER JOIN #TonKhoBackup B
    ON TK.MaThuoc = B.MaThuoc
   AND TK.SoLo = B.SoLo
INNER JOIN Thuoc T
    ON TK.MaThuoc = T.MaThuoc;

/*
    Default is rollback for safety.
    After reviewing all result sets and confirming they are correct,
    change ROLLBACK TRAN to COMMIT TRAN and run the transaction section again.
*/
ROLLBACK TRAN;
