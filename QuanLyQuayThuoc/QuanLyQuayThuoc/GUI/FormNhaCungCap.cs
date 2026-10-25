using System;
using System.Data;
using System.Windows.Forms;
using QuanLyQuayThuoc.BUS;
using QuanLyQuayThuoc.DTO;

namespace QuanLyQuayThuoc.GUI
{
    public partial class FormNhaCungCap : Form
    {
        private int _maNCCDangChon = -1;

        public FormNhaCungCap()
        {
            InitializeComponent();
        }

        private void FormNhaCungCap_Load(object sender, EventArgs e)
        {
            TaiDanhSachNhaCungCap();
            btnLamMoi_Click(null, null);
        }

        private void TaiDanhSachNhaCungCap()
        {
            dgvNhaCungCap.DataSource = NhaCungCapBUS.Instance.LayDanhSach();
            if (dgvNhaCungCap.Columns["MaNCC"] != null) dgvNhaCungCap.Columns["MaNCC"].HeaderText = "M\u00E3 NCC";
            if (dgvNhaCungCap.Columns["TenNCC"] != null) dgvNhaCungCap.Columns["TenNCC"].HeaderText = "T\u00EAn nh\u00E0 cung c\u1EA5p";
            if (dgvNhaCungCap.Columns["DiaChi"] != null) dgvNhaCungCap.Columns["DiaChi"].HeaderText = "\u0110\u1ECBa ch\u1EC9";
            if (dgvNhaCungCap.Columns["SoDienThoai"] != null) dgvNhaCungCap.Columns["SoDienThoai"].HeaderText = "S\u1ED1 \u0111i\u1EC7n tho\u1EA1i";
            if (dgvNhaCungCap.Columns["Email"] != null) dgvNhaCungCap.Columns["Email"].HeaderText = "Email";
            if (dgvNhaCungCap.Columns["GhiChu"] != null) dgvNhaCungCap.Columns["GhiChu"].HeaderText = "Ghi ch\u00FA";
        }

        private NhaCungCapDTO TaoDtoTuForm()
        {
            return new NhaCungCapDTO
            {
                MaNCC = _maNCCDangChon,
                TenNCC = txtTenNCC.Text.Trim(),
                DiaChi = txtDiaChi.Text.Trim(),
                SoDienThoai = txtSoDienThoai.Text.Trim(),
                Email = txtEmail.Text.Trim(),
                GhiChu = txtGhiChu.Text.Trim()
            };
        }

        private void dgvNhaCungCap_SelectionChanged(object sender, EventArgs e)
        {
            if (dgvNhaCungCap.CurrentRow == null || dgvNhaCungCap.CurrentRow.Index < 0 || dgvNhaCungCap.CurrentRow.IsNewRow)
                return;

            DataGridViewRow row = dgvNhaCungCap.CurrentRow;
            object ma = row.Cells["MaNCC"].Value;
            if (ma == null || ma == DBNull.Value)
                return;

            _maNCCDangChon = Convert.ToInt32(ma);
            lblMaNCC.Text = "M\u00E3 NCC \u0111ang ch\u1ECDn: " + _maNCCDangChon;
            txtTenNCC.Text = row.Cells["TenNCC"].Value?.ToString() ?? string.Empty;
            txtDiaChi.Text = row.Cells["DiaChi"].Value?.ToString() ?? string.Empty;
            txtSoDienThoai.Text = row.Cells["SoDienThoai"].Value?.ToString() ?? string.Empty;
            txtEmail.Text = row.Cells["Email"].Value?.ToString() ?? string.Empty;
            txtGhiChu.Text = row.Cells["GhiChu"].Value?.ToString() ?? string.Empty;
        }

        private void btnThem_Click(object sender, EventArgs e)
        {
            NhaCungCapDTO dto = TaoDtoTuForm();
            if (NhaCungCapBUS.Instance.Them(dto, out string thongBao))
            {
                MessageBox.Show("Th\u00EAm nh\u00E0 cung c\u1EA5p th\u00E0nh c\u00F4ng.", "Th\u00F4ng b\u00E1o", MessageBoxButtons.OK, MessageBoxIcon.Information);
                TaiDanhSachNhaCungCap();
                btnLamMoi_Click(null, null);
            }
            else
            {
                MessageBox.Show(thongBao, "L\u1ED7i", MessageBoxButtons.OK, MessageBoxIcon.Warning);
            }
        }

        private void btnSua_Click(object sender, EventArgs e)
        {
            NhaCungCapDTO dto = TaoDtoTuForm();
            if (NhaCungCapBUS.Instance.Sua(dto, out string thongBao))
            {
                MessageBox.Show("C\u1EADp nh\u1EADt nh\u00E0 cung c\u1EA5p th\u00E0nh c\u00F4ng.", "Th\u00F4ng b\u00E1o", MessageBoxButtons.OK, MessageBoxIcon.Information);
                TaiDanhSachNhaCungCap();
            }
            else
            {
                MessageBox.Show(thongBao, "L\u1ED7i", MessageBoxButtons.OK, MessageBoxIcon.Warning);
            }
        }

        private void btnXoa_Click(object sender, EventArgs e)
        {
            if (_maNCCDangChon <= 0)
            {
                MessageBox.Show("Ch\u1ECDn nh\u00E0 cung c\u1EA5p c\u1EA7n x\u00F3a.", "Th\u00F4ng b\u00E1o", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;
            }

            DialogResult xacNhan = MessageBox.Show(
                "B\u1EA1n c\u00F3 ch\u1EAFc ch\u1EAFn mu\u1ED1n x\u00F3a nh\u00E0 cung c\u1EA5p n\u00E0y kh\u00F4ng?",
                "X\u00E1c nh\u1EADn x\u00F3a",
                MessageBoxButtons.YesNo,
                MessageBoxIcon.Question);

            if (xacNhan != DialogResult.Yes)
                return;

            if (NhaCungCapBUS.Instance.Xoa(_maNCCDangChon, out string thongBao))
            {
                MessageBox.Show("X\u00F3a nh\u00E0 cung c\u1EA5p th\u00E0nh c\u00F4ng.", "Th\u00F4ng b\u00E1o", MessageBoxButtons.OK, MessageBoxIcon.Information);
                TaiDanhSachNhaCungCap();
                btnLamMoi_Click(null, null);
            }
            else
            {
                MessageBox.Show(thongBao, "L\u1ED7i", MessageBoxButtons.OK, MessageBoxIcon.Warning);
            }
        }

        private void btnLamMoi_Click(object sender, EventArgs e)
        {
            _maNCCDangChon = -1;
            lblMaNCC.Text = "M\u00E3 NCC \u0111ang ch\u1ECDn: (ch\u01B0a ch\u1ECDn)";
            txtTenNCC.Clear();
            txtDiaChi.Clear();
            txtSoDienThoai.Clear();
            txtEmail.Clear();
            txtGhiChu.Clear();
            dgvNhaCungCap.ClearSelection();
        }
    }
}
