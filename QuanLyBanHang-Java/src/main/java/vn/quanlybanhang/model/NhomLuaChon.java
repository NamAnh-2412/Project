package vn.quanlybanhang.model;

import java.util.ArrayList;
import java.util.List;

public class NhomLuaChon {

    private int id;
    private String tenNhom;
    private String moTa;
    private int thuTu;
    private boolean dangHoatDong;
    private boolean batBuoc;
    private List<LuaChon> luaChonList = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTenNhom() {
        return tenNhom;
    }

    public void setTenNhom(String tenNhom) {
        this.tenNhom = tenNhom;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
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

    public boolean isBatBuoc() {
        return batBuoc;
    }

    public void setBatBuoc(boolean batBuoc) {
        this.batBuoc = batBuoc;
    }

    public List<LuaChon> getLuaChonList() {
        return luaChonList;
    }

    public void setLuaChonList(List<LuaChon> luaChonList) {
        this.luaChonList = luaChonList;
    }
}
