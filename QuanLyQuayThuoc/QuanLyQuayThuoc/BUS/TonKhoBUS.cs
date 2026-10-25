using System;
using System.Data;
using System.Text;
using QuanLyQuayThuoc.DAO;

namespace QuanLyQuayThuoc.BUS
{
    public class TonKhoBUS
    {
        private static TonKhoBUS instance;
        public static TonKhoBUS Instance
        {
            get { if (instance == null) instance = new TonKhoBUS(); return instance; }
            private set { instance = value; }
        }

        private TonKhoBUS() { }

        public DataTable LayTonKhoTongHop(string tuKhoa, object maDM)
        {
            return TonKhoDAO.Instance.LayTonKhoTongHop(tuKhoa, maDM);
        }

        public DataTable LayTonKhoChiTiet(string tuKhoa, object maThuoc = null)
        {
            return TonKhoDAO.Instance.LayTonKhoChiTiet(tuKhoa, maThuoc);
        }

        public DataTable LayCanhBaoHanSuDung(string tuKhoa, int soNgayCanhBao)
        {
            return TonKhoDAO.Instance.LayCanhBaoHanSuDung(tuKhoa, soNgayCanhBao);
        }

        public DataTable LayDanhSachLoBanHang()
        {
            return TonKhoDAO.Instance.LayDanhSachLoBanHang();
        }

        public DataTable LayThuocHetHangTonTongHop(string tuKhoa, object maDM)
        {
            return TonKhoDAO.Instance.LayThuocHetHangTonTongHop(tuKhoa, maDM);
        }

        public DataTable LayThuocSapHetHangTonTongHop(string tuKhoa, object maDM)
        {
            return TonKhoDAO.Instance.LayThuocSapHetHangTonTongHop(tuKhoa, maDM);
        }

        public string TaoThongBaoTonKhoDangNhap()
        {
            DataTable hetHang = LayThuocHetHangTonTongHop("", null);
            DataTable sapHet = LayThuocSapHetHangTonTongHop("", null);

            if (hetHang.Rows.Count == 0 && sapHet.Rows.Count == 0)
                return null;

            const int dongToiDa = 12;
            var sb = new StringBuilder();

            sb.AppendLine("CẢNH BÁO TỒN KHO");
            sb.AppendLine();

            if (hetHang.Rows.Count > 0)
            {
                sb.AppendLine("Đã hết sạch hàng (tổng tồn = 0):");
                int n = 0;
                foreach (DataRow row in hetHang.Rows)
                {
                    AppendMotDongThongKeTon(sb, row);
                    if (++n >= dongToiDa && hetHang.Rows.Count > dongToiDa)
                    {
                        sb.AppendLine($"  ... và {hetHang.Rows.Count - dongToiDa} loại khác.");
                        break;
                    }
                }
                sb.AppendLine();
            }

            if (sapHet.Rows.Count > 0)
            {
                sb.AppendLine("Sắp hết hàng (tồn > 0 nhưng không vượt quá ngưỡng tối thiểu đã quy đổi):");
                int n = 0;
                foreach (DataRow row in sapHet.Rows)
                {
                    AppendMotDongThongKeTon(sb, row);
                    if (++n >= dongToiDa && sapHet.Rows.Count > dongToiDa)
                    {
                        sb.AppendLine($"  ... và {sapHet.Rows.Count - dongToiDa} loại khác.");
                        break;
                    }
                }
                sb.AppendLine();
            }

            sb.AppendLine("Khuyến nghị: mở Xem tồn kho và tab Cảnh báo tồn kho để xem đầy đủ.");

            return sb.ToString();
        }

        private static void AppendMotDongThongKeTon(StringBuilder sb, DataRow row)
        {
            string tenThuoc = Convert.ToString(row["TenThuoc"]);
            int tongTon = Convert.ToInt32(row["TongTonKho"]);
            int nguong = row.Table.Columns.Contains("NguongToiThieu_DVNho") && row["NguongToiThieu_DVNho"] != DBNull.Value
                ? Convert.ToInt32(row["NguongToiThieu_DVNho"])
                : 0;
            sb.AppendLine($"  • {tenThuoc} — tồn (ĐV nhỏ): {tongTon} / ngưỡng cảnh báo quy đổi sang ĐV nhỏ: {nguong}");
        }
    }
}
