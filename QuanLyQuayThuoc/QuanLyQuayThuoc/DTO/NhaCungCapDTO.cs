using System.Data;

namespace QuanLyQuayThuoc.DTO
{
    public class NhaCungCapDTO
    {
        public int MaNCC { get; set; }
        public string TenNCC { get; set; }
        public string DiaChi { get; set; }
        public string SoDienThoai { get; set; }
        public string Email { get; set; }
        public string GhiChu { get; set; }

        public NhaCungCapDTO() { }

        public NhaCungCapDTO(DataRow row)
        {
            MaNCC = (int)row["MaNCC"];
            TenNCC = row["TenNCC"].ToString();
            DiaChi = row["DiaChi"] == System.DBNull.Value ? null : row["DiaChi"].ToString();
            SoDienThoai = row["SoDienThoai"] == System.DBNull.Value ? null : row["SoDienThoai"].ToString();
            Email = row["Email"] == System.DBNull.Value ? null : row["Email"].ToString();
            GhiChu = row["GhiChu"] == System.DBNull.Value ? null : row["GhiChu"].ToString();
        }
    }
}
