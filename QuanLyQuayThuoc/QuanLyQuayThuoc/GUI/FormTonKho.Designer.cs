namespace QuanLyQuayThuoc.GUI
{
    partial class FormTonKho
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
            this.panelTongQuan = new System.Windows.Forms.Panel();
            this.txtSoLoCanhBao = new System.Windows.Forms.TextBox();
            this.lblSoLoCanhBao = new System.Windows.Forms.Label();
            this.txtTongGiaTriTon = new System.Windows.Forms.TextBox();
            this.lblTongGiaTriTon = new System.Windows.Forms.Label();
            this.txtTongMatHang = new System.Windows.Forms.TextBox();
            this.lblTongMatHang = new System.Windows.Forms.Label();
            this.panelLoc = new System.Windows.Forms.Panel();
            this.btnBoLoc = new System.Windows.Forms.Button();
            this.btnTimKiem = new System.Windows.Forms.Button();
            this.cboDanhMuc = new System.Windows.Forms.ComboBox();
            this.lblDanhMuc = new System.Windows.Forms.Label();
            this.cboSoNgayCanhBao = new System.Windows.Forms.ComboBox();
            this.lblSoNgayCanhBao = new System.Windows.Forms.Label();
            this.txtTuKhoa = new System.Windows.Forms.TextBox();
            this.lblTuKhoa = new System.Windows.Forms.Label();
            this.tabTonKho = new System.Windows.Forms.TabControl();
            this.tabTongHop = new System.Windows.Forms.TabPage();
            this.dgvTongHop = new System.Windows.Forms.DataGridView();
            this.tabChiTiet = new System.Windows.Forms.TabPage();
            this.dgvChiTiet = new System.Windows.Forms.DataGridView();
            this.tabCanhBao = new System.Windows.Forms.TabPage();
            this.tabCanhBaoTonKho = new System.Windows.Forms.TabPage();
            this.tlpCanhBaoTonKho = new System.Windows.Forms.TableLayoutPanel();
            this.grpDaHetHang = new System.Windows.Forms.GroupBox();
            this.dgvDaHetHang = new System.Windows.Forms.DataGridView();
            this.grpSapHetHang = new System.Windows.Forms.GroupBox();
            this.dgvSapHetHang = new System.Windows.Forms.DataGridView();
            this.panelCanhBaoLoc = new System.Windows.Forms.Panel();
            this.lblTimKiemCB = new System.Windows.Forms.Label();
            this.txtTimKiemCanhBao = new System.Windows.Forms.TextBox();
            this.lblTrangThaiCB = new System.Windows.Forms.Label();
            this.cboTrangThaiCanhBao = new System.Windows.Forms.ComboBox();
            this.btnLamMoiCanhBao = new System.Windows.Forms.Button();
            this.dgvLichSuCanhBao = new System.Windows.Forms.DataGridView();
            this.panelTongQuan.SuspendLayout();
            this.panelLoc.SuspendLayout();
            this.tabTonKho.SuspendLayout();
            this.tabTongHop.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.dgvTongHop)).BeginInit();
            this.tabChiTiet.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.dgvChiTiet)).BeginInit();
            this.tabCanhBao.SuspendLayout();
            this.panelCanhBaoLoc.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.dgvLichSuCanhBao)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.dgvDaHetHang)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.dgvSapHetHang)).BeginInit();
            this.tabCanhBaoTonKho.SuspendLayout();
            this.tlpCanhBaoTonKho.SuspendLayout();
            this.grpDaHetHang.SuspendLayout();
            this.grpSapHetHang.SuspendLayout();
            this.SuspendLayout();
            // 
            // panelTongQuan
            // 
            this.panelTongQuan.BackColor = System.Drawing.Color.WhiteSmoke;
            this.panelTongQuan.Controls.Add(this.txtSoLoCanhBao);
            this.panelTongQuan.Controls.Add(this.lblSoLoCanhBao);
            this.panelTongQuan.Controls.Add(this.txtTongGiaTriTon);
            this.panelTongQuan.Controls.Add(this.lblTongGiaTriTon);
            this.panelTongQuan.Controls.Add(this.txtTongMatHang);
            this.panelTongQuan.Controls.Add(this.lblTongMatHang);
            this.panelTongQuan.Dock = System.Windows.Forms.DockStyle.Top;
            this.panelTongQuan.Location = new System.Drawing.Point(0, 0);
            this.panelTongQuan.Name = "panelTongQuan";
            this.panelTongQuan.Padding = new System.Windows.Forms.Padding(12, 8, 12, 8);
            this.panelTongQuan.Size = new System.Drawing.Size(980, 52);
            this.panelTongQuan.TabIndex = 0;
            // 
            // txtSoLoCanhBao
            // 
            this.txtSoLoCanhBao.Location = new System.Drawing.Point(811, 14);
            this.txtSoLoCanhBao.Name = "txtSoLoCanhBao";
            this.txtSoLoCanhBao.ReadOnly = true;
            this.txtSoLoCanhBao.Size = new System.Drawing.Size(70, 23);
            this.txtSoLoCanhBao.TabIndex = 5;
            // 
            // lblSoLoCanhBao
            // 
            this.lblSoLoCanhBao.AutoSize = true;
            this.lblSoLoCanhBao.Location = new System.Drawing.Point(702, 17);
            this.lblSoLoCanhBao.Name = "lblSoLoCanhBao";
            this.lblSoLoCanhBao.Size = new System.Drawing.Size(103, 15);
            this.lblSoLoCanhBao.TabIndex = 4;
            this.lblSoLoCanhBao.Text = "Số lô cần cảnh báo";
            // 
            // txtTongGiaTriTon
            // 
            this.txtTongGiaTriTon.Location = new System.Drawing.Point(516, 14);
            this.txtTongGiaTriTon.Name = "txtTongGiaTriTon";
            this.txtTongGiaTriTon.ReadOnly = true;
            this.txtTongGiaTriTon.Size = new System.Drawing.Size(159, 23);
            this.txtTongGiaTriTon.TabIndex = 3;
            // 
            // lblTongGiaTriTon
            // 
            this.lblTongGiaTriTon.AutoSize = true;
            this.lblTongGiaTriTon.Location = new System.Drawing.Point(410, 17);
            this.lblTongGiaTriTon.Name = "lblTongGiaTriTon";
            this.lblTongGiaTriTon.Size = new System.Drawing.Size(100, 15);
            this.lblTongGiaTriTon.TabIndex = 2;
            this.lblTongGiaTriTon.Text = "Tổng giá trị tồn kho";
            // 
            // txtTongMatHang
            // 
            this.txtTongMatHang.Location = new System.Drawing.Point(125, 14);
            this.txtTongMatHang.Name = "txtTongMatHang";
            this.txtTongMatHang.ReadOnly = true;
            this.txtTongMatHang.Size = new System.Drawing.Size(70, 23);
            this.txtTongMatHang.TabIndex = 1;
            // 
            // lblTongMatHang
            // 
            this.lblTongMatHang.AutoSize = true;
            this.lblTongMatHang.Location = new System.Drawing.Point(15, 17);
            this.lblTongMatHang.Name = "lblTongMatHang";
            this.lblTongMatHang.Size = new System.Drawing.Size(104, 15);
            this.lblTongMatHang.TabIndex = 0;
            this.lblTongMatHang.Text = "Tổng mặt hàng tồn";
            // 
            // panelLoc
            // 
            this.panelLoc.Controls.Add(this.btnBoLoc);
            this.panelLoc.Controls.Add(this.btnTimKiem);
            this.panelLoc.Controls.Add(this.cboSoNgayCanhBao);
            this.panelLoc.Controls.Add(this.lblSoNgayCanhBao);
            this.panelLoc.Controls.Add(this.cboDanhMuc);
            this.panelLoc.Controls.Add(this.lblDanhMuc);
            this.panelLoc.Controls.Add(this.txtTuKhoa);
            this.panelLoc.Controls.Add(this.lblTuKhoa);
            this.panelLoc.Dock = System.Windows.Forms.DockStyle.Top;
            this.panelLoc.Location = new System.Drawing.Point(0, 52);
            this.panelLoc.Name = "panelLoc";
            this.panelLoc.Padding = new System.Windows.Forms.Padding(12, 8, 12, 8);
            this.panelLoc.Size = new System.Drawing.Size(980, 58);
            this.panelLoc.TabIndex = 0;
            // 
            // btnBoLoc
            // 
            this.btnBoLoc.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnBoLoc.Location = new System.Drawing.Point(644, 15);
            this.btnBoLoc.Name = "btnBoLoc";
            this.btnBoLoc.Size = new System.Drawing.Size(85, 30);
            this.btnBoLoc.TabIndex = 5;
            this.btnBoLoc.Text = "Bỏ lọc";
            this.btnBoLoc.UseVisualStyleBackColor = true;
            this.btnBoLoc.Click += new System.EventHandler(this.btnBoLoc_Click);
            // 
            // btnTimKiem
            // 
            this.btnTimKiem.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnTimKiem.Location = new System.Drawing.Point(553, 15);
            this.btnTimKiem.Name = "btnTimKiem";
            this.btnTimKiem.Size = new System.Drawing.Size(85, 30);
            this.btnTimKiem.TabIndex = 4;
            this.btnTimKiem.Text = "Tìm kiếm";
            this.btnTimKiem.UseVisualStyleBackColor = true;
            this.btnTimKiem.Click += new System.EventHandler(this.btnTimKiem_Click);
            // 
            // cboSoNgayCanhBao
            // 
            this.cboSoNgayCanhBao.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.cboSoNgayCanhBao.FormattingEnabled = true;
            this.cboSoNgayCanhBao.Location = new System.Drawing.Point(844, 19);
            this.cboSoNgayCanhBao.Name = "cboSoNgayCanhBao";
            this.cboSoNgayCanhBao.Size = new System.Drawing.Size(121, 23);
            this.cboSoNgayCanhBao.TabIndex = 7;
            // 
            // lblSoNgayCanhBao
            // 
            this.lblSoNgayCanhBao.AutoSize = true;
            this.lblSoNgayCanhBao.Location = new System.Drawing.Point(735, 22);
            this.lblSoNgayCanhBao.Name = "lblSoNgayCanhBao";
            this.lblSoNgayCanhBao.Size = new System.Drawing.Size(103, 15);
            this.lblSoNgayCanhBao.TabIndex = 6;
            this.lblSoNgayCanhBao.Text = "Cảnh báo dưới ngày";
            // 
            // cboDanhMuc
            // 
            this.cboDanhMuc.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.cboDanhMuc.FormattingEnabled = true;
            this.cboDanhMuc.Location = new System.Drawing.Point(367, 19);
            this.cboDanhMuc.Name = "cboDanhMuc";
            this.cboDanhMuc.Size = new System.Drawing.Size(170, 23);
            this.cboDanhMuc.TabIndex = 3;
            // 
            // lblDanhMuc
            // 
            this.lblDanhMuc.AutoSize = true;
            this.lblDanhMuc.Location = new System.Drawing.Point(299, 22);
            this.lblDanhMuc.Name = "lblDanhMuc";
            this.lblDanhMuc.Size = new System.Drawing.Size(62, 15);
            this.lblDanhMuc.TabIndex = 2;
            this.lblDanhMuc.Text = "Danh mục";
            // 
            // txtTuKhoa
            // 
            this.txtTuKhoa.Location = new System.Drawing.Point(74, 19);
            this.txtTuKhoa.Name = "txtTuKhoa";
            this.txtTuKhoa.Size = new System.Drawing.Size(208, 23);
            this.txtTuKhoa.TabIndex = 1;
            // 
            // lblTuKhoa
            // 
            this.lblTuKhoa.AutoSize = true;
            this.lblTuKhoa.Location = new System.Drawing.Point(15, 22);
            this.lblTuKhoa.Name = "lblTuKhoa";
            this.lblTuKhoa.Size = new System.Drawing.Size(53, 15);
            this.lblTuKhoa.TabIndex = 0;
            this.lblTuKhoa.Text = "Từ khóa";
            // 
            // tabTonKho
            // 
            this.tabTonKho.Controls.Add(this.tabTongHop);
            this.tabTonKho.Controls.Add(this.tabChiTiet);
            this.tabTonKho.Controls.Add(this.tabCanhBao);
            this.tabTonKho.Controls.Add(this.tabCanhBaoTonKho);
            this.tabTonKho.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tabTonKho.Location = new System.Drawing.Point(0, 110);
            this.tabTonKho.Name = "tabTonKho";
            this.tabTonKho.SelectedIndex = 0;
            this.tabTonKho.Size = new System.Drawing.Size(980, 542);
            this.tabTonKho.TabIndex = 1;
            // 
            // tabTongHop
            // 
            this.tabTongHop.Controls.Add(this.dgvTongHop);
            this.tabTongHop.Location = new System.Drawing.Point(4, 24);
            this.tabTongHop.Name = "tabTongHop";
            this.tabTongHop.Padding = new System.Windows.Forms.Padding(3);
            this.tabTongHop.Size = new System.Drawing.Size(972, 514);
            this.tabTongHop.TabIndex = 0;
            this.tabTongHop.Text = "Tồn kho tổng hợp";
            this.tabTongHop.UseVisualStyleBackColor = true;
            // 
            // dgvTongHop
            // 
            this.dgvTongHop.AllowUserToAddRows = false;
            this.dgvTongHop.AllowUserToDeleteRows = false;
            this.dgvTongHop.AutoSizeColumnsMode = System.Windows.Forms.DataGridViewAutoSizeColumnsMode.Fill;
            this.dgvTongHop.BackgroundColor = System.Drawing.Color.White;
            this.dgvTongHop.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
            this.dgvTongHop.Dock = System.Windows.Forms.DockStyle.Fill;
            this.dgvTongHop.Location = new System.Drawing.Point(3, 3);
            this.dgvTongHop.MultiSelect = false;
            this.dgvTongHop.Name = "dgvTongHop";
            this.dgvTongHop.ReadOnly = true;
            this.dgvTongHop.RowHeadersVisible = false;
            this.dgvTongHop.SelectionMode = System.Windows.Forms.DataGridViewSelectionMode.FullRowSelect;
            this.dgvTongHop.Size = new System.Drawing.Size(966, 508);
            this.dgvTongHop.TabIndex = 0;
            this.dgvTongHop.CellDoubleClick += new System.Windows.Forms.DataGridViewCellEventHandler(this.dgvTongHop_CellDoubleClick);
            // 
            // tabChiTiet
            // 
            this.tabChiTiet.Controls.Add(this.dgvChiTiet);
            this.tabChiTiet.Location = new System.Drawing.Point(4, 24);
            this.tabChiTiet.Name = "tabChiTiet";
            this.tabChiTiet.Padding = new System.Windows.Forms.Padding(3);
            this.tabChiTiet.Size = new System.Drawing.Size(972, 514);
            this.tabChiTiet.TabIndex = 1;
            this.tabChiTiet.Text = "Tồn kho theo lô";
            this.tabChiTiet.UseVisualStyleBackColor = true;
            // 
            // dgvChiTiet
            // 
            this.dgvChiTiet.AllowUserToAddRows = false;
            this.dgvChiTiet.AllowUserToDeleteRows = false;
            this.dgvChiTiet.AutoSizeColumnsMode = System.Windows.Forms.DataGridViewAutoSizeColumnsMode.Fill;
            this.dgvChiTiet.BackgroundColor = System.Drawing.Color.White;
            this.dgvChiTiet.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
            this.dgvChiTiet.Dock = System.Windows.Forms.DockStyle.Fill;
            this.dgvChiTiet.Location = new System.Drawing.Point(3, 3);
            this.dgvChiTiet.MultiSelect = false;
            this.dgvChiTiet.Name = "dgvChiTiet";
            this.dgvChiTiet.ReadOnly = true;
            this.dgvChiTiet.RowHeadersVisible = false;
            this.dgvChiTiet.SelectionMode = System.Windows.Forms.DataGridViewSelectionMode.FullRowSelect;
            this.dgvChiTiet.Size = new System.Drawing.Size(966, 508);
            this.dgvChiTiet.TabIndex = 0;
            // 
            // tabCanhBao
            // 
            this.tabCanhBao.Controls.Add(this.dgvLichSuCanhBao);
            this.tabCanhBao.Controls.Add(this.panelCanhBaoLoc);
            this.tabCanhBao.Location = new System.Drawing.Point(4, 24);
            this.tabCanhBao.Name = "tabCanhBao";
            this.tabCanhBao.Padding = new System.Windows.Forms.Padding(3);
            this.tabCanhBao.Size = new System.Drawing.Size(972, 514);
            this.tabCanhBao.TabIndex = 2;
            this.tabCanhBao.Text = "Cảnh báo hạn dùng";
            this.tabCanhBao.UseVisualStyleBackColor = true;
            // 
            // panelCanhBaoLoc
            // 
            this.panelCanhBaoLoc.Controls.Add(this.lblTimKiemCB);
            this.panelCanhBaoLoc.Controls.Add(this.txtTimKiemCanhBao);
            this.panelCanhBaoLoc.Controls.Add(this.lblTrangThaiCB);
            this.panelCanhBaoLoc.Controls.Add(this.cboTrangThaiCanhBao);
            this.panelCanhBaoLoc.Controls.Add(this.btnLamMoiCanhBao);
            this.panelCanhBaoLoc.Dock = System.Windows.Forms.DockStyle.Top;
            this.panelCanhBaoLoc.Location = new System.Drawing.Point(3, 3);
            this.panelCanhBaoLoc.Name = "panelCanhBaoLoc";
            this.panelCanhBaoLoc.Padding = new System.Windows.Forms.Padding(6, 6, 6, 6);
            this.panelCanhBaoLoc.Size = new System.Drawing.Size(966, 40);
            this.panelCanhBaoLoc.TabIndex = 1;
            // 
            // lblTimKiemCB
            // 
            this.lblTimKiemCB.AutoSize = true;
            this.lblTimKiemCB.Location = new System.Drawing.Point(6, 12);
            this.lblTimKiemCB.Name = "lblTimKiemCB";
            this.lblTimKiemCB.Size = new System.Drawing.Size(53, 15);
            this.lblTimKiemCB.TabIndex = 0;
            this.lblTimKiemCB.Text = "Tìm kiếm";
            // 
            // txtTimKiemCanhBao
            // 
            this.txtTimKiemCanhBao.Location = new System.Drawing.Point(65, 8);
            this.txtTimKiemCanhBao.Name = "txtTimKiemCanhBao";
            this.txtTimKiemCanhBao.Size = new System.Drawing.Size(200, 23);
            this.txtTimKiemCanhBao.TabIndex = 1;
            this.txtTimKiemCanhBao.TextChanged += new System.EventHandler(this.txtTimKiemCanhBao_TextChanged);
            // 
            // lblTrangThaiCB
            // 
            this.lblTrangThaiCB.AutoSize = true;
            this.lblTrangThaiCB.Location = new System.Drawing.Point(280, 12);
            this.lblTrangThaiCB.Name = "lblTrangThaiCB";
            this.lblTrangThaiCB.Size = new System.Drawing.Size(62, 15);
            this.lblTrangThaiCB.TabIndex = 2;
            this.lblTrangThaiCB.Text = "Trạng thái";
            // 
            // cboTrangThaiCanhBao
            // 
            this.cboTrangThaiCanhBao.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.cboTrangThaiCanhBao.FormattingEnabled = true;
            this.cboTrangThaiCanhBao.Location = new System.Drawing.Point(348, 8);
            this.cboTrangThaiCanhBao.Name = "cboTrangThaiCanhBao";
            this.cboTrangThaiCanhBao.Size = new System.Drawing.Size(160, 23);
            this.cboTrangThaiCanhBao.TabIndex = 3;
            this.cboTrangThaiCanhBao.SelectedIndexChanged += new System.EventHandler(this.cboTrangThaiCanhBao_SelectedIndexChanged);
            // 
            // btnLamMoiCanhBao
            // 
            this.btnLamMoiCanhBao.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnLamMoiCanhBao.Location = new System.Drawing.Point(524, 7);
            this.btnLamMoiCanhBao.Name = "btnLamMoiCanhBao";
            this.btnLamMoiCanhBao.Size = new System.Drawing.Size(90, 26);
            this.btnLamMoiCanhBao.TabIndex = 4;
            this.btnLamMoiCanhBao.Text = "🔄 Làm mới";
            this.btnLamMoiCanhBao.UseVisualStyleBackColor = true;
            this.btnLamMoiCanhBao.Click += new System.EventHandler(this.btnLamMoiCanhBao_Click);
            // 
            // dgvLichSuCanhBao
            // 
            this.dgvLichSuCanhBao.AllowUserToAddRows = false;
            this.dgvLichSuCanhBao.AllowUserToDeleteRows = false;
            this.dgvLichSuCanhBao.AutoSizeColumnsMode = System.Windows.Forms.DataGridViewAutoSizeColumnsMode.Fill;
            this.dgvLichSuCanhBao.BackgroundColor = System.Drawing.Color.White;
            this.dgvLichSuCanhBao.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
            this.dgvLichSuCanhBao.Dock = System.Windows.Forms.DockStyle.Fill;
            this.dgvLichSuCanhBao.Location = new System.Drawing.Point(3, 43);
            this.dgvLichSuCanhBao.MultiSelect = false;
            this.dgvLichSuCanhBao.Name = "dgvLichSuCanhBao";
            this.dgvLichSuCanhBao.ReadOnly = true;
            this.dgvLichSuCanhBao.RowHeadersVisible = false;
            this.dgvLichSuCanhBao.SelectionMode = System.Windows.Forms.DataGridViewSelectionMode.FullRowSelect;
            this.dgvLichSuCanhBao.Size = new System.Drawing.Size(966, 468);
            this.dgvLichSuCanhBao.TabIndex = 2;
            // 
            // tabCanhBaoTonKho
            // 
            this.tabCanhBaoTonKho.Controls.Add(this.tlpCanhBaoTonKho);
            this.tabCanhBaoTonKho.Location = new System.Drawing.Point(4, 24);
            this.tabCanhBaoTonKho.Name = "tabCanhBaoTonKho";
            this.tabCanhBaoTonKho.Padding = new System.Windows.Forms.Padding(6);
            this.tabCanhBaoTonKho.Size = new System.Drawing.Size(972, 514);
            this.tabCanhBaoTonKho.TabIndex = 3;
            this.tabCanhBaoTonKho.Text = "Cảnh báo tồn kho";
            this.tabCanhBaoTonKho.UseVisualStyleBackColor = true;
            // 
            // tlpCanhBaoTonKho
            // 
            this.tlpCanhBaoTonKho.ColumnCount = 1;
            this.tlpCanhBaoTonKho.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tlpCanhBaoTonKho.Controls.Add(this.grpDaHetHang, 0, 0);
            this.tlpCanhBaoTonKho.Controls.Add(this.grpSapHetHang, 0, 1);
            this.tlpCanhBaoTonKho.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tlpCanhBaoTonKho.Location = new System.Drawing.Point(6, 6);
            this.tlpCanhBaoTonKho.Name = "tlpCanhBaoTonKho";
            this.tlpCanhBaoTonKho.RowCount = 2;
            this.tlpCanhBaoTonKho.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Percent, 50F));
            this.tlpCanhBaoTonKho.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Percent, 50F));
            this.tlpCanhBaoTonKho.Size = new System.Drawing.Size(960, 502);
            this.tlpCanhBaoTonKho.TabIndex = 0;
            // 
            // grpDaHetHang
            // 
            this.grpDaHetHang.Controls.Add(this.dgvDaHetHang);
            this.grpDaHetHang.Dock = System.Windows.Forms.DockStyle.Fill;
            this.grpDaHetHang.ForeColor = System.Drawing.Color.Firebrick;
            this.grpDaHetHang.Location = new System.Drawing.Point(3, 3);
            this.grpDaHetHang.Name = "grpDaHetHang";
            this.grpDaHetHang.Padding = new System.Windows.Forms.Padding(6, 8, 6, 8);
            this.grpDaHetHang.Size = new System.Drawing.Size(954, 245);
            this.grpDaHetHang.TabIndex = 0;
            this.grpDaHetHang.TabStop = false;
            this.grpDaHetHang.Text = "🚫 ĐÃ HẾT SẠCH HÀNG (TỒN = 0)";
            // 
            // dgvDaHetHang
            // 
            this.dgvDaHetHang.AllowUserToAddRows = false;
            this.dgvDaHetHang.AllowUserToDeleteRows = false;
            this.dgvDaHetHang.AutoSizeColumnsMode = System.Windows.Forms.DataGridViewAutoSizeColumnsMode.Fill;
            this.dgvDaHetHang.BackgroundColor = System.Drawing.Color.White;
            this.dgvDaHetHang.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
            this.dgvDaHetHang.Dock = System.Windows.Forms.DockStyle.Fill;
            this.dgvDaHetHang.Location = new System.Drawing.Point(6, 24);
            this.dgvDaHetHang.MultiSelect = false;
            this.dgvDaHetHang.Name = "dgvDaHetHang";
            this.dgvDaHetHang.ReadOnly = true;
            this.dgvDaHetHang.RowHeadersVisible = false;
            this.dgvDaHetHang.SelectionMode = System.Windows.Forms.DataGridViewSelectionMode.FullRowSelect;
            this.dgvDaHetHang.Size = new System.Drawing.Size(942, 213);
            this.dgvDaHetHang.TabIndex = 0;
            // 
            // grpSapHetHang
            // 
            this.grpSapHetHang.Controls.Add(this.dgvSapHetHang);
            this.grpSapHetHang.Dock = System.Windows.Forms.DockStyle.Fill;
            this.grpSapHetHang.ForeColor = System.Drawing.Color.DarkOrange;
            this.grpSapHetHang.Location = new System.Drawing.Point(3, 254);
            this.grpSapHetHang.Name = "grpSapHetHang";
            this.grpSapHetHang.Padding = new System.Windows.Forms.Padding(6, 8, 6, 8);
            this.grpSapHetHang.Size = new System.Drawing.Size(954, 245);
            this.grpSapHetHang.TabIndex = 1;
            this.grpSapHetHang.TabStop = false;
            this.grpSapHetHang.Text = "⚠️ SẮP HẾT HÀNG (TỒN ≤ NGƯỠNG TỐI THIỂU)";
            // 
            // dgvSapHetHang
            // 
            this.dgvSapHetHang.AllowUserToAddRows = false;
            this.dgvSapHetHang.AllowUserToDeleteRows = false;
            this.dgvSapHetHang.AutoSizeColumnsMode = System.Windows.Forms.DataGridViewAutoSizeColumnsMode.Fill;
            this.dgvSapHetHang.BackgroundColor = System.Drawing.Color.White;
            this.dgvSapHetHang.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
            this.dgvSapHetHang.Dock = System.Windows.Forms.DockStyle.Fill;
            this.dgvSapHetHang.Location = new System.Drawing.Point(6, 24);
            this.dgvSapHetHang.MultiSelect = false;
            this.dgvSapHetHang.Name = "dgvSapHetHang";
            this.dgvSapHetHang.ReadOnly = true;
            this.dgvSapHetHang.RowHeadersVisible = false;
            this.dgvSapHetHang.SelectionMode = System.Windows.Forms.DataGridViewSelectionMode.FullRowSelect;
            this.dgvSapHetHang.Size = new System.Drawing.Size(942, 213);
            this.dgvSapHetHang.TabIndex = 0;
            // 
            // FormTonKho
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(7F, 15F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(980, 600);
            this.Controls.Add(this.tabTonKho);
            this.Controls.Add(this.panelLoc);
            this.Controls.Add(this.panelTongQuan);
            this.Font = new System.Drawing.Font("Segoe UI", 9F);
            this.Name = "FormTonKho";
            this.Text = "Xem tồn kho";
            this.Load += new System.EventHandler(this.FormTonKho_Load);
            this.panelTongQuan.ResumeLayout(false);
            this.panelTongQuan.PerformLayout();
            this.panelLoc.ResumeLayout(false);
            this.panelLoc.PerformLayout();
            this.tabTonKho.ResumeLayout(false);
            this.tabTongHop.ResumeLayout(false);
            ((System.ComponentModel.ISupportInitialize)(this.dgvTongHop)).EndInit();
            this.tabChiTiet.ResumeLayout(false);
            ((System.ComponentModel.ISupportInitialize)(this.dgvChiTiet)).EndInit();
            this.tabCanhBao.ResumeLayout(false);
            this.panelCanhBaoLoc.ResumeLayout(false);
            this.panelCanhBaoLoc.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.dgvLichSuCanhBao)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.dgvDaHetHang)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.dgvSapHetHang)).EndInit();
            this.tabCanhBaoTonKho.ResumeLayout(false);
            this.tlpCanhBaoTonKho.ResumeLayout(false);
            this.grpDaHetHang.ResumeLayout(false);
            this.grpSapHetHang.ResumeLayout(false);
            this.ResumeLayout(false);
        }

        private System.Windows.Forms.Panel panelTongQuan;
        private System.Windows.Forms.TextBox txtSoLoCanhBao;
        private System.Windows.Forms.Label lblSoLoCanhBao;
        private System.Windows.Forms.TextBox txtTongGiaTriTon;
        private System.Windows.Forms.Label lblTongGiaTriTon;
        private System.Windows.Forms.TextBox txtTongMatHang;
        private System.Windows.Forms.Label lblTongMatHang;
        private System.Windows.Forms.Panel panelLoc;
        private System.Windows.Forms.Button btnBoLoc;
        private System.Windows.Forms.Button btnTimKiem;
        private System.Windows.Forms.ComboBox cboSoNgayCanhBao;
        private System.Windows.Forms.Label lblSoNgayCanhBao;
        private System.Windows.Forms.ComboBox cboDanhMuc;
        private System.Windows.Forms.Label lblDanhMuc;
        private System.Windows.Forms.TextBox txtTuKhoa;
        private System.Windows.Forms.Label lblTuKhoa;
        private System.Windows.Forms.TabControl tabTonKho;
        private System.Windows.Forms.TabPage tabTongHop;
        private System.Windows.Forms.DataGridView dgvTongHop;
        private System.Windows.Forms.TabPage tabChiTiet;
        private System.Windows.Forms.DataGridView dgvChiTiet;
        private System.Windows.Forms.TabPage tabCanhBao;
        private System.Windows.Forms.Panel panelCanhBaoLoc;
        private System.Windows.Forms.Label lblTimKiemCB;
        private System.Windows.Forms.TextBox txtTimKiemCanhBao;
        private System.Windows.Forms.Label lblTrangThaiCB;
        private System.Windows.Forms.ComboBox cboTrangThaiCanhBao;
        private System.Windows.Forms.Button btnLamMoiCanhBao;
        private System.Windows.Forms.DataGridView dgvLichSuCanhBao;
        private System.Windows.Forms.TabPage tabCanhBaoTonKho;
        private System.Windows.Forms.TableLayoutPanel tlpCanhBaoTonKho;
        private System.Windows.Forms.GroupBox grpDaHetHang;
        private System.Windows.Forms.DataGridView dgvDaHetHang;
        private System.Windows.Forms.GroupBox grpSapHetHang;
        private System.Windows.Forms.DataGridView dgvSapHetHang;
    }
}
