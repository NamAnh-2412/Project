using System;
using System.Data;

namespace QuanLyQuayThuoc.DAO
{
    public class BaoCaoDAO
    {
        private static BaoCaoDAO instance;
        public static BaoCaoDAO Instance
        {
            get { if (instance == null) instance = new BaoCaoDAO(); return instance; }
            private set { instance = value; }
        }

        private BaoCaoDAO() { }

        public DataTable LayChiTietHoaDonTheoNgay(DateTime tuNgay, DateTime denNgay)
        {
            string query = @"
SELECT 
    HD.MaHD,
    HD.NgayBan,
    NV.HoTen,
    KH.TenKH,
    HD.TongTien,
    HD.GiamGia,
    HD.ThanhToan,
    HD.GhiChu
FROM HoaDon HD
LEFT JOIN NhanVien NV ON HD.MaNV = NV.MaNV
LEFT JOIN KhachHang KH ON HD.MaKH = KH.MaKH
WHERE CAST(HD.NgayBan AS DATE) >= @TuNgay
  AND CAST(HD.NgayBan AS DATE) <= @DenNgay
ORDER BY HD.NgayBan DESC, HD.MaHD DESC";

            return DataProvider.Instance.ExecuteQuery(query, new object[]
            {
                tuNgay.Date,
                denNgay.Date
            });
        }

        public object LayTongDoanhThu(DateTime tuNgay, DateTime denNgay)
        {
            string query = @"
SELECT ISNULL(SUM(TongTien - GiamGia), 0)
FROM HoaDon
WHERE CAST(NgayBan AS DATE) >= @TuNgay
  AND CAST(NgayBan AS DATE) <= @DenNgay";

            return DataProvider.Instance.ExecuteScalar(query, new object[]
            {
                tuNgay.Date,
                denNgay.Date
            });
        }

        public object LaySoHoaDon(DateTime tuNgay, DateTime denNgay)
        {
            string query = @"
SELECT COUNT(*)
FROM HoaDon
WHERE CAST(NgayBan AS DATE) >= @TuNgay
  AND CAST(NgayBan AS DATE) <= @DenNgay";

            return DataProvider.Instance.ExecuteScalar(query, new object[]
            {
                tuNgay.Date,
                denNgay.Date
            });
        }

        public DataTable LayThuocBanChay(DateTime tuNgay, DateTime denNgay)
        {
            string query = @"
SELECT TOP 10
    CTHD.MaThuoc,
    T.TenThuoc,
    CASE 
        WHEN ISNULL(T.TyLeQuyDoi, 1) > 1 AND ISNULL(T.DonViQuyDoi, '') <> '' THEN T.DonViQuyDoi
        ELSE T.DonViTinh
    END AS DonViTinh,
    SUM(CTHD.SoLuongBan) AS TongSoLuongBan,
    SUM(CTHD.ThanhTien) AS TongDoanhThu
FROM ChiTietHoaDon CTHD
INNER JOIN HoaDon HD ON CTHD.MaHD = HD.MaHD
INNER JOIN Thuoc T ON CTHD.MaThuoc = T.MaThuoc
WHERE CAST(HD.NgayBan AS DATE) >= @TuNgay
  AND CAST(HD.NgayBan AS DATE) <= @DenNgay
GROUP BY CTHD.MaThuoc, T.TenThuoc,
    CASE 
        WHEN ISNULL(T.TyLeQuyDoi, 1) > 1 AND ISNULL(T.DonViQuyDoi, '') <> '' THEN T.DonViQuyDoi
        ELSE T.DonViTinh
    END
ORDER BY TongSoLuongBan DESC, TongDoanhThu DESC";

            return DataProvider.Instance.ExecuteQuery(query, new object[]
            {
                tuNgay.Date,
                denNgay.Date
            });
        }
    }
}
