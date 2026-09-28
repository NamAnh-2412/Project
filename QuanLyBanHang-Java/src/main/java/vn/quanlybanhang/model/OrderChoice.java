package vn.quanlybanhang.model;

import java.math.BigDecimal;

public class OrderChoice {

    private int nhomLuaChonId;
    private int luaChonId;
    private String tenNhom;
    private String tenLuaChon;
    private BigDecimal giaTangThem;

    public int getNhomLuaChonId() {
        return nhomLuaChonId;
    }

    public void setNhomLuaChonId(int nhomLuaChonId) {
        this.nhomLuaChonId = nhomLuaChonId;
    }

    public int getLuaChonId() {
        return luaChonId;
    }

    public void setLuaChonId(int luaChonId) {
        this.luaChonId = luaChonId;
    }

    public String getTenNhom() {
        return tenNhom;
    }

    public void setTenNhom(String tenNhom) {
        this.tenNhom = tenNhom;
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
}
