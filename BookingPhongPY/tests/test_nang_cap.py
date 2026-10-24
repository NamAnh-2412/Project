import os
import unittest

os.environ['TESTING'] = '1'

from dich_vu.hoa_don_service import tinh_hoa_don
from dich_vu.email_service import mat_khau_smtp
from dich_vu.payment_service import ma_coc, url_vietqr
from phu_tro.bao_mat import lam_sach
from phu_tro.tro_giup import thong_tin_trang


class TestLogic(unittest.TestCase):
    def test_tinh_hoa_don(self):
        kq = tinh_hoa_don(100, 150, 3500, 10, 15, 15000, 2000000, 100000)
        self.assertEqual(kq['dien_tieu_thu'], 50)
        self.assertEqual(kq['nuoc_tieu_thu'], 5)
        self.assertEqual(kq['tong'], 50 * 3500 + 5 * 15000 + 2000000 + 100000)

    def test_so_moi_nho_hon_van_khong_am(self):
        kq = tinh_hoa_don(200, 100, 1000, 20, 10, 1000, 0, 0)
        self.assertEqual(kq['dien_tieu_thu'], 0)
        self.assertEqual(kq['tong'], 0)

    def test_phan_trang(self):
        trang = thong_tin_trang(2, 25, per_page=9)
        self.assertEqual(trang['pages'], 3)
        self.assertEqual(trang['offset'], 9)

    def test_lam_sach(self):
        self.assertEqual(lam_sach('  a\x00b  ', 10), 'ab')

    def test_ma_coc(self):
        self.assertEqual(ma_coc(12), 'COCPHONG12')

    def test_mat_khau_gmail_bo_khoang_trang(self):
        cu = os.environ.get('SMTP_PASSWORD')
        os.environ['SMTP_PASSWORD'] = 'abcd efgh ijkl mnop'
        try:
            self.assertEqual(mat_khau_smtp(), 'abcdefghijklmnop')
        finally:
            if cu is None:
                os.environ.pop('SMTP_PASSWORD', None)
            else:
                os.environ['SMTP_PASSWORD'] = cu

    def test_vietqr_thieu_tai_khoan(self):
        os.environ['BANK_ACCOUNT'] = ''
        self.assertIsNone(url_vietqr(1000, 'COCPHONG1'))


class TestTrang(unittest.TestCase):
    def test_trang_chu(self):
        from app import app
        client = app.test_client()
        res = client.get('/')
        self.assertEqual(res.status_code, 200)
        self.assertIn('Danh sách phòng'.encode(), res.data)

    def test_dang_nhap_thieu_csrf(self):
        from app import app
        client = app.test_client()
        res = client.post('/auth/login', data={'email': 'a@b.c', 'password': 'x', 'login_type': 'user'})
        self.assertEqual(res.status_code, 400)

    def test_dang_nhap_mo_duoc(self):
        from app import app
        client = app.test_client()
        res = client.get('/auth/login')
        self.assertEqual(res.status_code, 200)
        self.assertIn(b'csrf_token', res.data)

    def test_suc_khoe(self):
        from app import app
        client = app.test_client()
        res = client.get('/suc-khoe')
        self.assertEqual(res.status_code, 200)
        self.assertTrue(res.get_json()['ok'])

    def test_webhook_khong_co_bi_mat_bi_chan(self):
        from app import app
        res = app.test_client().post('/thanh-toan/webhook', json={'noi_dung': 'COCPHONG1'})
        self.assertEqual(res.status_code, 403)

    def test_chat_chua_dang_nhap(self):
        from app import app
        res = app.test_client().get('/api/tin-nhan?voi=1&phong_id=1&sau=0')
        self.assertEqual(res.status_code, 401)

    def test_dang_nhap_sai_mat_khau(self):
        import re
        from app import app
        client = app.test_client()
        trang = client.get('/auth/login')
        khoa = re.search(br'name="csrf_token"[^>]*value="([^"]+)"', trang.data)
        self.assertIsNotNone(khoa)
        res = client.post('/auth/login', data={
            'email': 'khong-co-nguoi@test.local',
            'password': 'sai-mat-khau',
            'login_type': 'user',
            'csrf_token': khoa.group(1).decode(),
        })
        self.assertEqual(res.status_code, 200)
        self.assertIn('không đúng'.encode(), res.data)

    def test_khoa_bi_mat_mau_bi_chan(self):
        from cau_hinh.van_hanh import khoa_bi_mat_yeu
        self.assertTrue(khoa_bi_mat_yeu('dev-only-doi-trong-env'))
        self.assertTrue(khoa_bi_mat_yeu('ngan'))
        self.assertFalse(khoa_bi_mat_yeu('day-la-chuoi-bi-mat-du-dai-cho-production'))
