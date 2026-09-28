package vn.quanlybanhang.model;

public class DanhMuc {

    private int id;
    private String tenDanhMuc;
    private String moTa;
    private Integer danhMucCha;
    private Integer thuTu;
    private boolean dangHoatDong;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTenDanhMuc() {
        return tenDanhMuc;
    }

    public void setTenDanhMuc(String tenDanhMuc) {
        this.tenDanhMuc = tenDanhMuc;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public Integer getDanhMucCha() {
        return danhMucCha;
    }

    public void setDanhMucCha(Integer danhMucCha) {
        this.danhMucCha = danhMucCha;
    }

    public Integer getThuTu() {
        return thuTu;
    }

    public void setThuTu(Integer thuTu) {
        this.thuTu = thuTu;
    }

    public boolean isDangHoatDong() {
        return dangHoatDong;
    }

    public void setDangHoatDong(boolean dangHoatDong) {
        this.dangHoatDong = dangHoatDong;
    }
}
