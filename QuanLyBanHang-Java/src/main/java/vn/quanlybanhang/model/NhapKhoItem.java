package vn.quanlybanhang.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class NhapKhoItem {

    private int id;
    private int nguyenVatLieuId;
    private String tenNguyenVatLieu;
    private BigDecimal soLuongNhap;
    private String donViTinh;
    private BigDecimal donGia;
    private BigDecimal thanhTien;
    private LocalDate hanSuDung;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNguyenVatLieuId() {
        return nguyenVatLieuId;
    }

    public void setNguyenVatLieuId(int nguyenVatLieuId) {
        this.nguyenVatLieuId = nguyenVatLieuId;
    }

    public String getTenNguyenVatLieu() {
        return tenNguyenVatLieu;
    }

    public void setTenNguyenVatLieu(String tenNguyenVatLieu) {
        this.tenNguyenVatLieu = tenNguyenVatLieu;
    }

    public BigDecimal getSoLuongNhap() {
        return soLuongNhap;
    }

    public void setSoLuongNhap(BigDecimal soLuongNhap) {
        this.soLuongNhap = soLuongNhap;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
    }

    public BigDecimal getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(BigDecimal thanhTien) {
        this.thanhTien = thanhTien;
    }

    public LocalDate getHanSuDung() {
        return hanSuDung;
    }

    public void setHanSuDung(LocalDate hanSuDung) {
        this.hanSuDung = hanSuDung;
    }
}
