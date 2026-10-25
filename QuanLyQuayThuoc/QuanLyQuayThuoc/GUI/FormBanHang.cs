using System;
using System.Collections.Generic;
using System.Data;
using System.Globalization;
using System.Drawing;
using System.Windows.Forms;
using QuanLyQuayThuoc.BUS;
using QuanLyQuayThuoc.DTO;

namespace QuanLyQuayThuoc.GUI
{
    public partial class FormBanHang : Form
    {
        private readonly NhanVienDTO _nhanVienDangNhap;
        private DataTable _duLieuLoBanHang;
        private DataTable _duLieuKhachHang;
        private List<HoaDonTamDTO> _danhSachLuuTam = new List<HoaDonTamDTO>();

        public FormBanHang(NhanVienDTO nhanVienDangNhap)
        {
            InitializeComponent();
            _nhanVienDangNhap = nhanVienDangNhap;
        }

        private void FormBanHang_Load(object sender, EventArgs e)
        {
            txtNhanVien.Text = _nhanVienDangNhap.HoTen;
            dtpNgayBan.Value = DateTime.Now;
            dtpTuNgay.Value = DateTime.Today.AddDays(-30);
            dtpDenNgay.Value = DateTime.Today;
            NapDuLieuLoBanHang();
            NapDanhSachKhachHang();
            KhoiTaoBangChiTietBan();
            CapNhatTongTien();
            cboLoaiKhach.SelectedIndex = 0;
            CapNhatTrangThaiKhachHang();
            TaiDanhSachHoaDon();
        }

        private void NapDuLieuLoBanHang()
        {
            _duLieuLoBanHang = TonKhoBUS.Instance.LayDanhSachLoBanHang();
        }

        private void NapDanhSachKhachHang(string tuKhoa = "")
        {
            _duLieuKhachHang = KhachHangBUS.Instance.LayDanhSach(tuKhoa);
            cboKhachHang.DataSource = null;

            if (_duLieuKhachHang.Rows.Count == 0)
                return;

            cboKhachHang.DataSource = _duLieuKhachHang;
            cboKhachHang.DisplayMember = "TenKH";
            cboKhachHang.ValueMember = "MaKH";
            cboKhachHang.SelectedIndex = 0;
        }

        private void CapNhatTrangThaiKhachHang()
        {
            bool laKhachLauDai = cboLoaiKhach.SelectedIndex == 1;
            txtTimKhachHang.Enabled = laKhachLauDai;
            btnTimKhachHang.Enabled = laKhachLauDai;
            cboKhachHang.Enabled = laKhachLauDai;
            btnQuanLyKhachHang.Enabled = true;
        }

        private void KhoiTaoBangChiTietBan()
        {
            dgvChiTiet.AllowUserToAddRows = true;
            dgvChiTiet.AllowUserToDeleteRows = true;
            dgvChiTiet.AutoGenerateColumns = false;
            dgvChiTiet.Columns.Clear();

            DataGridViewComboBoxColumn colLo = new DataGridViewComboBoxColumn();
            colLo.Name = "colLo";
            colLo.HeaderText = "Lô thuốc";
            colLo.DataSource = _duLieuLoBanHang;
            colLo.DisplayMember = "SoLo";
            colLo.ValueMember = "SoLo";
            colLo.DisplayStyle = DataGridViewComboBoxDisplayStyle.DropDownButton;
            colLo.Width = 140;
            dgvChiTiet.Columns.Add(colLo);

            DataGridViewTextBoxColumn colTenThuoc = TaoCotText("colTenThuoc", "Tên thuốc", 220);
            colTenThuoc.ReadOnly = true;
            dgvChiTiet.Columns.Add(colTenThuoc);

            DataGridViewTextBoxColumn colMaThuoc = TaoCotText("colMaThuoc", "Mã thuốc", 80);
            colMaThuoc.ReadOnly = true;
            dgvChiTiet.Columns.Add(colMaThuoc);

            DataGridViewTextBoxColumn colTonKho = TaoCotText("colSoLuongTon", "Tồn (ĐV nhỏ)", 90);
            colTonKho.ReadOnly = true;
            dgvChiTiet.Columns.Add(colTonKho);

            dgvChiTiet.Columns.Add(TaoCotText("colSoLuongBan", "SL bán", 80));

            DataGridViewComboBoxColumn colDonVi = new DataGridViewComboBoxColumn();
            colDonVi.Name = "colDonViBan";
            colDonVi.HeaderText = "Đơn vị";
            colDonVi.Width = 80;
            colDonVi.DisplayStyle = DataGridViewComboBoxDisplayStyle.ComboBox;
            dgvChiTiet.Columns.Add(colDonVi);

            DataGridViewTextBoxColumn colDonGiaBan = TaoCotText("colDonGiaBan", "Đơn giá bán", 120);
            colDonGiaBan.ReadOnly = true;
            dgvChiTiet.Columns.Add(colDonGiaBan);

            DataGridViewTextBoxColumn colThanhTien = TaoCotText("colThanhTien", "Thành tiền", 120);
            colThanhTien.ReadOnly = true;
            dgvChiTiet.Columns.Add(colThanhTien);

            DataGridViewTextBoxColumn colTyLe = TaoCotText("colTyLeQuyDoi", "Tỷ lệ", 0);
            colTyLe.Visible = false;
            dgvChiTiet.Columns.Add(colTyLe);

            DataGridViewTextBoxColumn colGiaGoc = TaoCotText("colGiaGoc", "Giá gốc", 0);
            colGiaGoc.Visible = false;
            dgvChiTiet.Columns.Add(colGiaGoc);

            DataGridViewTextBoxColumn colDonViCoBan = TaoCotText("colDonViCoBan", "Đơn vị gốc", 0);
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

            if (dgvChiTiet.Columns[e.ColumnIndex].Name == "colLo")
            {
                NapThongTinLoChoDong(row);
            }

            if (dgvChiTiet.Columns[e.ColumnIndex].Name == "colDonViBan")
            {
                CapNhatGiaTheoDonVi(row);
                TinhThanhTienDong(row);
                KiemTraSoLuongBanVaTonKhoHoacHetHang(row);
            }

            if (dgvChiTiet.Columns[e.ColumnIndex].Name == "colSoLuongBan")
            {
                TinhThanhTienDong(row);
                KiemTraSoLuongBanVaTonKhoHoacHetHang(row);
            }

            CapNhatTongTien();
        }

        private void dgvChiTiet_CurrentCellDirtyStateChanged(object sender, EventArgs e)
        {
            if (dgvChiTiet.IsCurrentCellDirty)
                dgvChiTiet.CommitEdit(DataGridViewDataErrorContexts.Commit);
        }

        private void dgvChiTiet_RowsRemoved(object sender, DataGridViewRowsRemovedEventArgs e)
        {
            CapNhatTongTien();
        }

        private void LamRongDongChiTietTheoHetHang(DataGridViewRow row)
        {
            row.Cells["colLo"].Value = null;
            row.Cells["colTenThuoc"].Value = null;
            row.Cells["colMaThuoc"].Value = null;
            row.Cells["colSoLuongTon"].Value = null;
            row.Cells["colGiaGoc"].Value = null;
            row.Cells["colTyLeQuyDoi"].Value = null;
            row.Cells["colDonViCoBan"].Value = null;
            row.Cells["colDonViBan"].Value = null;
            row.Cells["colDonGiaBan"].Value = null;
            row.Cells["colSoLuongBan"].Value = null;
            row.Cells["colThanhTien"].Value = null;
            ((DataGridViewComboBoxCell)row.Cells["colDonViBan"]).Items.Clear();
        }

        private int LaySoLuongBanQuyVeDonViNho(DataGridViewRow row)
        {
            if (row.IsNewRow)
                return 0;

            string donViBan = Convert.ToString(row.Cells["colDonViBan"].Value);
            string donViCoBan = Convert.ToString(row.Cells["colDonViCoBan"].Value);
            int tyLeQuyDoi = DocInt(row.Cells["colTyLeQuyDoi"].Value);
            if (tyLeQuyDoi <= 0)
                tyLeQuyDoi = 1;

            int soLuongBanHienThi = DocInt(row.Cells["colSoLuongBan"].Value);
            int soLuongThucTe = soLuongBanHienThi;
            if (donViBan == donViCoBan && tyLeQuyDoi > 1)
                soLuongThucTe = soLuongBanHienThi * tyLeQuyDoi;

            return soLuongThucTe;
        }

        /// <summary>
        /// Khi đã chọn lô: không cho nhập SL vượt tồn (theo đơn vị nhỏ), chặn nếu tồn = 0.
        /// </summary>
        private void KiemTraSoLuongBanVaTonKhoHoacHetHang(DataGridViewRow row)
        {
            string soLo = Convert.ToString(row.Cells["colLo"].Value);
            if (string.IsNullOrWhiteSpace(soLo))
                return;

            int maThuoc = DocInt(row.Cells["colMaThuoc"].Value);
            if (maThuoc <= 0)
                return;

            int tonKho = DocInt(row.Cells["colSoLuongTon"].Value);
            int soLuongTheoDvNho = LaySoLuongBanQuyVeDonViNho(row);

            if (tonKho <= 0)
            {
                MessageBox.Show("Lô thuốc này đã HẾT HÀNG! Vui lòng nhập hàng hoặc chọn lô khác.",
                    "Không đủ tồn kho",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Error);
                LamRongDongChiTietTheoHetHang(row);
                return;
            }

            if (soLuongTheoDvNho > tonKho)
            {
                MessageBox.Show($"Số lượng bán ({soLuongTheoDvNho} đơn vị nhỏ) vượt quá tồn kho ({tonKho}) của lô này. Vui lòng giảm số lượng hoặc chọn lô khác.",
                    "Không đủ tồn kho",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Warning);
                row.Cells["colSoLuongBan"].Value = null;
                TinhThanhTienDong(row);
            }
        }

        private void NapThongTinLoChoDong(DataGridViewRow row)
        {
            string soLo = Convert.ToString(row.Cells["colLo"].Value);
            if (string.IsNullOrWhiteSpace(soLo))
                return;

            DataRow[] rows = _duLieuLoBanHang.Select("SoLo = '" + soLo.Replace("'", "''") + "'");
            if (rows.Length == 0)
                return;

            DataRow data = rows[0];
            int tonKho = data["SoLuongTon"] == DBNull.Value ? 0 : Convert.ToInt32(data["SoLuongTon"]);
            if (tonKho <= 0)
            {
                MessageBox.Show("Lô thuốc này đã HẾT HÀNG! Vui lòng nhập hàng hoặc chọn lô khác.",
                    "Không đủ tồn kho",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Error);
                LamRongDongChiTietTheoHetHang(row);
                return;
            }

            row.Cells["colTenThuoc"].Value = data["TenThuoc"];
            row.Cells["colMaThuoc"].Value = data["MaThuoc"];
            row.Cells["colSoLuongTon"].Value = tonKho;

            decimal giaBanGoc = Convert.ToDecimal(data["GiaBan"]);
            row.Cells["colGiaGoc"].Value = giaBanGoc;

            int tyLe = data.Table.Columns.Contains("TyLeQuyDoi") && data["TyLeQuyDoi"] != DBNull.Value ? Convert.ToInt32(data["TyLeQuyDoi"]) : 1;
            row.Cells["colTyLeQuyDoi"].Value = tyLe;

            string donViCoBan = data["DonViTinh"].ToString();
            row.Cells["colDonViCoBan"].Value = donViCoBan;

            DataGridViewComboBoxCell unitCell = (DataGridViewComboBoxCell)row.Cells["colDonViBan"];
            unitCell.Items.Clear();
            unitCell.Items.Add(donViCoBan);

            string donViQuyDoi = data.Table.Columns.Contains("DonViQuyDoi") && data["DonViQuyDoi"] != DBNull.Value ? data["DonViQuyDoi"].ToString() : "";
            if (!string.IsNullOrEmpty(donViQuyDoi) && tyLe > 1) 
            {
                if (!unitCell.Items.Contains(donViQuyDoi))
                    unitCell.Items.Add(donViQuyDoi);
            }

            row.Cells["colDonViBan"].Value = donViCoBan;

            row.Cells["colDonGiaBan"].Value = DinhDangTien(giaBanGoc);
            TinhThanhTienDong(row);
        }

        private void CapNhatGiaTheoDonVi(DataGridViewRow row)
        {
            string donViChon = Convert.ToString(row.Cells["colDonViBan"].Value);
            string donViCoBan = Convert.ToString(row.Cells["colDonViCoBan"].Value);
            decimal giaGoc = DocDecimal(row.Cells["colGiaGoc"].Value);
            int tyLe = DocInt(row.Cells["colTyLeQuyDoi"].Value);

            if (string.IsNullOrEmpty(donViChon) || donViChon == donViCoBan || tyLe <= 1)
            {
                row.Cells["colDonGiaBan"].Value = DinhDangTien(giaGoc);
            }
            else
            {
                decimal giaQuyDoi = giaGoc / tyLe;
                row.Cells["colDonGiaBan"].Value = DinhDangTien(giaQuyDoi);
            }
        }

        private void TinhThanhTienDong(DataGridViewRow row)
        {
            if (row == null || row.IsNewRow)
                return;

            int soLuongBan = DocInt(row.Cells["colSoLuongBan"].Value);
            decimal donGiaBan = DocDecimal(row.Cells["colDonGiaBan"].Value);
            row.Cells["colThanhTien"].Value = DinhDangTien(soLuongBan * donGiaBan);
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
                tongTien += DocInt(row.Cells["colSoLuongBan"].Value) * DocDecimal(row.Cells["colDonGiaBan"].Value);
            }

            txtTongTien.Text = DinhDangTien(tongTien);
            CapNhatThanhToanTuDong();
        }

        private void CapNhatThanhToanTuDong()
        {
            decimal tongTien = DocDecimal(txtTongTien.Text);
            decimal giamGia = DocDecimal(txtGiamGia.Text);
            decimal thanhToan = tongTien - giamGia;
            if (thanhToan < 0)
                thanhToan = 0;

            txtThanhToan.Text = DinhDangTien(thanhToan);
        }

        private void txtGiamGia_TextChanged(object sender, EventArgs e)
        {
            CapNhatThanhToanTuDong();
        }

        private void txtGiamGia_Leave(object sender, EventArgs e)
        {
            txtGiamGia.Text = DinhDangTien(DocDecimal(txtGiamGia.Text));
            CapNhatThanhToanTuDong();
        }

        private List<ChiTietHoaDonDTO> LayChiTietTuBang()
        {
            Dictionary<string, ChiTietHoaDonDTO> chiTietTheoLo = new Dictionary<string, ChiTietHoaDonDTO>(StringComparer.OrdinalIgnoreCase);
            Dictionary<string, int> tonKhoTheoLo = new Dictionary<string, int>(StringComparer.OrdinalIgnoreCase);
            foreach (DataGridViewRow row in dgvChiTiet.Rows)
            {
                if (row.IsNewRow)
                    continue;

                if (row.Cells["colLo"].Value == null &&
                    string.IsNullOrWhiteSpace(Convert.ToString(row.Cells["colSoLuongBan"].Value)))
                    continue;

                string donViBan = Convert.ToString(row.Cells["colDonViBan"].Value);
                string donViCoBan = Convert.ToString(row.Cells["colDonViCoBan"].Value);
                int tyLeQuyDoi = DocInt(row.Cells["colTyLeQuyDoi"].Value);
                if (tyLeQuyDoi <= 0) tyLeQuyDoi = 1;

                int soLuongThucTe = LaySoLuongBanQuyVeDonViNho(row);
                decimal donGiaThucTe = DocDecimal(row.Cells["colDonGiaBan"].Value);

                if (donViBan == donViCoBan && tyLeQuyDoi > 1)
                    donGiaThucTe = donGiaThucTe / tyLeQuyDoi;

                ChiTietHoaDonDTO chiTiet = new ChiTietHoaDonDTO
                {
                    SoLo = Convert.ToString(row.Cells["colLo"].Value),
                    TenThuoc = Convert.ToString(row.Cells["colTenThuoc"].Value),
                    MaThuoc = DocInt(row.Cells["colMaThuoc"].Value),
                    DonViBan = donViBan,
                    SoLuongBanHienThi = DocInt(row.Cells["colSoLuongBan"].Value),
                    DonGiaBanHienThi = DocDecimal(row.Cells["colDonGiaBan"].Value),
                    SoLuongBan = soLuongThucTe,
                    DonGiaBan = donGiaThucTe
                };

                int tonKho = DocInt(row.Cells["colSoLuongTon"].Value);
                string khoaChiTiet = chiTiet.MaThuoc + "|" + chiTiet.SoLo;
                tonKhoTheoLo[khoaChiTiet] = tonKho;

                if (chiTietTheoLo.TryGetValue(khoaChiTiet, out ChiTietHoaDonDTO chiTietDaCo))
                {
                    if (chiTietDaCo.DonGiaBan != chiTiet.DonGiaBan)
                        throw new Exception("Không thể lưu cùng một lô thuốc với nhiều đơn giá khác nhau trong cùng hóa đơn.");

                    chiTietDaCo.SoLuongBan += chiTiet.SoLuongBan;

                    if (chiTietDaCo.DonViBan == chiTiet.DonViBan &&
                        chiTietDaCo.DonGiaBanHienThi == chiTiet.DonGiaBanHienThi)
                    {
                        chiTietDaCo.SoLuongBanHienThi += chiTiet.SoLuongBanHienThi;
                    }
                    else
                    {
                        chiTietDaCo.DonViBan = string.Empty;
                        chiTietDaCo.SoLuongBanHienThi = chiTietDaCo.SoLuongBan;
                        chiTietDaCo.DonGiaBanHienThi = chiTietDaCo.DonGiaBan;
                    }
                }
                else
                {
                    chiTietTheoLo[khoaChiTiet] = chiTiet;
                }

                int tonHienTai = tonKhoTheoLo[khoaChiTiet];
                if (tonHienTai <= 0 && chiTietTheoLo[khoaChiTiet].SoLuongBan > 0)
                    throw new Exception("Lô thuốc này đã HẾT HÀNG! Vui lòng nhập hàng hoặc chọn lô khác.");

                if (chiTietTheoLo[khoaChiTiet].SoLuongBan > tonHienTai)
                    throw new Exception("Tổng số lượng bán không được lớn hơn tồn kho (quy đổi) của lô đã chọn.");
            }

            return new List<ChiTietHoaDonDTO>(chiTietTheoLo.Values);
        }

        private void btnLuuHoaDon_Click(object sender, EventArgs e)
        {
            try
            {
                List<ChiTietHoaDonDTO> chiTietHoaDonList = LayChiTietTuBang();
                int? maKH = LayMaKhachHangDaChon();

                if (cboLoaiKhach.SelectedIndex == 1 && !maKH.HasValue)
                {
                    MessageBox.Show("Vui lòng chọn khách hàng lâu dài trước khi lưu hóa đơn.", "Thông báo", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                    return;
                }

                HoaDonDTO hoaDon = new HoaDonDTO
                {
                    MaNV = _nhanVienDangNhap.MaNV,
                    MaKH = maKH,
                    NgayBan = dtpNgayBan.Value,
                    GiamGia = DocDecimal(txtGiamGia.Text),
                    ThanhToan = DocDecimal(txtThanhToan.Text),
                    GhiChu = txtGhiChu.Text.Trim()
                };

                if (HoaDonBUS.Instance.LuuHoaDon(hoaDon, chiTietHoaDonList, out string thongBao, out int maHD))
                {
                    MessageBox.Show("Lưu hóa đơn thành công. Mã hóa đơn: " + maHD, "Thông báo", MessageBoxButtons.OK, MessageBoxIcon.Information);
                    btnLamMoi_Click(null, null);
                    NapDuLieuLoBanHang();
                    KhoiTaoBangChiTietBan();
                    TaiDanhSachHoaDon();
                    tabBanHang.SelectedTab = tabLichSu;
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
            dtpNgayBan.Value = DateTime.Now;
            txtGiamGia.Text = "0";
            txtThanhToan.Text = "0";
            txtGhiChu.Clear();
            dgvChiTiet.Rows.Clear();
            CapNhatTongTien();
        }

        private void btnLuuTam_Click(object sender, EventArgs e)
        {
            if (dgvChiTiet.Rows.Count == 0 || (dgvChiTiet.Rows.Count == 1 && dgvChiTiet.Rows[0].IsNewRow))
            {
                MessageBox.Show("Không có dữ liệu thuốc để lưu tạm.", "Thông báo", MessageBoxButtons.OK, MessageBoxIcon.Information);
                return;
            }

            try
            {
                List<ChiTietHoaDonDTO> chiTietList = LayChiTietTuBang();
                if (chiTietList.Count == 0)
                {
                    MessageBox.Show("Không có thuốc nào hợp lệ để lưu.", "Thông báo", MessageBoxButtons.OK, MessageBoxIcon.Information);
                    return;
                }

                int? maKH = LayMaKhachHangDaChon();
                string tenKhach = maKH.HasValue ? cboKhachHang.Text : "Khách lẻ";
                string idLuuTam = $"Đơn {DateTime.Now:HH:mm:ss} - {tenKhach}";

                HoaDonTamDTO donTam = new HoaDonTamDTO
                {
                    IdLuuTam = idLuuTam,
                    MaKH = maKH,
                    LoaiKhachIndex = cboLoaiKhach.SelectedIndex,
                    TenKhachHangTam = tenKhach,
                    GiamGia = DocDecimal(txtGiamGia.Text),
                    ThanhToan = DocDecimal(txtThanhToan.Text),
                    GhiChu = txtGhiChu.Text.Trim(),
                    ChiTietList = chiTietList
                };

                _danhSachLuuTam.Add(donTam);
                btnDanhSachLuuTam.Text = $"Đơn tạm ({_danhSachLuuTam.Count})";

                btnLamMoi_Click(null, null);
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message, "Lỗi", MessageBoxButtons.OK, MessageBoxIcon.Warning);
            }
        }

        private void btnDanhSachLuuTam_Click(object sender, EventArgs e)
        {
            if (_danhSachLuuTam.Count == 0)
            {
                MessageBox.Show("Chưa có đơn hàng nào được lưu tạm.", "Thông báo", MessageBoxButtons.OK, MessageBoxIcon.Information);
                return;
            }

            ContextMenuStrip menu = new ContextMenuStrip();
            foreach (var donTam in _danhSachLuuTam)
            {
                var menuItem = new ToolStripMenuItem(donTam.IdLuuTam);
                menuItem.Tag = donTam;
                menuItem.Click += MenuItem_PhucHoiDon_Click;
                menu.Items.Add(menuItem);
            }

            menu.Show(btnDanhSachLuuTam, new Point(0, btnDanhSachLuuTam.Height));
        }

        private void MenuItem_PhucHoiDon_Click(object sender, EventArgs e)
        {
            ToolStripMenuItem item = sender as ToolStripMenuItem;
            if (item != null && item.Tag is HoaDonTamDTO donTam)
            {
                PhucHoiDonTam(donTam);
            }
        }

        private void PhucHoiDonTam(HoaDonTamDTO donTam)
        {
            btnLamMoi_Click(null, null);

            cboLoaiKhach.SelectedIndex = donTam.LoaiKhachIndex;
            if (donTam.MaKH.HasValue)
            {
                cboKhachHang.SelectedValue = donTam.MaKH.Value;
            }

            txtGiamGia.Text = donTam.GiamGia.ToString("0.##", CultureInfo.CurrentCulture);
            txtThanhToan.Text = donTam.ThanhToan.ToString("0.##", CultureInfo.CurrentCulture);
            txtGhiChu.Text = donTam.GhiChu;

            foreach (var chiTiet in donTam.ChiTietList)
            {
                int rowIndex = dgvChiTiet.Rows.Add();
                DataGridViewRow row = dgvChiTiet.Rows[rowIndex];

                row.Cells["colLo"].Value = chiTiet.SoLo;
                NapThongTinLoChoDong(row);

                if (!string.IsNullOrWhiteSpace(chiTiet.DonViBan))
                    row.Cells["colDonViBan"].Value = chiTiet.DonViBan;

                CapNhatGiaTheoDonVi(row);

                if (chiTiet.SoLuongBanHienThi > 0)
                {
                    row.Cells["colSoLuongBan"].Value = chiTiet.SoLuongBanHienThi;
                    row.Cells["colDonGiaBan"].Value = chiTiet.DonGiaBanHienThi.ToString("0.##", CultureInfo.CurrentCulture);
                }
                else
                {
                    row.Cells["colSoLuongBan"].Value = chiTiet.SoLuongBan;
                }

                TinhThanhTienDong(row);
            }

            CapNhatTongTien();

            _danhSachLuuTam.Remove(donTam);
            btnDanhSachLuuTam.Text = $"Đơn tạm ({_danhSachLuuTam.Count})";
        }

        private void TaiDanhSachHoaDon()
        {
            dgvLichSuHoaDon.DataSource = HoaDonBUS.Instance.LayDanhSachHoaDon(
                dtpTuNgay.Value,
                dtpDenNgay.Value,
                txtTuKhoaLichSu.Text.Trim());

            DatTenCotLichSu();

            if (dgvLichSuHoaDon.Rows.Count > 0)
            {
                dgvLichSuHoaDon.Rows[0].Selected = true;
                TaiChiTietHoaDonDaChon();
            }
            else
            {
                dgvChiTietLichSu.DataSource = null;
                lblChiTietHoaDon.Text = "Chi tiết hóa đơn: (chưa chọn)";
            }
        }

        private void DatTenCotLichSu()
        {
            if (dgvLichSuHoaDon.Columns["MaHD"] != null) dgvLichSuHoaDon.Columns["MaHD"].HeaderText = "Mã HĐ";
            if (dgvLichSuHoaDon.Columns["NgayBan"] != null) dgvLichSuHoaDon.Columns["NgayBan"].HeaderText = "Ngày bán";
            if (dgvLichSuHoaDon.Columns["HoTen"] != null) dgvLichSuHoaDon.Columns["HoTen"].HeaderText = "Nhân viên";
            if (dgvLichSuHoaDon.Columns["TenKH"] != null) dgvLichSuHoaDon.Columns["TenKH"].HeaderText = "Khách hàng";
            if (dgvLichSuHoaDon.Columns["TongTien"] != null) dgvLichSuHoaDon.Columns["TongTien"].HeaderText = "Tổng tiền";
            if (dgvLichSuHoaDon.Columns["GiamGia"] != null) dgvLichSuHoaDon.Columns["GiamGia"].HeaderText = "Giảm giá";
            if (dgvLichSuHoaDon.Columns["ThanhToan"] != null) dgvLichSuHoaDon.Columns["ThanhToan"].HeaderText = "Thanh toán";
            if (dgvLichSuHoaDon.Columns["GhiChu"] != null) dgvLichSuHoaDon.Columns["GhiChu"].HeaderText = "Ghi chú";

            DinhDangCotTien(dgvLichSuHoaDon, "TongTien");
            DinhDangCotTien(dgvLichSuHoaDon, "GiamGia");
            DinhDangCotTien(dgvLichSuHoaDon, "ThanhToan");
        }

        private void TaiChiTietHoaDonDaChon()
        {
            if (dgvLichSuHoaDon.CurrentRow == null || dgvLichSuHoaDon.CurrentRow.IsNewRow)
                return;

            object maHD = dgvLichSuHoaDon.CurrentRow.Cells["MaHD"].Value;
            if (maHD == null || maHD == DBNull.Value)
                return;

            int maHoaDon = Convert.ToInt32(maHD);
            dgvChiTietLichSu.DataSource = HoaDonBUS.Instance.LayChiTietHoaDon(maHoaDon);
            lblChiTietHoaDon.Text = "Chi tiết hóa đơn: " + maHoaDon;
            DatTenCotChiTietLichSu();
        }

        private void DatTenCotChiTietLichSu()
        {
            if (dgvChiTietLichSu.Columns["MaThuoc"] != null) dgvChiTietLichSu.Columns["MaThuoc"].HeaderText = "Mã thuốc";
            if (dgvChiTietLichSu.Columns["TenThuoc"] != null) dgvChiTietLichSu.Columns["TenThuoc"].HeaderText = "Tên thuốc";
            if (dgvChiTietLichSu.Columns["SoLo"] != null) dgvChiTietLichSu.Columns["SoLo"].HeaderText = "Số lô";
            if (dgvChiTietLichSu.Columns["SoLuongBan"] != null) dgvChiTietLichSu.Columns["SoLuongBan"].HeaderText = "SL bán";
            if (dgvChiTietLichSu.Columns["DonGiaBan"] != null) dgvChiTietLichSu.Columns["DonGiaBan"].HeaderText = "Đơn giá bán";
            if (dgvChiTietLichSu.Columns["ThanhTien"] != null) dgvChiTietLichSu.Columns["ThanhTien"].HeaderText = "Thành tiền";
            if (dgvChiTietLichSu.Columns["DonViTinh"] != null) dgvChiTietLichSu.Columns["DonViTinh"].HeaderText = "Đơn vị";

            DinhDangCotTien(dgvChiTietLichSu, "DonGiaBan");
            DinhDangCotTien(dgvChiTietLichSu, "ThanhTien");
        }

        private void btnTimLichSu_Click(object sender, EventArgs e)
        {
            TaiDanhSachHoaDon();
        }

        private void dgvLichSuHoaDon_SelectionChanged(object sender, EventArgs e)
        {
            TaiChiTietHoaDonDaChon();
        }

        private int? LayMaKhachHangDaChon()
        {
            if (cboLoaiKhach.SelectedIndex == 0)
                return null;

            if (cboKhachHang.SelectedValue == null || cboKhachHang.SelectedValue == DBNull.Value)
                return null;

            return Convert.ToInt32(cboKhachHang.SelectedValue);
        }

        private void cboLoaiKhach_SelectedIndexChanged(object sender, EventArgs e)
        {
            CapNhatTrangThaiKhachHang();
        }

        private void btnTimKhachHang_Click(object sender, EventArgs e)
        {
            NapDanhSachKhachHang(txtTimKhachHang.Text.Trim());
        }

        private void btnQuanLyKhachHang_Click(object sender, EventArgs e)
        {
            using (FormKhachHang formKhachHang = new FormKhachHang())
            {
                formKhachHang.ShowDialog(this);
            }

            NapDanhSachKhachHang(txtTimKhachHang.Text.Trim());
        }
    }
}
