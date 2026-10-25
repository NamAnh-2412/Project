using System;
using System.Windows.Forms;
using QuanLyQuayThuoc.BUS;
using QuanLyQuayThuoc.DTO;

namespace QuanLyQuayThuoc.GUI
{
    public partial class FormLogin : Form
    {
        public FormLogin()
        {
            InitializeComponent();
        }

        private void btnLogin_Click(object sender, EventArgs e)
        {
            string userName = txtUserName.Text.Trim();
            string passWord = txtPassWord.Text.Trim();

            // Giao việc cho Lớp BUS xử lý
            NhanVienDTO nhanVien = NhanVienBUS.Instance.Login(userName, passWord);

            if (nhanVien != null)
            {
                MessageBox.Show($"Đăng nhập thành công!\nXin chào {nhanVien.HoTen} (Vai trò: {nhanVien.Quyen})", 
                                "Thông báo Hệ thống", MessageBoxButtons.OK, MessageBoxIcon.Information);
                
                // Sau đó sẽ mở Màn hình chính
                FormMain frm = new FormMain(nhanVien);
                this.Hide();
                frm.ShowDialog();
                this.Close(); 
            }
            else
            {
                MessageBox.Show("Sai tên đăng nhập hoặc mật khẩu!\nVui lòng nhâp lại.", 
                                "Lỗi Xác Thực", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        private void btnExit_Click(object sender, EventArgs e)
        {
            Application.Exit();
        }

        private void chkShowPassword_CheckedChanged(object sender, EventArgs e)
        {
            // Hiển thị mật khẩu hoặc chuyển lại chữ chấm tròn
            if (chkShowPassword.Checked)
            {
                txtPassWord.PasswordChar = '\0';
            }
            else
            {
                txtPassWord.PasswordChar = '•';
            }
        }
    }
}
