using System;
using System.Collections.Generic;
using System.Data;
using System.Configuration;
using System.Data.SqlClient;
using QuanLyQuayThuoc.DTO;

namespace QuanLyQuayThuoc.DAO
{
    public class PhieuNhapDAO
    {
        private static PhieuNhapDAO instance;
        public static PhieuNhapDAO Instance
        {
            get { if (instance == null) instance = new PhieuNhapDAO(); return instance; }
            private set { instance = value; }
        }

        private readonly string connectionString = ConfigurationManager.ConnectionStrings["QuayThuocConnection"].ConnectionString;

        private PhieuNhapDAO() { }

        public int LuuPhieuNhap(PhieuNhapDTO phieuNhap, List<ChiTietNhapDTO> chiTietNhapList)
        {
            using (SqlConnection connection = new SqlConnection(connectionString))
            {
                connection.Open();
                SqlTransaction transaction = connection.BeginTransaction();

                try
                {
                    string queryPhieuNhap = @"
INSERT INTO PhieuNhap (MaNV, MaNCC, NgayNhap, SoHoaDonNCC, TongTien, DaThanhToan, GhiChu)
VALUES (@MaNV, @MaNCC, @NgayNhap, @SoHoaDonNCC, @TongTien, @DaThanhToan, @GhiChu);
SELECT CAST(SCOPE_IDENTITY() AS INT);";

                    SqlCommand cmdPhieuNhap = new SqlCommand(queryPhieuNhap, connection, transaction);
                    cmdPhieuNhap.Parameters.AddWithValue("@MaNV", (object)phieuNhap.MaNV ?? DBNull.Value);
                    cmdPhieuNhap.Parameters.AddWithValue("@MaNCC", (object)phieuNhap.MaNCC ?? DBNull.Value);
                    cmdPhieuNhap.Parameters.AddWithValue("@NgayNhap", phieuNhap.NgayNhap);
                    cmdPhieuNhap.Parameters.AddWithValue("@SoHoaDonNCC", ToDbValue(phieuNhap.SoHoaDonNCC));
                    cmdPhieuNhap.Parameters.AddWithValue("@TongTien", phieuNhap.TongTien);
                    cmdPhieuNhap.Parameters.AddWithValue("@DaThanhToan", phieuNhap.DaThanhToan);
                    cmdPhieuNhap.Parameters.AddWithValue("@GhiChu", ToDbValue(phieuNhap.GhiChu));

                    int maPhieuNhap = Convert.ToInt32(cmdPhieuNhap.ExecuteScalar());

                    string queryChiTiet = @"
INSERT INTO ChiTietNhap (MaPhieuNhap, MaThuoc, SoLo, HanSuDung, SoLuongNhap, DonGiaNhap)
VALUES (@MaPhieuNhap, @MaThuoc, @SoLo, @HanSuDung, @SoLuongNhap, @DonGiaNhap);";

                    foreach (ChiTietNhapDTO chiTiet in chiTietNhapList)
                    {
                        SqlCommand cmdChiTiet = new SqlCommand(queryChiTiet, connection, transaction);
                        cmdChiTiet.Parameters.AddWithValue("@MaPhieuNhap", maPhieuNhap);
                        cmdChiTiet.Parameters.AddWithValue("@MaThuoc", chiTiet.MaThuoc);
                        cmdChiTiet.Parameters.AddWithValue("@SoLo", chiTiet.SoLo);
                        cmdChiTiet.Parameters.AddWithValue("@HanSuDung", chiTiet.HanSuDung);
                        cmdChiTiet.Parameters.AddWithValue("@SoLuongNhap", chiTiet.SoLuongNhap);
                        cmdChiTiet.Parameters.AddWithValue("@DonGiaNhap", chiTiet.DonGiaNhap);
                        cmdChiTiet.ExecuteNonQuery();
                    }

                    transaction.Commit();
                    return maPhieuNhap;
                }
                catch
                {
                    transaction.Rollback();
                    throw;
                }
            }
        }

        public DataTable LayDanhSachPhieuNhap(DateTime tuNgay, DateTime denNgay, string tuKhoa)
        {
            using (SqlConnection connection = new SqlConnection(connectionString))
            {
                connection.Open();
                string query = @"
SELECT 
    PN.MaPhieuNhap,
    PN.NgayNhap,
    NCC.TenNCC,
    NV.HoTen,
    PN.SoHoaDonNCC,
    PN.TongTien,
    PN.DaThanhToan,
    PN.GhiChu
FROM PhieuNhap PN
LEFT JOIN NhaCungCap NCC ON PN.MaNCC = NCC.MaNCC
LEFT JOIN NhanVien NV ON PN.MaNV = NV.MaNV
WHERE CAST(PN.NgayNhap AS DATE) >= @TuNgay
  AND CAST(PN.NgayNhap AS DATE) <= @DenNgay
  AND (@TuKhoa = '' OR CAST(PN.MaPhieuNhap AS NVARCHAR(20)) LIKE '%' + @TuKhoa + '%' OR NCC.TenNCC LIKE '%' + @TuKhoa + '%' OR PN.SoHoaDonNCC LIKE '%' + @TuKhoa + '%')
ORDER BY PN.MaPhieuNhap DESC";

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

        public DataTable LayChiTietPhieuNhap(int maPhieuNhap)
        {
            using (SqlConnection connection = new SqlConnection(connectionString))
            {
                connection.Open();
                string query = @"
SELECT 
    CTN.MaThuoc,
    T.TenThuoc,
    CTN.SoLo,
    CTN.HanSuDung,
    CTN.SoLuongNhap,
    CTN.DonGiaNhap,
    CTN.ThanhTien,
    CASE 
        WHEN ISNULL(T.TyLeQuyDoi, 1) > 1 AND ISNULL(T.DonViQuyDoi, '') <> '' THEN T.DonViQuyDoi
        ELSE T.DonViTinh
    END AS DonViTinh
FROM ChiTietNhap CTN
INNER JOIN Thuoc T ON CTN.MaThuoc = T.MaThuoc
WHERE CTN.MaPhieuNhap = @MaPhieuNhap
ORDER BY T.TenThuoc, CTN.SoLo";

                SqlCommand command = new SqlCommand(query, connection);
                command.Parameters.AddWithValue("@MaPhieuNhap", maPhieuNhap);

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
