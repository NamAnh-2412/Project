using System.Data;
using QuanLyQuayThuoc.DAO;
using QuanLyQuayThuoc.DTO;

namespace QuanLyQuayThuoc.BUS
{
    public class KhachHangBUS
    {
        private static KhachHangBUS instance;
        public static KhachHangBUS Instance
        {
            get { if (instance == null) instance = new KhachHangBUS(); return instance; }
            private set { instance = value; }
        }

        private KhachHangBUS() { }

        public DataTable LayDanhSach(string tuKhoa = "")
        {
            return KhachHangDAO.Instance.LayDanhSach(tuKhoa);
        }

        public bool Them(KhachHangDTO khachHang, out string thongBao)
        {
            thongBao = KiemTraDuLieu(khachHang, false);
            if (thongBao != null)
                return false;

            int ketQua = KhachHangDAO.Instance.Them(khachHang.TenKH, khachHang.SoDienThoai, khachHang.DiaChi);
            if (ketQua <= 0)
            {
                thongBao = "Không thể thêm khách hàng.";
                return false;
            }

            thongBao = null;
            return true;
        }

        public bool Sua(KhachHangDTO khachHang, out string thongBao)
        {
            thongBao = KiemTraDuLieu(khachHang, true);
            if (thongBao != null)
                return false;

            int ketQua = KhachHangDAO.Instance.Sua(khachHang.MaKH, khachHang.TenKH, khachHang.SoDienThoai, khachHang.DiaChi);
            if (ketQua <= 0)
            {
                thongBao = "Không thể cập nhật khách hàng.";
                return false;
            }

            thongBao = null;
            return true;
        }

        public bool Xoa(int maKH, out string thongBao)
        {
            if (maKH <= 0)
            {
                thongBao = "Chưa chọn khách hàng cần xóa.";
                return false;
            }

            if (KhachHangDAO.Instance.DaDuocSuDung(maKH))
            {
                thongBao = "Khách hàng đã phát sinh hóa đơn nên không thể xóa.";
                return false;
            }

            int ketQua = KhachHangDAO.Instance.Xoa(maKH);
            if (ketQua <= 0)
            {
                thongBao = "Không thể xóa khách hàng.";
                return false;
            }

            thongBao = null;
            return true;
        }

        public DataTable LayThongKeMuaHang()
        {
            return KhachHangDAO.Instance.LayThongKeMuaHang();
        }

        public DataTable LayLichSuMuaHang(int maKH)
        {
            return KhachHangDAO.Instance.LayLichSuMuaHang(maKH);
        }

        private string KiemTraDuLieu(KhachHangDTO khachHang, bool canSua)
        {
            if (khachHang == null)
                return "Dữ liệu khách hàng không hợp lệ.";

            if (string.IsNullOrWhiteSpace(khachHang.TenKH))
                return "Tên khách hàng không được để trống.";

            if (canSua && khachHang.MaKH <= 0)
                return "Chưa chọn khách hàng cần sửa.";

            return null;
        }
    }
}
