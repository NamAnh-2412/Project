using System;
using System.Collections.Generic;
using System.Configuration;
using System.Data;
using System.Data.SqlClient;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;

namespace QuanLyQuayThuoc.DAO
{
    public class DataProvider
    {
        // Ứng dụng mẫu Singleton - Giúp đảm bảo trong toàn bộ app chỉ có 1 class DataProvider hoạt động
        private static DataProvider instance;

        public static DataProvider Instance
        {
            get { if (instance == null) instance = new DataProvider(); return DataProvider.instance; }
            private set { DataProvider.instance = value; }
        }

        private DataProvider() { }

        // Lấy chuỗi kết nối từ App.config thay vì hardcode (An toàn và dễ sửa)
        private string connectionString = ConfigurationManager.ConnectionStrings["QuayThuocConnection"].ConnectionString;

        // ==============================================================
        // HÀM 1: LẤY DỮ LIỆU (Dành cho câu lệnh SELECT)
        // Trả về một cái Bảng (DataTable) chứa kết quả.
        // ==============================================================
        public DataTable ExecuteQuery(string query, object[] parameter = null)
        {
            DataTable data = new DataTable();

            using (SqlConnection connection = new SqlConnection(connectionString))
            {
                connection.Open();

                SqlCommand command = new SqlCommand(query, connection);

                // Nếu có truyền parameter (Chống bị SQL Injection)
                if (parameter != null)
                {
                    int i = 0;
                    foreach (string paramName in LayDanhSachParameter(query))
                    {
                        if (i >= parameter.Length)
                        {
                            throw new IndexOutOfRangeException("Số lượng parameter truyền vào không khớp với câu SQL.");
                        }

                        command.Parameters.AddWithValue(paramName, parameter[i]);
                        i++;
                    }
                }

                SqlDataAdapter adapter = new SqlDataAdapter(command);
                adapter.Fill(data);

                connection.Close();
            }

            return data;
        }


        // ==============================================================
        // HÀM 2: CẬP NHẬT DỮ LIỆU (Dành cho câu lệnh INSERT, UPDATE, DELETE)
        // Trả về số dòng thành công. Reutrn > 0 là thành công.
        // ==============================================================
        public int ExecuteNonQuery(string query, object[] parameter = null)
        {
            int data = 0;

            using (SqlConnection connection = new SqlConnection(connectionString))
            {
                connection.Open();

                SqlCommand command = new SqlCommand(query, connection);

                if (parameter != null)
                {
                    int i = 0;
                    foreach (string paramName in LayDanhSachParameter(query))
                    {
                        if (i >= parameter.Length)
                        {
                            throw new IndexOutOfRangeException("Số lượng parameter truyền vào không khớp với câu SQL.");
                        }

                        command.Parameters.AddWithValue(paramName, parameter[i]);
                        i++;
                    }
                }

                data = command.ExecuteNonQuery();

                connection.Close();
            }

            return data;
        }


        // ==============================================================
        // HÀM 3: ĐẾM MỘT GIÁ TRỊ (Dành cho câu lệnh SELECT COUNT(*) )
        // Trả về dòng đầu tiên, cột đầu tiên.
        // ==============================================================
        public object ExecuteScalar(string query, object[] parameter = null)
        {
            object data = 0;

            using (SqlConnection connection = new SqlConnection(connectionString))
            {
                connection.Open();

                SqlCommand command = new SqlCommand(query, connection);

                if (parameter != null)
                {
                    int i = 0;
                    foreach (string paramName in LayDanhSachParameter(query))
                    {
                        if (i >= parameter.Length)
                        {
                            throw new IndexOutOfRangeException("Số lượng parameter truyền vào không khớp với câu SQL.");
                        }

                        command.Parameters.AddWithValue(paramName, parameter[i]);
                        i++;
                    }
                }

                data = command.ExecuteScalar();

                connection.Close();
            }

            return data;
        }

        private List<string> LayDanhSachParameter(string query)
        {
            List<string> ketQua = new List<string>();
            HashSet<string> daThem = new HashSet<string>(StringComparer.OrdinalIgnoreCase);

            MatchCollection matches = Regex.Matches(query, @"@\w+");
            foreach (Match match in matches)
            {
                string tenParam = match.Value;
                if (daThem.Contains(tenParam))
                    continue;

                daThem.Add(tenParam);
                ketQua.Add(tenParam);
            }

            return ketQua;
        }
    }
}
