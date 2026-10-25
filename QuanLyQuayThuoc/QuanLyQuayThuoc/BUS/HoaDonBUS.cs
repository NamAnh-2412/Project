using System;
using System.Collections.Generic;
using System.Data;
using QuanLyQuayThuoc.DAO;
using QuanLyQuayThuoc.DTO;

namespace QuanLyQuayThuoc.BUS
{
    public class HoaDonBUS
    {
        private static HoaDonBUS instance;
        public static HoaDonBUS Instance
        {
            get { if (instance == null) instance = new HoaDonBUS(); return instance; }
            private set { instance = value; }
        }

        private HoaDonBUS() { }

        public bool LuuHoaDon(HoaDonDTO hoaDon, List<ChiTietHoaDonDTO> chiTietHoaDonList, out string thongBao, out int maHD)
        {
            maHD = 0;
            thongBao = KiemTraDuLieu(hoaDon, chiTietHoaDonList);
            if (thongBao != null)
                return false;

            try
            {
                hoaDon.TongTien = 0;
                foreach (ChiTietHoaDonDTO chiTiet in chiTietHoaDonList)
                    hoaDon.TongTien += chiTiet.ThanhTien;

                maHD = HoaDonDAO.Instance.LuuHoaDon(hoaDon, chiTietHoaDonList);
                thongBao = null;
                return true;
            }
            catch (Exception ex)
            {
                thongBao = "Không thể lưu hóa đơn. " + ex.Message;
                return false;
            }
        }

        public DataTable LayDanhSachHoaDon(DateTime tuNgay, DateTime denNgay, string tuKhoa)
        {
            return HoaDonDAO.Instance.LayDanhSachHoaDon(tuNgay, denNgay, tuKhoa);
        }

        public DataTable LayChiTietHoaDon(int maHD)
        {
            return HoaDonDAO.Instance.LayChiTietHoaDon(maHD);
        }

        private string KiemTraDuLieu(HoaDonDTO hoaDon, List<ChiTietHoaDonDTO> chiTietHoaDonList)
        {
            if (!hoaDon.MaNV.HasValue || hoaDon.MaNV.Value <= 0)
                return "Không xác định được nhân viên bán hàng.";

            if (hoaDon.GiamGia < 0)
                return "Giảm giá không được âm.";

            if (hoaDon.ThanhToan < 0)
                return "Thanh toán không được âm.";

            if (chiTietHoaDonList == null || chiTietHoaDonList.Count == 0)
                return "Hóa đơn phải có ít nhất một dòng chi tiết.";

            decimal tongTien = 0;
            foreach (ChiTietHoaDonDTO chiTiet in chiTietHoaDonList)
            {
                if (chiTiet.MaThuoc <= 0)
                    return "Vui lòng chọn thuốc cho tất cả các dòng.";

                if (string.IsNullOrWhiteSpace(chiTiet.SoLo))
                    return "Vui lòng chọn số lô cho tất cả các dòng.";

                if (chiTiet.SoLuongBan <= 0)
                    return "Số lượng bán phải lớn hơn 0.";

                if (chiTiet.DonGiaBan < 0)
                    return "Đơn giá bán không được âm.";

                if (TonKhoDAO.Instance.LoDaHetHan(chiTiet.MaThuoc, chiTiet.SoLo))
                    return "Lô thuốc " + chiTiet.SoLo + " của " + chiTiet.TenThuoc + " đã hết hạn sử dụng, không thể bán.";

                tongTien += chiTiet.ThanhTien;
            }

            if (hoaDon.GiamGia > tongTien)
                return "Giảm giá không được lớn hơn tổng tiền.";

            if (hoaDon.ThanhToan < (tongTien - hoaDon.GiamGia))
                return "Thanh toán chưa đủ để lưu hóa đơn.";

            return null;
        }
    }
}
