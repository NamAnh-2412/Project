namespace QuanLyQuayThuoc.GUI
{
    partial class FormNhapKho
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
            this.tabNhapKho = new System.Windows.Forms.TabControl();
            this.tabTaoPhieu = new System.Windows.Forms.TabPage();
            this.dgvChiTiet = new System.Windows.Forms.DataGridView();
            this.panelNut = new System.Windows.Forms.Panel();
            this.lblHuongDan = new System.Windows.Forms.Label();
            this.btnLamMoi = new System.Windows.Forms.Button();
            this.btnLuuPhieu = new System.Windows.Forms.Button();
            this.panelThongTin = new System.Windows.Forms.Panel();
            this.txtTongTien = new System.Windows.Forms.TextBox();
            this.lblTongTien = new System.Windows.Forms.Label();
            this.txtGhiChu = new System.Windows.Forms.TextBox();
            this.lblGhiChu = new System.Windows.Forms.Label();
            this.txtDaThanhToan = new System.Windows.Forms.TextBox();
            this.lblDaThanhToan = new System.Windows.Forms.Label();
            this.txtSoHoaDonNCC = new System.Windows.Forms.TextBox();
            this.lblSoHoaDonNCC = new System.Windows.Forms.Label();
            this.dtpNgayNhap = new System.Windows.Forms.DateTimePicker();
            this.lblNgayNhap = new System.Windows.Forms.Label();
            this.cboNhaCC = new System.Windows.Forms.ComboBox();
            this.lblNhaCC = new System.Windows.Forms.Label();
            this.txtNhanVien = new System.Windows.Forms.TextBox();
            this.lblNhanVien = new System.Windows.Forms.Label();
            this.tabLichSu = new System.Windows.Forms.TabPage();
            this.dgvChiTietLichSu = new System.Windows.Forms.DataGridView();
            this.lblChiTietPhieu = new System.Windows.Forms.Label();
            this.dgvLichSuPhieuNhap = new System.Windows.Forms.DataGridView();
            this.panelLocLichSu = new System.Windows.Forms.Panel();
            this.btnTimLichSu = new System.Windows.Forms.Button();
            this.txtTuKhoaLichSu = new System.Windows.Forms.TextBox();
            this.lblTuKhoaLichSu = new System.Windows.Forms.Label();
            this.dtpDenNgay = new System.Windows.Forms.DateTimePicker();
            this.lblDenNgay = new System.Windows.Forms.Label();
            this.dtpTuNgay = new System.Windows.Forms.DateTimePicker();
            this.lblTuNgay = new System.Windows.Forms.Label();
            this.tabNhapKho.SuspendLayout();
            this.tabTaoPhieu.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.dgvChiTiet)).BeginInit();
            this.panelNut.SuspendLayout();
            this.panelThongTin.SuspendLayout();
            this.tabLichSu.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.dgvChiTietLichSu)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.dgvLichSuPhieuNhap)).BeginInit();
            this.panelLocLichSu.SuspendLayout();
            this.SuspendLayout();
            // 
            // tabNhapKho
            // 
            this.tabNhapKho.Controls.Add(this.tabTaoPhieu);
            this.tabNhapKho.Controls.Add(this.tabLichSu);
            this.tabNhapKho.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tabNhapKho.Location = new System.Drawing.Point(0, 0);
            this.tabNhapKho.Name = "tabNhapKho";
            this.tabNhapKho.SelectedIndex = 0;
            this.tabNhapKho.Size = new System.Drawing.Size(980, 600);
            this.tabNhapKho.TabIndex = 0;
            // 
            // tabTaoPhieu
            // 
            this.tabTaoPhieu.Controls.Add(this.dgvChiTiet);
            this.tabTaoPhieu.Controls.Add(this.panelNut);
            this.tabTaoPhieu.Controls.Add(this.panelThongTin);
            this.tabTaoPhieu.Location = new System.Drawing.Point(4, 24);
            this.tabTaoPhieu.Name = "tabTaoPhieu";
            this.tabTaoPhieu.Padding = new System.Windows.Forms.Padding(3);
            this.tabTaoPhieu.Size = new System.Drawing.Size(972, 572);
            this.tabTaoPhieu.TabIndex = 0;
            this.tabTaoPhieu.Text = "Nhập mới";
            this.tabTaoPhieu.UseVisualStyleBackColor = true;
            // 
            // dgvChiTiet
            // 
            this.dgvChiTiet.BackgroundColor = System.Drawing.Color.White;
            this.dgvChiTiet.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
            this.dgvChiTiet.Dock = System.Windows.Forms.DockStyle.Fill;
            this.dgvChiTiet.Location = new System.Drawing.Point(3, 198);
            this.dgvChiTiet.Name = "dgvChiTiet";
            this.dgvChiTiet.RowHeadersVisible = false;
            this.dgvChiTiet.Size = new System.Drawing.Size(966, 371);
            this.dgvChiTiet.TabIndex = 2;
            this.dgvChiTiet.CellValueChanged += new System.Windows.Forms.DataGridViewCellEventHandler(this.dgvChiTiet_CellValueChanged);
            this.dgvChiTiet.CurrentCellDirtyStateChanged += new System.EventHandler(this.dgvChiTiet_CurrentCellDirtyStateChanged);
            this.dgvChiTiet.CellEndEdit += new System.Windows.Forms.DataGridViewCellEventHandler(this.dgvChiTiet_CellEndEdit);
            this.dgvChiTiet.RowsRemoved += new System.Windows.Forms.DataGridViewRowsRemovedEventHandler(this.dgvChiTiet_RowsRemoved);
            this.dgvChiTiet.UserAddedRow += new System.Windows.Forms.DataGridViewRowEventHandler(this.dgvChiTiet_UserAddedRow);
            // 
            // panelNut
            // 
            this.panelNut.Controls.Add(this.lblHuongDan);
            this.panelNut.Controls.Add(this.btnLamMoi);
            this.panelNut.Controls.Add(this.btnLuuPhieu);
            this.panelNut.Dock = System.Windows.Forms.DockStyle.Top;
            this.panelNut.Location = new System.Drawing.Point(3, 146);
            this.panelNut.Name = "panelNut";
            this.panelNut.Padding = new System.Windows.Forms.Padding(12, 6, 12, 6);
            this.panelNut.Size = new System.Drawing.Size(966, 52);
            this.panelNut.TabIndex = 1;
            // 
            // lblHuongDan
            // 
            this.lblHuongDan.AutoSize = true;
            this.lblHuongDan.Location = new System.Drawing.Point(258, 17);
            this.lblHuongDan.Name = "lblHuongDan";
            this.lblHuongDan.Size = new System.Drawing.Size(401, 15);
            this.lblHuongDan.TabIndex = 2;
            this.lblHuongDan.Text = "Nhập hạn sử dụng theo định dạng ngày hợp lệ, ví dụ: 31/12/2026 hoặc 2026-12-31";
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
            // btnLuuPhieu
            // 
            this.btnLuuPhieu.BackColor = System.Drawing.Color.FromArgb(((int)(((byte)(0)))), ((int)(((byte)(122)))), ((int)(((byte)(204)))));
            this.btnLuuPhieu.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnLuuPhieu.ForeColor = System.Drawing.Color.White;
            this.btnLuuPhieu.Location = new System.Drawing.Point(15, 11);
            this.btnLuuPhieu.Name = "btnLuuPhieu";
            this.btnLuuPhieu.Size = new System.Drawing.Size(100, 30);
            this.btnLuuPhieu.TabIndex = 0;
            this.btnLuuPhieu.Text = "Lưu phiếu";
            this.btnLuuPhieu.UseVisualStyleBackColor = false;
            this.btnLuuPhieu.Click += new System.EventHandler(this.btnLuuPhieu_Click);
            // 
            // panelThongTin
            // 
            this.panelThongTin.BackColor = System.Drawing.Color.WhiteSmoke;
            this.panelThongTin.Controls.Add(this.txtTongTien);
            this.panelThongTin.Controls.Add(this.lblTongTien);
            this.panelThongTin.Controls.Add(this.txtGhiChu);
            this.panelThongTin.Controls.Add(this.lblGhiChu);
            this.panelThongTin.Controls.Add(this.txtDaThanhToan);
            this.panelThongTin.Controls.Add(this.lblDaThanhToan);
            this.panelThongTin.Controls.Add(this.txtSoHoaDonNCC);
            this.panelThongTin.Controls.Add(this.lblSoHoaDonNCC);
            this.panelThongTin.Controls.Add(this.dtpNgayNhap);
            this.panelThongTin.Controls.Add(this.lblNgayNhap);
            this.panelThongTin.Controls.Add(this.cboNhaCC);
            this.panelThongTin.Controls.Add(this.lblNhaCC);
            this.panelThongTin.Controls.Add(this.txtNhanVien);
            this.panelThongTin.Controls.Add(this.lblNhanVien);
            this.panelThongTin.Dock = System.Windows.Forms.DockStyle.Top;
            this.panelThongTin.Location = new System.Drawing.Point(3, 3);
            this.panelThongTin.Name = "panelThongTin";
            this.panelThongTin.Padding = new System.Windows.Forms.Padding(12, 8, 12, 8);
            this.panelThongTin.Size = new System.Drawing.Size(966, 143);
            this.panelThongTin.TabIndex = 0;
            // 
            // txtTongTien
            // 
            this.txtTongTien.Location = new System.Drawing.Point(815, 100);
            this.txtTongTien.Name = "txtTongTien";
            this.txtTongTien.ReadOnly = true;
            this.txtTongTien.Size = new System.Drawing.Size(145, 23);
            this.txtTongTien.TabIndex = 13;
            // 
            // lblTongTien
            // 
            this.lblTongTien.AutoSize = true;
            this.lblTongTien.Location = new System.Drawing.Point(748, 103);
            this.lblTongTien.Name = "lblTongTien";
            this.lblTongTien.Size = new System.Drawing.Size(55, 15);
            this.lblTongTien.TabIndex = 12;
            this.lblTongTien.Text = "Tổng tiền";
            // 
            // txtGhiChu
            // 
            this.txtGhiChu.Location = new System.Drawing.Point(95, 100);
            this.txtGhiChu.Name = "txtGhiChu";
            this.txtGhiChu.Size = new System.Drawing.Size(632, 23);
            this.txtGhiChu.TabIndex = 11;
            // 
            // lblGhiChu
            // 
            this.lblGhiChu.AutoSize = true;
            this.lblGhiChu.Location = new System.Drawing.Point(15, 103);
            this.lblGhiChu.Name = "lblGhiChu";
            this.lblGhiChu.Size = new System.Drawing.Size(47, 15);
            this.lblGhiChu.TabIndex = 10;
            this.lblGhiChu.Text = "Ghi chú";
            // 
            // txtDaThanhToan
            // 
            this.txtDaThanhToan.Location = new System.Drawing.Point(815, 60);
            this.txtDaThanhToan.Name = "txtDaThanhToan";
            this.txtDaThanhToan.Size = new System.Drawing.Size(145, 23);
            this.txtDaThanhToan.TabIndex = 9;
            this.txtDaThanhToan.Text = "0";
            this.txtDaThanhToan.Leave += new System.EventHandler(this.txtDaThanhToan_Leave);
            // 
            // lblDaThanhToan
            // 
            this.lblDaThanhToan.AutoSize = true;
            this.lblDaThanhToan.Location = new System.Drawing.Point(725, 63);
            this.lblDaThanhToan.Name = "lblDaThanhToan";
            this.lblDaThanhToan.Size = new System.Drawing.Size(84, 15);
            this.lblDaThanhToan.TabIndex = 8;
            this.lblDaThanhToan.Text = "Đã thanh toán";
            // 
            // txtSoHoaDonNCC
            // 
            this.txtSoHoaDonNCC.Location = new System.Drawing.Point(489, 60);
            this.txtSoHoaDonNCC.Name = "txtSoHoaDonNCC";
            this.txtSoHoaDonNCC.Size = new System.Drawing.Size(220, 23);
            this.txtSoHoaDonNCC.TabIndex = 7;
            // 
            // lblSoHoaDonNCC
            // 
            this.lblSoHoaDonNCC.AutoSize = true;
            this.lblSoHoaDonNCC.Location = new System.Drawing.Point(392, 63);
            this.lblSoHoaDonNCC.Name = "lblSoHoaDonNCC";
            this.lblSoHoaDonNCC.Size = new System.Drawing.Size(91, 15);
            this.lblSoHoaDonNCC.TabIndex = 6;
            this.lblSoHoaDonNCC.Text = "Số hóa đơn NCC";
            // 
            // dtpNgayNhap
            // 
            this.dtpNgayNhap.CustomFormat = "dd/MM/yyyy HH:mm";
            this.dtpNgayNhap.Format = System.Windows.Forms.DateTimePickerFormat.Custom;
            this.dtpNgayNhap.Location = new System.Drawing.Point(95, 60);
            this.dtpNgayNhap.Name = "dtpNgayNhap";
            this.dtpNgayNhap.Size = new System.Drawing.Size(160, 23);
            this.dtpNgayNhap.TabIndex = 5;
            // 
            // lblNgayNhap
            // 
            this.lblNgayNhap.AutoSize = true;
            this.lblNgayNhap.Location = new System.Drawing.Point(15, 63);
            this.lblNgayNhap.Name = "lblNgayNhap";
            this.lblNgayNhap.Size = new System.Drawing.Size(61, 15);
            this.lblNgayNhap.TabIndex = 4;
            this.lblNgayNhap.Text = "Ngày nhập";
            // 
            // cboNhaCC
            // 
            this.cboNhaCC.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.cboNhaCC.FormattingEnabled = true;
            this.cboNhaCC.Location = new System.Drawing.Point(489, 20);
            this.cboNhaCC.Name = "cboNhaCC";
            this.cboNhaCC.Size = new System.Drawing.Size(471, 23);
            this.cboNhaCC.TabIndex = 3;
            // 
            // lblNhaCC
            // 
            this.lblNhaCC.AutoSize = true;
            this.lblNhaCC.Location = new System.Drawing.Point(392, 23);
            this.lblNhaCC.Name = "lblNhaCC";
            this.lblNhaCC.Size = new System.Drawing.Size(84, 15);
            this.lblNhaCC.TabIndex = 2;
            this.lblNhaCC.Text = "Nhà cung cấp";
            // 
            // txtNhanVien
            // 
            this.txtNhanVien.Location = new System.Drawing.Point(95, 20);
            this.txtNhanVien.Name = "txtNhanVien";
            this.txtNhanVien.ReadOnly = true;
            this.txtNhanVien.Size = new System.Drawing.Size(273, 23);
            this.txtNhanVien.TabIndex = 1;
            // 
            // lblNhanVien
            // 
            this.lblNhanVien.AutoSize = true;
            this.lblNhanVien.Location = new System.Drawing.Point(15, 23);
            this.lblNhanVien.Name = "lblNhanVien";
            this.lblNhanVien.Size = new System.Drawing.Size(61, 15);
            this.lblNhanVien.TabIndex = 0;
            this.lblNhanVien.Text = "Nhân viên";
            // 
            // tabLichSu
            // 
            this.tabLichSu.Controls.Add(this.dgvChiTietLichSu);
            this.tabLichSu.Controls.Add(this.lblChiTietPhieu);
            this.tabLichSu.Controls.Add(this.dgvLichSuPhieuNhap);
            this.tabLichSu.Controls.Add(this.panelLocLichSu);
            this.tabLichSu.Location = new System.Drawing.Point(4, 24);
            this.tabLichSu.Name = "tabLichSu";
            this.tabLichSu.Padding = new System.Windows.Forms.Padding(3);
            this.tabLichSu.Size = new System.Drawing.Size(972, 572);
            this.tabLichSu.TabIndex = 1;
            this.tabLichSu.Text = "Lịch sử phiếu nhập";
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
            // lblChiTietPhieu
            // 
            this.lblChiTietPhieu.Dock = System.Windows.Forms.DockStyle.Top;
            this.lblChiTietPhieu.Font = new System.Drawing.Font("Segoe UI", 9F, System.Drawing.FontStyle.Bold);
            this.lblChiTietPhieu.Location = new System.Drawing.Point(3, 307);
            this.lblChiTietPhieu.Name = "lblChiTietPhieu";
            this.lblChiTietPhieu.Padding = new System.Windows.Forms.Padding(8, 6, 0, 0);
            this.lblChiTietPhieu.Size = new System.Drawing.Size(966, 25);
            this.lblChiTietPhieu.TabIndex = 2;
            this.lblChiTietPhieu.Text = "Chi tiết phiếu nhập: (chưa chọn)";
            // 
            // dgvLichSuPhieuNhap
            // 
            this.dgvLichSuPhieuNhap.AllowUserToAddRows = false;
            this.dgvLichSuPhieuNhap.AllowUserToDeleteRows = false;
            this.dgvLichSuPhieuNhap.AutoSizeColumnsMode = System.Windows.Forms.DataGridViewAutoSizeColumnsMode.Fill;
            this.dgvLichSuPhieuNhap.BackgroundColor = System.Drawing.Color.White;
            this.dgvLichSuPhieuNhap.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
            this.dgvLichSuPhieuNhap.Dock = System.Windows.Forms.DockStyle.Top;
            this.dgvLichSuPhieuNhap.Location = new System.Drawing.Point(3, 65);
            this.dgvLichSuPhieuNhap.MultiSelect = false;
            this.dgvLichSuPhieuNhap.Name = "dgvLichSuPhieuNhap";
            this.dgvLichSuPhieuNhap.ReadOnly = true;
            this.dgvLichSuPhieuNhap.RowHeadersVisible = false;
            this.dgvLichSuPhieuNhap.SelectionMode = System.Windows.Forms.DataGridViewSelectionMode.FullRowSelect;
            this.dgvLichSuPhieuNhap.Size = new System.Drawing.Size(966, 242);
            this.dgvLichSuPhieuNhap.TabIndex = 1;
            this.dgvLichSuPhieuNhap.SelectionChanged += new System.EventHandler(this.dgvLichSuPhieuNhap_SelectionChanged);
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
            // FormNhapKho
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(7F, 15F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(980, 600);
            this.Controls.Add(this.tabNhapKho);
            this.Font = new System.Drawing.Font("Segoe UI", 9F);
            this.Name = "FormNhapKho";
            this.Text = "Nhập kho";
            this.Load += new System.EventHandler(this.FormNhapKho_Load);
            this.tabNhapKho.ResumeLayout(false);
            this.tabTaoPhieu.ResumeLayout(false);
            ((System.ComponentModel.ISupportInitialize)(this.dgvChiTiet)).EndInit();
            this.panelNut.ResumeLayout(false);
            this.panelNut.PerformLayout();
            this.panelThongTin.ResumeLayout(false);
            this.panelThongTin.PerformLayout();
            this.tabLichSu.ResumeLayout(false);
            ((System.ComponentModel.ISupportInitialize)(this.dgvChiTietLichSu)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.dgvLichSuPhieuNhap)).EndInit();
            this.panelLocLichSu.ResumeLayout(false);
            this.panelLocLichSu.PerformLayout();
            this.ResumeLayout(false);
        }

        private System.Windows.Forms.TabControl tabNhapKho;
        private System.Windows.Forms.TabPage tabTaoPhieu;
        private System.Windows.Forms.DataGridView dgvChiTiet;
        private System.Windows.Forms.Panel panelNut;
        private System.Windows.Forms.Label lblHuongDan;
        private System.Windows.Forms.Button btnLamMoi;
        private System.Windows.Forms.Button btnLuuPhieu;
        private System.Windows.Forms.Panel panelThongTin;
        private System.Windows.Forms.TextBox txtTongTien;
        private System.Windows.Forms.Label lblTongTien;
        private System.Windows.Forms.TextBox txtGhiChu;
        private System.Windows.Forms.Label lblGhiChu;
        private System.Windows.Forms.TextBox txtDaThanhToan;
        private System.Windows.Forms.Label lblDaThanhToan;
        private System.Windows.Forms.TextBox txtSoHoaDonNCC;
        private System.Windows.Forms.Label lblSoHoaDonNCC;
        private System.Windows.Forms.DateTimePicker dtpNgayNhap;
        private System.Windows.Forms.Label lblNgayNhap;
        private System.Windows.Forms.ComboBox cboNhaCC;
        private System.Windows.Forms.Label lblNhaCC;
        private System.Windows.Forms.TextBox txtNhanVien;
        private System.Windows.Forms.Label lblNhanVien;
        private System.Windows.Forms.TabPage tabLichSu;
        private System.Windows.Forms.DataGridView dgvChiTietLichSu;
        private System.Windows.Forms.Label lblChiTietPhieu;
        private System.Windows.Forms.DataGridView dgvLichSuPhieuNhap;
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
