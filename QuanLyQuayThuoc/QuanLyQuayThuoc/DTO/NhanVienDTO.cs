using System.Data;

namespace QuanLyQuayThuoc.DTO
{
    public class NhanVienDTO
    {
        public int MaNV { get; set; }
        public string HoTen { get; set; }
        public string TenDangNhap { get; set; }
        public string MatKhau { get; set; }
        public string SoDienThoai { get; set; }
        public string Quyen { get; set; }

        public NhanVienDTO() { }

        // Hàm dựng này giúp tự động nhét dữ liệu từ Database (DataRow) vào Object C#
        public NhanVienDTO(DataRow row)
        {
            this.MaNV = (int)row["MaNV"];
            this.HoTen = row["HoTen"].ToString();
            this.TenDangNhap = row["TenDangNhap"].ToString();
            this.MatKhau = row["MatKhau"].ToString();
            this.SoDienThoai = row["SoDienThoai"].ToString();
            this.Quyen = row["Quyen"].ToString();
        }
    }
}
