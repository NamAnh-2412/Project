using System;
using System.Collections.Generic;
using System.Configuration;
using System.Data;
using System.Data.SqlClient;
using QuanLyQuayThuoc.DTO;

namespace QuanLyQuayThuoc.DAO
{
    public class CanhBaoHanDungDAO
    {
        private static CanhBaoHanDungDAO instance;
        public static CanhBaoHanDungDAO Instance
        {
            get { if (instance == null) instance = new CanhBaoHanDungDAO(); return instance; }
            private set { instance = value; }
        }

        private readonly string connectionString = ConfigurationManager.ConnectionStrings["QuayThuocConnection"].ConnectionString;

        private CanhBaoHanDungDAO() { }

        public DataTable LayDanhSachLoCanKiemTra()
        {
            string query = @"
SELECT 
    TK.MaThuoc,
    T.TenThuoc,
    TK.SoLo,
    TK.HanSuDung,
    TK.SoLuongTon,
    CASE 
        WHEN ISNULL(T.TyLeQuyDoi, 1) > 1 AND ISNULL(T.DonViQuyDoi, '') <> '' THEN T.DonViQuyDoi
        ELSE T.DonViTinh
    END AS DonViTinh,
    DATEDIFF(DAY, GETDATE(), TK.HanSuDung) AS SoNgayConLai
FROM TonKho TK
INNER JOIN Thuoc T ON TK.MaThuoc = T.MaThuoc
WHERE TK.SoLuongTon > 0
  AND DATEDIFF(DAY, GETDATE(), TK.HanSuDung) <= 7
ORDER BY SoNgayConLai, TK.HanSuDung";

            return DataProvider.Instance.ExecuteQuery(query);
        }

        public bool DaCanhBao(int maThuoc, string soLo, int mocCanhBao)
        {
            string query = "SELECT COUNT(*) FROM CanhBaoHanDung WHERE MaThuoc = @MaThuoc AND SoLo = @SoLo AND MocCanhBao = @MocCanhBao";
            int soLuong = Convert.ToInt32(DataProvider.Instance.ExecuteScalar(query, new object[] { maThuoc, soLo, mocCanhBao }));
            return soLuong > 0;
        }

        public void ThemCanhBao(int maThuoc, string soLo, int mocCanhBao, string ghiChu)
        {
            using (SqlConnection connection = new SqlConnection(connectionString))
            {
                connection.Open();
                string query = @"
INSERT INTO CanhBaoHanDung (MaThuoc, SoLo, MocCanhBao, NgayCanhBao, TrangThai, GhiChu, DaXuLy)
VALUES (@MaThuoc, @SoLo, @MocCanhBao, GETDATE(), @TrangThai, @GhiChu, 0)";

                SqlCommand command = new SqlCommand(query, connection);
                command.Parameters.AddWithValue("@MaThuoc", maThuoc);
                command.Parameters.AddWithValue("@SoLo", soLo);
                command.Parameters.AddWithValue("@MocCanhBao", mocCanhBao);
                command.Parameters.AddWithValue("@TrangThai", "DaCanhBao");
                command.Parameters.AddWithValue("@GhiChu", string.IsNullOrWhiteSpace(ghiChu) ? (object)DBNull.Value : ghiChu);
                command.ExecuteNonQuery();
                connection.Close();
            }
        }

        /// <summary>
        /// Tu dong danh dau DaXuLy = 1 cho cac canh bao ma lo thuoc da het hang (SoLuongTon = 0).
        /// </summary>
        public void TuDongHuyCanhBaoKhiHetHang()
        {
            string query = @"
UPDATE CanhBaoHanDung
SET DaXuLy = 1
WHERE DaXuLy = 0
  AND NOT EXISTS (
      SELECT 1 FROM TonKho TK
      WHERE TK.MaThuoc = CanhBaoHanDung.MaThuoc
        AND TK.SoLo    = CanhBaoHanDung.SoLo
        AND TK.SoLuongTon > 0
  )";
            DataProvider.Instance.ExecuteNonQuery(query);
        }

        /// <summary>
        /// Lay lich su canh bao, ho tro loc theo tu khoa va trang thai xu ly.
        /// daXuLy = null: tat ca | true: da xu ly | false: chua xu ly
        /// </summary>
        public DataTable LayLichSuCanhBao(string tuKhoa, bool? daXuLy)
        {
            string query = @"
SELECT
    CBD.Id,
    CBD.MaThuoc,
    T.TenThuoc,
    CBD.SoLo,
    CBD.MocCanhBao,
    CBD.NgayCanhBao,
    CBD.TrangThai,
    CBD.DaXuLy,
    CBD.GhiChu,
    TK.HanSuDung,
    ISNULL(TK.SoLuongTon, 0) AS SoLuongTon,
    DATEDIFF(DAY, GETDATE(), TK.HanSuDung) AS SoNgayConLai
FROM CanhBaoHanDung CBD
INNER JOIN Thuoc T ON CBD.MaThuoc = T.MaThuoc
LEFT JOIN TonKho TK ON TK.MaThuoc = CBD.MaThuoc AND TK.SoLo = CBD.SoLo
WHERE (@TuKhoa IS NULL OR T.TenThuoc LIKE '%' + @TuKhoa + '%' OR CBD.SoLo LIKE '%' + @TuKhoa + '%')
  AND (@DaXuLy IS NULL OR CBD.DaXuLy = @DaXuLy)
ORDER BY CBD.NgayCanhBao DESC";

            return DataProvider.Instance.ExecuteQuery(
                query,
                new object[]
                {
                    string.IsNullOrWhiteSpace(tuKhoa) ? (object)DBNull.Value : tuKhoa.Trim(),
                    daXuLy.HasValue ? (object)(daXuLy.Value ? 1 : 0) : DBNull.Value
                });
        }
    }
}
