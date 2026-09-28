package vn.quanlybanhang.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class KhachHang {

    private int id;
    private String tenKhachHang;
    private String soDienThoai;
    private String email;
    private String diaChi;
    private LocalDate ngaySinh;
    private String ghiChu;
    private String loaiThanhVien;
    private int tongDonHang;
    private BigDecimal tongTienDaChi;
    private LocalDateTime ngayTao;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getLoaiThanhVien() {
        return loaiThanhVien;
    }

    public void setLoaiThanhVien(String loaiThanhVien) {
        this.loaiThanhVien = loaiThanhVien;
    }

    public int getTongDonHang() {
        return tongDonHang;
    }

    public void setTongDonHang(int tongDonHang) {
        this.tongDonHang = tongDonHang;
    }

    public BigDecimal getTongTienDaChi() {
        return tongTienDaChi;
    }

    public void setTongTienDaChi(BigDecimal tongTienDaChi) {
        this.tongTienDaChi = tongTienDaChi;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }
}
