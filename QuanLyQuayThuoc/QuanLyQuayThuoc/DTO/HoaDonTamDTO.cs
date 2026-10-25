using System;
using System.Collections.Generic;

namespace QuanLyQuayThuoc.DTO
{
    public class HoaDonTamDTO
    {
        public string IdLuuTam { get; set; } // Tên hiển thị trên menu (VD: Đơn 15:30 - Khách Lẻ)
        public int? MaKH { get; set; }
        public int LoaiKhachIndex { get; set; }
        public string TenKhachHangTam { get; set; } // Để dễ tìm kiếm
        public decimal GiamGia { get; set; }
        public decimal ThanhToan { get; set; }
        public string GhiChu { get; set; }
        
        // Cần lưu đầy đủ thông tin để gán lại vào lưới
        public List<ChiTietHoaDonDTO> ChiTietList { get; set; }
        
        // Thêm 2 thuộc tính cần thiết khác dành riêng cho lưới để tránh gọi lại DB 
        public Dictionary<string, int> TonKhoList { get; set; } // Map: SoLo -> SoLuongTon
        
        public HoaDonTamDTO()
        {
            ChiTietList = new List<ChiTietHoaDonDTO>();
            TonKhoList = new Dictionary<string, int>();
        }
    }
}
