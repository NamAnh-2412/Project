using System;
using System.Drawing;
using System.Windows.Forms;
using QuanLyQuayThuoc.BUS;
using QuanLyQuayThuoc.DTO;

namespace QuanLyQuayThuoc.GUI
{
    public partial class FormMain : Form
    {
        // L\u01B0u tr\u1EEF th\u00F4ng tin nh\u00E2n vi\u00EAn t\u1EEB FormLogin truy\u1EC1n qua
        private NhanVienDTO _nhanVienDangNhap;
        private Form _formConHienTai;

        // H\u00E0m kh\u1EDFi t\u1EA1o y\u00EAu c\u1EA7u ph\u1EA3i truy\u1EC1n \u0111\u1ED1i t\u01B0\u1EE3ng NhanVien
        public FormMain(NhanVienDTO nhanVien)
        {
            InitializeComponent();
            _nhanVienDangNhap = nhanVien;
        }

        // S\u1EF1 ki\u1EC7n Form Load: ph\u00E2n quy\u1EC1n theo Quyen
        private void FormMain_Load(object sender, EventArgs e)
        {
            lblTenNhanVien.Text = $"\U0001f464 L\u1EC7nh b\u00E0i: {_nhanVienDangNhap.Quyen} | {_nhanVienDangNhap.HoTen}";

            if (_nhanVienDangNhap.Quyen == "NhanVien")
            {
                btnNhapKho.Visible = false;
                btnTonKho.Visible = false;
                btnKhoThuoc.Visible = false;
                btnKhachHang.Visible = false;
                btnBaoCao.Visible = false;
            }

            HienCanhBaoHanDungTuDong();
        }

        private void btnLogout_Click(object sender, EventArgs e)
        {
            this.Close();
        }

        private void btnBanHang_Click(object sender, EventArgs e)
        {
            lblTieuDe.Text = "QU\u1EA2N L\u00DD B\u00C1N H\u00C0NG";
            MoFormConTrongPanel(new FormBanHang(_nhanVienDangNhap));
        }

        private void btnNhapKho_Click(object sender, EventArgs e)
        {
            lblTieuDe.Text = "NH\u1EACP KHO";
            MoFormConTrongPanel(new FormNhapKho(_nhanVienDangNhap));
        }

        private void btnTonKho_Click(object sender, EventArgs e)
        {
            lblTieuDe.Text = "XEM T\u1ED2N KHO";
            MoFormConTrongPanel(new FormTonKho());
        }

        private void btnKhoThuoc_Click(object sender, EventArgs e)
        {
            lblTieuDe.Text = "QU\u1EA2N L\u00DD S\u1EA2N PH\u1EA8M (THU\u1ED0C)";
            MoFormConTrongPanel(new FormQuanLyThuoc());
        }

        private void btnKhachHang_Click(object sender, EventArgs e)
        {
            lblTieuDe.Text = "QU\u1EA2N L\u00DD KH\u00C1CH H\u00C0NG";
            MoFormConTrongPanel(new FormKhachHang());
        }

        private void MoFormConTrongPanel(Form childForm)
        {
            if (_formConHienTai != null)
            {
                panelDesktop.Controls.Remove(_formConHienTai);
                _formConHienTai.Dispose();
                _formConHienTai = null;
            }
            childForm.TopLevel = false;
            childForm.FormBorderStyle = FormBorderStyle.None;
            childForm.Dock = DockStyle.Fill;
            panelDesktop.Controls.Add(childForm);
            childForm.BringToFront();
            childForm.Show();
            _formConHienTai = childForm;
        }

        private void btnBaoCao_Click(object sender, EventArgs e)
        {
            lblTieuDe.Text = "B\u00C1O C\u00C1O DOANH THU";
            MoFormConTrongPanel(new FormBaoCaoDoanhThu());
        }

        private void HienCanhBaoHanDungTuDong()
        {
            try
            {
                string thongBaoHanSuDung = CanhBaoHanDungBUS.Instance.TaoThongBaoCanhBaoTuDong();
                string thongBaoTonKho = TonKhoBUS.Instance.TaoThongBaoTonKhoDangNhap();

                string noiDung = "";
                if (!string.IsNullOrWhiteSpace(thongBaoHanSuDung))
                    noiDung += thongBaoHanSuDung.TrimEnd();

                if (!string.IsNullOrWhiteSpace(thongBaoTonKho))
                {
                    if (!string.IsNullOrEmpty(noiDung))
                        noiDung += Environment.NewLine + Environment.NewLine;
                    noiDung += thongBaoTonKho.TrimEnd();
                }

                if (!string.IsNullOrWhiteSpace(noiDung))
                    MessageBox.Show(noiDung, "Cảnh báo hệ thống", MessageBoxButtons.OK, MessageBoxIcon.Warning);
            }
            catch (Exception ex)
            {
                MessageBox.Show(
                    "Không thể kiểm tra cảnh báo tự động (hạn dùng / tồn kho). " + ex.Message,
                    "Thông báo",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Warning);
            }
        }
    }
}
