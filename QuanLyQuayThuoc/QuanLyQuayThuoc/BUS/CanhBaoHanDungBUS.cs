using System;
using System.Collections.Generic;
using System.Data;
using System.Text;
using QuanLyQuayThuoc.DAO;

namespace QuanLyQuayThuoc.BUS
{
    public class CanhBaoHanDungBUS
    {
        private static CanhBaoHanDungBUS instance;
        public static CanhBaoHanDungBUS Instance
        {
            get { if (instance == null) instance = new CanhBaoHanDungBUS(); return instance; }
            private set { instance = value; }
        }

        private CanhBaoHanDungBUS() { }

        public string TaoThongBaoCanhBaoTuDong()
        {
            // Truoc tien tu dong huy canh bao cho cac lo thuoc da het hang
            CanhBaoHanDungDAO.Instance.TuDongHuyCanhBaoKhiHetHang();

            DataTable data = CanhBaoHanDungDAO.Instance.LayDanhSachLoCanKiemTra();
            if (data.Rows.Count == 0)
                return null;

            List<string> danhSachThongBao = new List<string>();

            foreach (DataRow row in data.Rows)
            {
                int maThuoc = Convert.ToInt32(row["MaThuoc"]);
                string tenThuoc = row["TenThuoc"].ToString();
                string soLo = row["SoLo"].ToString();
                DateTime hanSuDung = Convert.ToDateTime(row["HanSuDung"]);
                int soLuongTon = Convert.ToInt32(row["SoLuongTon"]);
                string donViTinh = row.Table.Columns.Contains("DonViTinh") ? Convert.ToString(row["DonViTinh"]) : string.Empty;
                int soNgayConLai = Convert.ToInt32(row["SoNgayConLai"]);

                int? mocCanhBao = XacDinhMocCanhBao(soNgayConLai);
                if (!mocCanhBao.HasValue)
                    continue;

                if (CanhBaoHanDungDAO.Instance.DaCanhBao(maThuoc, soLo, mocCanhBao.Value))
                    continue;

                string dongThongBao;
                if (mocCanhBao.Value == 0)
                {
                    string tonHienThi = string.IsNullOrWhiteSpace(donViTinh)
                        ? soLuongTon.ToString()
                        : soLuongTon + " " + donViTinh;
                    dongThongBao = "- Thuoc: " + tenThuoc + " | Lo: " + soLo + " da het han su dung | Ton (DV nho): " + tonHienThi;
                }
                else
                {
                    dongThongBao = "- Thuoc: " + tenThuoc + " | Lo: " + soLo + " | Con " + soNgayConLai + " ngay den han | HSD: " + hanSuDung.ToString("dd/MM/yyyy");
                }

                danhSachThongBao.Add(dongThongBao);
                CanhBaoHanDungDAO.Instance.ThemCanhBao(maThuoc, soLo, mocCanhBao.Value, dongThongBao);
            }

            if (danhSachThongBao.Count == 0)
                return null;

            StringBuilder thongBao = new StringBuilder();
            thongBao.AppendLine("Canh bao han su dung tu dong:");
            thongBao.AppendLine();

            foreach (string dong in danhSachThongBao)
                thongBao.AppendLine(dong);

            thongBao.AppendLine();
            thongBao.AppendLine("Khuyen nghi: kiem tra tab Canh bao han dung trong muc Xem Ton Kho.");

            return thongBao.ToString();
        }

        private int? XacDinhMocCanhBao(int soNgayConLai)
        {
            if (soNgayConLai <= 0)
                return 0;

            if (soNgayConLai <= 3)
                return 3;

            if (soNgayConLai <= 7)
                return 7;

            return null;
        }

        /// <summary>
        /// Lay lich su canh bao tu bang CanhBaoHanDung, co ho tro loc.
        /// daXuLy = null: tat ca | true: da xu ly | false: chua xu ly
        /// </summary>
        public DataTable LayLichSuCanhBao(string tuKhoa, bool? daXuLy)
        {
            return CanhBaoHanDungDAO.Instance.LayLichSuCanhBao(tuKhoa, daXuLy);
        }
    }
}
