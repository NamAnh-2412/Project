using QuanLyQuayThuoc.DAO;
using QuanLyQuayThuoc.DTO;
using System.Data;

namespace QuanLyQuayThuoc.BUS
{
    public class ThuocBUS
    {
        private static ThuocBUS instance;
        public static ThuocBUS Instance
        {
            get { if (instance == null) instance = new ThuocBUS(); return instance; }
            private set { instance = value; }
        }
        private ThuocBUS() { }

        public DataTable LayDanhSachHienThi()
        {
            return ThuocDAO.Instance.GetAllHienThi();
        }

        public DataTable TimKiemVaLoc(string tuKhoa, object maDM, object maNCC)
        {
            return ThuocDAO.Instance.TimKiemVaLoc(tuKhoa, maDM, maNCC);
        }

        public DataTable LayDanhSachChoNhapKho()
        {
            return ThuocDAO.Instance.GetDanhSachChoNhapKho();
        }

        private string KiemTraDuLieu(ThuocDTO thuoc, bool canSua)
        {
            if (string.IsNullOrWhiteSpace(thuoc.TenThuoc))
                return "Tên thuốc không được để trống.";

            if (thuoc.GiaBan <= 0)
                return "Giá bán phải lớn hơn 0.";

            if (string.IsNullOrWhiteSpace(thuoc.DonViTinh))
                return "Đơn vị tính không được để trống.";

            if (!thuoc.MaDM.HasValue || thuoc.MaDM.Value <= 0)
                return "Vui l\u00F2ng ch\u1ECDn danh m\u1EE5c (M\u00E3 DM) h\u1EE3p l\u1EC7.";

            if (!thuoc.MaNCC.HasValue || thuoc.MaNCC.Value <= 0)
                return "Vui l\u00F2ng ch\u1ECDn nh\u00E0 cung c\u1EA5p (M\u00E3 NCC) h\u1EE3p l\u1EC7.";

            if (canSua && thuoc.MaThuoc <= 0)
                return "Chưa chọn thuốc để sửa (thiếu Mã thuốc).";

            return null;
        }

        public bool ThemThuoc(ThuocDTO thuoc, out string thongBaoLoi)
        {
            thongBaoLoi = KiemTraDuLieu(thuoc, false);
            if (thongBaoLoi != null)
                return false;

            int n = ThuocDAO.Instance.ThemThuoc(thuoc);
            if (n <= 0)
            {
                thongBaoLoi = "Không thể thêm thuốc. Kiểm tra kết nối CSDL hoặc dữ liệu tham chiếu (DM/NCC).";
                return false;
            }
            thongBaoLoi = null;
            return true;
        }

        public bool SuaThuoc(ThuocDTO thuoc, out string thongBaoLoi)
        {
            thongBaoLoi = KiemTraDuLieu(thuoc, true);
            if (thongBaoLoi != null)
                return false;

            int n = ThuocDAO.Instance.SuaThuoc(thuoc);
            if (n <= 0)
            {
                thongBaoLoi = "Không có dòng nào được cập nhật. Kiểm tra Mã thuốc hoặc CSDL.";
                return false;
            }
            thongBaoLoi = null;
            return true;
        }

        public bool XoaThuoc(int maThuoc, out string thongBaoLoi)
        {
            if (maThuoc <= 0)
            {
                thongBaoLoi = "Chưa chọn thuốc cần xóa.";
                return false;
            }

            if (ThuocDAO.Instance.DaPhatSinhDuLieuLienQuan(maThuoc))
            {
                thongBaoLoi = "Thuốc đã phát sinh ở tồn kho, phiếu nhập hoặc hóa đơn nên không thể xóa.";
                return false;
            }

            int n = ThuocDAO.Instance.XoaThuoc(maThuoc);
            if (n <= 0)
            {
                thongBaoLoi = "Không thể xóa thuốc. Kiểm tra lại CSDL.";
                return false;
            }

            thongBaoLoi = null;
            return true;
        }
    }
}
