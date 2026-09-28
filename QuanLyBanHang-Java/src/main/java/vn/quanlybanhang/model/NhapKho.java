package vn.quanlybanhang.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NhapKho {

    private int id;
    private String maPhieuNhap;
    private Integer nhaCungCapId;
    private String tenNhaCungCap;
    private int nhanVienNhap;
    private String tenNhanVienNhap;
    private BigDecimal tongTien;
    private String trangThai;
    private String ghiChu;
    private LocalDateTime ngayNhap;
    private List<NhapKhoItem> items = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMaPhieuNhap() {
        return maPhieuNhap;
    }

    public void setMaPhieuNhap(String maPhieuNhap) {
        this.maPhieuNhap = maPhieuNhap;
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

    public int getNhanVienNhap() {
        return nhanVienNhap;
    }

    public void setNhanVienNhap(int nhanVienNhap) {
        this.nhanVienNhap = nhanVienNhap;
    }

    public String getTenNhanVienNhap() {
        return tenNhanVienNhap;
    }

    public void setTenNhanVienNhap(String tenNhanVienNhap) {
        this.tenNhanVienNhap = tenNhanVienNhap;
    }

    public BigDecimal getTongTien() {
        return tongTien;
    }

    public void setTongTien(BigDecimal tongTien) {
        this.tongTien = tongTien;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public LocalDateTime getNgayNhap() {
        return ngayNhap;
    }

    public void setNgayNhap(LocalDateTime ngayNhap) {
        this.ngayNhap = ngayNhap;
    }

    public List<NhapKhoItem> getItems() {
        return items;
    }

    public void setItems(List<NhapKhoItem> items) {
        this.items = items;
    }
}
