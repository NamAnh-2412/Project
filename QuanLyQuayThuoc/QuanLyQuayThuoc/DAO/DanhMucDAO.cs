using System.Data;

namespace QuanLyQuayThuoc.DAO
{
    public class DanhMucDAO
    {
        private static DanhMucDAO instance;
        public static DanhMucDAO Instance
        {
            get { if (instance == null) instance = new DanhMucDAO(); return instance; }
            private set { instance = value; }
        }
        private DanhMucDAO() { }

        public DataTable GetAll()
        {
            string query = "SELECT MaDM, TenDanhMuc, MoTa FROM DanhMuc ORDER BY TenDanhMuc";
            return DataProvider.Instance.ExecuteQuery(query);
        }

        public int Them(string tenDanhMuc, string moTa)
        {
            string query = "INSERT INTO DanhMuc (TenDanhMuc, MoTa) VALUES ( @TenDanhMuc , @MoTa )";
            return DataProvider.Instance.ExecuteNonQuery(query, new object[]
            {
                tenDanhMuc,
                ToDbValue(moTa)
            });
        }

        public int Sua(int maDM, string tenDanhMuc, string moTa)
        {
            string query = "UPDATE DanhMuc SET TenDanhMuc = @TenDanhMuc , MoTa = @MoTa WHERE MaDM = @MaDM";
            return DataProvider.Instance.ExecuteNonQuery(query, new object[]
            {
                tenDanhMuc,
                ToDbValue(moTa),
                maDM
            });
        }

        public int Xoa(int maDM)
        {
            string query = "DELETE FROM DanhMuc WHERE MaDM = @MaDM";
            return DataProvider.Instance.ExecuteNonQuery(query, new object[] { maDM });
        }

        public bool DaDuocSuDung(int maDM)
        {
            string query = "SELECT COUNT(*) FROM Thuoc WHERE MaDM = @MaDM";
            int soThuoc = System.Convert.ToInt32(DataProvider.Instance.ExecuteScalar(query, new object[] { maDM }));
            return soThuoc > 0;
        }

        private object ToDbValue(string value)
        {
            return string.IsNullOrWhiteSpace(value) ? (object)System.DBNull.Value : value.Trim();
        }
    }
}
