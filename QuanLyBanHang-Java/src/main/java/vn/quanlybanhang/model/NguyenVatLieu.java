package vn.quanlybanhang.model;

import java.math.BigDecimal;

public class NguyenVatLieu {

    private int id;
    private String maNguyenLieu;
    private String tenNguyenLieu;
    private String danhMucNl;
    private String donViTinh;
    private String trangThai;
    private BigDecimal soLuongTon;
    private BigDecimal soLuongToiThieu;
    private BigDecimal giaNhap;
    /** Lien ket NCC (neu bang co cot nha_cung_cap_id). */
    private Integer nhaCungCapId;
    /** Doc tu JOIN, chi hien thi danh sach. */
    private String tenNhaCungCap;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMaNguyenLieu() {
        return maNguyenLieu;
    }

    public void setMaNguyenLieu(String maNguyenLieu) {
        this.maNguyenLieu = maNguyenLieu;
    }

    public String getTenNguyenLieu() {
        return tenNguyenLieu;
    }

    public void setTenNguyenLieu(String tenNguyenLieu) {
        this.tenNguyenLieu = tenNguyenLieu;
    }

    public String getDanhMucNl() {
        return danhMucNl;
    }

    public void setDanhMucNl(String danhMucNl) {
        this.danhMucNl = danhMucNl;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public BigDecimal getSoLuongTon() {
        return soLuongTon;
    }

    public void setSoLuongTon(BigDecimal soLuongTon) {
        this.soLuongTon = soLuongTon;
    }

    public BigDecimal getSoLuongToiThieu() {
        return soLuongToiThieu;
    }

    public void setSoLuongToiThieu(BigDecimal soLuongToiThieu) {
        this.soLuongToiThieu = soLuongToiThieu;
    }

    public BigDecimal getGiaNhap() {
        return giaNhap;
    }

    public void setGiaNhap(BigDecimal giaNhap) {
        this.giaNhap = giaNhap;
    }

    public Integer getNhaCungCapId() {
        return nhaCungCapId;
    }

    public void setNhaCungCapId(Integer nhaCungCapId) {
        this.nhaCungCapId = nhaCungCapId;
    }

    public String getTenNhaCungCap() {
        return tenNhaCungCap;
    }

    public void setTenNhaCungCap(String tenNhaCungCap) {
        this.tenNhaCungCap = tenNhaCungCap;
    }
}
