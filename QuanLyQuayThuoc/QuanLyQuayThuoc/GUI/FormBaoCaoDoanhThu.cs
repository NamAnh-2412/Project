using System;
using System.Data;
using System.Globalization;
using System.Windows.Forms;
using QuanLyQuayThuoc.BUS;

namespace QuanLyQuayThuoc.GUI
{
    public partial class FormBaoCaoDoanhThu : Form
    {
        public FormBaoCaoDoanhThu()
        {
            InitializeComponent();
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

        private void FormBaoCaoDoanhThu_Load(object sender, EventArgs e)
        {
            dtpTuNgay.Value = DateTime.Today.AddDays(-30);
            dtpDenNgay.Value = DateTime.Today;
            TaiBaoCao();
        }

        private void TaiBaoCao()
        {
            DateTime tuNgay = dtpTuNgay.Value;
            DateTime denNgay = dtpDenNgay.Value;

            if (tuNgay.Date > denNgay.Date)
            {
                MessageBox.Show("Từ ngày không được lớn hơn đến ngày.", "Thông báo", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;
            }

            txtTongDoanhThu.Text = DinhDangTien(BaoCaoBUS.Instance.LayTongDoanhThu(tuNgay, denNgay));
            txtSoHoaDon.Text = BaoCaoBUS.Instance.LaySoHoaDon(tuNgay, denNgay).ToString();
            dgvChiTietHoaDon.DataSource = BaoCaoBUS.Instance.LayChiTietHoaDonTheoNgay(tuNgay, denNgay);
            dgvThuocBanChay.DataSource = BaoCaoBUS.Instance.LayThuocBanChay(tuNgay, denNgay);

            DatTenCotChiTietHoaDon();
            DatTenCotThuocBanChay();
        }

        private void DatTenCotChiTietHoaDon()
        {
            if (dgvChiTietHoaDon.Columns["MaHD"] != null) dgvChiTietHoaDon.Columns["MaHD"].HeaderText = "Mã HĐ";
            if (dgvChiTietHoaDon.Columns["NgayBan"] != null) dgvChiTietHoaDon.Columns["NgayBan"].HeaderText = "Ngày bán";
            if (dgvChiTietHoaDon.Columns["HoTen"] != null) dgvChiTietHoaDon.Columns["HoTen"].HeaderText = "Nhân viên";
            if (dgvChiTietHoaDon.Columns["TenKH"] != null) dgvChiTietHoaDon.Columns["TenKH"].HeaderText = "Khách hàng";
            if (dgvChiTietHoaDon.Columns["TongTien"] != null) dgvChiTietHoaDon.Columns["TongTien"].HeaderText = "Tổng tiền";
            if (dgvChiTietHoaDon.Columns["GiamGia"] != null) dgvChiTietHoaDon.Columns["GiamGia"].HeaderText = "Giảm giá";
            if (dgvChiTietHoaDon.Columns["ThanhToan"] != null) dgvChiTietHoaDon.Columns["ThanhToan"].HeaderText = "Thanh toán";
            if (dgvChiTietHoaDon.Columns["GhiChu"] != null) dgvChiTietHoaDon.Columns["GhiChu"].HeaderText = "Ghi chú";

            DinhDangCotTien(dgvChiTietHoaDon, "TongTien");
            DinhDangCotTien(dgvChiTietHoaDon, "GiamGia");
            DinhDangCotTien(dgvChiTietHoaDon, "ThanhToan");
        }

        private void DatTenCotThuocBanChay()
        {
            if (dgvThuocBanChay.Columns["MaThuoc"] != null) dgvThuocBanChay.Columns["MaThuoc"].HeaderText = "Mã thuốc";
            if (dgvThuocBanChay.Columns["TenThuoc"] != null) dgvThuocBanChay.Columns["TenThuoc"].HeaderText = "Tên thuốc";
            if (dgvThuocBanChay.Columns["DonViTinh"] != null) dgvThuocBanChay.Columns["DonViTinh"].HeaderText = "Đơn vị";
            if (dgvThuocBanChay.Columns["TongSoLuongBan"] != null) dgvThuocBanChay.Columns["TongSoLuongBan"].HeaderText = "Tổng SL bán";
            if (dgvThuocBanChay.Columns["TongDoanhThu"] != null) dgvThuocBanChay.Columns["TongDoanhThu"].HeaderText = "Tổng doanh thu";

            DinhDangCotTien(dgvThuocBanChay, "TongDoanhThu");
        }

        private void btnXemBaoCao_Click(object sender, EventArgs e)
        {
            TaiBaoCao();
        }
    }
}
