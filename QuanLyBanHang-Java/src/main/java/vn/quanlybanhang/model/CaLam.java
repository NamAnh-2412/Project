package vn.quanlybanhang.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CaLam {

    private int id;
    private int nhanVienId;
    private String tenNhanVien;
    private String trangThai;
    private BigDecimal tienDauCa = BigDecimal.ZERO;
    private BigDecimal tienMat = BigDecimal.ZERO;
    private BigDecimal tienChuyenKhoan = BigDecimal.ZERO;
    private BigDecimal tienThe = BigDecimal.ZERO;
    private BigDecimal tienKhac = BigDecimal.ZERO;
    private int soDon;
    private BigDecimal tienKyVong = BigDecimal.ZERO;
    private BigDecimal tienThucTe;
    private BigDecimal chenhLech;
    private String ghiChu;
    private LocalDateTime moLuc;
    private LocalDateTime dongLuc;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNhanVienId() {
        return nhanVienId;
    }

    public void setNhanVienId(int nhanVienId) {
        this.nhanVienId = nhanVienId;
    }

    public String getTenNhanVien() {
        return tenNhanVien;
    }

    public void setTenNhanVien(String tenNhanVien) {
        this.tenNhanVien = tenNhanVien;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public BigDecimal getTienDauCa() {
        return tienDauCa;
    }

    public void setTienDauCa(BigDecimal tienDauCa) {
        this.tienDauCa = tienDauCa;
    }

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

    public BigDecimal getTienThucTe() {
        return tienThucTe;
    }

    public void setTienThucTe(BigDecimal tienThucTe) {
        this.tienThucTe = tienThucTe;
    }

    public BigDecimal getChenhLech() {
        return chenhLech;
    }

    public void setChenhLech(BigDecimal chenhLech) {
        this.chenhLech = chenhLech;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public LocalDateTime getMoLuc() {
        return moLuc;
    }

    public void setMoLuc(LocalDateTime moLuc) {
        this.moLuc = moLuc;
    }

    public LocalDateTime getDongLuc() {
        return dongLuc;
    }

    public void setDongLuc(LocalDateTime dongLuc) {
        this.dongLuc = dongLuc;
    }
}
