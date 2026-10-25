-- Them cot DaXuLy vao bang CanhBaoHanDung neu chua co
USE QuayThuocDB;
GO

IF NOT EXISTS (
    SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'CanhBaoHanDung' AND COLUMN_NAME = 'DaXuLy'
)
BEGIN
    ALTER TABLE CanhBaoHanDung ADD DaXuLy BIT NOT NULL DEFAULT 0;
    PRINT N'Da them cot DaXuLy thanh cong.';
END
ELSE
BEGIN
    PRINT N'Cot DaXuLy da ton tai.';
END
