using System.Data;
using QuanLyQuayThuoc.DAO;
using QuanLyQuayThuoc.DTO;

namespace QuanLyQuayThuoc.BUS
{
    public class NhaCungCapBUS
    {
        private static NhaCungCapBUS instance;
        public static NhaCungCapBUS Instance
        {
            get { if (instance == null) instance = new NhaCungCapBUS(); return instance; }
            private set { instance = value; }
        }

        private NhaCungCapBUS() { }

        public DataTable LayDanhSach()
        {
            return NhaCungCapDAO.Instance.GetAll();
        }

        public bool Them(NhaCungCapDTO nhaCungCap, out string thongBao)
        {
            thongBao = KiemTraDuLieu(nhaCungCap, false);
            if (thongBao != null)
                return false;

            int ketQua = NhaCungCapDAO.Instance.Them(
                nhaCungCap.TenNCC,
                nhaCungCap.DiaChi,
                nhaCungCap.SoDienThoai,
                nhaCungCap.Email,
                nhaCungCap.GhiChu);

            if (ketQua <= 0)
            {
                thongBao = "Kh\u00F4ng th\u1EC3 th\u00EAm nh\u00E0 cung c\u1EA5p.";
                return false;
            }

            thongBao = null;
            return true;
        }

        public bool Sua(NhaCungCapDTO nhaCungCap, out string thongBao)
        {
            thongBao = KiemTraDuLieu(nhaCungCap, true);
            if (thongBao != null)
                return false;

            int ketQua = NhaCungCapDAO.Instance.Sua(
                nhaCungCap.MaNCC,
                nhaCungCap.TenNCC,
                nhaCungCap.DiaChi,
                nhaCungCap.SoDienThoai,
                nhaCungCap.Email,
                nhaCungCap.GhiChu);

            if (ketQua <= 0)
            {
                thongBao = "Kh\u00F4ng th\u1EC3 c\u1EADp nh\u1EADt nh\u00E0 cung c\u1EA5p.";
                return false;
            }

            thongBao = null;
            return true;
        }

        public bool Xoa(int maNCC, out string thongBao)
        {
            if (maNCC <= 0)
            {
                thongBao = "Ch\u01B0a ch\u1ECDn nh\u00E0 cung c\u1EA5p c\u1EA7n x\u00F3a.";
                return false;
            }

            if (NhaCungCapDAO.Instance.DaDuocSuDung(maNCC))
            {
                thongBao = "Nh\u00E0 cung c\u1EA5p \u0111\u00E3 \u0111\u01B0\u1EE3c s\u1EED d\u1EE5ng trong Thu\u1ED1c ho\u1EB7c Phi\u1EBFu nh\u1EADp, kh\u00F4ng th\u1EC3 x\u00F3a.";
                return false;
            }

            int ketQua = NhaCungCapDAO.Instance.Xoa(maNCC);
            if (ketQua <= 0)
            {
                thongBao = "Kh\u00F4ng th\u1EC3 x\u00F3a nh\u00E0 cung c\u1EA5p.";
                return false;
            }

            thongBao = null;
            return true;
        }

        private string KiemTraDuLieu(NhaCungCapDTO nhaCungCap, bool canSua)
        {
            if (nhaCungCap == null)
                return "D\u1EEF li\u1EC7u nh\u00E0 cung c\u1EA5p kh\u00F4ng h\u1EE3p l\u1EC7.";

            if (string.IsNullOrWhiteSpace(nhaCungCap.TenNCC))
                return "T\u00EAn nh\u00E0 cung c\u1EA5p kh\u00F4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng.";

            if (canSua && nhaCungCap.MaNCC <= 0)
                return "Ch\u01B0a ch\u1ECDn nh\u00E0 cung c\u1EA5p c\u1EA7n s\u1EEDa.";

            return null;
        }
    }
}
