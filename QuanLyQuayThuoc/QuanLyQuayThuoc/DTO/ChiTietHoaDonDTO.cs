namespace QuanLyQuayThuoc.DTO
{
    public class ChiTietHoaDonDTO
    {
        public int MaHD { get; set; }
        public int MaThuoc { get; set; }
        public string TenThuoc { get; set; }
        public string SoLo { get; set; }
        public string DonViBan { get; set; }
        public int SoLuongBanHienThi { get; set; }
        public decimal DonGiaBanHienThi { get; set; }
        public int SoLuongBan { get; set; }
        public decimal DonGiaBan { get; set; }
        public decimal ThanhTien
        {
            get { return SoLuongBan * DonGiaBan; }
        }
    }
}
