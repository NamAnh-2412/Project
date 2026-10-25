using System;
using System.Data;
using System.Windows.Forms;
using QuanLyQuayThuoc.BUS;
using QuanLyQuayThuoc.DTO;

namespace QuanLyQuayThuoc.GUI
{
    public partial class FormKhachHang : Form
    {
        private int _maKHDangChon = -1;

        public FormKhachHang()
        {
            InitializeComponent();
        }

        private void DinhDangCotTien(DataGridView grid, string tenCot)
        {
            if (grid.Columns[tenCot] == null)
                return;

            grid.Columns[tenCot].DefaultCellStyle.Format = "#,0.##";
            grid.Columns[tenCot].DefaultCellStyle.Alignment = DataGridViewContentAlignment.MiddleRight;
        }

        private void FormKhachHang_Load(object sender, EventArgs e)
        {
            TaiDanhSachKhachHang();
            TaiThongKeMuaHang();
            btnLamMoi_Click(null, null);
        }

        private void TaiDanhSachKhachHang()
        {
            dgvKhachHang.DataSource = KhachHangBUS.Instance.LayDanhSach(txtTuKhoa.Text.Trim());
            if (dgvKhachHang.Columns["MaKH"] != null) dgvKhachHang.Columns["MaKH"].HeaderText = "Mã KH";
            if (dgvKhachHang.Columns["TenKH"] != null) dgvKhachHang.Columns["TenKH"].HeaderText = "Tên khách hàng";
            if (dgvKhachHang.Columns["SoDienThoai"] != null) dgvKhachHang.Columns["SoDienThoai"].HeaderText = "Số điện thoại";
            if (dgvKhachHang.Columns["DiaChi"] != null) dgvKhachHang.Columns["DiaChi"].HeaderText = "Địa chỉ";
            if (dgvKhachHang.Columns["DiemTichLuy"] != null) dgvKhachHang.Columns["DiemTichLuy"].HeaderText = "Điểm tích lũy";
            if (dgvKhachHang.Columns["NgayDangKy"] != null) dgvKhachHang.Columns["NgayDangKy"].HeaderText = "Ngày đăng ký";
        }

        private void TaiThongKeMuaHang()
        {
            dgvThongKeMuaHang.DataSource = KhachHangBUS.Instance.LayThongKeMuaHang();
            if (dgvThongKeMuaHang.Columns["MaKH"] != null) dgvThongKeMuaHang.Columns["MaKH"].HeaderText = "Mã KH";
            if (dgvThongKeMuaHang.Columns["TenKH"] != null) dgvThongKeMuaHang.Columns["TenKH"].HeaderText = "Tên khách hàng";
            if (dgvThongKeMuaHang.Columns["SoDienThoai"] != null) dgvThongKeMuaHang.Columns["SoDienThoai"].HeaderText = "Số điện thoại";
            if (dgvThongKeMuaHang.Columns["SoLanMua"] != null) dgvThongKeMuaHang.Columns["SoLanMua"].HeaderText = "Số lần mua";
            if (dgvThongKeMuaHang.Columns["TongGiaTriMua"] != null) dgvThongKeMuaHang.Columns["TongGiaTriMua"].HeaderText = "Tổng giá trị mua";
            if (dgvThongKeMuaHang.Columns["DiaChi"] != null) dgvThongKeMuaHang.Columns["DiaChi"].Visible = false;
            if (dgvThongKeMuaHang.Columns["DiemTichLuy"] != null) dgvThongKeMuaHang.Columns["DiemTichLuy"].Visible = false;
            if (dgvThongKeMuaHang.Columns["NgayDangKy"] != null) dgvThongKeMuaHang.Columns["NgayDangKy"].Visible = false;

            DinhDangCotTien(dgvThongKeMuaHang, "TongGiaTriMua");
        }

        private void TaiLichSuMuaHang()
        {
            if (_maKHDangChon <= 0)
            {
                dgvLichSuMuaHang.DataSource = null;
                lblLichSuMuaHang.Text = "Lịch sử mua hàng: (chưa chọn khách)";
                return;
            }

            dgvLichSuMuaHang.DataSource = KhachHangBUS.Instance.LayLichSuMuaHang(_maKHDangChon);
            lblLichSuMuaHang.Text = "Lịch sử mua hàng của KH: " + _maKHDangChon;
            if (dgvLichSuMuaHang.Columns["MaHD"] != null) dgvLichSuMuaHang.Columns["MaHD"].HeaderText = "Mã HĐ";
            if (dgvLichSuMuaHang.Columns["NgayBan"] != null) dgvLichSuMuaHang.Columns["NgayBan"].HeaderText = "Ngày bán";
            if (dgvLichSuMuaHang.Columns["TongTien"] != null) dgvLichSuMuaHang.Columns["TongTien"].HeaderText = "Tổng tiền";
            if (dgvLichSuMuaHang.Columns["GiamGia"] != null) dgvLichSuMuaHang.Columns["GiamGia"].HeaderText = "Giảm giá";
            if (dgvLichSuMuaHang.Columns["ThanhToan"] != null) dgvLichSuMuaHang.Columns["ThanhToan"].HeaderText = "Thanh toán";
            if (dgvLichSuMuaHang.Columns["HoTen"] != null) dgvLichSuMuaHang.Columns["HoTen"].HeaderText = "Nhân viên";
            if (dgvLichSuMuaHang.Columns["GhiChu"] != null) dgvLichSuMuaHang.Columns["GhiChu"].HeaderText = "Ghi chú";

            DinhDangCotTien(dgvLichSuMuaHang, "TongTien");
            DinhDangCotTien(dgvLichSuMuaHang, "GiamGia");
            DinhDangCotTien(dgvLichSuMuaHang, "ThanhToan");
        }

        private KhachHangDTO TaoDtoTuForm()
        {
            return new KhachHangDTO
            {
                MaKH = _maKHDangChon,
                TenKH = txtTenKH.Text.Trim(),
                SoDienThoai = txtSoDienThoai.Text.Trim(),
                DiaChi = txtDiaChi.Text.Trim()
            };
        }

        private void dgvKhachHang_SelectionChanged(object sender, EventArgs e)
        {
            if (dgvKhachHang.CurrentRow == null || dgvKhachHang.CurrentRow.Index < 0 || dgvKhachHang.CurrentRow.IsNewRow)
                return;

            DataGridViewRow row = dgvKhachHang.CurrentRow;
            object ma = row.Cells["MaKH"].Value;
            if (ma == null || ma == DBNull.Value)
                return;

            _maKHDangChon = Convert.ToInt32(ma);
            lblMaKH.Text = "Mã KH đang chọn: " + _maKHDangChon;
            txtTenKH.Text = row.Cells["TenKH"].Value?.ToString() ?? string.Empty;
            txtSoDienThoai.Text = row.Cells["SoDienThoai"].Value?.ToString() ?? string.Empty;
            txtDiaChi.Text = row.Cells["DiaChi"].Value?.ToString() ?? string.Empty;
            TaiLichSuMuaHang();
        }

        private void btnThem_Click(object sender, EventArgs e)
        {
            KhachHangDTO dto = TaoDtoTuForm();
            if (KhachHangBUS.Instance.Them(dto, out string thongBao))
            {
                MessageBox.Show("Thêm khách hàng thành công.", "Thông báo", MessageBoxButtons.OK, MessageBoxIcon.Information);
                TaiDanhSachKhachHang();
                TaiThongKeMuaHang();
                btnLamMoi_Click(null, null);
            }
            else
            {
                MessageBox.Show(thongBao, "Lỗi", MessageBoxButtons.OK, MessageBoxIcon.Warning);
            }
        }

        private void btnSua_Click(object sender, EventArgs e)
        {
            KhachHangDTO dto = TaoDtoTuForm();
            if (KhachHangBUS.Instance.Sua(dto, out string thongBao))
            {
                MessageBox.Show("Cập nhật khách hàng thành công.", "Thông báo", MessageBoxButtons.OK, MessageBoxIcon.Information);
                TaiDanhSachKhachHang();
                TaiThongKeMuaHang();
            }
            else
            {
                MessageBox.Show(thongBao, "Lỗi", MessageBoxButtons.OK, MessageBoxIcon.Warning);
            }
        }

        private void btnXoa_Click(object sender, EventArgs e)
        {
            if (_maKHDangChon <= 0)
            {
                MessageBox.Show("Chọn khách hàng cần xóa.", "Thông báo", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;
            }

            DialogResult xacNhan = MessageBox.Show(
                "Bạn có chắc chắn muốn xóa khách hàng này không?",
                "Xác nhận xóa",
                MessageBoxButtons.YesNo,
                MessageBoxIcon.Question);

            if (xacNhan != DialogResult.Yes)
                return;

            if (KhachHangBUS.Instance.Xoa(_maKHDangChon, out string thongBao))
            {
                MessageBox.Show("Xóa khách hàng thành công.", "Thông báo", MessageBoxButtons.OK, MessageBoxIcon.Information);
                TaiDanhSachKhachHang();
                TaiThongKeMuaHang();
                btnLamMoi_Click(null, null);
            }
            else
            {
                MessageBox.Show(thongBao, "Lỗi", MessageBoxButtons.OK, MessageBoxIcon.Warning);
            }
        }

        private void btnTimKiem_Click(object sender, EventArgs e)
        {
            TaiDanhSachKhachHang();
            TaiThongKeMuaHang();
        }

        private void btnLamMoi_Click(object sender, EventArgs e)
        {
            _maKHDangChon = -1;
            lblMaKH.Text = "Mã KH đang chọn: (chưa chọn)";
            txtTenKH.Clear();
            txtSoDienThoai.Clear();
            txtDiaChi.Clear();
            dgvKhachHang.ClearSelection();
            TaiLichSuMuaHang();
        }
    }
}
