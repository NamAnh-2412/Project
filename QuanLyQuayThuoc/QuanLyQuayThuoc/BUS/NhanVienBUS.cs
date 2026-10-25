using QuanLyQuayThuoc.DAO;
using QuanLyQuayThuoc.DTO;

namespace QuanLyQuayThuoc.BUS
{
    public class NhanVienBUS
    {
        private static NhanVienBUS instance;
        public static NhanVienBUS Instance
        {
            get { if (instance == null) instance = new NhanVienBUS(); return instance; }
            private set { instance = value; }
        }
        private NhanVienBUS() { }

        // Hàm chứa LOGIC kiểm tra do người dùng nhập vào Form
        public NhanVienDTO Login(string userName, string passWord)
        {
            // 1. Logic đầu vào: Không cho phép nhập rỗng
            if (string.IsNullOrEmpty(userName) || string.IsNullOrEmpty(passWord))
            {
                return null; // Form hiển thị lỗi báo "Chưa nhập đủ"
            }
            
            // 2. Logic nâng cao (nếu có): Chiều dài mật khẩu, ký tự đặc biệt...
            
            // 3. Đẩy xuống tầng DAO để kiểm tra so sánh với Database
            return NhanVienDAO.Instance.Login(userName, passWord);
        }
    }
}
