namespace QuanLyQuayThuoc.GUI
{
    partial class FormKhachHang
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
            this.btnTimKiem = new System.Windows.Forms.Button();
            this.txtTuKhoa = new System.Windows.Forms.TextBox();
            this.lblTuKhoa = new System.Windows.Forms.Label();
            this.panelNhapLieu = new System.Windows.Forms.Panel();
            this.txtDiaChi = new System.Windows.Forms.TextBox();
            this.lblDiaChi = new System.Windows.Forms.Label();
            this.txtSoDienThoai = new System.Windows.Forms.TextBox();
            this.lblSoDienThoai = new System.Windows.Forms.Label();
            this.txtTenKH = new System.Windows.Forms.TextBox();
            this.lblTenKH = new System.Windows.Forms.Label();
            this.lblMaKH = new System.Windows.Forms.Label();
            this.panelNut = new System.Windows.Forms.Panel();
            this.btnLamMoi = new System.Windows.Forms.Button();
            this.btnXoa = new System.Windows.Forms.Button();
            this.btnSua = new System.Windows.Forms.Button();
            this.btnThem = new System.Windows.Forms.Button();
            this.dgvKhachHang = new System.Windows.Forms.DataGridView();
            this.lblThongKeMuaHang = new System.Windows.Forms.Label();
            this.dgvThongKeMuaHang = new System.Windows.Forms.DataGridView();
            this.lblLichSuMuaHang = new System.Windows.Forms.Label();
            this.dgvLichSuMuaHang = new System.Windows.Forms.DataGridView();
            this.panelLoc.SuspendLayout();
            this.panelNhapLieu.SuspendLayout();
            this.panelNut.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.dgvKhachHang)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.dgvThongKeMuaHang)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.dgvLichSuMuaHang)).BeginInit();
            this.SuspendLayout();
            // 
            // panelLoc
            // 
            this.panelLoc.Controls.Add(this.btnTimKiem);
            this.panelLoc.Controls.Add(this.txtTuKhoa);
            this.panelLoc.Controls.Add(this.lblTuKhoa);
            this.panelLoc.Dock = System.Windows.Forms.DockStyle.Top;
            this.panelLoc.Location = new System.Drawing.Point(0, 0);
            this.panelLoc.Name = "panelLoc";
            this.panelLoc.Padding = new System.Windows.Forms.Padding(12, 8, 12, 8);
            this.panelLoc.Size = new System.Drawing.Size(900, 58);
            this.panelLoc.TabIndex = 0;
            // 
            // btnTimKiem
            // 
            this.btnTimKiem.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnTimKiem.Location = new System.Drawing.Point(356, 15);
            this.btnTimKiem.Name = "btnTimKiem";
            this.btnTimKiem.Size = new System.Drawing.Size(90, 30);
            this.btnTimKiem.TabIndex = 2;
            this.btnTimKiem.Text = "Tìm kiếm";
            this.btnTimKiem.UseVisualStyleBackColor = true;
            this.btnTimKiem.Click += new System.EventHandler(this.btnTimKiem_Click);
            // 
            // txtTuKhoa
            // 
            this.txtTuKhoa.Location = new System.Drawing.Point(82, 19);
            this.txtTuKhoa.Name = "txtTuKhoa";
            this.txtTuKhoa.Size = new System.Drawing.Size(256, 23);
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
            // panelNhapLieu
            // 
            this.panelNhapLieu.BackColor = System.Drawing.Color.WhiteSmoke;
            this.panelNhapLieu.Controls.Add(this.txtDiaChi);
            this.panelNhapLieu.Controls.Add(this.lblDiaChi);
            this.panelNhapLieu.Controls.Add(this.txtSoDienThoai);
            this.panelNhapLieu.Controls.Add(this.lblSoDienThoai);
            this.panelNhapLieu.Controls.Add(this.txtTenKH);
            this.panelNhapLieu.Controls.Add(this.lblTenKH);
            this.panelNhapLieu.Controls.Add(this.lblMaKH);
            this.panelNhapLieu.Dock = System.Windows.Forms.DockStyle.Top;
            this.panelNhapLieu.Location = new System.Drawing.Point(0, 58);
            this.panelNhapLieu.Name = "panelNhapLieu";
            this.panelNhapLieu.Padding = new System.Windows.Forms.Padding(12, 8, 12, 8);
            this.panelNhapLieu.Size = new System.Drawing.Size(900, 108);
            this.panelNhapLieu.TabIndex = 1;
            // 
            // txtDiaChi
            // 
            this.txtDiaChi.Location = new System.Drawing.Point(570, 47);
            this.txtDiaChi.Name = "txtDiaChi";
            this.txtDiaChi.Size = new System.Drawing.Size(295, 23);
            this.txtDiaChi.TabIndex = 3;
            // 
            // lblDiaChi
            // 
            this.lblDiaChi.AutoSize = true;
            this.lblDiaChi.Location = new System.Drawing.Point(514, 50);
            this.lblDiaChi.Name = "lblDiaChi";
            this.lblDiaChi.Size = new System.Drawing.Size(42, 15);
            this.lblDiaChi.TabIndex = 5;
            this.lblDiaChi.Text = "Địa chỉ";
            // 
            // txtSoDienThoai
            // 
            this.txtSoDienThoai.Location = new System.Drawing.Point(358, 47);
            this.txtSoDienThoai.Name = "txtSoDienThoai";
            this.txtSoDienThoai.Size = new System.Drawing.Size(135, 23);
            this.txtSoDienThoai.TabIndex = 2;
            // 
            // lblSoDienThoai
            // 
            this.lblSoDienThoai.AutoSize = true;
            this.lblSoDienThoai.Location = new System.Drawing.Point(264, 50);
            this.lblSoDienThoai.Name = "lblSoDienThoai";
            this.lblSoDienThoai.Size = new System.Drawing.Size(79, 15);
            this.lblSoDienThoai.TabIndex = 3;
            this.lblSoDienThoai.Text = "Số điện thoại";
            // 
            // txtTenKH
            // 
            this.txtTenKH.Location = new System.Drawing.Point(83, 47);
            this.txtTenKH.Name = "txtTenKH";
            this.txtTenKH.Size = new System.Drawing.Size(164, 23);
            this.txtTenKH.TabIndex = 1;
            // 
            // lblTenKH
            // 
            this.lblTenKH.AutoSize = true;
            this.lblTenKH.Location = new System.Drawing.Point(15, 50);
            this.lblTenKH.Name = "lblTenKH";
            this.lblTenKH.Size = new System.Drawing.Size(59, 15);
            this.lblTenKH.TabIndex = 1;
            this.lblTenKH.Text = "Tên KH";
            // 
            // lblMaKH
            // 
            this.lblMaKH.AutoSize = true;
            this.lblMaKH.Font = new System.Drawing.Font("Segoe UI", 9F, System.Drawing.FontStyle.Bold);
            this.lblMaKH.Location = new System.Drawing.Point(15, 15);
            this.lblMaKH.Name = "lblMaKH";
            this.lblMaKH.Size = new System.Drawing.Size(160, 15);
            this.lblMaKH.TabIndex = 0;
            this.lblMaKH.Text = "Mã KH đang chọn: (chưa chọn)";
            // 
            // panelNut
            // 
            this.panelNut.Controls.Add(this.btnLamMoi);
            this.panelNut.Controls.Add(this.btnXoa);
            this.panelNut.Controls.Add(this.btnSua);
            this.panelNut.Controls.Add(this.btnThem);
            this.panelNut.Dock = System.Windows.Forms.DockStyle.Top;
            this.panelNut.Location = new System.Drawing.Point(0, 166);
            this.panelNut.Name = "panelNut";
            this.panelNut.Padding = new System.Windows.Forms.Padding(12, 6, 12, 6);
            this.panelNut.Size = new System.Drawing.Size(900, 48);
            this.panelNut.TabIndex = 2;
            // 
            // btnLamMoi
            // 
            this.btnLamMoi.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnLamMoi.Location = new System.Drawing.Point(360, 8);
            this.btnLamMoi.Name = "btnLamMoi";
            this.btnLamMoi.Size = new System.Drawing.Size(100, 30);
            this.btnLamMoi.TabIndex = 3;
            this.btnLamMoi.Text = "Làm mới";
            this.btnLamMoi.UseVisualStyleBackColor = true;
            this.btnLamMoi.Click += new System.EventHandler(this.btnLamMoi_Click);
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
            // dgvKhachHang
            // 
            this.dgvKhachHang.AllowUserToAddRows = false;
            this.dgvKhachHang.AllowUserToDeleteRows = false;
            this.dgvKhachHang.AutoSizeColumnsMode = System.Windows.Forms.DataGridViewAutoSizeColumnsMode.Fill;
            this.dgvKhachHang.BackgroundColor = System.Drawing.Color.White;
            this.dgvKhachHang.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
            this.dgvKhachHang.Dock = System.Windows.Forms.DockStyle.Fill;
            this.dgvKhachHang.Location = new System.Drawing.Point(0, 214);
            this.dgvKhachHang.MultiSelect = false;
            this.dgvKhachHang.Name = "dgvKhachHang";
            this.dgvKhachHang.ReadOnly = true;
            this.dgvKhachHang.RowHeadersVisible = false;
            this.dgvKhachHang.SelectionMode = System.Windows.Forms.DataGridViewSelectionMode.FullRowSelect;
            this.dgvKhachHang.Size = new System.Drawing.Size(900, 180);
            this.dgvKhachHang.TabIndex = 3;
            this.dgvKhachHang.SelectionChanged += new System.EventHandler(this.dgvKhachHang_SelectionChanged);
            // 
            // lblThongKeMuaHang
            // 
            this.lblThongKeMuaHang.Dock = System.Windows.Forms.DockStyle.Bottom;
            this.lblThongKeMuaHang.Font = new System.Drawing.Font("Segoe UI", 9F, System.Drawing.FontStyle.Bold);
            this.lblThongKeMuaHang.Location = new System.Drawing.Point(0, 394);
            this.lblThongKeMuaHang.Name = "lblThongKeMuaHang";
            this.lblThongKeMuaHang.Padding = new System.Windows.Forms.Padding(8, 6, 0, 0);
            this.lblThongKeMuaHang.Size = new System.Drawing.Size(900, 25);
            this.lblThongKeMuaHang.TabIndex = 4;
            this.lblThongKeMuaHang.Text = "Thống kê số lần mua";
            // 
            // dgvThongKeMuaHang
            // 
            this.dgvThongKeMuaHang.AllowUserToAddRows = false;
            this.dgvThongKeMuaHang.AllowUserToDeleteRows = false;
            this.dgvThongKeMuaHang.AutoSizeColumnsMode = System.Windows.Forms.DataGridViewAutoSizeColumnsMode.Fill;
            this.dgvThongKeMuaHang.BackgroundColor = System.Drawing.Color.White;
            this.dgvThongKeMuaHang.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
            this.dgvThongKeMuaHang.Dock = System.Windows.Forms.DockStyle.Bottom;
            this.dgvThongKeMuaHang.Location = new System.Drawing.Point(0, 419);
            this.dgvThongKeMuaHang.MultiSelect = false;
            this.dgvThongKeMuaHang.Name = "dgvThongKeMuaHang";
            this.dgvThongKeMuaHang.ReadOnly = true;
            this.dgvThongKeMuaHang.RowHeadersVisible = false;
            this.dgvThongKeMuaHang.SelectionMode = System.Windows.Forms.DataGridViewSelectionMode.FullRowSelect;
            this.dgvThongKeMuaHang.Size = new System.Drawing.Size(900, 101);
            this.dgvThongKeMuaHang.TabIndex = 5;
            // 
            // lblLichSuMuaHang
            // 
            this.lblLichSuMuaHang.Dock = System.Windows.Forms.DockStyle.Bottom;
            this.lblLichSuMuaHang.Font = new System.Drawing.Font("Segoe UI", 9F, System.Drawing.FontStyle.Bold);
            this.lblLichSuMuaHang.Location = new System.Drawing.Point(0, 369);
            this.lblLichSuMuaHang.Name = "lblLichSuMuaHang";
            this.lblLichSuMuaHang.Padding = new System.Windows.Forms.Padding(8, 6, 0, 0);
            this.lblLichSuMuaHang.Size = new System.Drawing.Size(900, 25);
            this.lblLichSuMuaHang.TabIndex = 6;
            this.lblLichSuMuaHang.Text = "Lịch sử mua hàng: (chưa chọn khách)";
            // 
            // dgvLichSuMuaHang
            // 
            this.dgvLichSuMuaHang.AllowUserToAddRows = false;
            this.dgvLichSuMuaHang.AllowUserToDeleteRows = false;
            this.dgvLichSuMuaHang.AutoSizeColumnsMode = System.Windows.Forms.DataGridViewAutoSizeColumnsMode.Fill;
            this.dgvLichSuMuaHang.BackgroundColor = System.Drawing.Color.White;
            this.dgvLichSuMuaHang.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
            this.dgvLichSuMuaHang.Dock = System.Windows.Forms.DockStyle.Bottom;
            this.dgvLichSuMuaHang.Location = new System.Drawing.Point(0, 214);
            this.dgvLichSuMuaHang.MultiSelect = false;
            this.dgvLichSuMuaHang.Name = "dgvLichSuMuaHang";
            this.dgvLichSuMuaHang.ReadOnly = true;
            this.dgvLichSuMuaHang.RowHeadersVisible = false;
            this.dgvLichSuMuaHang.SelectionMode = System.Windows.Forms.DataGridViewSelectionMode.FullRowSelect;
            this.dgvLichSuMuaHang.Size = new System.Drawing.Size(900, 155);
            this.dgvLichSuMuaHang.TabIndex = 7;
            // 
            // FormKhachHang
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(7F, 15F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(900, 520);
            this.Controls.Add(this.lblThongKeMuaHang);
            this.Controls.Add(this.dgvThongKeMuaHang);
            this.Controls.Add(this.lblLichSuMuaHang);
            this.Controls.Add(this.dgvLichSuMuaHang);
            this.Controls.Add(this.dgvKhachHang);
            this.Controls.Add(this.panelNut);
            this.Controls.Add(this.panelNhapLieu);
            this.Controls.Add(this.panelLoc);
            this.Font = new System.Drawing.Font("Segoe UI", 9F);
            this.Name = "FormKhachHang";
            this.Text = "Quản lý khách hàng";
            this.Load += new System.EventHandler(this.FormKhachHang_Load);
            this.panelLoc.ResumeLayout(false);
            this.panelLoc.PerformLayout();
            this.panelNhapLieu.ResumeLayout(false);
            this.panelNhapLieu.PerformLayout();
            this.panelNut.ResumeLayout(false);
            ((System.ComponentModel.ISupportInitialize)(this.dgvKhachHang)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.dgvThongKeMuaHang)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.dgvLichSuMuaHang)).EndInit();
            this.ResumeLayout(false);
        }

        private System.Windows.Forms.Panel panelLoc;
        private System.Windows.Forms.Button btnTimKiem;
        private System.Windows.Forms.TextBox txtTuKhoa;
        private System.Windows.Forms.Label lblTuKhoa;
        private System.Windows.Forms.Panel panelNhapLieu;
        private System.Windows.Forms.TextBox txtDiaChi;
        private System.Windows.Forms.Label lblDiaChi;
        private System.Windows.Forms.TextBox txtSoDienThoai;
        private System.Windows.Forms.Label lblSoDienThoai;
        private System.Windows.Forms.TextBox txtTenKH;
        private System.Windows.Forms.Label lblTenKH;
        private System.Windows.Forms.Label lblMaKH;
        private System.Windows.Forms.Panel panelNut;
        private System.Windows.Forms.Button btnLamMoi;
        private System.Windows.Forms.Button btnXoa;
        private System.Windows.Forms.Button btnSua;
        private System.Windows.Forms.Button btnThem;
        private System.Windows.Forms.DataGridView dgvKhachHang;
        private System.Windows.Forms.Label lblThongKeMuaHang;
        private System.Windows.Forms.DataGridView dgvThongKeMuaHang;
        private System.Windows.Forms.Label lblLichSuMuaHang;
        private System.Windows.Forms.DataGridView dgvLichSuMuaHang;
    }
}
