using System;
using System.Collections.Generic;
using System.Configuration;
using System.Data;
using System.Data.SqlClient;
using QuanLyQuayThuoc.DTO;

namespace QuanLyQuayThuoc.DAO
{
    public class HoaDonDAO
    {
        private static HoaDonDAO instance;
        public static HoaDonDAO Instance
        {
            get { if (instance == null) instance = new HoaDonDAO(); return instance; }
            private set { instance = value; }
        }

        private readonly string connectionString = ConfigurationManager.ConnectionStrings["QuayThuocConnection"].ConnectionString;

        private HoaDonDAO() { }

        public int LuuHoaDon(HoaDonDTO hoaDon, List<ChiTietHoaDonDTO> chiTietHoaDonList)
        {
            using (SqlConnection connection = new SqlConnection(connectionString))
            {
                connection.Open();
                SqlTransaction transaction = connection.BeginTransaction();

                try
                {
                    string queryHoaDon = @"
INSERT INTO HoaDon (MaNV, MaKH, NgayBan, TongTien, GiamGia, ThanhToan, GhiChu)
VALUES (@MaNV, @MaKH, @NgayBan, @TongTien, @GiamGia, @ThanhToan, @GhiChu);
SELECT CAST(SCOPE_IDENTITY() AS INT);";

                    SqlCommand cmdHoaDon = new SqlCommand(queryHoaDon, connection, transaction);
                    cmdHoaDon.Parameters.AddWithValue("@MaNV", (object)hoaDon.MaNV ?? DBNull.Value);
                    cmdHoaDon.Parameters.AddWithValue("@MaKH", (object)hoaDon.MaKH ?? DBNull.Value);
                    cmdHoaDon.Parameters.AddWithValue("@NgayBan", hoaDon.NgayBan);
                    cmdHoaDon.Parameters.AddWithValue("@TongTien", hoaDon.TongTien);
                    cmdHoaDon.Parameters.AddWithValue("@GiamGia", hoaDon.GiamGia);
                    cmdHoaDon.Parameters.AddWithValue("@ThanhToan", hoaDon.ThanhToan);
                    cmdHoaDon.Parameters.AddWithValue("@GhiChu", ToDbValue(hoaDon.GhiChu));

                    int maHD = Convert.ToInt32(cmdHoaDon.ExecuteScalar());

                    string queryChiTiet = @"
INSERT INTO ChiTietHoaDon (MaHD, MaThuoc, SoLo, SoLuongBan, DonGiaBan)
VALUES (@MaHD, @MaThuoc, @SoLo, @SoLuongBan, @DonGiaBan);";

                    foreach (ChiTietHoaDonDTO chiTiet in chiTietHoaDonList)
                    {
                        SqlCommand cmdChiTiet = new SqlCommand(queryChiTiet, connection, transaction);
                        cmdChiTiet.Parameters.AddWithValue("@MaHD", maHD);
                        cmdChiTiet.Parameters.AddWithValue("@MaThuoc", chiTiet.MaThuoc);
                        cmdChiTiet.Parameters.AddWithValue("@SoLo", chiTiet.SoLo);
                        cmdChiTiet.Parameters.AddWithValue("@SoLuongBan", chiTiet.SoLuongBan);
                        cmdChiTiet.Parameters.AddWithValue("@DonGiaBan", chiTiet.DonGiaBan);
                        cmdChiTiet.ExecuteNonQuery();
                    }

                    transaction.Commit();
                    return maHD;
                }
                catch
                {
                    transaction.Rollback();
                    throw;
                }
            }
        }

        public DataTable LayDanhSachHoaDon(DateTime tuNgay, DateTime denNgay, string tuKhoa)
        {
            using (SqlConnection connection = new SqlConnection(connectionString))
            {
                connection.Open();
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
  AND (@TuKhoa = '' OR CAST(HD.MaHD AS NVARCHAR(20)) LIKE '%' + @TuKhoa + '%' OR KH.TenKH LIKE '%' + @TuKhoa + '%')
ORDER BY HD.MaHD DESC";

                SqlCommand command = new SqlCommand(query, connection);
                command.Parameters.AddWithValue("@TuNgay", tuNgay.Date);
                command.Parameters.AddWithValue("@DenNgay", denNgay.Date);
                command.Parameters.AddWithValue("@TuKhoa", tuKhoa ?? string.Empty);

                SqlDataAdapter adapter = new SqlDataAdapter(command);
                DataTable data = new DataTable();
                adapter.Fill(data);
                connection.Close();
                return data;
            }
        }

        public DataTable LayChiTietHoaDon(int maHD)
        {
            using (SqlConnection connection = new SqlConnection(connectionString))
            {
                connection.Open();
                string query = @"
SELECT 
    CTHD.MaThuoc,
    T.TenThuoc,
    CTHD.SoLo,
    CTHD.SoLuongBan,
    CTHD.DonGiaBan,
    CTHD.ThanhTien,
    CASE 
        WHEN ISNULL(T.TyLeQuyDoi, 1) > 1 AND ISNULL(T.DonViQuyDoi, '') <> '' THEN T.DonViQuyDoi
        ELSE T.DonViTinh
    END AS DonViTinh
FROM ChiTietHoaDon CTHD
INNER JOIN Thuoc T ON CTHD.MaThuoc = T.MaThuoc
WHERE CTHD.MaHD = @MaHD
ORDER BY T.TenThuoc, CTHD.SoLo";

                SqlCommand command = new SqlCommand(query, connection);
                command.Parameters.AddWithValue("@MaHD", maHD);

                SqlDataAdapter adapter = new SqlDataAdapter(command);
                DataTable data = new DataTable();
                adapter.Fill(data);
                connection.Close();
                return data;
            }
        }

        private object ToDbValue(string value)
        {
            return string.IsNullOrWhiteSpace(value) ? (object)DBNull.Value : value.Trim();
        }
    }
}
