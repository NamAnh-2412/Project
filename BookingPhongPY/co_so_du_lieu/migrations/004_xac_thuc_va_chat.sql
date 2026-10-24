-- Xac thuc email tach khoi dau chu nha uy tin. Chat gan them don.

ALTER TABLE nguoi_dung ADD COLUMN IF NOT EXISTS email_da_xac_thuc BOOLEAN DEFAULT FALSE;
ALTER TABLE nguoi_dung ADD COLUMN IF NOT EXISTS email_xac_thuc_token VARCHAR(64);
ALTER TABLE nguoi_dung ADD COLUMN IF NOT EXISTS email_xac_thuc_het_han TIMESTAMP;

UPDATE nguoi_dung
SET email_da_xac_thuc = TRUE
WHERE email_da_xac_thuc IS DISTINCT FROM TRUE
  AND email_xac_thuc_token IS NULL
  AND ngay_tao < TIMESTAMP '2026-10-25 00:00:00';

ALTER TABLE tin_nhan ADD COLUMN IF NOT EXISTS dat_phong_id INTEGER REFERENCES dat_phong(id) ON DELETE SET NULL;
ALTER TABLE tin_nhan ADD COLUMN IF NOT EXISTS la_the BOOLEAN DEFAULT FALSE;
