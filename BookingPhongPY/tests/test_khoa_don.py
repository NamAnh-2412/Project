"""Tranh chấp đặt phòng trên PostgreSQL thật."""
import os
import threading
import unittest
import uuid
from datetime import date, datetime, timedelta

os.environ['TESTING'] = '1'

from cau_hinh.co_so_du_lieu import get_cursor, get_db_connection
from dich_vu.dat_phong_service import (
    LoiDon,
    ghi_nhan_coc,
    huy_don_qua_han,
    tao_don,
    tao_hop_dong,
    xac_nhan_don,
)


class TestKhoaDon(unittest.TestCase):
    def setUp(self):
        self.hau_to = uuid.uuid4().hex[:10]
        self.conn = get_db_connection()
        cur = get_cursor(self.conn)
        cur.execute(
            """INSERT INTO nguoi_dung (email, mat_khau, ho_ten, vai_tro, email_da_xac_thuc)
               VALUES (%s, 'x', 'Chu test', 'chu_nha', TRUE) RETURNING id""",
            (f'chu-{self.hau_to}@test.local',),
        )
        self.chu = cur.fetchone()['id']
        cur.execute(
            """INSERT INTO nguoi_dung (email, mat_khau, ho_ten, vai_tro, email_da_xac_thuc)
               VALUES (%s, 'x', 'Khach A', 'nguoi_thue', TRUE) RETURNING id""",
            (f'a-{self.hau_to}@test.local',),
        )
        self.khach_a = cur.fetchone()['id']
        cur.execute(
            """INSERT INTO nguoi_dung (email, mat_khau, ho_ten, vai_tro, email_da_xac_thuc)
               VALUES (%s, 'x', 'Khach B', 'nguoi_thue', TRUE) RETURNING id""",
            (f'b-{self.hau_to}@test.local',),
        )
        self.khach_b = cur.fetchone()['id']
        cur.execute(
            """INSERT INTO phong (tieu_de, gia, dia_chi, nguoi_dung_id, trang_thai, trang_thai_duyet, tien_coc)
               VALUES ('Phong test', 2000000, 'Test', %s, 'con_trong', 'da_duyet', 500000)
               RETURNING id""",
            (self.chu,),
        )
        self.phong = cur.fetchone()['id']
        self.conn.commit()

    def tearDown(self):
        cur = get_cursor(self.conn)
        ids = [self.chu, self.khach_a, self.khach_b]
        cur.execute('DELETE FROM thong_bao WHERE nguoi_dung_id = ANY(%s)', (ids,))
        cur.execute('DELETE FROM phong WHERE id = %s', (self.phong,))
        cur.execute('DELETE FROM ma_giam WHERE ma = %s', (f'TEST{self.hau_to}'.upper(),))
        cur.execute('DELETE FROM nguoi_dung WHERE id = ANY(%s)', (ids,))
        self.conn.commit()
        self.conn.close()

    def _dat(self, conn, khach, ngay=None):
        return tao_don(conn, self.phong, khach, '', '0900000000', ngay or date.today(), 3, 1)

    def test_hai_nguoi_chi_mot_don(self):
        bar = threading.Barrier(2)
        ket_qua = []

        def chay(khach):
            conn = get_db_connection()
            bar.wait()
            try:
                don = self._dat(conn, khach)
                ket_qua.append(('ok', don['id']))
            except LoiDon as exc:
                ket_qua.append(('loi', exc.message))
            finally:
                conn.close()

        luong = [
            threading.Thread(target=chay, args=(self.khach_a,)),
            threading.Thread(target=chay, args=(self.khach_b,)),
        ]
        for t in luong:
            t.start()
        for t in luong:
            t.join()
        self.assertEqual(sum(1 for loai, _ in ket_qua if loai == 'ok'), 1)
        self.assertTrue(any(loai == 'loi' and 'giữ' in text for loai, text in ket_qua))

    def test_cung_khach_dat_lan_hai(self):
        don = self._dat(self.conn, self.khach_a)
        with self.assertRaises(LoiDon) as ctx:
            self._dat(self.conn, self.khach_a)
        self.assertEqual(ctx.exception.don_cu_id, don['id'])

    def test_hop_dong_khong_de_don_nguoi_khac(self):
        don = self._dat(self.conn, self.khach_a)
        xac_nhan_don(self.conn, don['id'], self.chu)
        with self.assertRaises(LoiDon):
            tao_hop_dong(
                self.conn, self.chu, self.phong, self.khach_b,
                date.today(), None, 2000000, 500000,
            )

    def test_ma_giam_khong_tru_coc(self):
        cur = get_cursor(self.conn)
        cur.execute(
            """INSERT INTO ma_giam (ma, loai, gia_tri, so_lan_toi_da)
               VALUES (%s, 'phan_tram', 10, 5)""",
            (f'TEST{self.hau_to}'.upper(),),
        )
        self.conn.commit()
        don = tao_don(
            self.conn, self.phong, self.khach_a, '', '0900000000',
            date.today(), 2, 1, f'TEST{self.hau_to}',
        )
        self.assertEqual(float(don['tien_coc_phai_tra']), 500000)
        self.assertEqual(float(don['gia_thue_chot']), 2000000)
        self.assertEqual(float(don['giam_thang_dau']), 200000)

    def test_xac_nhan_coc_mot_lan(self):
        don = self._dat(self.conn, self.khach_a)
        lan_1, moi_1 = ghi_nhan_coc(self.conn, don['id'])
        lan_2, moi_2 = ghi_nhan_coc(self.conn, don['id'])
        self.assertTrue(moi_1)
        self.assertFalse(moi_2)
        self.assertEqual(lan_1['trang_thai_thanh_toan'], 'da_dat_coc')
        self.assertEqual(lan_2['trang_thai_thanh_toan'], 'da_dat_coc')
        cur = get_cursor(self.conn)
        cur.execute(
            """SELECT COUNT(*) AS c FROM giao_dich_thanh_toan
               WHERE dat_phong_id = %s AND loai = 'coc' AND trang_thai <> 'da_huy'""",
            (don['id'],),
        )
        self.assertEqual(cur.fetchone()['c'], 1)

    def test_don_qua_han_tu_huy(self):
        don = self._dat(self.conn, self.khach_a)
        cur = get_cursor(self.conn)
        cur.execute(
            'UPDATE dat_phong SET het_han_giu = %s WHERE id = %s',
            (datetime.now() - timedelta(minutes=5), don['id']),
        )
        self.conn.commit()
        so = huy_don_qua_han(self.conn)
        self.assertGreaterEqual(so, 1)
        cur.execute('SELECT trang_thai, ly_do_huy FROM dat_phong WHERE id = %s', (don['id'],))
        row = cur.fetchone()
        self.assertEqual(row['trang_thai'], 'da_huy')
        self.assertEqual(row['ly_do_huy'], 'het_han')


if __name__ == '__main__':
    unittest.main()
