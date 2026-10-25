-- Tao bang CanhBaoHanDung neu chua ton tai
IF NOT EXISTS (
    SELECT * FROM INFORMATION_SCHEMA.TABLES 
    WHERE TABLE_NAME = 'CanhBaoHanDung'
)
BEGIN
    CREATE TABLE CanhBaoHanDung (
        Id          INT IDENTITY(1,1) PRIMARY KEY,
        MaThuoc     INT NOT NULL,
        SoLo        NVARCHAR(50) NOT NULL,
        MocCanhBao  INT NOT NULL,          -- So ngay con lai khi canh bao (vd: 7, 30, 90)
        NgayCanhBao DATETIME NOT NULL DEFAULT GETDATE(),
        TrangThai   NVARCHAR(50) NOT NULL DEFAULT 'DaCanhBao',
        GhiChu      NVARCHAR(500) NULL,

        CONSTRAINT FK_CanhBaoHanDung_Thuoc
            FOREIGN KEY (MaThuoc) REFERENCES Thuoc(MaThuoc)
    );

    PRINT N'Da tao bang CanhBaoHanDung thanh cong.';
END
ELSE
BEGIN
    PRINT N'Bang CanhBaoHanDung da ton tai, khong can tao lai.';
END
