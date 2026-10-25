using System;
using System.Collections.Generic;
using System.Data;
using System.Globalization;
using System.Windows.Forms;
using QuanLyQuayThuoc.BUS;
using QuanLyQuayThuoc.DTO;

namespace QuanLyQuayThuoc.GUI
{
    public partial class FormQuanLyThuoc : Form
    {
        private int _maThuocDangChon = -1;

        public FormQuanLyThuoc()
        {
            InitializeComponent();
        }

        private void FormQuanLyThuoc_Load(object sender, EventArgs e)
        {
            NapComboDanhMuc();
            NapComboNhaCC();
            NapBoLoc();
            TaiDanhSachThuoc();
        }

        private void NapComboDanhMuc()
        {
            DataTable dm = DanhMucBUS.Instance.LayDanhSach();
            cboDanhMuc.DataSource = dm;
            cboDanhMuc.DisplayMember = "TenDanhMuc";
            cboDanhMuc.ValueMember = "MaDM";
        }

        private void NapComboNhaCC()
        {
            DataTable ncc = NhaCungCapBUS.Instance.LayDanhSach();
            cboNhaCC.DataSource = ncc;
            cboNhaCC.DisplayMember = "TenNCC";
            cboNhaCC.ValueMember = "MaNCC";
        }

        private void NapBoLoc()
        {
            DataTable dm = DanhMucBUS.Instance.LayDanhSach();
            DataTable dmLoc = dm.Copy();
            DataRow dongTatCaDM = dmLoc.NewRow();
            dongTatCaDM["MaDM"] = 0;
            dongTatCaDM["TenDanhMuc"] = "Tất cả";
            dmLoc.Rows.InsertAt(dongTatCaDM, 0);
            cboLocDanhMuc.DataSource = dmLoc;
            cboLocDanhMuc.DisplayMember = "TenDanhMuc";
            cboLocDanhMuc.ValueMember = "MaDM";

            DataTable ncc = NhaCungCapBUS.Instance.LayDanhSach();
            DataTable nccLoc = ncc.Copy();
            DataRow dongTatCaNCC = nccLoc.NewRow();
            dongTatCaNCC["MaNCC"] = 0;
            dongTatCaNCC["TenNCC"] = "Tất cả";
            nccLoc.Rows.InsertAt(dongTatCaNCC, 0);
            cboLocNhaCC.DataSource = nccLoc;
            cboLocNhaCC.DisplayMember = "TenNCC";
            cboLocNhaCC.ValueMember = "MaNCC";
        }

        private void TaiDanhSachThuoc()
        {
            dgvThuoc.DataSource = ThuocBUS.Instance.LayDanhSachHienThi();
            DatTenCotBang();
        }

        private void DatTenCotBang()
        {
            if (dgvThuoc.Columns.Count == 0) return;
            var map = new Dictionary<string, string>
            {
                { "MaThuoc", "Mã thuốc" },
                { "TenThuoc", "Tên thuốc" },
                { "MaDM", "Mã DM" },
                { "TenDanhMuc", "Danh mục" },
                { "MaNCC", "Mã NCC" },
                { "TenNCC", "Nhà cung cấp" },
                { "DonViTinh", "Đơn vị" },
                { "HoatChat", "Hoạt chất" },
                { "HamLuong", "Hàm lượng" },
                { "XuatXu", "Xuất xứ" },
                { "GiaBan", "Giá bán" },
                { "ThongTinMoTa", "Mô tả" },
                { "DonViQuyDoi", "ĐV Quy đổi" },
                { "TyLeQuyDoi", "Tỷ lệ QĐ" },
                { "SoLuongToiThieu", "Tồn tối thiểu" }
            };
            foreach (var kv in map)
            {
                if (dgvThuoc.Columns[kv.Key] != null)
                    dgvThuoc.Columns[kv.Key].HeaderText = kv.Value;
            }
        }

        private void dgvThuoc_SelectionChanged(object sender, EventArgs e)
        {
            if (dgvThuoc.CurrentRow == null || dgvThuoc.CurrentRow.Index < 0 || dgvThuoc.CurrentRow.IsNewRow)
                return;
            DataGridViewRow row = dgvThuoc.CurrentRow;
            object ma = row.Cells["MaThuoc"].Value;
            if (ma == null || ma == DBNull.Value) return;

            _maThuocDangChon = Convert.ToInt32(ma);
            lblMaThuoc.Text = "Mã thuốc đang chọn: " + _maThuocDangChon;

            txtTenThuoc.Text = row.Cells["TenThuoc"].Value?.ToString() ?? "";
            cboDonViTinh.Text = row.Cells["DonViTinh"].Value?.ToString() ?? "";
            cboDonViQuyDoi.Text = row.Cells["DonViQuyDoi"]?.Value?.ToString() ?? "";
            txtTyLeQuyDoi.Text = row.Cells["TyLeQuyDoi"]?.Value?.ToString() ?? "1";
            txtSoLuongToiThieu.Text = row.Cells["SoLuongToiThieu"]?.Value?.ToString() ?? "10";
            txtHoatChat.Text = row.Cells["HoatChat"].Value?.ToString() ?? "";
            txtHamLuong.Text = row.Cells["HamLuong"].Value?.ToString() ?? "";
            txtXuatXu.Text = row.Cells["XuatXu"].Value?.ToString() ?? "";
            txtMoTa.Text = row.Cells["ThongTinMoTa"].Value?.ToString() ?? "";

            if (row.Cells["GiaBan"].Value != null && row.Cells["GiaBan"].Value != DBNull.Value)
                txtGiaBan.Text = Convert.ToDecimal(row.Cells["GiaBan"].Value).ToString("0.##", CultureInfo.CurrentCulture);
            else
                txtGiaBan.Text = "";

            if (row.Cells["MaDM"].Value != null && row.Cells["MaDM"].Value != DBNull.Value)
                cboDanhMuc.SelectedValue = Convert.ToInt32(row.Cells["MaDM"].Value);
            else
                cboDanhMuc.SelectedIndex = -1;

            if (row.Cells["MaNCC"].Value != null && row.Cells["MaNCC"].Value != DBNull.Value)
                cboNhaCC.SelectedValue = Convert.ToInt32(row.Cells["MaNCC"].Value);
            else
                cboNhaCC.SelectedIndex = -1;
        }

        private bool DocGiaBan(out decimal gia)
        {
            string s = txtGiaBan.Text.Trim().Replace(" ", "");
            if (s.Contains(",") && !s.Contains("."))
                s = s.Replace(",", ".");
            return decimal.TryParse(s, NumberStyles.Any, CultureInfo.InvariantCulture, out gia);
        }

        private ThuocDTO TaoDtoTuForm(decimal gia)
        {
            var thuoc = new ThuocDTO
            {
                MaThuoc = _maThuocDangChon > 0 ? _maThuocDangChon : 0,
                TenThuoc = txtTenThuoc.Text.Trim(),
                DonViTinh = cboDonViTinh.Text.Trim(),
                DonViQuyDoi = cboDonViQuyDoi.Text.Trim(),
                TyLeQuyDoi = int.TryParse(txtTyLeQuyDoi.Text.Trim(), out int tyLe) && tyLe > 0 ? tyLe : 1,
                SoLuongToiThieu = int.TryParse(txtSoLuongToiThieu.Text.Trim(), out int slMin) && slMin >= 0 ? slMin : 10,
                HoatChat = txtHoatChat.Text.Trim(),
                HamLuong = txtHamLuong.Text.Trim(),
                XuatXu = txtXuatXu.Text.Trim(),
                ThongTinMoTa = txtMoTa.Text.Trim(),
                GiaBan = gia
            };

            if (cboDanhMuc.SelectedValue != null && cboDanhMuc.SelectedValue != DBNull.Value)
                thuoc.MaDM = Convert.ToInt32(cboDanhMuc.SelectedValue);
            if (cboNhaCC.SelectedValue != null && cboNhaCC.SelectedValue != DBNull.Value)
                thuoc.MaNCC = Convert.ToInt32(cboNhaCC.SelectedValue);

            return thuoc;
        }

        private void btnThem_Click(object sender, EventArgs e)
        {
            if (!DocGiaBan(out decimal gia))
            {
                MessageBox.Show("Gi\u00E1 b\u00E1n kh\u00F4ng h\u1EE3p l\u1EC7.", "Th\u00F4ng b\u00E1o", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;
            }
            ThuocDTO thuoc = TaoDtoTuForm(gia);

            if (ThuocBUS.Instance.ThemThuoc(thuoc, out string loi))
            {
                MessageBox.Show("Th\u00EAm thu\u1ED1c th\u00E0nh c\u00F4ng.", "Th\u00F4ng b\u00E1o", MessageBoxButtons.OK, MessageBoxIcon.Information);
                TaiDanhSachThuoc();
                btnLamMoi_Click(null, null);
            }
            else
                MessageBox.Show(loi, "L\u1ED7i", MessageBoxButtons.OK, MessageBoxIcon.Warning);
        }

        private void btnSua_Click(object sender, EventArgs e)
        {
            if (_maThuocDangChon <= 0)
            {
                MessageBox.Show("Ch\u1ECDn m\u1ED9t d\u00F2ng trong danh s\u00E1ch \u0111\u1EC3 s\u1EEDa.", "Th\u00F4ng b\u00E1o", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;
            }
            if (!DocGiaBan(out decimal gia))
            {
                MessageBox.Show("Gi\u00E1 b\u00E1n kh\u00F4ng h\u1EE3p l\u1EC7.", "Th\u00F4ng b\u00E1o", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;
            }
            ThuocDTO thuoc = TaoDtoTuForm(gia);

            if (ThuocBUS.Instance.SuaThuoc(thuoc, out string loi))
            {
                MessageBox.Show("C\u1EADp nh\u1EADt thu\u1ED1c th\u00E0nh c\u00F4ng.", "Th\u00F4ng b\u00E1o", MessageBoxButtons.OK, MessageBoxIcon.Information);
                TaiDanhSachThuoc();
            }
            else
                MessageBox.Show(loi, "L\u1ED7i", MessageBoxButtons.OK, MessageBoxIcon.Warning);
        }

        private void btnLamMoi_Click(object sender, EventArgs e)
        {
            _maThuocDangChon = -1;
            lblMaThuoc.Text = "Mã thuốc đang chọn: (chưa chọn)";
            txtTenThuoc.Clear();
            txtGiaBan.Clear();
            txtHamLuong.Clear();
            txtXuatXu.Clear();
            cboDonViTinh.Text = "Hộp";
            cboDonViQuyDoi.SelectedIndex = -1;
            cboDonViQuyDoi.Text = "";
            txtTyLeQuyDoi.Text = "1";
            txtSoLuongToiThieu.Text = "10";
            txtHoatChat.Clear();
            txtMoTa.Clear();
            cboDanhMuc.SelectedIndex = cboDanhMuc.Items.Count > 0 ? 0 : -1;
            cboNhaCC.SelectedIndex = cboNhaCC.Items.Count > 0 ? 0 : -1;
            dgvThuoc.ClearSelection();
        }

        private void btnXoa_Click(object sender, EventArgs e)
        {
            if (_maThuocDangChon <= 0)
            {
                MessageBox.Show("Chọn một dòng trong danh sách để xóa.", "Thông báo", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;
            }

            DialogResult xacNhan = MessageBox.Show(
                "Bạn có chắc chắn muốn xóa thuốc này không?",
                "Xác nhận xóa",
                MessageBoxButtons.YesNo,
                MessageBoxIcon.Question);

            if (xacNhan != DialogResult.Yes)
                return;

            if (ThuocBUS.Instance.XoaThuoc(_maThuocDangChon, out string loi))
            {
                MessageBox.Show("Xóa thuốc thành công.", "Thông báo", MessageBoxButtons.OK, MessageBoxIcon.Information);
                TaiDanhSachThuoc();
                btnLamMoi_Click(null, null);
            }
            else
            {
                MessageBox.Show(loi, "Lỗi", MessageBoxButtons.OK, MessageBoxIcon.Warning);
            }
        }

        private void btnQuanLyNCC_Click(object sender, EventArgs e)
        {
            using (FormNhaCungCap formNhaCungCap = new FormNhaCungCap())
            {
                formNhaCungCap.ShowDialog(this);
            }

            NapComboNhaCC();
            NapBoLoc();
        }

        private void btnQuanLyDanhMuc_Click(object sender, EventArgs e)
        {
            using (FormDanhMuc formDanhMuc = new FormDanhMuc())
            {
                formDanhMuc.ShowDialog(this);
            }

            NapComboDanhMuc();
            NapBoLoc();
        }

        private void btnTimKiem_Click(object sender, EventArgs e)
        {
            object maDM = null;
            object maNCC = null;

            if (cboLocDanhMuc.SelectedValue != null && Convert.ToInt32(cboLocDanhMuc.SelectedValue) > 0)
                maDM = Convert.ToInt32(cboLocDanhMuc.SelectedValue);

            if (cboLocNhaCC.SelectedValue != null && Convert.ToInt32(cboLocNhaCC.SelectedValue) > 0)
                maNCC = Convert.ToInt32(cboLocNhaCC.SelectedValue);

            dgvThuoc.DataSource = ThuocBUS.Instance.TimKiemVaLoc(txtTuKhoa.Text.Trim(), maDM, maNCC);
            DatTenCotBang();
        }

        private void btnBoLoc_Click(object sender, EventArgs e)
        {
            txtTuKhoa.Clear();
            cboLocDanhMuc.SelectedIndex = 0;
            cboLocNhaCC.SelectedIndex = 0;
            TaiDanhSachThuoc();
        }
    }
}
