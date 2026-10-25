using System.Data;
using QuanLyQuayThuoc.DAO;
using QuanLyQuayThuoc.DTO;

namespace QuanLyQuayThuoc.BUS
{
    public class DanhMucBUS
    {
        private static DanhMucBUS instance;
        public static DanhMucBUS Instance
        {
            get { if (instance == null) instance = new DanhMucBUS(); return instance; }
            private set { instance = value; }
        }

        private DanhMucBUS() { }

        public DataTable LayDanhSach()
        {
            return DanhMucDAO.Instance.GetAll();
        }

        public bool Them(DanhMucDTO danhMuc, out string thongBao)
        {
            thongBao = KiemTraDuLieu(danhMuc, false);
            if (thongBao != null)
                return false;

            int ketQua = DanhMucDAO.Instance.Them(danhMuc.TenDanhMuc, danhMuc.MoTa);
            if (ketQua <= 0)
            {
                thongBao = "Kh\u00F4ng th\u1EC3 th\u00EAm danh m\u1EE5c.";
                return false;
            }

            thongBao = null;
            return true;
        }

        public bool Sua(DanhMucDTO danhMuc, out string thongBao)
        {
            thongBao = KiemTraDuLieu(danhMuc, true);
            if (thongBao != null)
                return false;

            int ketQua = DanhMucDAO.Instance.Sua(danhMuc.MaDM, danhMuc.TenDanhMuc, danhMuc.MoTa);
            if (ketQua <= 0)
            {
                thongBao = "Kh\u00F4ng th\u1EC3 c\u1EADp nh\u1EADt danh m\u1EE5c.";
                return false;
            }

            thongBao = null;
            return true;
        }

        public bool Xoa(int maDM, out string thongBao)
        {
            if (maDM <= 0)
            {
                thongBao = "Ch\u01B0a ch\u1ECDn danh m\u1EE5c c\u1EA7n x\u00F3a.";
                return false;
            }

            if (DanhMucDAO.Instance.DaDuocSuDung(maDM))
            {
                thongBao = "Danh m\u1EE5c \u0111\u00E3 \u0111\u01B0\u1EE3c s\u1EED d\u1EE5ng trong Thu\u1ED1c, kh\u00F4ng th\u1EC3 x\u00F3a.";
                return false;
            }

            int ketQua = DanhMucDAO.Instance.Xoa(maDM);
            if (ketQua <= 0)
            {
                thongBao = "Kh\u00F4ng th\u1EC3 x\u00F3a danh m\u1EE5c.";
                return false;
            }

            thongBao = null;
            return true;
        }

        private string KiemTraDuLieu(DanhMucDTO danhMuc, bool canSua)
        {
            if (danhMuc == null)
                return "D\u1EEF li\u1EC7u danh m\u1EE5c kh\u00F4ng h\u1EE3p l\u1EC7.";

            if (string.IsNullOrWhiteSpace(danhMuc.TenDanhMuc))
                return "T\u00EAn danh m\u1EE5c kh\u00F4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng.";

            if (canSua && danhMuc.MaDM <= 0)
                return "Ch\u01B0a ch\u1ECDn danh m\u1EE5c c\u1EA7n s\u1EEDa.";

            return null;
        }
    }
}
