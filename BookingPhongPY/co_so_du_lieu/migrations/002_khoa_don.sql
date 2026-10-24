-- Khoa don dat phong: chup gia, han giu cho, chan chong ngay.

CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE dat_phong ADD COLUMN IF NOT EXISTS gia_thue_chot DECIMAL(12,2);
ALTER TABLE dat_phong ADD COLUMN IF NOT EXISTS het_han_giu TIMESTAMP;
ALTER TABLE dat_phong ADD COLUMN IF NOT EXISTS ngay_ket_thuc_du_kien DATE;

CREATE INDEX IF NOT EXISTS idx_dat_phong_phong_trang ON dat_phong (phong_id, trang_thai);
CREATE INDEX IF NOT EXISTS idx_dat_phong_het_han ON dat_phong (het_han_giu);

CREATE UNIQUE INDEX IF NOT EXISTS uq_dat_phong_dang_mo
    ON dat_phong (phong_id, nguoi_dung_id)
    WHERE trang_thai = 'cho_xac_nhan';

ALTER TABLE dat_phong DROP CONSTRAINT IF EXISTS ex_dat_phong_chong_ngay;
ALTER TABLE dat_phong ADD CONSTRAINT ex_dat_phong_chong_ngay
    EXCLUDE USING gist (
        phong_id WITH =,
        daterange(ngay_chuyen_vao, ngay_ket_thuc_du_kien, '[)') WITH &&
    )
    WHERE (
        trang_thai IN ('cho_xac_nhan', 'da_xac_nhan')
        AND ngay_chuyen_vao IS NOT NULL
        AND ngay_ket_thuc_du_kien IS NOT NULL
    );
