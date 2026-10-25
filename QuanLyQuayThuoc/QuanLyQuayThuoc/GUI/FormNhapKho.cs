using System;
using System.Collections.Generic;
using System.Data;
using System.Globalization;
using System.Windows.Forms;
using QuanLyQuayThuoc.BUS;
using QuanLyQuayThuoc.DTO;

namespace QuanLyQuayThuoc.GUI
{
    public partial class FormNhapKho : Form
    {
        private readonly NhanVienDTO _nhanVienDangNhap;
        private DataTable _duLieuThuocNhap;

        public FormNhapKho(NhanVienDTO nhanVienDangNhap)
        {
            InitializeComponent();
            _nhanVienDangNhap = nhanVienDangNhap;
        }

        private void FormNhapKho_Load(object sender, EventArgs e)
        {
            txtNhanVien.Text = _nhanVienDangNhap.HoTen;
            dtpNgayNhap.Value = DateTime.Now;
            dtpTuNgay.Value = DateTime.Today.AddDays(-30);
            dtpDenNgay.Value = DateTime.Today;
            NapComboNhaCC();
            NapDanhSachThuocNhap();
            KhoiTaoBangChiTietNhap();
            CapNhatTongTien();
            TaiDanhSachPhieuNhap();
        }

        private void NapDanhSachThuocNhap()
        {
            _duLieuThuocNhap = ThuocBUS.Instance.LayDanhSachChoNhapKho();
        }

        private void NapComboNhaCC()
        {
            DataTable ncc = NhaCungCapBUS.Instance.LayDanhSach();
            cboNhaCC.DataSource = ncc;
            cboNhaCC.DisplayMember = "TenNCC";
            cboNhaCC.ValueMember = "MaNCC";

            DataTable nccLichSu = ncc.Copy();
            DataRow dongTatCa = nccLichSu.NewRow();
            dongTatCa["MaNCC"] = 0;
            dongTatCa["TenNCC"] = "Tất cả";
            nccLichSu.Rows.InsertAt(dongTatCa, 0);
        }

        private void KhoiTaoBangChiTietNhap()
        {
            dgvChiTiet.AllowUserToAddRows = true;
            dgvChiTiet.AllowUserToDeleteRows = true;
            dgvChiTiet.AutoGenerateColumns = false;
            dgvChiTiet.Columns.Clear();

            DataGridViewComboBoxColumn colThuoc = new DataGridViewComboBoxColumn();
            colThuoc.Name = "colThuoc";
            colThuoc.HeaderText = "Thuốc";
            colThuoc.DataSource = _duLieuThuocNhap;
            colThuoc.DisplayMember = "TenThuoc";
            colThuoc.ValueMember = "MaThuoc";
            colThuoc.DisplayStyle = DataGridViewComboBoxDisplayStyle.DropDownButton;
            colThuoc.Width = 220;
            dgvChiTiet.Columns.Add(colThuoc);

            dgvChiTiet.Columns.Add(TaoCotText("colSoLo", "Số lô", 120));
            dgvChiTiet.Columns.Add(TaoCotText("colHanSuDung", "Hạn sử dụng", 120));
            dgvChiTiet.Columns.Add(TaoCotText("colSoLuongNhap", "SL nhập", 90));
            DataGridViewComboBoxColumn colDonViNhap = new DataGridViewComboBoxColumn();
            colDonViNhap.Name = "colDonViNhap";
            colDonViNhap.HeaderText = "Đơn vị";
            colDonViNhap.Width = 90;
            colDonViNhap.DisplayStyle = DataGridViewComboBoxDisplayStyle.ComboBox;
            dgvChiTiet.Columns.Add(colDonViNhap);
            dgvChiTiet.Columns.Add(TaoCotText("colDonGiaNhap", "Đơn giá nhập", 120));

            DataGridViewTextBoxColumn colThanhTien = TaoCotText("colThanhTien", "Thành tiền", 120);
            colThanhTien.ReadOnly = true;
            dgvChiTiet.Columns.Add(colThanhTien);

            DataGridViewTextBoxColumn colTyLe = TaoCotText("colTyLeQuyDoi", "Tỷ lệ", 0);
            colTyLe.Visible = false;
            dgvChiTiet.Columns.Add(colTyLe);

            DataGridViewTextBoxColumn colDonViCoBan = TaoCotText("colDonViCoBan", "Đơn vị cơ bản", 0);
            colDonViCoBan.Visible = false;
            dgvChiTiet.Columns.Add(colDonViCoBan);
        }

        private DataGridViewTextBoxColumn TaoCotText(string name, string headerText, int width)
        {
            return new DataGridViewTextBoxColumn
            {
                Name = name,
                HeaderText = headerText,
                Width = width
            };
        }

        private void dgvChiTiet_CellValueChanged(object sender, DataGridViewCellEventArgs e)
        {
            if (e.RowIndex < 0)
                return;

            DataGridViewRow row = dgvChiTiet.Rows[e.RowIndex];
            if (row.IsNewRow)
                return;

            string tenCot = dgvChiTiet.Columns[e.ColumnIndex].Name;
            if (tenCot == "colThuoc")
                NapThongTinThuocChoDong(row);

            if (tenCot == "colThuoc" || tenCot == "colSoLuongNhap" || tenCot == "colDonGiaNhap" || tenCot == "colDonViNhap")
            {
                TinhThanhTienDong(e.RowIndex);
                CapNhatTongTien();
            }
        }

        private void dgvChiTiet_CurrentCellDirtyStateChanged(object sender, EventArgs e)
        {
            if (dgvChiTiet.IsCurrentCellDirty)
                dgvChiTiet.CommitEdit(DataGridViewDataErrorContexts.Commit);
        }

        private void dgvChiTiet_CellEndEdit(object sender, DataGridViewCellEventArgs e)
        {
            if (e.RowIndex < 0)
                return;

            TinhThanhTienDong(e.RowIndex);
            CapNhatTongTien();
        }

        private void NapThongTinThuocChoDong(DataGridViewRow row)
        {
            if (row == null || row.IsNewRow || row.Cells["colThuoc"].Value == null)
                return;

            int maThuoc = Convert.ToInt32(row.Cells["colThuoc"].Value);
            DataRow[] rows = _duLieuThuocNhap.Select("MaThuoc = " + maThuoc);
            if (rows.Length == 0)
                return;

            DataRow data = rows[0];
            string donViCoBan = Convert.ToString(data["DonViTinh"]);
            string donViQuyDoi = data.Table.Columns.Contains("DonViQuyDoi") && data["DonViQuyDoi"] != DBNull.Value
                ? Convert.ToString(data["DonViQuyDoi"])
                : string.Empty;
            int tyLeQuyDoi = data.Table.Columns.Contains("TyLeQuyDoi") && data["TyLeQuyDoi"] != DBNull.Value
                ? Convert.ToInt32(data["TyLeQuyDoi"])
                : 1;

            row.Cells["colDonViCoBan"].Value = donViCoBan;
            row.Cells["colTyLeQuyDoi"].Value = tyLeQuyDoi;

            DataGridViewComboBoxCell unitCell = (DataGridViewComboBoxCell)row.Cells["colDonViNhap"];
            unitCell.Items.Clear();
            if (!string.IsNullOrWhiteSpace(donViCoBan))
                unitCell.Items.Add(donViCoBan);

            if (!string.IsNullOrWhiteSpace(donViQuyDoi) && tyLeQuyDoi > 1 && !unitCell.Items.Contains(donViQuyDoi))
                unitCell.Items.Add(donViQuyDoi);

            row.Cells["colDonViNhap"].Value = donViCoBan;
        }

        private void dgvChiTiet_RowsRemoved(object sender, DataGridViewRowsRemovedEventArgs e)
        {
            CapNhatTongTien();
        }

        private void dgvChiTiet_UserAddedRow(object sender, DataGridViewRowEventArgs e)
        {
            CapNhatTongTien();
        }

        private void TinhThanhTienDong(int rowIndex)
        {
            if (rowIndex < 0 || rowIndex >= dgvChiTiet.Rows.Count)
                return;

            DataGridViewRow row = dgvChiTiet.Rows[rowIndex];
            if (row.IsNewRow)
                return;

            int soLuong = DocInt(row.Cells["colSoLuongNhap"].Value);
            decimal donGia = DocDecimal(row.Cells["colDonGiaNhap"].Value);
            row.Cells["colThanhTien"].Value = DinhDangTien(soLuong * donGia);
        }

        private int DocInt(object value)
        {
            int ketQua;
            return int.TryParse(Convert.ToString(value), out ketQua) ? ketQua : 0;
        }

        private decimal DocDecimal(object value)
        {
            string text = Convert.ToString(value);
            if (string.IsNullOrWhiteSpace(text))
                return 0;

            decimal ketQua;
            text = text.Trim();
            if (decimal.TryParse(text, NumberStyles.Any, CultureInfo.CurrentCulture, out ketQua))
                return ketQua;

            if (decimal.TryParse(text, NumberStyles.Any, CultureInfo.InvariantCulture, out ketQua))
                return ketQua;

            text = text.Replace(" ", "");
            string dauPhanCachHangNghin = CultureInfo.CurrentCulture.NumberFormat.NumberGroupSeparator;
            string dauThapPhan = CultureInfo.CurrentCulture.NumberFormat.NumberDecimalSeparator;

            if (!string.IsNullOrEmpty(dauPhanCachHangNghin))
                text = text.Replace(dauPhanCachHangNghin, "");

            if (!string.IsNullOrEmpty(dauThapPhan) && dauThapPhan != ".")
                text = text.Replace(dauThapPhan, ".");

            return decimal.TryParse(text, NumberStyles.Any, CultureInfo.InvariantCulture, out ketQua) ? ketQua : 0;
        }

        private string DinhDangTien(decimal soTien)
        {
            return soTien.ToString("#,0.##", CultureInfo.CurrentCulture);
        }

        private void DinhDangCotTien(DataGridView grid, string tenCot)
        {
            if (grid.Columns[tenCot] == null)
                return;

            grid.Columns[tenCot].DefaultCellStyle.Format = "#,0.##";
            grid.Columns[tenCot].DefaultCellStyle.Alignment = DataGridViewContentAlignment.MiddleRight;
        }

        private void CapNhatTongTien()
        {
            decimal tongTien = 0;
            foreach (DataGridViewRow row in dgvChiTiet.Rows)
            {
                if (row.IsNewRow)
                    continue;
                tongTien += DocInt(row.Cells["colSoLuongNhap"].Value) * DocDecimal(row.Cells["colDonGiaNhap"].Value);
            }

            txtTongTien.Text = DinhDangTien(tongTien);
        }

        private void txtDaThanhToan_Leave(object sender, EventArgs e)
        {
            txtDaThanhToan.Text = DinhDangTien(DocDecimal(txtDaThanhToan.Text));
        }

        private List<ChiTietNhapDTO> LayChiTietTuBang()
        {
            List<ChiTietNhapDTO> danhSach = new List<ChiTietNhapDTO>();
            foreach (DataGridViewRow row in dgvChiTiet.Rows)
            {
                if (row.IsNewRow)
                    continue;

                if (row.Cells["colThuoc"].Value == null &&
                    string.IsNullOrWhiteSpace(Convert.ToString(row.Cells["colSoLo"].Value)) &&
                    string.IsNullOrWhiteSpace(Convert.ToString(row.Cells["colHanSuDung"].Value)))
                    continue;

                DateTime hanSuDung;
                if (!DateTime.TryParse(Convert.ToString(row.Cells["colHanSuDung"].Value), out hanSuDung))
                    throw new Exception("Hạn sử dụng phải đúng định dạng ngày hợp lệ.");

                string donViNhap = Convert.ToString(row.Cells["colDonViNhap"].Value);
                string donViCoBan = Convert.ToString(row.Cells["colDonViCoBan"].Value);
                int tyLeQuyDoi = DocInt(row.Cells["colTyLeQuyDoi"].Value);
                if (tyLeQuyDoi <= 0) tyLeQuyDoi = 1;

                int soLuongThucTe = DocInt(row.Cells["colSoLuongNhap"].Value);
                decimal donGiaThucTe = DocDecimal(row.Cells["colDonGiaNhap"].Value);

                if (donViNhap == donViCoBan && tyLeQuyDoi > 1)
                {
                    soLuongThucTe = soLuongThucTe * tyLeQuyDoi;
                    donGiaThucTe = donGiaThucTe / tyLeQuyDoi;
                }

                ChiTietNhapDTO chiTiet = new ChiTietNhapDTO
                {
                    MaThuoc = row.Cells["colThuoc"].Value == null ? 0 : Convert.ToInt32(row.Cells["colThuoc"].Value),
                    SoLo = Convert.ToString(row.Cells["colSoLo"].Value),
                    HanSuDung = hanSuDung,
                    SoLuongNhap = soLuongThucTe,
                    DonGiaNhap = donGiaThucTe
                };
                danhSach.Add(chiTiet);
            }
            return danhSach;
        }

        private void btnLuuPhieu_Click(object sender, EventArgs e)
        {
            try
            {
                List<ChiTietNhapDTO> chiTietNhapList = LayChiTietTuBang();

                PhieuNhapDTO phieuNhap = new PhieuNhapDTO
                {
                    MaNV = _nhanVienDangNhap.MaNV,
                    MaNCC = cboNhaCC.SelectedValue == null ? (int?)null : Convert.ToInt32(cboNhaCC.SelectedValue),
                    NgayNhap = dtpNgayNhap.Value,
                    SoHoaDonNCC = txtSoHoaDonNCC.Text.Trim(),
                    DaThanhToan = DocDecimal(txtDaThanhToan.Text),
                    GhiChu = txtGhiChu.Text.Trim()
                };

                if (PhieuNhapBUS.Instance.LuuPhieuNhap(phieuNhap, chiTietNhapList, out string thongBao, out int maPhieuNhap))
                {
                    MessageBox.Show("Lưu phiếu nhập thành công. Mã phiếu: " + maPhieuNhap, "Thông báo", MessageBoxButtons.OK, MessageBoxIcon.Information);
                    btnLamMoi_Click(null, null);
                    TaiDanhSachPhieuNhap();
                    tabNhapKho.SelectedTab = tabLichSu;
                }
                else
                {
                    MessageBox.Show(thongBao, "Lỗi", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message, "Lỗi", MessageBoxButtons.OK, MessageBoxIcon.Warning);
            }
        }

        private void btnLamMoi_Click(object sender, EventArgs e)
        {
            dtpNgayNhap.Value = DateTime.Now;
            txtSoHoaDonNCC.Clear();
            txtDaThanhToan.Text = "0";
            txtGhiChu.Clear();
            dgvChiTiet.Rows.Clear();
            CapNhatTongTien();
        }

        private void TaiDanhSachPhieuNhap()
        {
            dgvLichSuPhieuNhap.DataSource = PhieuNhapBUS.Instance.LayDanhSachPhieuNhap(
                dtpTuNgay.Value,
                dtpDenNgay.Value,
                txtTuKhoaLichSu.Text.Trim());

            DatTenCotLichSu();

            if (dgvLichSuPhieuNhap.Rows.Count > 0)
            {
                dgvLichSuPhieuNhap.Rows[0].Selected = true;
                TaiChiTietPhieuNhapDaChon();
            }
            else
            {
                dgvChiTietLichSu.DataSource = null;
                lblChiTietPhieu.Text = "Chi tiết phiếu nhập: (chưa chọn)";
            }
        }

        private void DatTenCotLichSu()
        {
            if (dgvLichSuPhieuNhap.Columns["MaPhieuNhap"] != null) dgvLichSuPhieuNhap.Columns["MaPhieuNhap"].HeaderText = "Mã phiếu";
            if (dgvLichSuPhieuNhap.Columns["NgayNhap"] != null) dgvLichSuPhieuNhap.Columns["NgayNhap"].HeaderText = "Ngày nhập";
            if (dgvLichSuPhieuNhap.Columns["TenNCC"] != null) dgvLichSuPhieuNhap.Columns["TenNCC"].HeaderText = "Nhà cung cấp";
            if (dgvLichSuPhieuNhap.Columns["HoTen"] != null) dgvLichSuPhieuNhap.Columns["HoTen"].HeaderText = "Nhân viên";
            if (dgvLichSuPhieuNhap.Columns["SoHoaDonNCC"] != null) dgvLichSuPhieuNhap.Columns["SoHoaDonNCC"].HeaderText = "Số hóa đơn NCC";
            if (dgvLichSuPhieuNhap.Columns["TongTien"] != null) dgvLichSuPhieuNhap.Columns["TongTien"].HeaderText = "Tổng tiền";
            if (dgvLichSuPhieuNhap.Columns["DaThanhToan"] != null) dgvLichSuPhieuNhap.Columns["DaThanhToan"].HeaderText = "Đã thanh toán";
            if (dgvLichSuPhieuNhap.Columns["GhiChu"] != null) dgvLichSuPhieuNhap.Columns["GhiChu"].HeaderText = "Ghi chú";

            DinhDangCotTien(dgvLichSuPhieuNhap, "TongTien");
            DinhDangCotTien(dgvLichSuPhieuNhap, "DaThanhToan");
        }

        private void TaiChiTietPhieuNhapDaChon()
        {
            if (dgvLichSuPhieuNhap.CurrentRow == null || dgvLichSuPhieuNhap.CurrentRow.IsNewRow)
                return;

            object maPhieu = dgvLichSuPhieuNhap.CurrentRow.Cells["MaPhieuNhap"].Value;
            if (maPhieu == null || maPhieu == DBNull.Value)
                return;

            int maPhieuNhap = Convert.ToInt32(maPhieu);
            dgvChiTietLichSu.DataSource = PhieuNhapBUS.Instance.LayChiTietPhieuNhap(maPhieuNhap);
            lblChiTietPhieu.Text = "Chi tiết phiếu nhập: " + maPhieuNhap;
            DatTenCotChiTietLichSu();
        }

        private void DatTenCotChiTietLichSu()
        {
            if (dgvChiTietLichSu.Columns["MaThuoc"] != null) dgvChiTietLichSu.Columns["MaThuoc"].HeaderText = "Mã thuốc";
            if (dgvChiTietLichSu.Columns["TenThuoc"] != null) dgvChiTietLichSu.Columns["TenThuoc"].HeaderText = "Tên thuốc";
            if (dgvChiTietLichSu.Columns["SoLo"] != null) dgvChiTietLichSu.Columns["SoLo"].HeaderText = "Số lô";
            if (dgvChiTietLichSu.Columns["HanSuDung"] != null) dgvChiTietLichSu.Columns["HanSuDung"].HeaderText = "Hạn sử dụng";
            if (dgvChiTietLichSu.Columns["SoLuongNhap"] != null) dgvChiTietLichSu.Columns["SoLuongNhap"].HeaderText = "SL nhập";
            if (dgvChiTietLichSu.Columns["DonGiaNhap"] != null) dgvChiTietLichSu.Columns["DonGiaNhap"].HeaderText = "Đơn giá nhập";
            if (dgvChiTietLichSu.Columns["ThanhTien"] != null) dgvChiTietLichSu.Columns["ThanhTien"].HeaderText = "Thành tiền";
            if (dgvChiTietLichSu.Columns["DonViTinh"] != null) dgvChiTietLichSu.Columns["DonViTinh"].HeaderText = "Đơn vị";

            DinhDangCotTien(dgvChiTietLichSu, "DonGiaNhap");
            DinhDangCotTien(dgvChiTietLichSu, "ThanhTien");
        }

        private void btnTimLichSu_Click(object sender, EventArgs e)
        {
            TaiDanhSachPhieuNhap();
        }

        private void dgvLichSuPhieuNhap_SelectionChanged(object sender, EventArgs e)
        {
            TaiChiTietPhieuNhapDaChon();
        }
    }
}
