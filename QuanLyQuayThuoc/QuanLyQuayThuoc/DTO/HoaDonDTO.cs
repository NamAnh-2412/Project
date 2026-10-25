using System;

namespace QuanLyQuayThuoc.DTO
{
    public class HoaDonDTO
    {
        public int MaHD { get; set; }
        public int? MaNV { get; set; }
        public int? MaKH { get; set; }
        public DateTime NgayBan { get; set; }
        public decimal TongTien { get; set; }
        public decimal GiamGia { get; set; }
        public decimal ThanhToan { get; set; }
        public string GhiChu { get; set; }
    }
}
