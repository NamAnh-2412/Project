package vn.quanlybanhang.model;

import java.math.BigDecimal;

public class KiemKhoChiTiet {

    private int id;
    private int kiemKhoId;
    private int nguyenLieuId;
    private String maNguyenLieu;
    private String tenNguyenLieu;
    private String donViTinh;
    private BigDecimal tonHeThong = BigDecimal.ZERO;
    private BigDecimal tonThucTe = BigDecimal.ZERO;
    private BigDecimal chenhLech = BigDecimal.ZERO;
    private String lyDo;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getKiemKhoId() {
        return kiemKhoId;
    }

    public void setKiemKhoId(int kiemKhoId) {
        this.kiemKhoId = kiemKhoId;
    }

    public int getNguyenLieuId() {
        return nguyenLieuId;
    }

    public void setNguyenLieuId(int nguyenLieuId) {
        this.nguyenLieuId = nguyenLieuId;
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

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public BigDecimal getTonHeThong() {
        return tonHeThong;
    }

    public void setTonHeThong(BigDecimal tonHeThong) {
        this.tonHeThong = tonHeThong;
    }

    public BigDecimal getTonThucTe() {
        return tonThucTe;
    }

    public void setTonThucTe(BigDecimal tonThucTe) {
        this.tonThucTe = tonThucTe;
    }

    public BigDecimal getChenhLech() {
        return chenhLech;
    }

    public void setChenhLech(BigDecimal chenhLech) {
        this.chenhLech = chenhLech;
    }

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }
}
