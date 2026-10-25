using System;
using System.Data;

namespace QuanLyQuayThuoc.DTO
{
    public class ThuocDTO
    {
        public int MaThuoc { get; set; }
        public string TenThuoc { get; set; }
        public int? MaDM { get; set; }
        public int? MaNCC { get; set; }
        public string DonViTinh { get; set; }
        public string HoatChat { get; set; }
        public string HamLuong { get; set; }
        public string XuatXu { get; set; }
        public decimal GiaBan { get; set; }
        public string ThongTinMoTa { get; set; }
        public string DonViQuyDoi { get; set; }
        public int TyLeQuyDoi { get; set; }
        public int SoLuongToiThieu { get; set; }

        public ThuocDTO() { }

        public ThuocDTO(DataRow row)
        {
            MaThuoc = (int)row["MaThuoc"];
            TenThuoc = row["TenThuoc"].ToString();
            MaDM = row["MaDM"] == DBNull.Value ? (int?)null : (int)row["MaDM"];
            MaNCC = row["MaNCC"] == DBNull.Value ? (int?)null : (int)row["MaNCC"];
            DonViTinh = row["DonViTinh"].ToString();
            HoatChat = row["HoatChat"] == DBNull.Value ? null : row["HoatChat"].ToString();
            HamLuong = row["HamLuong"] == DBNull.Value ? null : row["HamLuong"].ToString();
            XuatXu = row["XuatXu"] == DBNull.Value ? null : row["XuatXu"].ToString();
            GiaBan = Convert.ToDecimal(row["GiaBan"]);
            ThongTinMoTa = row["ThongTinMoTa"] == DBNull.Value ? null : row["ThongTinMoTa"].ToString();
            DonViQuyDoi = row.Table.Columns.Contains("DonViQuyDoi") && row["DonViQuyDoi"] != DBNull.Value ? row["DonViQuyDoi"].ToString() : null;
            TyLeQuyDoi = row.Table.Columns.Contains("TyLeQuyDoi") && row["TyLeQuyDoi"] != DBNull.Value ? Convert.ToInt32(row["TyLeQuyDoi"]) : 1;
            SoLuongToiThieu = row.Table.Columns.Contains("SoLuongToiThieu") && row["SoLuongToiThieu"] != DBNull.Value ? Convert.ToInt32(row["SoLuongToiThieu"]) : 10;
        }
    }
}
