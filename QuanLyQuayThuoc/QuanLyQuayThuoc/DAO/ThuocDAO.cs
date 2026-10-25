using System;
using System.Data;
using QuanLyQuayThuoc.DTO;

namespace QuanLyQuayThuoc.DAO
{
    public class ThuocDAO
    {
        private static ThuocDAO instance;
        public static ThuocDAO Instance
        {
            get { if (instance == null) instance = new ThuocDAO(); return instance; }
            private set { instance = value; }
        }
        private ThuocDAO() { }

        private static object SqlStr(string s)
        {
            return string.IsNullOrWhiteSpace(s) ? (object)DBNull.Value : s;
        }

        public DataTable GetAllHienThi()
        {
            string query = @"
 SELECT 
                    T.MaThuoc,
                    T.TenThuoc,
                    T.MaDM,
                    DM.TenDanhMuc,
                    T.MaNCC,
                    NCC.TenNCC,
                    T.DonViTinh,
                    T.HoatChat,
                    T.HamLuong,
                    T.XuatXu,
                    T.GiaBan,
                    T.ThongTinMoTa,
                    T.DonViQuyDoi,
                    T.TyLeQuyDoi,
                    T.SoLuongToiThieu
                FROM Thuoc T
                LEFT JOIN DanhMuc DM ON T.MaDM = DM.MaDM
                LEFT JOIN NhaCungCap NCC ON T.MaNCC = NCC.MaNCC
                ORDER BY T.TenThuoc";
            return DataProvider.Instance.ExecuteQuery(query);
        }

        public DataTable GetDanhSachChoNhapKho()
        {
            string query = @"
SELECT 
    MaThuoc,
    TenThuoc,
    DonViTinh,
    DonViQuyDoi,
    TyLeQuyDoi
FROM Thuoc
ORDER BY TenThuoc";
            return DataProvider.Instance.ExecuteQuery(query);
        }

        public DataTable TimKiemVaLoc(string tuKhoa, object maDM, object maNCC)
        {
            string query = @"
SELECT 
    T.MaThuoc,
    T.TenThuoc,
    T.MaDM,
    DM.TenDanhMuc,
    T.MaNCC,
    NCC.TenNCC,
    T.DonViTinh,
    T.HoatChat,
    T.HamLuong,
    T.XuatXu,
    T.GiaBan,
    T.ThongTinMoTa,
    T.DonViQuyDoi,
    T.TyLeQuyDoi,
    T.SoLuongToiThieu
FROM Thuoc T
LEFT JOIN DanhMuc DM ON T.MaDM = DM.MaDM
LEFT JOIN NhaCungCap NCC ON T.MaNCC = NCC.MaNCC
WHERE (@TuKhoa = '' OR T.TenThuoc LIKE '%' + @TuKhoa + '%' OR T.HoatChat LIKE '%' + @TuKhoa + '%' OR T.XuatXu LIKE '%' + @TuKhoa + '%')
  AND (@MaDM IS NULL OR T.MaDM = @MaDM)
  AND (@MaNCC IS NULL OR T.MaNCC = @MaNCC)
ORDER BY T.TenThuoc";

            return DataProvider.Instance.ExecuteQuery(query, new object[]
            {
                tuKhoa ?? string.Empty,
                maDM ?? DBNull.Value,
                maNCC ?? DBNull.Value
            });
        }

        public int ThemThuoc(ThuocDTO thuoc)
        {
            string query = "INSERT INTO Thuoc (TenThuoc, MaDM, MaNCC, DonViTinh, HoatChat, HamLuong, XuatXu, GiaBan, ThongTinMoTa, DonViQuyDoi, TyLeQuyDoi, SoLuongToiThieu) " +
                "VALUES (@TenThuoc, @MaDM, @MaNCC, @DonViTinh, @HoatChat, @HamLuong, @XuatXu, @GiaBan, @ThongTinMoTa, @DonViQuyDoi, @TyLeQuyDoi, @SoLuongToiThieu)";
            return DataProvider.Instance.ExecuteNonQuery(query, new object[]
            {
                thuoc.TenThuoc,
                thuoc.MaDM.HasValue ? (object)thuoc.MaDM.Value : DBNull.Value,
                thuoc.MaNCC.HasValue ? (object)thuoc.MaNCC.Value : DBNull.Value,
                thuoc.DonViTinh,
                SqlStr(thuoc.HoatChat),
                SqlStr(thuoc.HamLuong),
                SqlStr(thuoc.XuatXu),
                thuoc.GiaBan,
                SqlStr(thuoc.ThongTinMoTa),
                SqlStr(thuoc.DonViQuyDoi),
                thuoc.TyLeQuyDoi,
                thuoc.SoLuongToiThieu
            });
        }

        public int SuaThuoc(ThuocDTO thuoc)
        {
            string query = "UPDATE Thuoc SET TenThuoc = @TenThuoc, MaDM = @MaDM, MaNCC = @MaNCC, DonViTinh = @DonViTinh, " +
                "HoatChat = @HoatChat, HamLuong = @HamLuong, XuatXu = @XuatXu, GiaBan = @GiaBan, ThongTinMoTa = @ThongTinMoTa, " +
                "DonViQuyDoi = @DonViQuyDoi, TyLeQuyDoi = @TyLeQuyDoi, SoLuongToiThieu = @SoLuongToiThieu " +
                "WHERE MaThuoc = @MaThuoc";
            return DataProvider.Instance.ExecuteNonQuery(query, new object[]
            {
                thuoc.TenThuoc,
                thuoc.MaDM.HasValue ? (object)thuoc.MaDM.Value : DBNull.Value,
                thuoc.MaNCC.HasValue ? (object)thuoc.MaNCC.Value : DBNull.Value,
                thuoc.DonViTinh,
                SqlStr(thuoc.HoatChat),
                SqlStr(thuoc.HamLuong),
                SqlStr(thuoc.XuatXu),
                thuoc.GiaBan,
                SqlStr(thuoc.ThongTinMoTa),
                SqlStr(thuoc.DonViQuyDoi),
                thuoc.TyLeQuyDoi,
                thuoc.SoLuongToiThieu,
                thuoc.MaThuoc
            });
        }

        public int XoaThuoc(int maThuoc)
        {
            string query = "DELETE FROM Thuoc WHERE MaThuoc = @MaThuoc";
            return DataProvider.Instance.ExecuteNonQuery(query, new object[] { maThuoc });
        }

        public bool DaPhatSinhDuLieuLienQuan(int maThuoc)
        {
            string query = "SELECT COUNT(*) FROM TonKho WHERE MaThuoc = @MaThuoc";
            if (Convert.ToInt32(DataProvider.Instance.ExecuteScalar(query, new object[] { maThuoc })) > 0)
                return true;

            query = "SELECT COUNT(*) FROM ChiTietNhap WHERE MaThuoc = @MaThuoc";
            if (Convert.ToInt32(DataProvider.Instance.ExecuteScalar(query, new object[] { maThuoc })) > 0)
                return true;

            query = "SELECT COUNT(*) FROM ChiTietHoaDon WHERE MaThuoc = @MaThuoc";
            return Convert.ToInt32(DataProvider.Instance.ExecuteScalar(query, new object[] { maThuoc })) > 0;
        }
    }
}
