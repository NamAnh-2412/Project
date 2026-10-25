using System;

namespace QuanLyQuayThuoc.DTO
{
    public class PhieuNhapDTO
    {
        public int MaPhieuNhap { get; set; }
        public int? MaNV { get; set; }
        public int? MaNCC { get; set; }
        public DateTime NgayNhap { get; set; }
        public string SoHoaDonNCC { get; set; }
        public decimal TongTien { get; set; }
        public decimal DaThanhToan { get; set; }
        public string GhiChu { get; set; }
    }
}
