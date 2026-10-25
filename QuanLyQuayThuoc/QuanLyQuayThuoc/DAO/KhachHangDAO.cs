using System.Data;

namespace QuanLyQuayThuoc.DAO
{
    public class KhachHangDAO
    {
        private static KhachHangDAO instance;
        public static KhachHangDAO Instance
        {
            get { if (instance == null) instance = new KhachHangDAO(); return instance; }
            private set { instance = value; }
        }

        private KhachHangDAO() { }

        public DataTable LayDanhSach(string tuKhoa = "")
        {
            string query = @"
SELECT MaKH, TenKH, SoDienThoai, DiaChi, DiemTichLuy, NgayDangKy
FROM KhachHang
WHERE (@TuKhoa = '' OR TenKH LIKE '%' + @TuKhoa + '%' OR SoDienThoai LIKE '%' + @TuKhoa + '%')
ORDER BY TenKH";

            return DataProvider.Instance.ExecuteQuery(query, new object[] { tuKhoa ?? string.Empty });
        }

        public int Them(string tenKH, string soDienThoai, string diaChi)
        {
            string query = "INSERT INTO KhachHang (TenKH, SoDienThoai, DiaChi) VALUES ( @TenKH , @SoDienThoai , @DiaChi )";
            return DataProvider.Instance.ExecuteNonQuery(query, new object[]
            {
                tenKH,
                ToDbValue(soDienThoai),
                ToDbValue(diaChi)
            });
        }

        public int Sua(int maKH, string tenKH, string soDienThoai, string diaChi)
        {
            string query = "UPDATE KhachHang SET TenKH = @TenKH , SoDienThoai = @SoDienThoai , DiaChi = @DiaChi WHERE MaKH = @MaKH";
            return DataProvider.Instance.ExecuteNonQuery(query, new object[]
            {
                tenKH,
                ToDbValue(soDienThoai),
                ToDbValue(diaChi),
                maKH
            });
        }

        public int Xoa(int maKH)
        {
            string query = "DELETE FROM KhachHang WHERE MaKH = @MaKH";
            return DataProvider.Instance.ExecuteNonQuery(query, new object[] { maKH });
        }

        public bool DaDuocSuDung(int maKH)
        {
            string query = "SELECT COUNT(*) FROM HoaDon WHERE MaKH = @MaKH";
            int soHoaDon = System.Convert.ToInt32(DataProvider.Instance.ExecuteScalar(query, new object[] { maKH }));
            return soHoaDon > 0;
        }

        public DataTable LayThongKeMuaHang()
        {
            string query = @"
SELECT 
    KH.MaKH,
    KH.TenKH,
    KH.SoDienThoai,
    KH.DiaChi,
    KH.DiemTichLuy,
    KH.NgayDangKy,
    COUNT(HD.MaHD) AS SoLanMua,
    ISNULL(SUM(HD.TongTien - HD.GiamGia), 0) AS TongGiaTriMua
FROM KhachHang KH
LEFT JOIN HoaDon HD ON KH.MaKH = HD.MaKH
GROUP BY KH.MaKH, KH.TenKH, KH.SoDienThoai, KH.DiaChi, KH.DiemTichLuy, KH.NgayDangKy
ORDER BY SoLanMua DESC, KH.TenKH";

            return DataProvider.Instance.ExecuteQuery(query);
        }

        public DataTable LayLichSuMuaHang(int maKH)
        {
            string query = @"
SELECT 
    HD.MaHD,
    HD.NgayBan,
    HD.TongTien,
    HD.GiamGia,
    HD.ThanhToan,
    HD.GhiChu,
    NV.HoTen
FROM HoaDon HD
LEFT JOIN NhanVien NV ON HD.MaNV = NV.MaNV
WHERE HD.MaKH = @MaKH
ORDER BY HD.MaHD DESC";

            return DataProvider.Instance.ExecuteQuery(query, new object[] { maKH });
        }

        private object ToDbValue(string value)
        {
            return string.IsNullOrWhiteSpace(value) ? (object)System.DBNull.Value : value.Trim();
        }
    }
}
