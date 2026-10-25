using System;
using System.Data;
using QuanLyQuayThuoc.DAO;

namespace QuanLyQuayThuoc.BUS
{
    public class BaoCaoBUS
    {
        private static BaoCaoBUS instance;
        public static BaoCaoBUS Instance
        {
            get { if (instance == null) instance = new BaoCaoBUS(); return instance; }
            private set { instance = value; }
        }

        private BaoCaoBUS() { }

        public DataTable LayChiTietHoaDonTheoNgay(DateTime tuNgay, DateTime denNgay)
        {
            return BaoCaoDAO.Instance.LayChiTietHoaDonTheoNgay(tuNgay, denNgay);
        }

        public decimal LayTongDoanhThu(DateTime tuNgay, DateTime denNgay)
        {
            return Convert.ToDecimal(BaoCaoDAO.Instance.LayTongDoanhThu(tuNgay, denNgay));
        }

        public int LaySoHoaDon(DateTime tuNgay, DateTime denNgay)
        {
            return Convert.ToInt32(BaoCaoDAO.Instance.LaySoHoaDon(tuNgay, denNgay));
        }

        public DataTable LayThuocBanChay(DateTime tuNgay, DateTime denNgay)
        {
            return BaoCaoDAO.Instance.LayThuocBanChay(tuNgay, denNgay);
        }
    }
}
