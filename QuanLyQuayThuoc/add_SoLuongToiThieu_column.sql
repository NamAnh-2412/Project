-- Them cot SoLuongToiThieu vao bang Thuoc neu chua co
USE QuayThuocDB;
GO

IF NOT EXISTS (
    SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'Thuoc' AND COLUMN_NAME = 'SoLuongToiThieu'
)
BEGIN
    ALTER TABLE Thuoc ADD SoLuongToiThieu INT NOT NULL DEFAULT 10;
    PRINT N'Da them cot SoLuongToiThieu thanh cong.';
END
ELSE
BEGIN
    PRINT N'Cot SoLuongToiThieu da ton tai.';
END
