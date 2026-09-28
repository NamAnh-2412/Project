package vn.quanlybanhang.model;

import java.math.BigDecimal;

public class LuaChon {

    private int id;
    private int nhomLuaChonId;
    private String tenLuaChon;
    private BigDecimal giaTangThem;
    private int thuTu;
    private boolean dangHoatDong;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNhomLuaChonId() {
        return nhomLuaChonId;
    }

    public void setNhomLuaChonId(int nhomLuaChonId) {
        this.nhomLuaChonId = nhomLuaChonId;
    }

    public String getTenLuaChon() {
        return tenLuaChon;
    }

    public void setTenLuaChon(String tenLuaChon) {
        this.tenLuaChon = tenLuaChon;
    }

    public BigDecimal getGiaTangThem() {
        return giaTangThem;
    }

    public void setGiaTangThem(BigDecimal giaTangThem) {
        this.giaTangThem = giaTangThem;
    }

    public int getThuTu() {
        return thuTu;
    }

    public void setThuTu(int thuTu) {
        this.thuTu = thuTu;
    }

    public boolean isDangHoatDong() {
        return dangHoatDong;
    }

    public void setDangHoatDong(boolean dangHoatDong) {
        this.dangHoatDong = dangHoatDong;
    }
}
