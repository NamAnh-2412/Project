using System;
using System.Data;
using System.Drawing;
using System.Globalization;
using System.Windows.Forms;
using QuanLyQuayThuoc.BUS;

namespace QuanLyQuayThuoc.GUI
{
    public partial class FormTonKho : Form
    {
        private int? _maThuocDangLocChiTiet;
        private string _tenThuocDangLocChiTiet;

        public FormTonKho()
        {
            InitializeComponent();
        }

        private void FormTonKho_Load(object sender, EventArgs e)
        {
            NapBoLocDanhMuc();
            NapSoNgayCanhBao();
            NapBoLocTrangThaiCanhBao();
            TaiDuLieu();
            TaiLichSuCanhBao();
        }

        private void NapBoLocDanhMuc()
        {
            DataTable dm = DanhMucBUS.Instance.LayDanhSach();
            DataTable dmLoc = dm.Copy();
            DataRow dongTatCa = dmLoc.NewRow();
            dongTatCa["MaDM"] = 0;
            dongTatCa["TenDanhMuc"] = "Tất cả";
            dmLoc.Rows.InsertAt(dongTatCa, 0);

            cboDanhMuc.DataSource = dmLoc;
            cboDanhMuc.DisplayMember = "TenDanhMuc";
            cboDanhMuc.ValueMember = "MaDM";
        }

        private void NapSoNgayCanhBao()
        {
            cboSoNgayCanhBao.Items.Clear();
            cboSoNgayCanhBao.Items.Add("30");
            cboSoNgayCanhBao.Items.Add("60");
            cboSoNgayCanhBao.Items.Add("90");
            cboSoNgayCanhBao.Items.Add("120");
            cboSoNgayCanhBao.SelectedItem = "90";
        }

        private void TaiDuLieu()
        {
            object maDM = null;
            if (cboDanhMuc.SelectedValue != null && Convert.ToInt32(cboDanhMuc.SelectedValue) > 0)
                maDM = Convert.ToInt32(cboDanhMuc.SelectedValue);

            int soNgayCanhBao = 90;
            if (cboSoNgayCanhBao.SelectedItem != null)
                int.TryParse(cboSoNgayCanhBao.SelectedItem.ToString(), out soNgayCanhBao);

            DataTable tonKhoTongHop = TonKhoBUS.Instance.LayTonKhoTongHop(txtTuKhoa.Text.Trim(), maDM);
            DataTable tonKhoChiTiet = TonKhoBUS.Instance.LayTonKhoChiTiet(txtTuKhoa.Text.Trim(), _maThuocDangLocChiTiet);
            DataTable canhBaoHanDung = TonKhoBUS.Instance.LayCanhBaoHanSuDung(txtTuKhoa.Text.Trim(), soNgayCanhBao);

            BoSungCotTonQuyDoi(tonKhoTongHop, "TongTonKho");
            BoSungCotTonQuyDoi(tonKhoChiTiet, "SoLuongTon");
            BoSungCotTonQuyDoi(canhBaoHanDung, "SoLuongTon");

            dgvTongHop.DataSource = tonKhoTongHop;
            dgvChiTiet.DataSource = tonKhoChiTiet;

            DatTenCotTongHop();
            DatTenCotChiTiet();
            CapNhatTenTabChiTiet();
            CapNhatTongQuan(canhBaoHanDung.Rows.Count);
            TaiCanhBaoTonKho(maDM, txtTuKhoa.Text.Trim());
        }

        private void TaiCanhBaoTonKho(object maDM, string tuKhoa)
        {
            DataTable dtHet = TonKhoBUS.Instance.LayThuocHetHangTonTongHop(tuKhoa, maDM);
            DataTable dtSap = TonKhoBUS.Instance.LayThuocSapHetHangTonTongHop(tuKhoa, maDM);
            BoSungCotTonQuyDoi(dtHet, "TongTonKho");
            BoSungCotTonQuyDoi(dtSap, "TongTonKho");
            dgvDaHetHang.DataSource = dtHet;
            dgvSapHetHang.DataSource = dtSap;
            DatTenCotCanhBaoTonKho();
            ToMauCanhBaoTonKho();
        }

        private void DatTenCotCanhBaoTonKho()
        {
            foreach (DataGridView grid in new[] { dgvDaHetHang, dgvSapHetHang })
            {
                if (grid.Columns["MaThuoc"] != null) grid.Columns["MaThuoc"].HeaderText = "Mã thuốc";
                if (grid.Columns["TenThuoc"] != null) grid.Columns["TenThuoc"].HeaderText = "Tên thuốc";
                if (grid.Columns["TenDanhMuc"] != null) grid.Columns["TenDanhMuc"].HeaderText = "Danh mục";
                if (grid.Columns["DonViTinh"] != null) grid.Columns["DonViTinh"].HeaderText = "Đơn vị";
                if (grid.Columns["TongTonKho"] != null) grid.Columns["TongTonKho"].HeaderText = "Tổng tồn (ĐV nhỏ)";
                if (grid.Columns["TonQuyDoi"] != null) grid.Columns["TonQuyDoi"].HeaderText = "Tồn quy đổi";
                if (grid.Columns["SoLuongToiThieu"] != null) grid.Columns["SoLuongToiThieu"].HeaderText = "Mức tối thiểu (ĐV lớn)";
                if (grid.Columns["NguongToiThieu_DVNho"] != null) grid.Columns["NguongToiThieu_DVNho"].HeaderText = "Ngưỡng cảnh báo (ĐV nhỏ)";
                if (grid.Columns["TyLeQuyDoi"] != null) grid.Columns["TyLeQuyDoi"].Visible = false;
                if (grid.Columns["DonViLon"] != null) grid.Columns["DonViLon"].Visible = false;
                if (grid.Columns["MaDM"] != null) grid.Columns["MaDM"].Visible = false;
            }
        }

        private void ToMauCanhBaoTonKho()
        {
            foreach (DataGridViewRow row in dgvDaHetHang.Rows)
            {
                if (row.IsNewRow) continue;
                row.DefaultCellStyle.BackColor = Color.MistyRose;
            }
            foreach (DataGridViewRow row in dgvSapHetHang.Rows)
            {
                if (row.IsNewRow) continue;
                row.DefaultCellStyle.BackColor = Color.LightYellow;
            }
        }

        private void DatTenCotTongHop()
        {
            if (dgvTongHop.Columns["MaThuoc"] != null) dgvTongHop.Columns["MaThuoc"].HeaderText = "Mã thuốc";
            if (dgvTongHop.Columns["TenThuoc"] != null) dgvTongHop.Columns["TenThuoc"].HeaderText = "Tên thuốc";
            if (dgvTongHop.Columns["TenDanhMuc"] != null) dgvTongHop.Columns["TenDanhMuc"].HeaderText = "Danh mục";
            if (dgvTongHop.Columns["DonViTinh"] != null) dgvTongHop.Columns["DonViTinh"].HeaderText = "Đơn vị";
            if (dgvTongHop.Columns["TongTonKho"] != null) dgvTongHop.Columns["TongTonKho"].HeaderText = "Tổng tồn (ĐV nhỏ)";
            if (dgvTongHop.Columns["TonQuyDoi"] != null) dgvTongHop.Columns["TonQuyDoi"].HeaderText = "Tồn quy đổi";
            if (dgvTongHop.Columns["TyLeQuyDoi"] != null) dgvTongHop.Columns["TyLeQuyDoi"].Visible = false;
            if (dgvTongHop.Columns["DonViLon"] != null) dgvTongHop.Columns["DonViLon"].Visible = false;
            if (dgvTongHop.Columns["MaDM"] != null) dgvTongHop.Columns["MaDM"].Visible = false;
        }

        private void DatTenCotChiTiet()
        {
            if (dgvChiTiet.Columns["MaThuoc"] != null) dgvChiTiet.Columns["MaThuoc"].HeaderText = "Mã thuốc";
            if (dgvChiTiet.Columns["TenThuoc"] != null) dgvChiTiet.Columns["TenThuoc"].HeaderText = "Tên thuốc";
            if (dgvChiTiet.Columns["TenDanhMuc"] != null) dgvChiTiet.Columns["TenDanhMuc"].HeaderText = "Danh mục";
            if (dgvChiTiet.Columns["SoLo"] != null) dgvChiTiet.Columns["SoLo"].HeaderText = "Số lô";
            if (dgvChiTiet.Columns["HanSuDung"] != null) dgvChiTiet.Columns["HanSuDung"].HeaderText = "Hạn sử dụng";
            if (dgvChiTiet.Columns["SoLuongTon"] != null) dgvChiTiet.Columns["SoLuongTon"].HeaderText = "Tồn (ĐV nhỏ)";
            if (dgvChiTiet.Columns["TonQuyDoi"] != null) dgvChiTiet.Columns["TonQuyDoi"].HeaderText = "Tồn quy đổi";
            if (dgvChiTiet.Columns["GiaBan"] != null) dgvChiTiet.Columns["GiaBan"].HeaderText = "Giá bán";
            if (dgvChiTiet.Columns["DonViTinh"] != null) dgvChiTiet.Columns["DonViTinh"].HeaderText = "Đơn vị";
            if (dgvChiTiet.Columns["TyLeQuyDoi"] != null) dgvChiTiet.Columns["TyLeQuyDoi"].Visible = false;
            if (dgvChiTiet.Columns["DonViLon"] != null) dgvChiTiet.Columns["DonViLon"].Visible = false;

            DinhDangCotTien(dgvChiTiet, "GiaBan");
        }

        private void btnTimKiem_Click(object sender, EventArgs e)
        {
            TaiDuLieu();
        }

        private void btnBoLoc_Click(object sender, EventArgs e)
        {
            txtTuKhoa.Clear();
            cboDanhMuc.SelectedIndex = 0;
            _maThuocDangLocChiTiet = null;
            _tenThuocDangLocChiTiet = null;
            TaiDuLieu();
        }

        private void dgvTongHop_CellDoubleClick(object sender, DataGridViewCellEventArgs e)
        {
            if (e.RowIndex < 0 || dgvTongHop.Rows.Count == 0)
                return;

            DataGridViewRow row = dgvTongHop.Rows[e.RowIndex];
            object maThuoc = row.Cells["MaThuoc"].Value;
            if (maThuoc == null || maThuoc == DBNull.Value)
                return;

            _maThuocDangLocChiTiet = Convert.ToInt32(maThuoc);
            _tenThuocDangLocChiTiet = Convert.ToString(row.Cells["TenThuoc"].Value);
            TaiDuLieu();
            tabTonKho.SelectedTab = tabChiTiet;
        }

        private void CapNhatTenTabChiTiet()
        {
            if (_maThuocDangLocChiTiet.HasValue && !string.IsNullOrWhiteSpace(_tenThuocDangLocChiTiet))
                tabChiTiet.Text = "Tồn kho theo lô - " + _tenThuocDangLocChiTiet;
            else
                tabChiTiet.Text = "Tồn kho theo lô";
        }

        private void CapNhatTongQuan(int demLoCanhBaoHanSapHetHan)
        {
            txtTongMatHang.Text = dgvTongHop.Rows.Count.ToString();

            decimal tongGiaTriTon = 0;
            foreach (DataGridViewRow row in dgvChiTiet.Rows)
            {
                if (row.IsNewRow)
                    continue;

                int soLuongTon = DocInt(row.Cells["SoLuongTon"].Value);
                decimal giaBan = DocDecimal(row.Cells["GiaBan"].Value);
                int tyLeQuyDoi = DocInt(row.Cells["TyLeQuyDoi"].Value);
                if (tyLeQuyDoi <= 0)
                    tyLeQuyDoi = 1;

                decimal giaBanTheoDonViTon = giaBan / tyLeQuyDoi;
                tongGiaTriTon += soLuongTon * giaBanTheoDonViTon;
            }

            txtTongGiaTriTon.Text = DinhDangTien(tongGiaTriTon);
            txtSoLoCanhBao.Text = demLoCanhBaoHanSapHetHan.ToString();
        }

        private int DocInt(object value)
        {
            int ketQua;
            return int.TryParse(Convert.ToString(value), out ketQua) ? ketQua : 0;
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

        private void BoSungCotTonQuyDoi(DataTable data, string tenCotSoLuong)
        {
            if (data == null || !data.Columns.Contains(tenCotSoLuong))
                return;

            if (!data.Columns.Contains("TonQuyDoi"))
                data.Columns.Add("TonQuyDoi", typeof(string));

            foreach (DataRow row in data.Rows)
            {
                int soLuongTon = row[tenCotSoLuong] == DBNull.Value ? 0 : Convert.ToInt32(row[tenCotSoLuong]);
                int tyLeQuyDoi = data.Columns.Contains("TyLeQuyDoi") && row["TyLeQuyDoi"] != DBNull.Value
                    ? Convert.ToInt32(row["TyLeQuyDoi"])
                    : 1;
                string donViNho = data.Columns.Contains("DonViTinh") && row["DonViTinh"] != DBNull.Value
                    ? Convert.ToString(row["DonViTinh"])
                    : string.Empty;
                string donViLon = data.Columns.Contains("DonViLon") && row["DonViLon"] != DBNull.Value
                    ? Convert.ToString(row["DonViLon"])
                    : string.Empty;

                row["TonQuyDoi"] = DinhDangTonQuyDoi(soLuongTon, tyLeQuyDoi, donViLon, donViNho);
            }
        }

        private string DinhDangTonQuyDoi(int soLuongTon, int tyLeQuyDoi, string donViLon, string donViNho)
        {
            if (soLuongTon <= 0)
                return "0";

            if (tyLeQuyDoi <= 1 || string.IsNullOrWhiteSpace(donViLon) || string.Equals(donViLon, donViNho, StringComparison.OrdinalIgnoreCase))
                return TaoSoLuongKemDonVi(soLuongTon, donViNho);

            int soLuongLon = soLuongTon / tyLeQuyDoi;
            int soLuongLe = soLuongTon % tyLeQuyDoi;

            string ketQua = string.Empty;
            if (soLuongLon > 0)
                ketQua = TaoSoLuongKemDonVi(soLuongLon, donViLon);

            if (soLuongLe > 0)
            {
                if (!string.IsNullOrEmpty(ketQua))
                    ketQua += " ";
                ketQua += TaoSoLuongKemDonVi(soLuongLe, donViNho);
            }

            return string.IsNullOrEmpty(ketQua) ? TaoSoLuongKemDonVi(0, donViNho) : ketQua;
        }

        private string TaoSoLuongKemDonVi(int soLuong, string donVi)
        {
            return string.IsNullOrWhiteSpace(donVi) ? soLuong.ToString() : soLuong + " " + donVi;
        }

        private decimal DocDecimal(object value)
        {
            string text = Convert.ToString(value);
            if (string.IsNullOrWhiteSpace(text))
                return 0;

            text = text.Trim().Replace(" ", "");
            if (text.Contains(",") && !text.Contains("."))
                text = text.Replace(",", ".");

            decimal ketQua;
            return decimal.TryParse(text, NumberStyles.Any, CultureInfo.InvariantCulture, out ketQua) ? ketQua : 0;
        }

        // ─── Canh bao lich su ───────────────────────────────────────────

        private void NapBoLocTrangThaiCanhBao()
        {
            cboTrangThaiCanhBao.Items.Clear();
            cboTrangThaiCanhBao.Items.Add("Tất cả");
            cboTrangThaiCanhBao.Items.Add("Chưa xử lý");
            cboTrangThaiCanhBao.Items.Add("Đã xử lý");
            cboTrangThaiCanhBao.SelectedIndex = 0;
        }

        private void TaiLichSuCanhBao()
        {
            string tuKhoa = txtTimKiemCanhBao.Text.Trim();

            bool? daXuLy = null;
            if (cboTrangThaiCanhBao.SelectedIndex == 1) daXuLy = false;
            else if (cboTrangThaiCanhBao.SelectedIndex == 2) daXuLy = true;

            DataTable lichSu = CanhBaoHanDungBUS.Instance.LayLichSuCanhBao(tuKhoa, daXuLy);
            dgvLichSuCanhBao.DataSource = lichSu;
            DatTenCotLichSuCanhBao();
            ToMauLichSuCanhBao();
        }

        private void DatTenCotLichSuCanhBao()
        {
            if (dgvLichSuCanhBao.Columns["Id"] != null)             dgvLichSuCanhBao.Columns["Id"].Visible = false;
            if (dgvLichSuCanhBao.Columns["MaThuoc"] != null)        dgvLichSuCanhBao.Columns["MaThuoc"].Visible = false;
            if (dgvLichSuCanhBao.Columns["TenThuoc"] != null)       dgvLichSuCanhBao.Columns["TenThuoc"].HeaderText = "Tên thuốc";
            if (dgvLichSuCanhBao.Columns["SoLo"] != null)           dgvLichSuCanhBao.Columns["SoLo"].HeaderText = "Số lô";
            if (dgvLichSuCanhBao.Columns["MocCanhBao"] != null)     dgvLichSuCanhBao.Columns["MocCanhBao"].HeaderText = "Mốc (ngày)";
            if (dgvLichSuCanhBao.Columns["NgayCanhBao"] != null)    dgvLichSuCanhBao.Columns["NgayCanhBao"].HeaderText = "Ngày cảnh báo";
            if (dgvLichSuCanhBao.Columns["TrangThai"] != null)      dgvLichSuCanhBao.Columns["TrangThai"].Visible = false;
            if (dgvLichSuCanhBao.Columns["DaXuLy"] != null)         dgvLichSuCanhBao.Columns["DaXuLy"].HeaderText = "Đã xử lý";
            if (dgvLichSuCanhBao.Columns["GhiChu"] != null)         dgvLichSuCanhBao.Columns["GhiChu"].HeaderText = "Ghi chú";
            if (dgvLichSuCanhBao.Columns["HanSuDung"] != null)      dgvLichSuCanhBao.Columns["HanSuDung"].HeaderText = "Hạn sử dụng";
            if (dgvLichSuCanhBao.Columns["SoLuongTon"] != null)     dgvLichSuCanhBao.Columns["SoLuongTon"].HeaderText = "Tồn hiện tại";
            if (dgvLichSuCanhBao.Columns["SoNgayConLai"] != null)   dgvLichSuCanhBao.Columns["SoNgayConLai"].HeaderText = "Ngày còn lại";
        }

        private void ToMauLichSuCanhBao()
        {
            foreach (DataGridViewRow row in dgvLichSuCanhBao.Rows)
            {
                if (row.IsNewRow) continue;

                bool daXuLy = false;
                bool.TryParse(Convert.ToString(row.Cells["DaXuLy"].Value), out daXuLy);
                // SQL BIT tra ve True/False hoac 1/0
                if (Convert.ToString(row.Cells["DaXuLy"].Value) == "1" || Convert.ToString(row.Cells["DaXuLy"].Value).ToLower() == "true")
                    daXuLy = true;

                if (daXuLy)
                {
                    row.DefaultCellStyle.BackColor = Color.FromArgb(235, 235, 235);
                    row.DefaultCellStyle.ForeColor = Color.Gray;
                }
                else
                {
                    int soNgayConLai;
                    int.TryParse(Convert.ToString(row.Cells["SoNgayConLai"].Value), out soNgayConLai);

                    if (soNgayConLai <= 0)
                        row.DefaultCellStyle.BackColor = Color.LightCoral;
                    else if (soNgayConLai <= 3)
                        row.DefaultCellStyle.BackColor = Color.MistyRose;
                    else
                        row.DefaultCellStyle.BackColor = Color.LightYellow;
                }
            }
        }

        private void txtTimKiemCanhBao_TextChanged(object sender, EventArgs e)
        {
            TaiLichSuCanhBao();
        }

        private void cboTrangThaiCanhBao_SelectedIndexChanged(object sender, EventArgs e)
        {
            TaiLichSuCanhBao();
        }

        private void btnLamMoiCanhBao_Click(object sender, EventArgs e)
        {
            txtTimKiemCanhBao.Clear();
            cboTrangThaiCanhBao.SelectedIndex = 0;
            TaiLichSuCanhBao();
        }
    }
}
