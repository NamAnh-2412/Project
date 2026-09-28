package vn.quanlybanhang.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderItem {

    /** id dong chitiet_donhang khi load tu DB (de lay nhom lua chon). */
    private Integer chiTietId;
    private int sanPhamId;
    private String tenSanPham;
    private Integer sizeId;
    private String size;
    private int soLuong;
    private BigDecimal donGia;
    private BigDecimal thanhTien;
    private List<OrderChoice> luaChonList = new ArrayList<>();

    public Integer getChiTietId() {
        return chiTietId;
    }

    public void setChiTietId(Integer chiTietId) {
        this.chiTietId = chiTietId;
    }

    public int getSanPhamId() {
        return sanPhamId;
    }

    public void setSanPhamId(int sanPhamId) {
        this.sanPhamId = sanPhamId;
    }

    public String getTenSanPham() {
        return tenSanPham;
    }

    public void setTenSanPham(String tenSanPham) {
        this.tenSanPham = tenSanPham;
    }

    public Integer getSizeId() {
        return sizeId;
    }

    public void setSizeId(Integer sizeId) {
        this.sizeId = sizeId;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
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

    public List<OrderChoice> getLuaChonList() {
        return luaChonList;
    }

    public void setLuaChonList(List<OrderChoice> luaChonList) {
        this.luaChonList = luaChonList;
    }
}
