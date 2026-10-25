using System;
using System.Data;

namespace QuanLyQuayThuoc.DTO
{
    public class KhachHangDTO
    {
        public int MaKH { get; set; }
        public string TenKH { get; set; }
        public string SoDienThoai { get; set; }
        public string DiaChi { get; set; }
        public int DiemTichLuy { get; set; }
        public DateTime? NgayDangKy { get; set; }

        public KhachHangDTO() { }

        public KhachHangDTO(DataRow row)
        {
            MaKH = (int)row["MaKH"];
            TenKH = row["TenKH"].ToString();
            SoDienThoai = row["SoDienThoai"] == DBNull.Value ? null : row["SoDienThoai"].ToString();
            DiaChi = row["DiaChi"] == DBNull.Value ? null : row["DiaChi"].ToString();
            DiemTichLuy = row["DiemTichLuy"] == DBNull.Value ? 0 : Convert.ToInt32(row["DiemTichLuy"]);
            NgayDangKy = row["NgayDangKy"] == DBNull.Value ? (DateTime?)null : Convert.ToDateTime(row["NgayDangKy"]);
        }
    }
}
