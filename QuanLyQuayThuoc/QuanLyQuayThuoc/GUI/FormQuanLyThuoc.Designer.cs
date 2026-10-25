namespace QuanLyQuayThuoc.GUI
{
    partial class FormQuanLyThuoc
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
            this.panelLoc = new System.Windows.Forms.Panel();
            this.btnBoLoc = new System.Windows.Forms.Button();
            this.btnTimKiem = new System.Windows.Forms.Button();
            this.cboLocNhaCC = new System.Windows.Forms.ComboBox();
            this.lblLocNhaCC = new System.Windows.Forms.Label();
            this.cboLocDanhMuc = new System.Windows.Forms.ComboBox();
            this.lblLocDanhMuc = new System.Windows.Forms.Label();
            this.txtTuKhoa = new System.Windows.Forms.TextBox();
            this.lblTuKhoa = new System.Windows.Forms.Label();
            this.panelNhapLieu = new System.Windows.Forms.Panel();
            this.txtMoTa = new System.Windows.Forms.TextBox();
            this.lblMoTa = new System.Windows.Forms.Label();
            this.txtHoatChat = new System.Windows.Forms.TextBox();
            this.lblHoatChat = new System.Windows.Forms.Label();
            this.cboDonViTinh = new System.Windows.Forms.ComboBox();
            this.lblDonViTinh = new System.Windows.Forms.Label();
            this.cboDonViQuyDoi = new System.Windows.Forms.ComboBox();
            this.lblDonViQuyDoi = new System.Windows.Forms.Label();
            this.txtTyLeQuyDoi = new System.Windows.Forms.TextBox();
            this.lblTyLeQuyDoi = new System.Windows.Forms.Label();
            this.cboNhaCC = new System.Windows.Forms.ComboBox();
            this.lblNhaCC = new System.Windows.Forms.Label();
            this.cboDanhMuc = new System.Windows.Forms.ComboBox();
            this.lblDanhMuc = new System.Windows.Forms.Label();
            this.txtXuatXu = new System.Windows.Forms.TextBox();
            this.lblXuatXu = new System.Windows.Forms.Label();
            this.txtHamLuong = new System.Windows.Forms.TextBox();
            this.lblHamLuong = new System.Windows.Forms.Label();
            this.txtGiaBan = new System.Windows.Forms.TextBox();
            this.lblGiaBan = new System.Windows.Forms.Label();
            this.txtTenThuoc = new System.Windows.Forms.TextBox();
            this.lblTenThuoc = new System.Windows.Forms.Label();
            this.lblMaThuoc = new System.Windows.Forms.Label();
            this.lblSoLuongToiThieu = new System.Windows.Forms.Label();
            this.txtSoLuongToiThieu = new System.Windows.Forms.TextBox();
            this.panelNut = new System.Windows.Forms.Panel();
            this.btnQuanLyDanhMuc = new System.Windows.Forms.Button();
            this.btnQuanLyNCC = new System.Windows.Forms.Button();
            this.btnLamMoi = new System.Windows.Forms.Button();
            this.btnXoa = new System.Windows.Forms.Button();
            this.btnSua = new System.Windows.Forms.Button();
            this.btnThem = new System.Windows.Forms.Button();
            this.dgvThuoc = new System.Windows.Forms.DataGridView();
            this.panelLoc.SuspendLayout();
            this.panelNhapLieu.SuspendLayout();
            this.panelNut.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.dgvThuoc)).BeginInit();
            this.SuspendLayout();
            // 
            // panelLoc
            // 
            this.panelLoc.Controls.Add(this.btnBoLoc);
            this.panelLoc.Controls.Add(this.btnTimKiem);
            this.panelLoc.Controls.Add(this.cboLocNhaCC);
            this.panelLoc.Controls.Add(this.lblLocNhaCC);
            this.panelLoc.Controls.Add(this.cboLocDanhMuc);
            this.panelLoc.Controls.Add(this.lblLocDanhMuc);
            this.panelLoc.Controls.Add(this.txtTuKhoa);
            this.panelLoc.Controls.Add(this.lblTuKhoa);
            this.panelLoc.Dock = System.Windows.Forms.DockStyle.Top;
            this.panelLoc.Location = new System.Drawing.Point(0, 0);
            this.panelLoc.Name = "panelLoc";
            this.panelLoc.Padding = new System.Windows.Forms.Padding(12, 8, 12, 8);
            this.panelLoc.Size = new System.Drawing.Size(980, 62);
            this.panelLoc.TabIndex = 0;
            // 
            // btnBoLoc
            // 
            this.btnBoLoc.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnBoLoc.Location = new System.Drawing.Point(875, 17);
            this.btnBoLoc.Name = "btnBoLoc";
            this.btnBoLoc.Size = new System.Drawing.Size(85, 30);
            this.btnBoLoc.TabIndex = 7;
            this.btnBoLoc.Text = "Bỏ lọc";
            this.btnBoLoc.UseVisualStyleBackColor = true;
            this.btnBoLoc.Click += new System.EventHandler(this.btnBoLoc_Click);
            // 
            // btnTimKiem
            // 
            this.btnTimKiem.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnTimKiem.Location = new System.Drawing.Point(784, 17);
            this.btnTimKiem.Name = "btnTimKiem";
            this.btnTimKiem.Size = new System.Drawing.Size(85, 30);
            this.btnTimKiem.TabIndex = 6;
            this.btnTimKiem.Text = "Tìm kiếm";
            this.btnTimKiem.UseVisualStyleBackColor = true;
            this.btnTimKiem.Click += new System.EventHandler(this.btnTimKiem_Click);
            // 
            // cboLocNhaCC
            // 
            this.cboLocNhaCC.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.cboLocNhaCC.FormattingEnabled = true;
            this.cboLocNhaCC.Location = new System.Drawing.Point(590, 21);
            this.cboLocNhaCC.Name = "cboLocNhaCC";
            this.cboLocNhaCC.Size = new System.Drawing.Size(180, 23);
            this.cboLocNhaCC.TabIndex = 5;
            // 
            // lblLocNhaCC
            // 
            this.lblLocNhaCC.AutoSize = true;
            this.lblLocNhaCC.Location = new System.Drawing.Point(500, 24);
            this.lblLocNhaCC.Name = "lblLocNhaCC";
            this.lblLocNhaCC.Size = new System.Drawing.Size(84, 15);
            this.lblLocNhaCC.TabIndex = 4;
            this.lblLocNhaCC.Text = "Nhà cung cấp";
            // 
            // cboLocDanhMuc
            // 
            this.cboLocDanhMuc.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.cboLocDanhMuc.FormattingEnabled = true;
            this.cboLocDanhMuc.Location = new System.Drawing.Point(329, 21);
            this.cboLocDanhMuc.Name = "cboLocDanhMuc";
            this.cboLocDanhMuc.Size = new System.Drawing.Size(155, 23);
            this.cboLocDanhMuc.TabIndex = 3;
            // 
            // lblLocDanhMuc
            // 
            this.lblLocDanhMuc.AutoSize = true;
            this.lblLocDanhMuc.Location = new System.Drawing.Point(261, 24);
            this.lblLocDanhMuc.Name = "lblLocDanhMuc";
            this.lblLocDanhMuc.Size = new System.Drawing.Size(62, 15);
            this.lblLocDanhMuc.TabIndex = 2;
            this.lblLocDanhMuc.Text = "Danh mục";
            // 
            // txtTuKhoa
            // 
            this.txtTuKhoa.Location = new System.Drawing.Point(74, 21);
            this.txtTuKhoa.Name = "txtTuKhoa";
            this.txtTuKhoa.Size = new System.Drawing.Size(171, 23);
            this.txtTuKhoa.TabIndex = 1;
            // 
            // lblTuKhoa
            // 
            this.lblTuKhoa.AutoSize = true;
            this.lblTuKhoa.Location = new System.Drawing.Point(15, 24);
            this.lblTuKhoa.Name = "lblTuKhoa";
            this.lblTuKhoa.Size = new System.Drawing.Size(53, 15);
            this.lblTuKhoa.TabIndex = 0;
            this.lblTuKhoa.Text = "Từ khóa";
            // 
            // panelNhapLieu
            // 
            this.panelNhapLieu.BackColor = System.Drawing.Color.WhiteSmoke;
            this.panelNhapLieu.Controls.Add(this.txtSoLuongToiThieu);
            this.panelNhapLieu.Controls.Add(this.lblSoLuongToiThieu);
            this.panelNhapLieu.Controls.Add(this.txtMoTa);
            this.panelNhapLieu.Controls.Add(this.lblMoTa);
            this.panelNhapLieu.Controls.Add(this.txtHoatChat);
            this.panelNhapLieu.Controls.Add(this.lblHoatChat);
            this.panelNhapLieu.Controls.Add(this.txtTyLeQuyDoi);
            this.panelNhapLieu.Controls.Add(this.lblTyLeQuyDoi);
            this.panelNhapLieu.Controls.Add(this.cboDonViQuyDoi);
            this.panelNhapLieu.Controls.Add(this.lblDonViQuyDoi);
            this.panelNhapLieu.Controls.Add(this.cboDonViTinh);
            this.panelNhapLieu.Controls.Add(this.lblDonViTinh);
            this.panelNhapLieu.Controls.Add(this.cboNhaCC);
            this.panelNhapLieu.Controls.Add(this.lblNhaCC);
            this.panelNhapLieu.Controls.Add(this.cboDanhMuc);
            this.panelNhapLieu.Controls.Add(this.lblDanhMuc);
            this.panelNhapLieu.Controls.Add(this.txtXuatXu);
            this.panelNhapLieu.Controls.Add(this.lblXuatXu);
            this.panelNhapLieu.Controls.Add(this.txtHamLuong);
            this.panelNhapLieu.Controls.Add(this.lblHamLuong);
            this.panelNhapLieu.Controls.Add(this.txtGiaBan);
            this.panelNhapLieu.Controls.Add(this.lblGiaBan);
            this.panelNhapLieu.Controls.Add(this.txtTenThuoc);
            this.panelNhapLieu.Controls.Add(this.lblTenThuoc);
            this.panelNhapLieu.Controls.Add(this.lblMaThuoc);
            this.panelNhapLieu.Dock = System.Windows.Forms.DockStyle.Top;
            this.panelNhapLieu.Location = new System.Drawing.Point(0, 62);
            this.panelNhapLieu.Name = "panelNhapLieu";
            this.panelNhapLieu.Padding = new System.Windows.Forms.Padding(12, 8, 12, 8);
            this.panelNhapLieu.Size = new System.Drawing.Size(980, 205);
            this.panelNhapLieu.TabIndex = 0;
            // 
            // lblMaThuoc
            // 
            this.lblMaThuoc.AutoSize = true;
            this.lblMaThuoc.Font = new System.Drawing.Font("Segoe UI", 9F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblMaThuoc.Location = new System.Drawing.Point(15, 12);
            this.lblMaThuoc.Name = "lblMaThuoc";
            this.lblMaThuoc.Size = new System.Drawing.Size(180, 15);
            this.lblMaThuoc.TabIndex = 0;
            this.lblMaThuoc.Text = "Mã thuốc đang chọn: (chưa chọn)";
            // 
            // lblTenThuoc
            // 
            this.lblTenThuoc.AutoSize = true;
            this.lblTenThuoc.Location = new System.Drawing.Point(15, 40);
            this.lblTenThuoc.Name = "lblTenThuoc";
            this.lblTenThuoc.Size = new System.Drawing.Size(60, 15);
            this.lblTenThuoc.TabIndex = 1;
            this.lblTenThuoc.Text = "Tên thuốc";
            // 
            // txtTenThuoc
            // 
            this.txtTenThuoc.Location = new System.Drawing.Point(100, 37);
            this.txtTenThuoc.Name = "txtTenThuoc";
            this.txtTenThuoc.Size = new System.Drawing.Size(220, 23);
            this.txtTenThuoc.TabIndex = 2;
            // 
            // lblGiaBan
            // 
            this.lblGiaBan.AutoSize = true;
            this.lblGiaBan.Location = new System.Drawing.Point(340, 40);
            this.lblGiaBan.Name = "lblGiaBan";
            this.lblGiaBan.Size = new System.Drawing.Size(47, 15);
            this.lblGiaBan.TabIndex = 3;
            this.lblGiaBan.Text = "Giá bán";
            // 
            // txtGiaBan
            // 
            this.txtGiaBan.Location = new System.Drawing.Point(400, 37);
            this.txtGiaBan.Name = "txtGiaBan";
            this.txtGiaBan.Size = new System.Drawing.Size(120, 23);
            this.txtGiaBan.TabIndex = 3;
            // 
            // lblHamLuong
            // 
            this.lblHamLuong.AutoSize = true;
            this.lblHamLuong.Location = new System.Drawing.Point(540, 40);
            this.lblHamLuong.Name = "lblHamLuong";
            this.lblHamLuong.Size = new System.Drawing.Size(66, 15);
            this.lblHamLuong.TabIndex = 5;
            this.lblHamLuong.Text = "Hàm lượng";
            // 
            // txtHamLuong
            // 
            this.txtHamLuong.Location = new System.Drawing.Point(620, 37);
            this.txtHamLuong.Name = "txtHamLuong";
            this.txtHamLuong.Size = new System.Drawing.Size(120, 23);
            this.txtHamLuong.TabIndex = 4;
            // 
            // lblXuatXu
            // 
            this.lblXuatXu.AutoSize = true;
            this.lblXuatXu.Location = new System.Drawing.Point(760, 40);
            this.lblXuatXu.Name = "lblXuatXu";
            this.lblXuatXu.Size = new System.Drawing.Size(49, 15);
            this.lblXuatXu.TabIndex = 7;
            this.lblXuatXu.Text = "Xu\u1EA5t x\u1EE9";
            // 
            // txtXuatXu
            // 
            this.txtXuatXu.Location = new System.Drawing.Point(820, 37);
            this.txtXuatXu.Name = "txtXuatXu";
            this.txtXuatXu.Size = new System.Drawing.Size(140, 23);
            this.txtXuatXu.TabIndex = 5;
            // 
            // lblDanhMuc
            // 
            this.lblDanhMuc.AutoSize = true;
            this.lblDanhMuc.Location = new System.Drawing.Point(15, 78);
            this.lblDanhMuc.Name = "lblDanhMuc";
            this.lblDanhMuc.Size = new System.Drawing.Size(62, 15);
            this.lblDanhMuc.TabIndex = 9;
            this.lblDanhMuc.Text = "Danh mục";
            // 
            // cboDanhMuc
            // 
            this.cboDanhMuc.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.cboDanhMuc.FormattingEnabled = true;
            this.cboDanhMuc.Location = new System.Drawing.Point(100, 75);
            this.cboDanhMuc.Name = "cboDanhMuc";
            this.cboDanhMuc.Size = new System.Drawing.Size(220, 23);
            this.cboDanhMuc.TabIndex = 6;
            // 
            // lblNhaCC
            // 
            this.lblNhaCC.AutoSize = true;
            this.lblNhaCC.Location = new System.Drawing.Point(340, 78);
            this.lblNhaCC.Name = "lblNhaCC";
            this.lblNhaCC.Size = new System.Drawing.Size(84, 15);
            this.lblNhaCC.TabIndex = 11;
            this.lblNhaCC.Text = "Nhà cung cấp";
            // 
            // cboNhaCC
            // 
            this.cboNhaCC.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.cboNhaCC.FormattingEnabled = true;
            this.cboNhaCC.Location = new System.Drawing.Point(430, 75);
            this.cboNhaCC.Name = "cboNhaCC";
            this.cboNhaCC.Size = new System.Drawing.Size(310, 23);
            this.cboNhaCC.TabIndex = 7;
            // 
            // lblDonViTinh
            // 
            this.lblDonViTinh.AutoSize = true;
            this.lblDonViTinh.Location = new System.Drawing.Point(760, 78);
            this.lblDonViTinh.Name = "lblDonViTinh";
            this.lblDonViTinh.Size = new System.Drawing.Size(59, 15);
            this.lblDonViTinh.TabIndex = 13;
            this.lblDonViTinh.Text = "Đơn vị tính";
            // 
            // cboDonViTinh
            // 
            this.cboDonViTinh.FormattingEnabled = true;
            this.cboDonViTinh.Items.AddRange(new object[] {
            "Hộp",
            "Chai",
            "Lọ",
            "Tuýp"});
            this.cboDonViTinh.Location = new System.Drawing.Point(830, 75);
            this.cboDonViTinh.Name = "cboDonViTinh";
            this.cboDonViTinh.Size = new System.Drawing.Size(130, 23);
            this.cboDonViTinh.TabIndex = 8;
            this.cboDonViTinh.Text = "Hộp";
            // 
            // lblDonViQuyDoi
            // 
            this.lblDonViQuyDoi.AutoSize = true;
            this.lblDonViQuyDoi.Location = new System.Drawing.Point(15, 156);
            this.lblDonViQuyDoi.Name = "lblDonViQuyDoi";
            this.lblDonViQuyDoi.Size = new System.Drawing.Size(73, 15);
            this.lblDonViQuyDoi.TabIndex = 18;
            this.lblDonViQuyDoi.Text = "ĐV Quy Đổi";
            // 
            // cboDonViQuyDoi
            // 
            this.cboDonViQuyDoi.FormattingEnabled = true;
            this.cboDonViQuyDoi.Items.AddRange(new object[] {
            "Viên",
            "Gói",
            "Vỉ",
            "Ống",
            "Miếng"});
            this.cboDonViQuyDoi.Location = new System.Drawing.Point(100, 153);
            this.cboDonViQuyDoi.Name = "cboDonViQuyDoi";
            this.cboDonViQuyDoi.Size = new System.Drawing.Size(150, 23);
            this.cboDonViQuyDoi.TabIndex = 11;
            // 
            // lblTyLeQuyDoi
            // 
            this.lblTyLeQuyDoi.AutoSize = true;
            this.lblTyLeQuyDoi.Location = new System.Drawing.Point(310, 156);
            this.lblTyLeQuyDoi.Name = "lblTyLeQuyDoi";
            this.lblTyLeQuyDoi.Size = new System.Drawing.Size(56, 15);
            this.lblTyLeQuyDoi.TabIndex = 19;
            this.lblTyLeQuyDoi.Text = "Tỷ lệ QĐ";
            // 
            // txtTyLeQuyDoi
            // 
            this.txtTyLeQuyDoi.Location = new System.Drawing.Point(400, 153);
            this.txtTyLeQuyDoi.Name = "txtTyLeQuyDoi";
            this.txtTyLeQuyDoi.Size = new System.Drawing.Size(80, 23);
            this.txtTyLeQuyDoi.TabIndex = 12;
            this.txtTyLeQuyDoi.Text = "1";
            // 
            // lblSoLuongToiThieu
            // 
            this.lblSoLuongToiThieu.AutoSize = true;
            this.lblSoLuongToiThieu.Location = new System.Drawing.Point(540, 156);
            this.lblSoLuongToiThieu.Name = "lblSoLuongToiThieu";
            this.lblSoLuongToiThieu.Size = new System.Drawing.Size(100, 15);
            this.lblSoLuongToiThieu.TabIndex = 22;
            this.lblSoLuongToiThieu.Text = "Mức tồn tối thiểu";
            // 
            // txtSoLuongToiThieu
            // 
            this.txtSoLuongToiThieu.Location = new System.Drawing.Point(660, 153);
            this.txtSoLuongToiThieu.Name = "txtSoLuongToiThieu";
            this.txtSoLuongToiThieu.Size = new System.Drawing.Size(100, 23);
            this.txtSoLuongToiThieu.TabIndex = 13;
            this.txtSoLuongToiThieu.Text = "10";
            // 
            // lblHoatChat
            // 
            this.lblHoatChat.AutoSize = true;
            this.lblHoatChat.Location = new System.Drawing.Point(15, 118);
            this.lblHoatChat.Name = "lblHoatChat";
            this.lblHoatChat.Size = new System.Drawing.Size(58, 15);
            this.lblHoatChat.TabIndex = 15;
            this.lblHoatChat.Text = "Hoạt chất";
            // 
            // txtHoatChat
            // 
            this.txtHoatChat.Location = new System.Drawing.Point(100, 115);
            this.txtHoatChat.Name = "txtHoatChat";
            this.txtHoatChat.Size = new System.Drawing.Size(220, 23);
            this.txtHoatChat.TabIndex = 9;
            // 
            // lblMoTa
            // 
            this.lblMoTa.AutoSize = true;
            this.lblMoTa.Location = new System.Drawing.Point(340, 118);
            this.lblMoTa.Name = "lblMoTa";
            this.lblMoTa.Size = new System.Drawing.Size(38, 15);
            this.lblMoTa.TabIndex = 17;
            this.lblMoTa.Text = "Mô tả";
            // 
            // txtMoTa
            // 
            this.txtMoTa.Location = new System.Drawing.Point(400, 115);
            this.txtMoTa.Name = "txtMoTa";
            this.txtMoTa.Size = new System.Drawing.Size(560, 23);
            this.txtMoTa.TabIndex = 10;
            // 
            // panelNut
            // 
            this.panelNut.Controls.Add(this.btnQuanLyDanhMuc);
            this.panelNut.Controls.Add(this.btnQuanLyNCC);
            this.panelNut.Controls.Add(this.btnLamMoi);
            this.panelNut.Controls.Add(this.btnXoa);
            this.panelNut.Controls.Add(this.btnSua);
            this.panelNut.Controls.Add(this.btnThem);
            this.panelNut.Dock = System.Windows.Forms.DockStyle.Top;
            this.panelNut.Location = new System.Drawing.Point(0, 230);
            this.panelNut.Name = "panelNut";
            this.panelNut.Padding = new System.Windows.Forms.Padding(12, 6, 12, 6);
            this.panelNut.Size = new System.Drawing.Size(980, 48);
            this.panelNut.TabIndex = 1;
            // 
            // btnThem
            // 
            this.btnThem.BackColor = System.Drawing.Color.FromArgb(((int)(((byte)(0)))), ((int)(((byte)(122)))), ((int)(((byte)(204)))));
            this.btnThem.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnThem.ForeColor = System.Drawing.Color.White;
            this.btnThem.Location = new System.Drawing.Point(15, 8);
            this.btnThem.Name = "btnThem";
            this.btnThem.Size = new System.Drawing.Size(100, 30);
            this.btnThem.TabIndex = 0;
            this.btnThem.Text = "Thêm";
            this.btnThem.UseVisualStyleBackColor = false;
            this.btnThem.Click += new System.EventHandler(this.btnThem_Click);
            // 
            // btnSua
            // 
            this.btnSua.BackColor = System.Drawing.Color.FromArgb(((int)(((byte)(76)))), ((int)(((byte)(175)))), ((int)(((byte)(80)))));
            this.btnSua.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnSua.ForeColor = System.Drawing.Color.White;
            this.btnSua.Location = new System.Drawing.Point(130, 8);
            this.btnSua.Name = "btnSua";
            this.btnSua.Size = new System.Drawing.Size(100, 30);
            this.btnSua.TabIndex = 1;
            this.btnSua.Text = "Sửa";
            this.btnSua.UseVisualStyleBackColor = false;
            this.btnSua.Click += new System.EventHandler(this.btnSua_Click);
            // 
            // btnXoa
            // 
            this.btnXoa.BackColor = System.Drawing.Color.Firebrick;
            this.btnXoa.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnXoa.ForeColor = System.Drawing.Color.White;
            this.btnXoa.Location = new System.Drawing.Point(245, 8);
            this.btnXoa.Name = "btnXoa";
            this.btnXoa.Size = new System.Drawing.Size(100, 30);
            this.btnXoa.TabIndex = 2;
            this.btnXoa.Text = "Xóa";
            this.btnXoa.UseVisualStyleBackColor = false;
            this.btnXoa.Click += new System.EventHandler(this.btnXoa_Click);
            // 
            // btnQuanLyDanhMuc
            // 
            this.btnQuanLyDanhMuc.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnQuanLyDanhMuc.Location = new System.Drawing.Point(621, 8);
            this.btnQuanLyDanhMuc.Name = "btnQuanLyDanhMuc";
            this.btnQuanLyDanhMuc.Size = new System.Drawing.Size(150, 30);
            this.btnQuanLyDanhMuc.TabIndex = 4;
            this.btnQuanLyDanhMuc.Text = "Quản lý danh mục";
            this.btnQuanLyDanhMuc.UseVisualStyleBackColor = true;
            this.btnQuanLyDanhMuc.Click += new System.EventHandler(this.btnQuanLyDanhMuc_Click);
            // 
            // btnQuanLyNCC
            // 
            this.btnQuanLyNCC.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnQuanLyNCC.Location = new System.Drawing.Point(475, 8);
            this.btnQuanLyNCC.Name = "btnQuanLyNCC";
            this.btnQuanLyNCC.Size = new System.Drawing.Size(140, 30);
            this.btnQuanLyNCC.TabIndex = 3;
            this.btnQuanLyNCC.Text = "Quản lý NCC";
            this.btnQuanLyNCC.UseVisualStyleBackColor = true;
            this.btnQuanLyNCC.Click += new System.EventHandler(this.btnQuanLyNCC_Click);
            // 
            // btnLamMoi
            // 
            this.btnLamMoi.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnLamMoi.Location = new System.Drawing.Point(360, 8);
            this.btnLamMoi.Name = "btnLamMoi";
            this.btnLamMoi.Size = new System.Drawing.Size(100, 30);
            this.btnLamMoi.TabIndex = 2;
            this.btnLamMoi.Text = "Làm mới";
            this.btnLamMoi.UseVisualStyleBackColor = true;
            this.btnLamMoi.Click += new System.EventHandler(this.btnLamMoi_Click);
            // 
            // dgvThuoc
            // 
            this.dgvThuoc.AllowUserToAddRows = false;
            this.dgvThuoc.AllowUserToDeleteRows = false;
            this.dgvThuoc.AutoSizeColumnsMode = System.Windows.Forms.DataGridViewAutoSizeColumnsMode.Fill;
            this.dgvThuoc.BackgroundColor = System.Drawing.Color.White;
            this.dgvThuoc.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
            this.dgvThuoc.Dock = System.Windows.Forms.DockStyle.Fill;
            this.dgvThuoc.Location = new System.Drawing.Point(0, 278);
            this.dgvThuoc.MultiSelect = false;
            this.dgvThuoc.Name = "dgvThuoc";
            this.dgvThuoc.ReadOnly = true;
            this.dgvThuoc.RowHeadersVisible = false;
            this.dgvThuoc.SelectionMode = System.Windows.Forms.DataGridViewSelectionMode.FullRowSelect;
            this.dgvThuoc.Size = new System.Drawing.Size(980, 322);
            this.dgvThuoc.TabIndex = 2;
            this.dgvThuoc.SelectionChanged += new System.EventHandler(this.dgvThuoc_SelectionChanged);
            // 
            // FormQuanLyThuoc
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(7F, 15F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(980, 600);
            this.Controls.Add(this.dgvThuoc);
            this.Controls.Add(this.panelNut);
            this.Controls.Add(this.panelNhapLieu);
            this.Controls.Add(this.panelLoc);
            this.Font = new System.Drawing.Font("Segoe UI", 9F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.Name = "FormQuanLyThuoc";
            this.Text = "Qu\u1EA3n l\u00FD s\u1EA3n ph\u1EA9m (Thu\u1ED1c)";
            this.Load += new System.EventHandler(this.FormQuanLyThuoc_Load);
            this.panelLoc.ResumeLayout(false);
            this.panelLoc.PerformLayout();
            this.panelNhapLieu.ResumeLayout(false);
            this.panelNhapLieu.PerformLayout();
            this.panelNut.ResumeLayout(false);
            ((System.ComponentModel.ISupportInitialize)(this.dgvThuoc)).EndInit();
            this.ResumeLayout(false);

        }

        private System.Windows.Forms.Panel panelLoc;
        private System.Windows.Forms.Button btnBoLoc;
        private System.Windows.Forms.Button btnTimKiem;
        private System.Windows.Forms.ComboBox cboLocNhaCC;
        private System.Windows.Forms.Label lblLocNhaCC;
        private System.Windows.Forms.ComboBox cboLocDanhMuc;
        private System.Windows.Forms.Label lblLocDanhMuc;
        private System.Windows.Forms.TextBox txtTuKhoa;
        private System.Windows.Forms.Label lblTuKhoa;
        private System.Windows.Forms.Panel panelNhapLieu;
        private System.Windows.Forms.Label lblMaThuoc;
        private System.Windows.Forms.Label lblTenThuoc;
        private System.Windows.Forms.TextBox txtTenThuoc;
        private System.Windows.Forms.Label lblGiaBan;
        private System.Windows.Forms.TextBox txtGiaBan;
        private System.Windows.Forms.Label lblHamLuong;
        private System.Windows.Forms.TextBox txtHamLuong;
        private System.Windows.Forms.Label lblXuatXu;
        private System.Windows.Forms.TextBox txtXuatXu;
        private System.Windows.Forms.Label lblDanhMuc;
        private System.Windows.Forms.ComboBox cboDanhMuc;
        private System.Windows.Forms.Label lblNhaCC;
        private System.Windows.Forms.ComboBox cboNhaCC;
        private System.Windows.Forms.Label lblDonViTinh;
        private System.Windows.Forms.ComboBox cboDonViTinh;
        private System.Windows.Forms.Label lblDonViQuyDoi;
        private System.Windows.Forms.ComboBox cboDonViQuyDoi;
        private System.Windows.Forms.Label lblTyLeQuyDoi;
        private System.Windows.Forms.TextBox txtTyLeQuyDoi;
        private System.Windows.Forms.Label lblHoatChat;
        private System.Windows.Forms.TextBox txtHoatChat;
        private System.Windows.Forms.Label lblMoTa;
        private System.Windows.Forms.TextBox txtMoTa;
        private System.Windows.Forms.Panel panelNut;
        private System.Windows.Forms.Button btnQuanLyDanhMuc;
        private System.Windows.Forms.Button btnQuanLyNCC;
        private System.Windows.Forms.Button btnThem;
        private System.Windows.Forms.Button btnSua;
        private System.Windows.Forms.Button btnXoa;
        private System.Windows.Forms.Button btnLamMoi;
        private System.Windows.Forms.Label lblSoLuongToiThieu;
        private System.Windows.Forms.TextBox txtSoLuongToiThieu;
        private System.Windows.Forms.DataGridView dgvThuoc;
    }
}
