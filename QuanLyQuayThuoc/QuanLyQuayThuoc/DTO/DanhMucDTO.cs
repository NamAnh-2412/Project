using System.Data;

namespace QuanLyQuayThuoc.DTO
{
    public class DanhMucDTO
    {
        public int MaDM { get; set; }
        public string TenDanhMuc { get; set; }
        public string MoTa { get; set; }

        public DanhMucDTO() { }

        public DanhMucDTO(DataRow row)
        {
            MaDM = (int)row["MaDM"];
            TenDanhMuc = row["TenDanhMuc"].ToString();
            MoTa = row["MoTa"] == System.DBNull.Value ? null : row["MoTa"].ToString();
        }
    }
}
