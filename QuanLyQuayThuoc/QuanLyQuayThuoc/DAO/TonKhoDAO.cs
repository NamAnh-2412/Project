using System;
using System.Data;

namespace QuanLyQuayThuoc.DAO
{
    public class TonKhoDAO
    {
        private static TonKhoDAO instance;
        public static TonKhoDAO Instance
        {
            get { if (instance == null) instance = new TonKhoDAO(); return instance; }
            private set { instance = value; }
        }

        private TonKhoDAO() { }

        public DataTable LayTonKhoTongHop(string tuKhoa, object maDM)
        {
            string query = @"
SELECT 
    TKTH.MaThuoc,
    TKTH.TenThuoc,
    TKTH.TenDanhMuc,
    T.MaDM,
    T.DonViTinh AS DonViLon,
    CASE 
        WHEN ISNULL(T.TyLeQuyDoi, 1) > 1 AND ISNULL(T.DonViQuyDoi, '') <> '' THEN T.DonViQuyDoi
        ELSE TKTH.DonViTinh
    END AS DonViTinh,
    TKTH.TongTonKho,
    T.TyLeQuyDoi
FROM vw_TonKho_TongHop TKTH
LEFT JOIN Thuoc T ON TKTH.MaThuoc = T.MaThuoc
WHERE (@TuKhoa = '' OR TKTH.TenThuoc LIKE '%' + @TuKhoa + '%' OR TKTH.TenDanhMuc LIKE '%' + @TuKhoa + '%')
  AND (@MaDM IS NULL OR T.MaDM = @MaDM)
ORDER BY TKTH.TenThuoc";

            return DataProvider.Instance.ExecuteQuery(query, new object[]
            {
                tuKhoa ?? string.Empty,
                maDM ?? DBNull.Value
            });
        }

        public DataTable LayTonKhoChiTiet(string tuKhoa, object maThuoc = null)
        {
            string query = @"
SELECT 
    TKCT.MaThuoc,
    TKCT.TenThuoc,
    DM.TenDanhMuc,
    TKCT.SoLo,
    TKCT.HanSuDung,
    TKCT.SoLuongTon,
    TKCT.GiaBan,
    T.DonViTinh AS DonViLon,
    CASE 
        WHEN ISNULL(T.TyLeQuyDoi, 1) > 1 AND ISNULL(T.DonViQuyDoi, '') <> '' THEN T.DonViQuyDoi
        ELSE TKCT.DonViTinh
    END AS DonViTinh,
    T.TyLeQuyDoi
FROM vw_TonKho_ChiTiet TKCT
LEFT JOIN Thuoc T ON TKCT.MaThuoc = T.MaThuoc
LEFT JOIN DanhMuc DM ON T.MaDM = DM.MaDM
WHERE (@TuKhoa = '' 
    OR TKCT.TenThuoc LIKE '%' + @TuKhoa + '%' 
    OR TKCT.SoLo LIKE '%' + @TuKhoa + '%' 
    OR DM.TenDanhMuc LIKE '%' + @TuKhoa + '%')
  AND (@MaThuoc IS NULL OR TKCT.MaThuoc = @MaThuoc)
ORDER BY TKCT.TenThuoc, TKCT.HanSuDung";

            return DataProvider.Instance.ExecuteQuery(query, new object[]
            {
                tuKhoa ?? string.Empty,
                maThuoc ?? DBNull.Value
            });
        }

        public DataTable LayCanhBaoHanSuDung(string tuKhoa, int soNgayCanhBao)
        {
            string query = @"
SELECT 
    CB.MaThuoc,
    CB.TenThuoc,
    DM.TenDanhMuc,
    CB.SoLo,
    CB.SoLuongTon,
    CB.HanSuDung,
    CB.SoNgayConLai,
    T.DonViTinh AS DonViLon,
    CASE 
        WHEN ISNULL(T.TyLeQuyDoi, 1) > 1 AND ISNULL(T.DonViQuyDoi, '') <> '' THEN T.DonViQuyDoi
        ELSE T.DonViTinh
    END AS DonViTinh,
    T.TyLeQuyDoi
FROM vw_VuotHanSuDung_CanhBao CB
LEFT JOIN Thuoc T ON CB.MaThuoc = T.MaThuoc
LEFT JOIN DanhMuc DM ON T.MaDM = DM.MaDM
WHERE (@TuKhoa = ''
    OR CB.TenThuoc LIKE '%' + @TuKhoa + '%'
    OR CB.SoLo LIKE '%' + @TuKhoa + '%'
    OR DM.TenDanhMuc LIKE '%' + @TuKhoa + '%')
  AND CB.SoNgayConLai <= @SoNgayCanhBao
ORDER BY CB.SoNgayConLai, CB.HanSuDung";

            return DataProvider.Instance.ExecuteQuery(query, new object[]
            {
                tuKhoa ?? string.Empty,
                soNgayCanhBao
            });
        }

        public DataTable LayDanhSachLoBanHang()
        {
            string query = @"
SELECT 
    TKCT.MaThuoc,
    TKCT.TenThuoc,
    TKCT.SoLo,
    TKCT.HanSuDung,
    TKCT.SoLuongTon,
    TKCT.GiaBan,
    TKCT.DonViTinh,
    T.DonViQuyDoi,
    T.TyLeQuyDoi
FROM vw_TonKho_ChiTiet TKCT
LEFT JOIN Thuoc T ON TKCT.MaThuoc = T.MaThuoc
WHERE TKCT.HanSuDung > CAST(GETDATE() AS DATE)
  AND TKCT.SoLuongTon > 0
ORDER BY TKCT.TenThuoc, TKCT.HanSuDung, TKCT.SoLo";

            return DataProvider.Instance.ExecuteQuery(query);
        }

        public bool LoDaHetHan(int maThuoc, string soLo)
        {
            string query = @"
SELECT COUNT(*) 
FROM TonKho 
WHERE MaThuoc = @MaThuoc AND SoLo = @SoLo AND HanSuDung <= CAST(GETDATE() AS DATE)";

            int soLuong = System.Convert.ToInt32(DataProvider.Instance.ExecuteScalar(query, new object[] { maThuoc, soLo }));
            return soLuong > 0;
        }

        /// <summary>
        /// Thuốc có tổng tồn Đơn vị nhỏ = 0 (theo tổng hợp tồn kho).
        /// </summary>
        public DataTable LayThuocHetHangTonTongHop(string tuKhoa, object maDM)
        {
            string query = @"
SELECT 
    TKTH.MaThuoc,
    TKTH.TenThuoc,
    TKTH.TenDanhMuc,
    T.MaDM,
    T.DonViTinh AS DonViLon,
    CASE 
        WHEN ISNULL(T.TyLeQuyDoi, 1) > 1 AND ISNULL(T.DonViQuyDoi, '') <> '' THEN T.DonViQuyDoi
        ELSE TKTH.DonViTinh
    END AS DonViTinh,
    TKTH.TongTonKho,
    T.TyLeQuyDoi,
    ISNULL(T.SoLuongToiThieu, 10) AS SoLuongToiThieu,
    CAST(ISNULL(T.SoLuongToiThieu, 10) AS BIGINT)
        * CAST(ISNULL(NULLIF(T.TyLeQuyDoi, 0), 1) AS BIGINT) AS NguongToiThieu_DVNho
FROM vw_TonKho_TongHop TKTH
LEFT JOIN Thuoc T ON TKTH.MaThuoc = T.MaThuoc
WHERE (@TuKhoa = '' OR TKTH.TenThuoc LIKE '%' + @TuKhoa + '%' OR TKTH.TenDanhMuc LIKE '%' + @TuKhoa + '%')
  AND (@MaDM IS NULL OR T.MaDM = @MaDM)
  AND TKTH.TongTonKho = 0
ORDER BY TKTH.TenThuoc";

            return DataProvider.Instance.ExecuteQuery(query, new object[]
            {
                tuKhoa ?? string.Empty,
                maDM ?? DBNull.Value
            });
        }

        /// <summary>
        /// Thuốc còn tồn &gt; 0 nhưng tổng tồn (ĐV nhỏ) không vượt ngưỡng tối thiểu (quy đổi sang ĐV nhỏ).
        /// </summary>
        public DataTable LayThuocSapHetHangTonTongHop(string tuKhoa, object maDM)
        {
            string query = @"
SELECT 
    TKTH.MaThuoc,
    TKTH.TenThuoc,
    TKTH.TenDanhMuc,
    T.MaDM,
    T.DonViTinh AS DonViLon,
    CASE 
        WHEN ISNULL(T.TyLeQuyDoi, 1) > 1 AND ISNULL(T.DonViQuyDoi, '') <> '' THEN T.DonViQuyDoi
        ELSE TKTH.DonViTinh
    END AS DonViTinh,
    TKTH.TongTonKho,
    T.TyLeQuyDoi,
    ISNULL(T.SoLuongToiThieu, 10) AS SoLuongToiThieu,
    CAST(ISNULL(T.SoLuongToiThieu, 10) AS BIGINT)
        * CAST(ISNULL(NULLIF(T.TyLeQuyDoi, 0), 1) AS BIGINT) AS NguongToiThieu_DVNho
FROM vw_TonKho_TongHop TKTH
LEFT JOIN Thuoc T ON TKTH.MaThuoc = T.MaThuoc
WHERE (@TuKhoa = '' OR TKTH.TenThuoc LIKE '%' + @TuKhoa + '%' OR TKTH.TenDanhMuc LIKE '%' + @TuKhoa + '%')
  AND (@MaDM IS NULL OR T.MaDM = @MaDM)
  AND TKTH.TongTonKho > 0
  AND TKTH.TongTonKho <= CAST(ISNULL(T.SoLuongToiThieu, 10) AS BIGINT)
      * CAST(ISNULL(NULLIF(T.TyLeQuyDoi, 0), 1) AS BIGINT)
ORDER BY TKTH.TongTonKho ASC, TKTH.TenThuoc";

            return DataProvider.Instance.ExecuteQuery(query, new object[]
            {
                tuKhoa ?? string.Empty,
                maDM ?? DBNull.Value
            });
        }
    }
}
