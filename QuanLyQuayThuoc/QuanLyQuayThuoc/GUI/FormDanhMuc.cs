using System;
using System.Data;
using System.Windows.Forms;
using QuanLyQuayThuoc.BUS;
using QuanLyQuayThuoc.DTO;

namespace QuanLyQuayThuoc.GUI
{
    public partial class FormDanhMuc : Form
    {
        private int _maDMDangChon = -1;

        public FormDanhMuc()
        {
            InitializeComponent();
        }

        private void FormDanhMuc_Load(object sender, EventArgs e)
        {
            TaiDanhSachDanhMuc();
            btnLamMoi_Click(null, null);
        }

        private void TaiDanhSachDanhMuc()
        {
            dgvDanhMuc.DataSource = DanhMucBUS.Instance.LayDanhSach();
            if (dgvDanhMuc.Columns["MaDM"] != null) dgvDanhMuc.Columns["MaDM"].HeaderText = "Mã DM";
            if (dgvDanhMuc.Columns["TenDanhMuc"] != null) dgvDanhMuc.Columns["TenDanhMuc"].HeaderText = "Tên danh mục";
            if (dgvDanhMuc.Columns["MoTa"] != null) dgvDanhMuc.Columns["MoTa"].HeaderText = "Mô tả";
        }

        private DanhMucDTO TaoDtoTuForm()
        {
            return new DanhMucDTO
            {
                MaDM = _maDMDangChon,
                TenDanhMuc = txtTenDanhMuc.Text.Trim(),
                MoTa = txtMoTa.Text.Trim()
            };
        }

        private void dgvDanhMuc_SelectionChanged(object sender, EventArgs e)
        {
            if (dgvDanhMuc.CurrentRow == null || dgvDanhMuc.CurrentRow.Index < 0 || dgvDanhMuc.CurrentRow.IsNewRow)
                return;

            DataGridViewRow row = dgvDanhMuc.CurrentRow;
            object ma = row.Cells["MaDM"].Value;
            if (ma == null || ma == DBNull.Value)
                return;

            _maDMDangChon = Convert.ToInt32(ma);
            lblMaDM.Text = "Mã DM đang chọn: " + _maDMDangChon;
            txtTenDanhMuc.Text = row.Cells["TenDanhMuc"].Value?.ToString() ?? string.Empty;
            txtMoTa.Text = row.Cells["MoTa"].Value?.ToString() ?? string.Empty;
        }

        private void btnThem_Click(object sender, EventArgs e)
        {
            DanhMucDTO dto = TaoDtoTuForm();
            if (DanhMucBUS.Instance.Them(dto, out string thongBao))
            {
                MessageBox.Show("Thêm danh mục thành công.", "Thông báo", MessageBoxButtons.OK, MessageBoxIcon.Information);
                TaiDanhSachDanhMuc();
                btnLamMoi_Click(null, null);
            }
            else
            {
                MessageBox.Show(thongBao, "Lỗi", MessageBoxButtons.OK, MessageBoxIcon.Warning);
            }
        }

        private void btnSua_Click(object sender, EventArgs e)
        {
            DanhMucDTO dto = TaoDtoTuForm();
            if (DanhMucBUS.Instance.Sua(dto, out string thongBao))
            {
                MessageBox.Show("Cập nhật danh mục thành công.", "Thông báo", MessageBoxButtons.OK, MessageBoxIcon.Information);
                TaiDanhSachDanhMuc();
            }
            else
            {
                MessageBox.Show(thongBao, "Lỗi", MessageBoxButtons.OK, MessageBoxIcon.Warning);
            }
        }

        private void btnXoa_Click(object sender, EventArgs e)
        {
            if (_maDMDangChon <= 0)
            {
                MessageBox.Show("Chọn danh mục cần xóa.", "Thông báo", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;
            }

            DialogResult xacNhan = MessageBox.Show(
                "Bạn có chắc chắn muốn xóa danh mục này không?",
                "Xác nhận xóa",
                MessageBoxButtons.YesNo,
                MessageBoxIcon.Question);

            if (xacNhan != DialogResult.Yes)
                return;

            if (DanhMucBUS.Instance.Xoa(_maDMDangChon, out string thongBao))
            {
                MessageBox.Show("Xóa danh mục thành công.", "Thông báo", MessageBoxButtons.OK, MessageBoxIcon.Information);
                TaiDanhSachDanhMuc();
                btnLamMoi_Click(null, null);
            }
            else
            {
                MessageBox.Show(thongBao, "Lỗi", MessageBoxButtons.OK, MessageBoxIcon.Warning);
            }
        }

        private void btnLamMoi_Click(object sender, EventArgs e)
        {
            _maDMDangChon = -1;
            lblMaDM.Text = "Mã DM đang chọn: (chưa chọn)";
            txtTenDanhMuc.Clear();
            txtMoTa.Clear();
            dgvDanhMuc.ClearSelection();
        }
    }
}
