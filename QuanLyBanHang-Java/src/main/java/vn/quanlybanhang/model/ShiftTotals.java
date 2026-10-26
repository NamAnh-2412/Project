package vn.quanlybanhang.model;

import java.math.BigDecimal;

public class ShiftTotals {

    private BigDecimal tienMat = BigDecimal.ZERO;
    private BigDecimal tienChuyenKhoan = BigDecimal.ZERO;
    private BigDecimal tienThe = BigDecimal.ZERO;
    private BigDecimal tienKhac = BigDecimal.ZERO;
    private int soDon;
    private BigDecimal tienKyVong = BigDecimal.ZERO;

    public BigDecimal getTienMat() {
        return tienMat;
    }

    public void setTienMat(BigDecimal tienMat) {
        this.tienMat = tienMat;
    }

    public BigDecimal getTienChuyenKhoan() {
        return tienChuyenKhoan;
    }

    public void setTienChuyenKhoan(BigDecimal tienChuyenKhoan) {
        this.tienChuyenKhoan = tienChuyenKhoan;
    }

    public BigDecimal getTienThe() {
        return tienThe;
    }

    public void setTienThe(BigDecimal tienThe) {
        this.tienThe = tienThe;
    }

    public BigDecimal getTienKhac() {
        return tienKhac;
    }

    public void setTienKhac(BigDecimal tienKhac) {
        this.tienKhac = tienKhac;
    }

    public int getSoDon() {
        return soDon;
    }

    public void setSoDon(int soDon) {
        this.soDon = soDon;
    }

    public BigDecimal getTienKyVong() {
        return tienKyVong;
    }

    public void setTienKyVong(BigDecimal tienKyVong) {
        this.tienKyVong = tienKyVong;
    }
}
