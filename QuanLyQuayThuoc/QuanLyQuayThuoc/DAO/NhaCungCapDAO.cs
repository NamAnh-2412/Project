using System.Data;

namespace QuanLyQuayThuoc.DAO
{
    public class NhaCungCapDAO
    {
        private static NhaCungCapDAO instance;
        public static NhaCungCapDAO Instance
        {
            get { if (instance == null) instance = new NhaCungCapDAO(); return instance; }
            private set { instance = value; }
        }
        private NhaCungCapDAO() { }

        public DataTable GetAll()
        {
            string query = "SELECT MaNCC, TenNCC, DiaChi, SoDienThoai, Email, GhiChu FROM NhaCungCap ORDER BY TenNCC";
            return DataProvider.Instance.ExecuteQuery(query);
        }

        public int Them(string tenNCC, string diaChi, string soDienThoai, string email, string ghiChu)
        {
            string query = "INSERT INTO NhaCungCap (TenNCC, DiaChi, SoDienThoai, Email, GhiChu) VALUES ( @TenNCC , @DiaChi , @SoDienThoai , @Email , @GhiChu )";
            return DataProvider.Instance.ExecuteNonQuery(query, new object[]
            {
                tenNCC,
                ToDbValue(diaChi),
                ToDbValue(soDienThoai),
                ToDbValue(email),
                ToDbValue(ghiChu)
            });
        }

        public int Sua(int maNCC, string tenNCC, string diaChi, string soDienThoai, string email, string ghiChu)
        {
            string query = "UPDATE NhaCungCap SET TenNCC = @TenNCC , DiaChi = @DiaChi , SoDienThoai = @SoDienThoai , Email = @Email , GhiChu = @GhiChu WHERE MaNCC = @MaNCC";
            return DataProvider.Instance.ExecuteNonQuery(query, new object[]
            {
                tenNCC,
                ToDbValue(diaChi),
                ToDbValue(soDienThoai),
                ToDbValue(email),
                ToDbValue(ghiChu),
                maNCC
            });
        }

        public int Xoa(int maNCC)
        {
            string query = "DELETE FROM NhaCungCap WHERE MaNCC = @MaNCC";
            return DataProvider.Instance.ExecuteNonQuery(query, new object[] { maNCC });
        }

        public bool DaDuocSuDung(int maNCC)
        {
            string query = "SELECT COUNT(*) FROM Thuoc WHERE MaNCC = @MaNCC";
            int soThuoc = System.Convert.ToInt32(DataProvider.Instance.ExecuteScalar(query, new object[] { maNCC }));
            if (soThuoc > 0)
                return true;

            query = "SELECT COUNT(*) FROM PhieuNhap WHERE MaNCC = @MaNCC";
            int soPhieuNhap = System.Convert.ToInt32(DataProvider.Instance.ExecuteScalar(query, new object[] { maNCC }));
            return soPhieuNhap > 0;
        }

        private object ToDbValue(string value)
        {
            return string.IsNullOrWhiteSpace(value) ? (object)System.DBNull.Value : value.Trim();
        }
    }
}
