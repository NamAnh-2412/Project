using System;

namespace QuanLyQuayThuoc.DTO
{
    public class ChiTietNhapDTO
    {
        public int MaPhieuNhap { get; set; }
        public int MaThuoc { get; set; }
        public string SoLo { get; set; }
        public DateTime HanSuDung { get; set; }
        public int SoLuongNhap { get; set; }
        public decimal DonGiaNhap { get; set; }
        public decimal ThanhTien
        {
            get { return SoLuongNhap * DonGiaNhap; }
        }
    }
}
