namespace QuanLyQuayThuoc.GUI
{
    partial class FormBaoCaoDoanhThu
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
            this.btnXemBaoCao = new System.Windows.Forms.Button();
            this.txtSoHoaDon = new System.Windows.Forms.TextBox();
            this.lblSoHoaDon = new System.Windows.Forms.Label();
            this.txtTongDoanhThu = new System.Windows.Forms.TextBox();
            this.lblTongDoanhThu = new System.Windows.Forms.Label();
            this.dtpDenNgay = new System.Windows.Forms.DateTimePicker();
            this.lblDenNgay = new System.Windows.Forms.Label();
            this.dtpTuNgay = new System.Windows.Forms.DateTimePicker();
            this.lblTuNgay = new System.Windows.Forms.Label();
            this.splitContainer1 = new System.Windows.Forms.SplitContainer();
            this.groupChiTietHoaDon = new System.Windows.Forms.GroupBox();
            this.dgvChiTietHoaDon = new System.Windows.Forms.DataGridView();
            this.groupThuocBanChay = new System.Windows.Forms.GroupBox();
            this.dgvThuocBanChay = new System.Windows.Forms.DataGridView();
            this.panelLoc.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.splitContainer1)).BeginInit();
            this.splitContainer1.Panel1.SuspendLayout();
            this.splitContainer1.Panel2.SuspendLayout();
            this.splitContainer1.SuspendLayout();
            this.groupChiTietHoaDon.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.dgvChiTietHoaDon)).BeginInit();
            this.groupThuocBanChay.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.dgvThuocBanChay)).BeginInit();
            this.SuspendLayout();
            // 
            // panelLoc
            // 
            this.panelLoc.Controls.Add(this.btnXemBaoCao);
            this.panelLoc.Controls.Add(this.txtSoHoaDon);
            this.panelLoc.Controls.Add(this.lblSoHoaDon);
            this.panelLoc.Controls.Add(this.txtTongDoanhThu);
            this.panelLoc.Controls.Add(this.lblTongDoanhThu);
            this.panelLoc.Controls.Add(this.dtpDenNgay);
            this.panelLoc.Controls.Add(this.lblDenNgay);
            this.panelLoc.Controls.Add(this.dtpTuNgay);
            this.panelLoc.Controls.Add(this.lblTuNgay);
            this.panelLoc.Dock = System.Windows.Forms.DockStyle.Top;
            this.panelLoc.Location = new System.Drawing.Point(0, 0);
            this.panelLoc.Name = "panelLoc";
            this.panelLoc.Padding = new System.Windows.Forms.Padding(12, 8, 12, 8);
            this.panelLoc.Size = new System.Drawing.Size(980, 74);
            this.panelLoc.TabIndex = 0;
            // 
            // btnXemBaoCao
            // 
            this.btnXemBaoCao.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnXemBaoCao.Location = new System.Drawing.Point(503, 21);
            this.btnXemBaoCao.Name = "btnXemBaoCao";
            this.btnXemBaoCao.Size = new System.Drawing.Size(110, 30);
            this.btnXemBaoCao.TabIndex = 8;
            this.btnXemBaoCao.Text = "Xem báo cáo";
            this.btnXemBaoCao.UseVisualStyleBackColor = true;
            this.btnXemBaoCao.Click += new System.EventHandler(this.btnXemBaoCao_Click);
            // 
            // txtSoHoaDon
            // 
            this.txtSoHoaDon.Location = new System.Drawing.Point(864, 25);
            this.txtSoHoaDon.Name = "txtSoHoaDon";
            this.txtSoHoaDon.ReadOnly = true;
            this.txtSoHoaDon.Size = new System.Drawing.Size(96, 23);
            this.txtSoHoaDon.TabIndex = 7;
            // 
            // lblSoHoaDon
            // 
            this.lblSoHoaDon.AutoSize = true;
            this.lblSoHoaDon.Location = new System.Drawing.Point(786, 28);
            this.lblSoHoaDon.Name = "lblSoHoaDon";
            this.lblSoHoaDon.Size = new System.Drawing.Size(72, 15);
            this.lblSoHoaDon.TabIndex = 6;
            this.lblSoHoaDon.Text = "Số hóa đơn";
            // 
            // txtTongDoanhThu
            // 
            this.txtTongDoanhThu.Location = new System.Drawing.Point(673, 25);
            this.txtTongDoanhThu.Name = "txtTongDoanhThu";
            this.txtTongDoanhThu.ReadOnly = true;
            this.txtTongDoanhThu.Size = new System.Drawing.Size(100, 23);
            this.txtTongDoanhThu.TabIndex = 5;
            // 
            // lblTongDoanhThu
            // 
            this.lblTongDoanhThu.AutoSize = true;
            this.lblTongDoanhThu.Location = new System.Drawing.Point(582, 28);
            this.lblTongDoanhThu.Name = "lblTongDoanhThu";
            this.lblTongDoanhThu.Size = new System.Drawing.Size(85, 15);
            this.lblTongDoanhThu.TabIndex = 4;
            this.lblTongDoanhThu.Text = "Tổng doanh thu";
            // 
            // dtpDenNgay
            // 
            this.dtpDenNgay.Format = System.Windows.Forms.DateTimePickerFormat.Short;
            this.dtpDenNgay.Location = new System.Drawing.Point(315, 25);
            this.dtpDenNgay.Name = "dtpDenNgay";
            this.dtpDenNgay.Size = new System.Drawing.Size(120, 23);
            this.dtpDenNgay.TabIndex = 3;
            // 
            // lblDenNgay
            // 
            this.lblDenNgay.AutoSize = true;
            this.lblDenNgay.Location = new System.Drawing.Point(254, 28);
            this.lblDenNgay.Name = "lblDenNgay";
            this.lblDenNgay.Size = new System.Drawing.Size(55, 15);
            this.lblDenNgay.TabIndex = 2;
            this.lblDenNgay.Text = "Đến ngày";
            // 
            // dtpTuNgay
            // 
            this.dtpTuNgay.Format = System.Windows.Forms.DateTimePickerFormat.Short;
            this.dtpTuNgay.Location = new System.Drawing.Point(82, 25);
            this.dtpTuNgay.Name = "dtpTuNgay";
            this.dtpTuNgay.Size = new System.Drawing.Size(120, 23);
            this.dtpTuNgay.TabIndex = 1;
            // 
            // lblTuNgay
            // 
            this.lblTuNgay.AutoSize = true;
            this.lblTuNgay.Location = new System.Drawing.Point(15, 28);
            this.lblTuNgay.Name = "lblTuNgay";
            this.lblTuNgay.Size = new System.Drawing.Size(47, 15);
            this.lblTuNgay.TabIndex = 0;
            this.lblTuNgay.Text = "Từ ngày";
            // 
            // splitContainer1
            // 
            this.splitContainer1.Dock = System.Windows.Forms.DockStyle.Fill;
            this.splitContainer1.Location = new System.Drawing.Point(0, 74);
            this.splitContainer1.Name = "splitContainer1";
            this.splitContainer1.Orientation = System.Windows.Forms.Orientation.Horizontal;
            // 
            // splitContainer1.Panel1
            // 
            this.splitContainer1.Panel1.Controls.Add(this.groupChiTietHoaDon);
            // 
            // splitContainer1.Panel2
            // 
            this.splitContainer1.Panel2.Controls.Add(this.groupThuocBanChay);
            this.splitContainer1.Size = new System.Drawing.Size(980, 526);
            this.splitContainer1.SplitterDistance = 315;
            this.splitContainer1.TabIndex = 1;
            // 
            // groupChiTietHoaDon
            // 
            this.groupChiTietHoaDon.Controls.Add(this.dgvChiTietHoaDon);
            this.groupChiTietHoaDon.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupChiTietHoaDon.Location = new System.Drawing.Point(0, 0);
            this.groupChiTietHoaDon.Name = "groupChiTietHoaDon";
            this.groupChiTietHoaDon.Size = new System.Drawing.Size(980, 315);
            this.groupChiTietHoaDon.TabIndex = 0;
            this.groupChiTietHoaDon.TabStop = false;
            this.groupChiTietHoaDon.Text = "Chi tiết hóa đơn theo khoảng thời gian";
            // 
            // dgvChiTietHoaDon
            // 
            this.dgvChiTietHoaDon.AllowUserToAddRows = false;
            this.dgvChiTietHoaDon.AllowUserToDeleteRows = false;
            this.dgvChiTietHoaDon.AutoSizeColumnsMode = System.Windows.Forms.DataGridViewAutoSizeColumnsMode.Fill;
            this.dgvChiTietHoaDon.BackgroundColor = System.Drawing.Color.White;
            this.dgvChiTietHoaDon.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
            this.dgvChiTietHoaDon.Dock = System.Windows.Forms.DockStyle.Fill;
            this.dgvChiTietHoaDon.Location = new System.Drawing.Point(3, 19);
            this.dgvChiTietHoaDon.MultiSelect = false;
            this.dgvChiTietHoaDon.Name = "dgvChiTietHoaDon";
            this.dgvChiTietHoaDon.ReadOnly = true;
            this.dgvChiTietHoaDon.RowHeadersVisible = false;
            this.dgvChiTietHoaDon.SelectionMode = System.Windows.Forms.DataGridViewSelectionMode.FullRowSelect;
            this.dgvChiTietHoaDon.Size = new System.Drawing.Size(974, 293);
            this.dgvChiTietHoaDon.TabIndex = 0;
            // 
            // groupThuocBanChay
            // 
            this.groupThuocBanChay.Controls.Add(this.dgvThuocBanChay);
            this.groupThuocBanChay.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupThuocBanChay.Location = new System.Drawing.Point(0, 0);
            this.groupThuocBanChay.Name = "groupThuocBanChay";
            this.groupThuocBanChay.Size = new System.Drawing.Size(980, 207);
            this.groupThuocBanChay.TabIndex = 0;
            this.groupThuocBanChay.TabStop = false;
            this.groupThuocBanChay.Text = "Top thuốc bán chạy";
            // 
            // dgvThuocBanChay
            // 
            this.dgvThuocBanChay.AllowUserToAddRows = false;
            this.dgvThuocBanChay.AllowUserToDeleteRows = false;
            this.dgvThuocBanChay.AutoSizeColumnsMode = System.Windows.Forms.DataGridViewAutoSizeColumnsMode.Fill;
            this.dgvThuocBanChay.BackgroundColor = System.Drawing.Color.White;
            this.dgvThuocBanChay.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
            this.dgvThuocBanChay.Dock = System.Windows.Forms.DockStyle.Fill;
            this.dgvThuocBanChay.Location = new System.Drawing.Point(3, 19);
            this.dgvThuocBanChay.MultiSelect = false;
            this.dgvThuocBanChay.Name = "dgvThuocBanChay";
            this.dgvThuocBanChay.ReadOnly = true;
            this.dgvThuocBanChay.RowHeadersVisible = false;
            this.dgvThuocBanChay.SelectionMode = System.Windows.Forms.DataGridViewSelectionMode.FullRowSelect;
            this.dgvThuocBanChay.Size = new System.Drawing.Size(974, 185);
            this.dgvThuocBanChay.TabIndex = 0;
            // 
            // FormBaoCaoDoanhThu
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(7F, 15F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(980, 600);
            this.Controls.Add(this.splitContainer1);
            this.Controls.Add(this.panelLoc);
            this.Font = new System.Drawing.Font("Segoe UI", 9F);
            this.Name = "FormBaoCaoDoanhThu";
            this.Text = "Báo cáo doanh thu";
            this.Load += new System.EventHandler(this.FormBaoCaoDoanhThu_Load);
            this.panelLoc.ResumeLayout(false);
            this.panelLoc.PerformLayout();
            this.splitContainer1.Panel1.ResumeLayout(false);
            this.splitContainer1.Panel2.ResumeLayout(false);
            ((System.ComponentModel.ISupportInitialize)(this.splitContainer1)).EndInit();
            this.splitContainer1.ResumeLayout(false);
            this.groupChiTietHoaDon.ResumeLayout(false);
            ((System.ComponentModel.ISupportInitialize)(this.dgvChiTietHoaDon)).EndInit();
            this.groupThuocBanChay.ResumeLayout(false);
            ((System.ComponentModel.ISupportInitialize)(this.dgvThuocBanChay)).EndInit();
            this.ResumeLayout(false);
        }

        private System.Windows.Forms.Panel panelLoc;
        private System.Windows.Forms.Button btnXemBaoCao;
        private System.Windows.Forms.TextBox txtSoHoaDon;
        private System.Windows.Forms.Label lblSoHoaDon;
        private System.Windows.Forms.TextBox txtTongDoanhThu;
        private System.Windows.Forms.Label lblTongDoanhThu;
        private System.Windows.Forms.DateTimePicker dtpDenNgay;
        private System.Windows.Forms.Label lblDenNgay;
        private System.Windows.Forms.DateTimePicker dtpTuNgay;
        private System.Windows.Forms.Label lblTuNgay;
        private System.Windows.Forms.SplitContainer splitContainer1;
        private System.Windows.Forms.GroupBox groupChiTietHoaDon;
        private System.Windows.Forms.DataGridView dgvChiTietHoaDon;
        private System.Windows.Forms.GroupBox groupThuocBanChay;
        private System.Windows.Forms.DataGridView dgvThuocBanChay;
    }
}
