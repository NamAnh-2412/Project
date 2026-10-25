namespace QuanLyQuayThuoc.GUI
{
    partial class FormBanHang
    {
        private System.ComponentModel.IContainer components = null;

        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        private void InitializeComponent()
        {
            this.btnLuuTam = new System.Windows.Forms.Button();
            this.btnDanhSachLuuTam = new System.Windows.Forms.Button();
            this.tabBanHang = new System.Windows.Forms.TabControl();
            this.tabTaoHoaDon = new System.Windows.Forms.TabPage();
            this.dgvChiTiet = new System.Windows.Forms.DataGridView();
            this.panelNut = new System.Windows.Forms.Panel();
            this.btnLamMoi = new System.Windows.Forms.Button();
            this.btnLuuHoaDon = new System.Windows.Forms.Button();
            this.panelThongTin = new System.Windows.Forms.Panel();
            this.btnQuanLyKhachHang = new System.Windows.Forms.Button();
            this.cboKhachHang = new System.Windows.Forms.ComboBox();
            this.btnTimKhachHang = new System.Windows.Forms.Button();
            this.txtTimKhachHang = new System.Windows.Forms.TextBox();
            this.lblKhachHang = new System.Windows.Forms.Label();
            this.cboLoaiKhach = new System.Windows.Forms.ComboBox();
            this.lblLoaiKhach = new System.Windows.Forms.Label();
            this.txtTongTien = new System.Windows.Forms.TextBox();
            this.lblTongTien = new System.Windows.Forms.Label();
            this.txtGhiChu = new System.Windows.Forms.TextBox();
            this.lblGhiChu = new System.Windows.Forms.Label();
            this.txtThanhToan = new System.Windows.Forms.TextBox();
            this.lblThanhToan = new System.Windows.Forms.Label();
            this.txtGiamGia = new System.Windows.Forms.TextBox();
            this.lblGiamGia = new System.Windows.Forms.Label();
            this.dtpNgayBan = new System.Windows.Forms.DateTimePicker();
            this.lblNgayBan = new System.Windows.Forms.Label();
            this.txtNhanVien = new System.Windows.Forms.TextBox();
            this.lblNhanVien = new System.Windows.Forms.Label();
            this.tabLichSu = new System.Windows.Forms.TabPage();
            this.dgvChiTietLichSu = new System.Windows.Forms.DataGridView();
            this.lblChiTietHoaDon = new System.Windows.Forms.Label();
            this.dgvLichSuHoaDon = new System.Windows.Forms.DataGridView();
            this.panelLocLichSu = new System.Windows.Forms.Panel();
            this.btnTimLichSu = new System.Windows.Forms.Button();
            this.txtTuKhoaLichSu = new System.Windows.Forms.TextBox();
            this.lblTuKhoaLichSu = new System.Windows.Forms.Label();
            this.dtpDenNgay = new System.Windows.Forms.DateTimePicker();
            this.lblDenNgay = new System.Windows.Forms.Label();
            this.dtpTuNgay = new System.Windows.Forms.DateTimePicker();
            this.lblTuNgay = new System.Windows.Forms.Label();
            this.tabBanHang.SuspendLayout();
            this.tabTaoHoaDon.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.dgvChiTiet)).BeginInit();
            this.panelNut.SuspendLayout();
            this.panelThongTin.SuspendLayout();
            this.tabLichSu.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.dgvChiTietLichSu)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.dgvLichSuHoaDon)).BeginInit();
            this.panelLocLichSu.SuspendLayout();
            this.SuspendLayout();
            // 
            // tabBanHang
            // 
            this.tabBanHang.Controls.Add(this.tabTaoHoaDon);
            this.tabBanHang.Controls.Add(this.tabLichSu);
            this.tabBanHang.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tabBanHang.Location = new System.Drawing.Point(0, 0);
            this.tabBanHang.Name = "tabBanHang";
            this.tabBanHang.SelectedIndex = 0;
            this.tabBanHang.Size = new System.Drawing.Size(980, 600);
            this.tabBanHang.TabIndex = 0;
            // 
            // tabTaoHoaDon
            // 
            this.tabTaoHoaDon.Controls.Add(this.dgvChiTiet);
            this.tabTaoHoaDon.Controls.Add(this.panelNut);
            this.tabTaoHoaDon.Controls.Add(this.panelThongTin);
            this.tabTaoHoaDon.Location = new System.Drawing.Point(4, 24);
            this.tabTaoHoaDon.Name = "tabTaoHoaDon";
            this.tabTaoHoaDon.Padding = new System.Windows.Forms.Padding(3);
            this.tabTaoHoaDon.Size = new System.Drawing.Size(972, 572);
            this.tabTaoHoaDon.TabIndex = 0;
            this.tabTaoHoaDon.Text = "Bán mới";
            this.tabTaoHoaDon.UseVisualStyleBackColor = true;
            // 
            // dgvChiTiet
            // 
            this.dgvChiTiet.BackgroundColor = System.Drawing.Color.White;
            this.dgvChiTiet.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
            this.dgvChiTiet.Dock = System.Windows.Forms.DockStyle.Fill;
            this.dgvChiTiet.Location = new System.Drawing.Point(3, 180);
            this.dgvChiTiet.Name = "dgvChiTiet";
            this.dgvChiTiet.RowHeadersVisible = false;
            this.dgvChiTiet.Size = new System.Drawing.Size(966, 389);
            this.dgvChiTiet.TabIndex = 2;
            this.dgvChiTiet.CellValueChanged += new System.Windows.Forms.DataGridViewCellEventHandler(this.dgvChiTiet_CellValueChanged);
            this.dgvChiTiet.CurrentCellDirtyStateChanged += new System.EventHandler(this.dgvChiTiet_CurrentCellDirtyStateChanged);
            this.dgvChiTiet.RowsRemoved += new System.Windows.Forms.DataGridViewRowsRemovedEventHandler(this.dgvChiTiet_RowsRemoved);
            // 
            // panelNut
            // 
            this.panelNut.Controls.Add(this.btnDanhSachLuuTam);
            this.panelNut.Controls.Add(this.btnLuuTam);
            this.panelNut.Controls.Add(this.btnLamMoi);
            this.panelNut.Controls.Add(this.btnLuuHoaDon);
            this.panelNut.Dock = System.Windows.Forms.DockStyle.Top;
            this.panelNut.Location = new System.Drawing.Point(3, 128);
            this.panelNut.Name = "panelNut";
            this.panelNut.Padding = new System.Windows.Forms.Padding(12, 6, 12, 6);
            this.panelNut.Size = new System.Drawing.Size(966, 52);
            this.panelNut.TabIndex = 1;
            // 
            // btnLamMoi
            // 
            this.btnLamMoi.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnLamMoi.Location = new System.Drawing.Point(130, 11);
            this.btnLamMoi.Name = "btnLamMoi";
            this.btnLamMoi.Size = new System.Drawing.Size(100, 30);
            this.btnLamMoi.TabIndex = 1;
            this.btnLamMoi.Text = "Làm mới";
            this.btnLamMoi.UseVisualStyleBackColor = true;
            this.btnLamMoi.Click += new System.EventHandler(this.btnLamMoi_Click);
            // 
            // btnLuuHoaDon
            // 
            this.btnLuuHoaDon.BackColor = System.Drawing.Color.FromArgb(((int)(((byte)(0)))), ((int)(((byte)(122)))), ((int)(((byte)(204)))));
            this.btnLuuHoaDon.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnLuuHoaDon.ForeColor = System.Drawing.Color.White;
            this.btnLuuHoaDon.Location = new System.Drawing.Point(15, 11);
            this.btnLuuHoaDon.Name = "btnLuuHoaDon";
            this.btnLuuHoaDon.Size = new System.Drawing.Size(100, 30);
            this.btnLuuHoaDon.TabIndex = 0;
            this.btnLuuHoaDon.Text = "Lưu hóa đơn";
            this.btnLuuHoaDon.UseVisualStyleBackColor = false;
            this.btnLuuHoaDon.Click += new System.EventHandler(this.btnLuuHoaDon_Click);
            // 
            // btnLuuTam
            // 
            this.btnLuuTam.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnLuuTam.Location = new System.Drawing.Point(245, 11);
            this.btnLuuTam.Name = "btnLuuTam";
            this.btnLuuTam.Size = new System.Drawing.Size(100, 30);
            this.btnLuuTam.TabIndex = 2;
            this.btnLuuTam.Text = "Lưu tạm";
            this.btnLuuTam.UseVisualStyleBackColor = true;
            this.btnLuuTam.Click += new System.EventHandler(this.btnLuuTam_Click);
            // 
            // btnDanhSachLuuTam
            // 
            this.btnDanhSachLuuTam.BackColor = System.Drawing.Color.FromArgb(((int)(((byte)(255)))), ((int)(((byte)(193)))), ((int)(((byte)(7)))));
            this.btnDanhSachLuuTam.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnDanhSachLuuTam.ForeColor = System.Drawing.Color.Black;
            this.btnDanhSachLuuTam.Location = new System.Drawing.Point(360, 11);
            this.btnDanhSachLuuTam.Name = "btnDanhSachLuuTam";
            this.btnDanhSachLuuTam.Size = new System.Drawing.Size(120, 30);
            this.btnDanhSachLuuTam.TabIndex = 3;
            this.btnDanhSachLuuTam.Text = "Đơn tạm (0)";
            this.btnDanhSachLuuTam.UseVisualStyleBackColor = false;
            this.btnDanhSachLuuTam.Click += new System.EventHandler(this.btnDanhSachLuuTam_Click);
            // 
            // panelThongTin
            // 
            this.panelThongTin.BackColor = System.Drawing.Color.WhiteSmoke;
            this.panelThongTin.Controls.Add(this.btnQuanLyKhachHang);
            this.panelThongTin.Controls.Add(this.cboKhachHang);
            this.panelThongTin.Controls.Add(this.btnTimKhachHang);
            this.panelThongTin.Controls.Add(this.txtTimKhachHang);
            this.panelThongTin.Controls.Add(this.lblKhachHang);
            this.panelThongTin.Controls.Add(this.cboLoaiKhach);
            this.panelThongTin.Controls.Add(this.lblLoaiKhach);
            this.panelThongTin.Controls.Add(this.txtTongTien);
            this.panelThongTin.Controls.Add(this.lblTongTien);
            this.panelThongTin.Controls.Add(this.txtGhiChu);
            this.panelThongTin.Controls.Add(this.lblGhiChu);
            this.panelThongTin.Controls.Add(this.txtThanhToan);
            this.panelThongTin.Controls.Add(this.lblThanhToan);
            this.panelThongTin.Controls.Add(this.txtGiamGia);
            this.panelThongTin.Controls.Add(this.lblGiamGia);
            this.panelThongTin.Controls.Add(this.dtpNgayBan);
            this.panelThongTin.Controls.Add(this.lblNgayBan);
            this.panelThongTin.Controls.Add(this.txtNhanVien);
            this.panelThongTin.Controls.Add(this.lblNhanVien);
            this.panelThongTin.Dock = System.Windows.Forms.DockStyle.Top;
            this.panelThongTin.Location = new System.Drawing.Point(3, 3);
            this.panelThongTin.Name = "panelThongTin";
            this.panelThongTin.Padding = new System.Windows.Forms.Padding(12, 8, 12, 8);
            this.panelThongTin.Size = new System.Drawing.Size(966, 125);
            this.panelThongTin.TabIndex = 0;
            // 
            // btnQuanLyKhachHang
            // 
            this.btnQuanLyKhachHang.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnQuanLyKhachHang.Location = new System.Drawing.Point(860, 51);
            this.btnQuanLyKhachHang.Name = "btnQuanLyKhachHang";
            this.btnQuanLyKhachHang.Size = new System.Drawing.Size(100, 23);
            this.btnQuanLyKhachHang.TabIndex = 18;
            this.btnQuanLyKhachHang.Text = "Quản lý KH";
            this.btnQuanLyKhachHang.UseVisualStyleBackColor = true;
            this.btnQuanLyKhachHang.Click += new System.EventHandler(this.btnQuanLyKhachHang_Click);
            // 
            // cboKhachHang
            // 
            this.cboKhachHang.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.cboKhachHang.FormattingEnabled = true;
            this.cboKhachHang.Location = new System.Drawing.Point(593, 52);
            this.cboKhachHang.Name = "cboKhachHang";
            this.cboKhachHang.Size = new System.Drawing.Size(261, 23);
            this.cboKhachHang.TabIndex = 17;
            // 
            // btnTimKhachHang
            // 
            this.btnTimKhachHang.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnTimKhachHang.Location = new System.Drawing.Point(502, 51);
            this.btnTimKhachHang.Name = "btnTimKhachHang";
            this.btnTimKhachHang.Size = new System.Drawing.Size(85, 23);
            this.btnTimKhachHang.TabIndex = 16;
            this.btnTimKhachHang.Text = "Tìm";
            this.btnTimKhachHang.UseVisualStyleBackColor = true;
            this.btnTimKhachHang.Click += new System.EventHandler(this.btnTimKhachHang_Click);
            // 
            // txtTimKhachHang
            // 
            this.txtTimKhachHang.Location = new System.Drawing.Point(311, 52);
            this.txtTimKhachHang.Name = "txtTimKhachHang";
            this.txtTimKhachHang.Size = new System.Drawing.Size(185, 23);
            this.txtTimKhachHang.TabIndex = 15;
            // 
            // lblKhachHang
            // 
            this.lblKhachHang.AutoSize = true;
            this.lblKhachHang.Location = new System.Drawing.Point(227, 55);
            this.lblKhachHang.Name = "lblKhachHang";
            this.lblKhachHang.Size = new System.Drawing.Size(71, 15);
            this.lblKhachHang.TabIndex = 14;
            this.lblKhachHang.Text = "Khách hàng";
            // 
            // cboLoaiKhach
            // 
            this.cboLoaiKhach.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.cboLoaiKhach.FormattingEnabled = true;
            this.cboLoaiKhach.Items.AddRange(new object[] {
            "Khách lẻ",
            "Khách lâu dài"});
            this.cboLoaiKhach.Location = new System.Drawing.Point(87, 52);
            this.cboLoaiKhach.Name = "cboLoaiKhach";
            this.cboLoaiKhach.Size = new System.Drawing.Size(121, 23);
            this.cboLoaiKhach.TabIndex = 13;
            this.cboLoaiKhach.SelectedIndexChanged += new System.EventHandler(this.cboLoaiKhach_SelectedIndexChanged);
            // 
            // lblLoaiKhach
            // 
            this.lblLoaiKhach.AutoSize = true;
            this.lblLoaiKhach.Location = new System.Drawing.Point(15, 55);
            this.lblLoaiKhach.Name = "lblLoaiKhach";
            this.lblLoaiKhach.Size = new System.Drawing.Size(63, 15);
            this.lblLoaiKhach.TabIndex = 12;
            this.lblLoaiKhach.Text = "Loại khách";
            // 
            // txtTongTien
            // 
            this.txtTongTien.Location = new System.Drawing.Point(815, 87);
            this.txtTongTien.Name = "txtTongTien";
            this.txtTongTien.ReadOnly = true;
            this.txtTongTien.Size = new System.Drawing.Size(145, 23);
            this.txtTongTien.TabIndex = 11;
            // 
            // lblTongTien
            // 
            this.lblTongTien.AutoSize = true;
            this.lblTongTien.Location = new System.Drawing.Point(755, 90);
            this.lblTongTien.Name = "lblTongTien";
            this.lblTongTien.Size = new System.Drawing.Size(55, 15);
            this.lblTongTien.TabIndex = 10;
            this.lblTongTien.Text = "Tổng tiền";
            // 
            // txtGhiChu
            // 
            this.txtGhiChu.Location = new System.Drawing.Point(87, 87);
            this.txtGhiChu.Name = "txtGhiChu";
            this.txtGhiChu.Size = new System.Drawing.Size(390, 23);
            this.txtGhiChu.TabIndex = 9;
            // 
            // lblGhiChu
            // 
            this.lblGhiChu.AutoSize = true;
            this.lblGhiChu.Location = new System.Drawing.Point(15, 90);
            this.lblGhiChu.Name = "lblGhiChu";
            this.lblGhiChu.Size = new System.Drawing.Size(47, 15);
            this.lblGhiChu.TabIndex = 8;
            this.lblGhiChu.Text = "Ghi chú";
            // 
            // txtThanhToan
            // 
            this.txtThanhToan.Location = new System.Drawing.Point(625, 87);
            this.txtThanhToan.Name = "txtThanhToan";
            this.txtThanhToan.ReadOnly = true;
            this.txtThanhToan.Size = new System.Drawing.Size(110, 23);
            this.txtThanhToan.TabIndex = 7;
            this.txtThanhToan.Text = "0";
            // 
            // lblThanhToan
            // 
            this.lblThanhToan.AutoSize = true;
            this.lblThanhToan.Location = new System.Drawing.Point(548, 90);
            this.lblThanhToan.Name = "lblThanhToan";
            this.lblThanhToan.Size = new System.Drawing.Size(71, 15);
            this.lblThanhToan.TabIndex = 6;
            this.lblThanhToan.Text = "Cần thu";
            // 
            // txtGiamGia
            // 
            this.txtGiamGia.Location = new System.Drawing.Point(625, 17);
            this.txtGiamGia.Name = "txtGiamGia";
            this.txtGiamGia.Size = new System.Drawing.Size(110, 23);
            this.txtGiamGia.TabIndex = 5;
            this.txtGiamGia.Text = "0";
            this.txtGiamGia.TextChanged += new System.EventHandler(this.txtGiamGia_TextChanged);
            this.txtGiamGia.Leave += new System.EventHandler(this.txtGiamGia_Leave);
            // 
            // lblGiamGia
            // 
            this.lblGiamGia.AutoSize = true;
            this.lblGiamGia.Location = new System.Drawing.Point(565, 20);
            this.lblGiamGia.Name = "lblGiamGia";
            this.lblGiamGia.Size = new System.Drawing.Size(54, 15);
            this.lblGiamGia.TabIndex = 4;
            this.lblGiamGia.Text = "Giảm giá";
            // 
            // dtpNgayBan
            // 
            this.dtpNgayBan.CustomFormat = "dd/MM/yyyy HH:mm";
            this.dtpNgayBan.Format = System.Windows.Forms.DateTimePickerFormat.Custom;
            this.dtpNgayBan.Location = new System.Drawing.Point(373, 17);
            this.dtpNgayBan.Name = "dtpNgayBan";
            this.dtpNgayBan.Size = new System.Drawing.Size(150, 23);
            this.dtpNgayBan.TabIndex = 3;
            // 
            // lblNgayBan
            // 
            this.lblNgayBan.AutoSize = true;
            this.lblNgayBan.Location = new System.Drawing.Point(309, 20);
            this.lblNgayBan.Name = "lblNgayBan";
            this.lblNgayBan.Size = new System.Drawing.Size(58, 15);
            this.lblNgayBan.TabIndex = 2;
            this.lblNgayBan.Text = "Ngày bán";
            // 
            // txtNhanVien
            // 
            this.txtNhanVien.Location = new System.Drawing.Point(87, 17);
            this.txtNhanVien.Name = "txtNhanVien";
            this.txtNhanVien.ReadOnly = true;
            this.txtNhanVien.Size = new System.Drawing.Size(200, 23);
            this.txtNhanVien.TabIndex = 1;
            // 
            // lblNhanVien
            // 
            this.lblNhanVien.AutoSize = true;
            this.lblNhanVien.Location = new System.Drawing.Point(15, 20);
            this.lblNhanVien.Name = "lblNhanVien";
            this.lblNhanVien.Size = new System.Drawing.Size(61, 15);
            this.lblNhanVien.TabIndex = 0;
            this.lblNhanVien.Text = "Nhân viên";
            // 
            // tabLichSu
            // 
            this.tabLichSu.Controls.Add(this.dgvChiTietLichSu);
            this.tabLichSu.Controls.Add(this.lblChiTietHoaDon);
            this.tabLichSu.Controls.Add(this.dgvLichSuHoaDon);
            this.tabLichSu.Controls.Add(this.panelLocLichSu);
            this.tabLichSu.Location = new System.Drawing.Point(4, 24);
            this.tabLichSu.Name = "tabLichSu";
            this.tabLichSu.Padding = new System.Windows.Forms.Padding(3);
            this.tabLichSu.Size = new System.Drawing.Size(972, 572);
            this.tabLichSu.TabIndex = 1;
            this.tabLichSu.Text = "Lịch sử hóa đơn";
            this.tabLichSu.UseVisualStyleBackColor = true;
            // 
            // dgvChiTietLichSu
            // 
            this.dgvChiTietLichSu.AllowUserToAddRows = false;
            this.dgvChiTietLichSu.AllowUserToDeleteRows = false;
            this.dgvChiTietLichSu.AutoSizeColumnsMode = System.Windows.Forms.DataGridViewAutoSizeColumnsMode.Fill;
            this.dgvChiTietLichSu.BackgroundColor = System.Drawing.Color.White;
            this.dgvChiTietLichSu.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
            this.dgvChiTietLichSu.Dock = System.Windows.Forms.DockStyle.Fill;
            this.dgvChiTietLichSu.Location = new System.Drawing.Point(3, 332);
            this.dgvChiTietLichSu.MultiSelect = false;
            this.dgvChiTietLichSu.Name = "dgvChiTietLichSu";
            this.dgvChiTietLichSu.ReadOnly = true;
            this.dgvChiTietLichSu.RowHeadersVisible = false;
            this.dgvChiTietLichSu.SelectionMode = System.Windows.Forms.DataGridViewSelectionMode.FullRowSelect;
            this.dgvChiTietLichSu.Size = new System.Drawing.Size(966, 237);
            this.dgvChiTietLichSu.TabIndex = 3;
            // 
            // lblChiTietHoaDon
            // 
            this.lblChiTietHoaDon.Dock = System.Windows.Forms.DockStyle.Top;
            this.lblChiTietHoaDon.Font = new System.Drawing.Font("Segoe UI", 9F, System.Drawing.FontStyle.Bold);
            this.lblChiTietHoaDon.Location = new System.Drawing.Point(3, 307);
            this.lblChiTietHoaDon.Name = "lblChiTietHoaDon";
            this.lblChiTietHoaDon.Padding = new System.Windows.Forms.Padding(8, 6, 0, 0);
            this.lblChiTietHoaDon.Size = new System.Drawing.Size(966, 25);
            this.lblChiTietHoaDon.TabIndex = 2;
            this.lblChiTietHoaDon.Text = "Chi tiết hóa đơn: (chưa chọn)";
            // 
            // dgvLichSuHoaDon
            // 
            this.dgvLichSuHoaDon.AllowUserToAddRows = false;
            this.dgvLichSuHoaDon.AllowUserToDeleteRows = false;
            this.dgvLichSuHoaDon.AutoSizeColumnsMode = System.Windows.Forms.DataGridViewAutoSizeColumnsMode.Fill;
            this.dgvLichSuHoaDon.BackgroundColor = System.Drawing.Color.White;
            this.dgvLichSuHoaDon.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
            this.dgvLichSuHoaDon.Dock = System.Windows.Forms.DockStyle.Top;
            this.dgvLichSuHoaDon.Location = new System.Drawing.Point(3, 65);
            this.dgvLichSuHoaDon.MultiSelect = false;
            this.dgvLichSuHoaDon.Name = "dgvLichSuHoaDon";
            this.dgvLichSuHoaDon.ReadOnly = true;
            this.dgvLichSuHoaDon.RowHeadersVisible = false;
            this.dgvLichSuHoaDon.SelectionMode = System.Windows.Forms.DataGridViewSelectionMode.FullRowSelect;
            this.dgvLichSuHoaDon.Size = new System.Drawing.Size(966, 242);
            this.dgvLichSuHoaDon.TabIndex = 1;
            this.dgvLichSuHoaDon.SelectionChanged += new System.EventHandler(this.dgvLichSuHoaDon_SelectionChanged);
            // 
            // panelLocLichSu
            // 
            this.panelLocLichSu.Controls.Add(this.btnTimLichSu);
            this.panelLocLichSu.Controls.Add(this.txtTuKhoaLichSu);
            this.panelLocLichSu.Controls.Add(this.lblTuKhoaLichSu);
            this.panelLocLichSu.Controls.Add(this.dtpDenNgay);
            this.panelLocLichSu.Controls.Add(this.lblDenNgay);
            this.panelLocLichSu.Controls.Add(this.dtpTuNgay);
            this.panelLocLichSu.Controls.Add(this.lblTuNgay);
            this.panelLocLichSu.Dock = System.Windows.Forms.DockStyle.Top;
            this.panelLocLichSu.Location = new System.Drawing.Point(3, 3);
            this.panelLocLichSu.Name = "panelLocLichSu";
            this.panelLocLichSu.Padding = new System.Windows.Forms.Padding(12, 8, 12, 8);
            this.panelLocLichSu.Size = new System.Drawing.Size(966, 62);
            this.panelLocLichSu.TabIndex = 0;
            // 
            // btnTimLichSu
            // 
            this.btnTimLichSu.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnTimLichSu.Location = new System.Drawing.Point(669, 16);
            this.btnTimLichSu.Name = "btnTimLichSu";
            this.btnTimLichSu.Size = new System.Drawing.Size(85, 30);
            this.btnTimLichSu.TabIndex = 6;
            this.btnTimLichSu.Text = "Tìm kiếm";
            this.btnTimLichSu.UseVisualStyleBackColor = true;
            this.btnTimLichSu.Click += new System.EventHandler(this.btnTimLichSu_Click);
            // 
            // txtTuKhoaLichSu
            // 
            this.txtTuKhoaLichSu.Location = new System.Drawing.Point(478, 19);
            this.txtTuKhoaLichSu.Name = "txtTuKhoaLichSu";
            this.txtTuKhoaLichSu.Size = new System.Drawing.Size(175, 23);
            this.txtTuKhoaLichSu.TabIndex = 5;
            // 
            // lblTuKhoaLichSu
            // 
            this.lblTuKhoaLichSu.AutoSize = true;
            this.lblTuKhoaLichSu.Location = new System.Drawing.Point(419, 22);
            this.lblTuKhoaLichSu.Name = "lblTuKhoaLichSu";
            this.lblTuKhoaLichSu.Size = new System.Drawing.Size(53, 15);
            this.lblTuKhoaLichSu.TabIndex = 4;
            this.lblTuKhoaLichSu.Text = "Từ khóa";
            // 
            // dtpDenNgay
            // 
            this.dtpDenNgay.Format = System.Windows.Forms.DateTimePickerFormat.Short;
            this.dtpDenNgay.Location = new System.Drawing.Point(280, 19);
            this.dtpDenNgay.Name = "dtpDenNgay";
            this.dtpDenNgay.Size = new System.Drawing.Size(120, 23);
            this.dtpDenNgay.TabIndex = 3;
            // 
            // lblDenNgay
            // 
            this.lblDenNgay.AutoSize = true;
            this.lblDenNgay.Location = new System.Drawing.Point(219, 22);
            this.lblDenNgay.Name = "lblDenNgay";
            this.lblDenNgay.Size = new System.Drawing.Size(55, 15);
            this.lblDenNgay.TabIndex = 2;
            this.lblDenNgay.Text = "Đến ngày";
            // 
            // dtpTuNgay
            // 
            this.dtpTuNgay.Format = System.Windows.Forms.DateTimePickerFormat.Short;
            this.dtpTuNgay.Location = new System.Drawing.Point(81, 19);
            this.dtpTuNgay.Name = "dtpTuNgay";
            this.dtpTuNgay.Size = new System.Drawing.Size(120, 23);
            this.dtpTuNgay.TabIndex = 1;
            // 
            // lblTuNgay
            // 
            this.lblTuNgay.AutoSize = true;
            this.lblTuNgay.Location = new System.Drawing.Point(15, 22);
            this.lblTuNgay.Name = "lblTuNgay";
            this.lblTuNgay.Size = new System.Drawing.Size(47, 15);
            this.lblTuNgay.TabIndex = 0;
            this.lblTuNgay.Text = "Từ ngày";
            // 
            // FormBanHang
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(7F, 15F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(980, 600);
            this.Controls.Add(this.tabBanHang);
            this.Font = new System.Drawing.Font("Segoe UI", 9F);
            this.Name = "FormBanHang";
            this.Text = "Bán hàng";
            this.Load += new System.EventHandler(this.FormBanHang_Load);
            this.tabBanHang.ResumeLayout(false);
            this.tabTaoHoaDon.ResumeLayout(false);
            ((System.ComponentModel.ISupportInitialize)(this.dgvChiTiet)).EndInit();
            this.panelNut.ResumeLayout(false);
            this.panelThongTin.ResumeLayout(false);
            this.panelThongTin.PerformLayout();
            this.tabLichSu.ResumeLayout(false);
            ((System.ComponentModel.ISupportInitialize)(this.dgvChiTietLichSu)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.dgvLichSuHoaDon)).EndInit();
            this.panelLocLichSu.ResumeLayout(false);
            this.panelLocLichSu.PerformLayout();
            this.ResumeLayout(false);
        }

        private System.Windows.Forms.TabControl tabBanHang;
        private System.Windows.Forms.TabPage tabTaoHoaDon;
        private System.Windows.Forms.DataGridView dgvChiTiet;
        private System.Windows.Forms.Panel panelNut;
        private System.Windows.Forms.Button btnLamMoi;
        private System.Windows.Forms.Button btnLuuTam;
        private System.Windows.Forms.Button btnDanhSachLuuTam;
        private System.Windows.Forms.Button btnLuuHoaDon;
        private System.Windows.Forms.Panel panelThongTin;
        private System.Windows.Forms.Button btnQuanLyKhachHang;
        private System.Windows.Forms.ComboBox cboKhachHang;
        private System.Windows.Forms.Button btnTimKhachHang;
        private System.Windows.Forms.TextBox txtTimKhachHang;
        private System.Windows.Forms.Label lblKhachHang;
        private System.Windows.Forms.ComboBox cboLoaiKhach;
        private System.Windows.Forms.Label lblLoaiKhach;
        private System.Windows.Forms.TextBox txtTongTien;
        private System.Windows.Forms.Label lblTongTien;
        private System.Windows.Forms.TextBox txtGhiChu;
        private System.Windows.Forms.Label lblGhiChu;
        private System.Windows.Forms.TextBox txtThanhToan;
        private System.Windows.Forms.Label lblThanhToan;
        private System.Windows.Forms.TextBox txtGiamGia;
        private System.Windows.Forms.Label lblGiamGia;
        private System.Windows.Forms.DateTimePicker dtpNgayBan;
        private System.Windows.Forms.Label lblNgayBan;
        private System.Windows.Forms.TextBox txtNhanVien;
        private System.Windows.Forms.Label lblNhanVien;
        private System.Windows.Forms.TabPage tabLichSu;
        private System.Windows.Forms.DataGridView dgvChiTietLichSu;
        private System.Windows.Forms.Label lblChiTietHoaDon;
        private System.Windows.Forms.DataGridView dgvLichSuHoaDon;
        private System.Windows.Forms.Panel panelLocLichSu;
        private System.Windows.Forms.Button btnTimLichSu;
        private System.Windows.Forms.TextBox txtTuKhoaLichSu;
        private System.Windows.Forms.Label lblTuKhoaLichSu;
        private System.Windows.Forms.DateTimePicker dtpDenNgay;
        private System.Windows.Forms.Label lblDenNgay;
        private System.Windows.Forms.DateTimePicker dtpTuNgay;
        private System.Windows.Forms.Label lblTuNgay;
    }
}
