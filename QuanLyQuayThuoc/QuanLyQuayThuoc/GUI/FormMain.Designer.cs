namespace QuanLyQuayThuoc.GUI
{
    partial class FormMain
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
            this.panelSidebar = new System.Windows.Forms.Panel();
            this.btnLogout = new System.Windows.Forms.Button();
            this.btnBaoCao = new System.Windows.Forms.Button();
            this.btnKhachHang = new System.Windows.Forms.Button();
            this.btnKhoThuoc = new System.Windows.Forms.Button();
            this.btnTonKho = new System.Windows.Forms.Button();
            this.btnNhapKho = new System.Windows.Forms.Button();
            this.btnBanHang = new System.Windows.Forms.Button();
            this.panelLogo = new System.Windows.Forms.Panel();
            this.lblLogo = new System.Windows.Forms.Label();
            this.panelTitle = new System.Windows.Forms.Panel();
            this.lblTenNhanVien = new System.Windows.Forms.Label();
            this.lblTieuDe = new System.Windows.Forms.Label();
            this.panelDesktop = new System.Windows.Forms.Panel();
            this.panelSidebar.SuspendLayout();
            this.panelLogo.SuspendLayout();
            this.panelTitle.SuspendLayout();
            this.SuspendLayout();
            // 
            // panelSidebar
            // 
            this.panelSidebar.BackColor = System.Drawing.Color.FromArgb(((int)(((byte)(51)))), ((int)(((byte)(51)))), ((int)(((byte)(76)))));
            this.panelSidebar.Controls.Add(this.btnLogout);
            this.panelSidebar.Controls.Add(this.btnBaoCao);
            this.panelSidebar.Controls.Add(this.btnKhachHang);
            this.panelSidebar.Controls.Add(this.btnKhoThuoc);
            this.panelSidebar.Controls.Add(this.btnTonKho);
            this.panelSidebar.Controls.Add(this.btnNhapKho);
            this.panelSidebar.Controls.Add(this.btnBanHang);
            this.panelSidebar.Controls.Add(this.panelLogo);
            this.panelSidebar.Dock = System.Windows.Forms.DockStyle.Left;
            this.panelSidebar.Location = new System.Drawing.Point(0, 0);
            this.panelSidebar.Name = "panelSidebar";
            this.panelSidebar.Size = new System.Drawing.Size(251, 640);
            this.panelSidebar.TabIndex = 0;
            // 
            // btnLogout
            // 
            this.btnLogout.Dock = System.Windows.Forms.DockStyle.Bottom;
            this.btnLogout.FlatAppearance.BorderSize = 0;
            this.btnLogout.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnLogout.Font = new System.Drawing.Font("Segoe UI", 11F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.btnLogout.ForeColor = System.Drawing.Color.LightCoral;
            this.btnLogout.Location = new System.Drawing.Point(0, 576);
            this.btnLogout.Name = "btnLogout";
            this.btnLogout.Padding = new System.Windows.Forms.Padding(23, 0, 0, 0);
            this.btnLogout.Size = new System.Drawing.Size(251, 64);
            this.btnLogout.TabIndex = 4;
            this.btnLogout.Text = "  Đăng Xuất";
            this.btnLogout.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            this.btnLogout.UseVisualStyleBackColor = true;
            this.btnLogout.Click += new System.EventHandler(this.btnLogout_Click);
            // 
            // btnBaoCao
            // 
            this.btnBaoCao.Dock = System.Windows.Forms.DockStyle.Top;
            this.btnBaoCao.FlatAppearance.BorderSize = 0;
            this.btnBaoCao.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnBaoCao.Font = new System.Drawing.Font("Segoe UI", 11F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.btnBaoCao.ForeColor = System.Drawing.Color.Gainsboro;
            this.btnBaoCao.Location = new System.Drawing.Point(0, 405);
            this.btnBaoCao.Name = "btnBaoCao";
            this.btnBaoCao.Padding = new System.Windows.Forms.Padding(23, 0, 0, 0);
            this.btnBaoCao.Size = new System.Drawing.Size(251, 64);
            this.btnBaoCao.TabIndex = 6;
            this.btnBaoCao.Text = "  Báo Cáo Doanh Thu";
            this.btnBaoCao.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            this.btnBaoCao.UseVisualStyleBackColor = true;
            this.btnBaoCao.Click += new System.EventHandler(this.btnBaoCao_Click);
            // 
            // btnKhachHang
            // 
            this.btnKhachHang.Dock = System.Windows.Forms.DockStyle.Top;
            this.btnKhachHang.FlatAppearance.BorderSize = 0;
            this.btnKhachHang.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnKhachHang.Font = new System.Drawing.Font("Segoe UI", 11F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.btnKhachHang.ForeColor = System.Drawing.Color.Gainsboro;
            this.btnKhachHang.Location = new System.Drawing.Point(0, 341);
            this.btnKhachHang.Name = "btnKhachHang";
            this.btnKhachHang.Padding = new System.Windows.Forms.Padding(23, 0, 0, 0);
            this.btnKhachHang.Size = new System.Drawing.Size(251, 64);
            this.btnKhachHang.TabIndex = 5;
            this.btnKhachHang.Text = "  Khách Hàng";
            this.btnKhachHang.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            this.btnKhachHang.UseVisualStyleBackColor = true;
            this.btnKhachHang.Click += new System.EventHandler(this.btnKhachHang_Click);
            // 
            // btnKhoThuoc
            // 
            this.btnKhoThuoc.Dock = System.Windows.Forms.DockStyle.Top;
            this.btnKhoThuoc.FlatAppearance.BorderSize = 0;
            this.btnKhoThuoc.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnKhoThuoc.Font = new System.Drawing.Font("Segoe UI", 11F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.btnKhoThuoc.ForeColor = System.Drawing.Color.Gainsboro;
            this.btnKhoThuoc.Location = new System.Drawing.Point(0, 277);
            this.btnKhoThuoc.Name = "btnKhoThuoc";
            this.btnKhoThuoc.Padding = new System.Windows.Forms.Padding(23, 0, 0, 0);
            this.btnKhoThuoc.Size = new System.Drawing.Size(251, 64);
            this.btnKhoThuoc.TabIndex = 4;
            this.btnKhoThuoc.Text = "  Quản Lý Thuốc";
            this.btnKhoThuoc.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            this.btnKhoThuoc.UseVisualStyleBackColor = true;
            this.btnKhoThuoc.Click += new System.EventHandler(this.btnKhoThuoc_Click);
            // 
            // btnTonKho
            // 
            this.btnTonKho.Dock = System.Windows.Forms.DockStyle.Top;
            this.btnTonKho.FlatAppearance.BorderSize = 0;
            this.btnTonKho.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnTonKho.Font = new System.Drawing.Font("Segoe UI", 11F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.btnTonKho.ForeColor = System.Drawing.Color.Gainsboro;
            this.btnTonKho.Location = new System.Drawing.Point(0, 213);
            this.btnTonKho.Name = "btnTonKho";
            this.btnTonKho.Padding = new System.Windows.Forms.Padding(23, 0, 0, 0);
            this.btnTonKho.Size = new System.Drawing.Size(251, 64);
            this.btnTonKho.TabIndex = 3;
            this.btnTonKho.Text = "  Xem Tồn Kho";
            this.btnTonKho.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            this.btnTonKho.UseVisualStyleBackColor = true;
            this.btnTonKho.Click += new System.EventHandler(this.btnTonKho_Click);
            // 
            // btnNhapKho
            // 
            this.btnNhapKho.Dock = System.Windows.Forms.DockStyle.Top;
            this.btnNhapKho.FlatAppearance.BorderSize = 0;
            this.btnNhapKho.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnNhapKho.Font = new System.Drawing.Font("Segoe UI", 11F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.btnNhapKho.ForeColor = System.Drawing.Color.Gainsboro;
            this.btnNhapKho.Location = new System.Drawing.Point(0, 149);
            this.btnNhapKho.Name = "btnNhapKho";
            this.btnNhapKho.Padding = new System.Windows.Forms.Padding(23, 0, 0, 0);
            this.btnNhapKho.Size = new System.Drawing.Size(251, 64);
            this.btnNhapKho.TabIndex = 2;
            this.btnNhapKho.Text = "  Nhập Kho";
            this.btnNhapKho.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            this.btnNhapKho.UseVisualStyleBackColor = true;
            this.btnNhapKho.Click += new System.EventHandler(this.btnNhapKho_Click);
            // 
            // btnBanHang
            // 
            this.btnBanHang.Dock = System.Windows.Forms.DockStyle.Top;
            this.btnBanHang.FlatAppearance.BorderSize = 0;
            this.btnBanHang.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnBanHang.Font = new System.Drawing.Font("Segoe UI", 11F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.btnBanHang.ForeColor = System.Drawing.Color.Gainsboro;
            this.btnBanHang.Location = new System.Drawing.Point(0, 85);
            this.btnBanHang.Name = "btnBanHang";
            this.btnBanHang.Padding = new System.Windows.Forms.Padding(23, 0, 0, 0);
            this.btnBanHang.Size = new System.Drawing.Size(251, 64);
            this.btnBanHang.TabIndex = 1;
            this.btnBanHang.Text = "  Bán Mới (Hóa Đơn)";
            this.btnBanHang.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            this.btnBanHang.UseVisualStyleBackColor = true;
            this.btnBanHang.Click += new System.EventHandler(this.btnBanHang_Click);
            // 
            // panelLogo
            // 
            this.panelLogo.BackColor = System.Drawing.Color.FromArgb(((int)(((byte)(39)))), ((int)(((byte)(39)))), ((int)(((byte)(58)))));
            this.panelLogo.Controls.Add(this.lblLogo);
            this.panelLogo.Dock = System.Windows.Forms.DockStyle.Top;
            this.panelLogo.Location = new System.Drawing.Point(0, 0);
            this.panelLogo.Name = "panelLogo";
            this.panelLogo.Size = new System.Drawing.Size(251, 85);
            this.panelLogo.TabIndex = 0;
            // 
            // lblLogo
            // 
            this.lblLogo.AutoSize = true;
            this.lblLogo.Font = new System.Drawing.Font("Segoe UI", 16F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblLogo.ForeColor = System.Drawing.Color.White;
            this.lblLogo.Location = new System.Drawing.Point(25, 27);
            this.lblLogo.Name = "lblLogo";
            this.lblLogo.Size = new System.Drawing.Size(193, 37);
            this.lblLogo.TabIndex = 0;
            this.lblLogo.Text = "QUẦY THUỐC";
            // 
            // panelTitle
            // 
            this.panelTitle.BackColor = System.Drawing.Color.FromArgb(((int)(((byte)(0)))), ((int)(((byte)(150)))), ((int)(((byte)(136)))));
            this.panelTitle.Controls.Add(this.lblTenNhanVien);
            this.panelTitle.Controls.Add(this.lblTieuDe);
            this.panelTitle.Dock = System.Windows.Forms.DockStyle.Top;
            this.panelTitle.Location = new System.Drawing.Point(251, 0);
            this.panelTitle.Name = "panelTitle";
            this.panelTitle.Size = new System.Drawing.Size(892, 85);
            this.panelTitle.TabIndex = 1;
            // 
            // lblTenNhanVien
            // 
            this.lblTenNhanVien.AutoSize = true;
            this.lblTenNhanVien.Font = new System.Drawing.Font("Segoe UI", 11F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblTenNhanVien.ForeColor = System.Drawing.Color.White;
            this.lblTenNhanVien.Location = new System.Drawing.Point(46, 33);
            this.lblTenNhanVien.Name = "lblTenNhanVien";
            this.lblTenNhanVien.Size = new System.Drawing.Size(106, 25);
            this.lblTenNhanVien.TabIndex = 1;
            this.lblTenNhanVien.Text = "Xin chào, ...";
            // 
            // lblTieuDe
            // 
            this.lblTieuDe.AutoSize = true;
            this.lblTieuDe.Font = new System.Drawing.Font("Segoe UI", 20F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblTieuDe.ForeColor = System.Drawing.Color.White;
            this.lblTieuDe.Location = new System.Drawing.Point(662, 17);
            this.lblTieuDe.Name = "lblTieuDe";
            this.lblTieuDe.Size = new System.Drawing.Size(218, 46);
            this.lblTieuDe.TabIndex = 0;
            this.lblTieuDe.Text = "TRANG CHỦ";
            // 
            // panelDesktop
            // 
            this.panelDesktop.BackColor = System.Drawing.Color.White;
            this.panelDesktop.Dock = System.Windows.Forms.DockStyle.Fill;
            this.panelDesktop.Location = new System.Drawing.Point(251, 85);
            this.panelDesktop.Name = "panelDesktop";
            this.panelDesktop.Size = new System.Drawing.Size(892, 555);
            this.panelDesktop.TabIndex = 2;
            // 
            // FormMain
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(8F, 16F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(1143, 640);
            this.Controls.Add(this.panelDesktop);
            this.Controls.Add(this.panelTitle);
            this.Controls.Add(this.panelSidebar);
            this.MinimumSize = new System.Drawing.Size(1026, 530);
            this.Name = "FormMain";
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
            this.Text = "Phần Mềm Quản Lý Quầy Thuốc";
            this.Load += new System.EventHandler(this.FormMain_Load);
            this.panelSidebar.ResumeLayout(false);
            this.panelLogo.ResumeLayout(false);
            this.panelLogo.PerformLayout();
            this.panelTitle.ResumeLayout(false);
            this.panelTitle.PerformLayout();
            this.ResumeLayout(false);

        }

        private System.Windows.Forms.Panel panelSidebar;
        private System.Windows.Forms.Panel panelLogo;
        private System.Windows.Forms.Label lblLogo;
        private System.Windows.Forms.Button btnBanHang;
        private System.Windows.Forms.Button btnNhapKho;
        private System.Windows.Forms.Button btnTonKho;
        private System.Windows.Forms.Button btnKhoThuoc;
        private System.Windows.Forms.Button btnKhachHang;
        private System.Windows.Forms.Button btnBaoCao;
        private System.Windows.Forms.Button btnLogout;
        private System.Windows.Forms.Panel panelTitle;
        private System.Windows.Forms.Label lblTieuDe;
        private System.Windows.Forms.Label lblTenNhanVien;
        private System.Windows.Forms.Panel panelDesktop;
    }
}
