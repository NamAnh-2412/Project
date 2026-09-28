package vn.quanlybanhang.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class KiemKho {

    private int id;
    private String maPhieuKiem;
    private LocalDate ngayKiem;
    private int nhanVienKiem;
    private String tenNhanVien;
    private String trangThai;
    private String ghiChu;
    private LocalDateTime ngayTao;
    private List<KiemKhoChiTiet> items = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMaPhieuKiem() {
        return maPhieuKiem;
    }

    public void setMaPhieuKiem(String maPhieuKiem) {
        this.maPhieuKiem = maPhieuKiem;
    }

    public LocalDate getNgayKiem() {
        return ngayKiem;
    }

    public void setNgayKiem(LocalDate ngayKiem) {
        this.ngayKiem = ngayKiem;
    }

    public int getNhanVienKiem() {
        return nhanVienKiem;
    }

    public void setNhanVienKiem(int nhanVienKiem) {
        this.nhanVienKiem = nhanVienKiem;
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

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public List<KiemKhoChiTiet> getItems() {
        return items;
    }

    public void setItems(List<KiemKhoChiTiet> items) {
        this.items = items;
    }
}
