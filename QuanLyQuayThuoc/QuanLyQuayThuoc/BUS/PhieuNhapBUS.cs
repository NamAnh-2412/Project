using System;
using System.Collections.Generic;
using System.Data;
using QuanLyQuayThuoc.DAO;
using QuanLyQuayThuoc.DTO;

namespace QuanLyQuayThuoc.BUS
{
    public class PhieuNhapBUS
    {
        private static PhieuNhapBUS instance;
        public static PhieuNhapBUS Instance
        {
            get { if (instance == null) instance = new PhieuNhapBUS(); return instance; }
            private set { instance = value; }
        }

        private PhieuNhapBUS() { }

        public bool LuuPhieuNhap(PhieuNhapDTO phieuNhap, List<ChiTietNhapDTO> chiTietNhapList, out string thongBao, out int maPhieuNhap)
        {
            maPhieuNhap = 0;
            thongBao = KiemTraDuLieu(phieuNhap, chiTietNhapList);
            if (thongBao != null)
                return false;

            try
            {
                phieuNhap.TongTien = 0;
                foreach (ChiTietNhapDTO chiTiet in chiTietNhapList)
                    phieuNhap.TongTien += chiTiet.ThanhTien;

                maPhieuNhap = PhieuNhapDAO.Instance.LuuPhieuNhap(phieuNhap, chiTietNhapList);
                thongBao = null;
                return true;
            }
            catch (Exception ex)
            {
                thongBao = "Không thể lưu phiếu nhập. " + ex.Message;
                return false;
            }
        }

        public DataTable LayDanhSachPhieuNhap(DateTime tuNgay, DateTime denNgay, string tuKhoa)
        {
            return PhieuNhapDAO.Instance.LayDanhSachPhieuNhap(tuNgay, denNgay, tuKhoa);
        }

        public DataTable LayChiTietPhieuNhap(int maPhieuNhap)
        {
            return PhieuNhapDAO.Instance.LayChiTietPhieuNhap(maPhieuNhap);
        }

        private string KiemTraDuLieu(PhieuNhapDTO phieuNhap, List<ChiTietNhapDTO> chiTietNhapList)
        {
            if (!phieuNhap.MaNCC.HasValue || phieuNhap.MaNCC.Value <= 0)
                return "Vui lòng chọn nhà cung cấp.";

            if (!phieuNhap.MaNV.HasValue || phieuNhap.MaNV.Value <= 0)
                return "Không xác định được nhân viên nhập kho.";

            if (phieuNhap.DaThanhToan < 0)
                return "Đã thanh toán không được âm.";

            if (chiTietNhapList == null || chiTietNhapList.Count == 0)
                return "Phiếu nhập phải có ít nhất một dòng chi tiết.";

            foreach (ChiTietNhapDTO chiTiet in chiTietNhapList)
            {
                if (chiTiet.MaThuoc <= 0)
                    return "Vui lòng chọn thuốc cho tất cả các dòng.";

                if (string.IsNullOrWhiteSpace(chiTiet.SoLo))
                    return "Số lô không được để trống.";

                if (chiTiet.HanSuDung.Date <= DateTime.Today.Date)
                    return "Hạn sử dụng phải lớn hơn ngày hiện tại.";

                if (chiTiet.SoLuongNhap <= 0)
                    return "Số lượng nhập phải lớn hơn 0.";

                if (chiTiet.DonGiaNhap < 0)
                    return "Đơn giá nhập không được âm.";
            }

            decimal tongTien = 0;
            foreach (ChiTietNhapDTO chiTiet in chiTietNhapList)
                tongTien += chiTiet.ThanhTien;

            if (phieuNhap.DaThanhToan > tongTien)
                return "Đã thanh toán không được lớn hơn tổng tiền phiếu nhập.";

            return null;
        }
    }
}
