using QuanLyQuayThuoc.DTO;
using System.Data;

namespace QuanLyQuayThuoc.DAO
{
    public class NhanVienDAO
    {
        private static NhanVienDAO instance;
        public static NhanVienDAO Instance
        {
            get { if (instance == null) instance = new NhanVienDAO(); return instance; }
            private set { instance = value; }
        }
        private NhanVienDAO() { }

        // Hàm tương tác trực tiếp với Database để kiểm tra đăng nhập
        public NhanVienDTO Login(string userName, string passWord)
        {
            // Viết câu Query và DÙNG PARAMETER để chống Hacker (SQL Injection)
            string query = "SELECT * FROM NhanVien WHERE TenDangNhap = @userName AND MatKhau = @passWord";
            
            // Gọi tầng DataProvider mà chúng ta đã làm lúc nãy để chạy lệnh
            DataTable result = DataProvider.Instance.ExecuteQuery(query, new object[] { userName, passWord });
            
            // Nếu có kết quả trả về (Số dòng > 0) -> Đăng nhập đúng thông tin
            if (result.Rows.Count > 0)
            {
                NhanVienDTO nhanVien = new NhanVienDTO(result.Rows[0]);
                return nhanVien;
            }

            // Nếu sai tài khoản / mật khẩu
            return null;
        }
    }
}
