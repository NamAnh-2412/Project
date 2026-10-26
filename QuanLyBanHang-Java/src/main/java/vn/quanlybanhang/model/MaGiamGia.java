package vn.quanlybanhang.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class MaGiamGia {

    private int id;
    private String ma;
    private String loai;
    private BigDecimal giaTri = BigDecimal.ZERO;
    private BigDecimal donToiThieu = BigDecimal.ZERO;
    private BigDecimal giamToiDa;
    private Integer soLanToiDa;
    private int soLanDaDung;
    private LocalDate batDau;
    private LocalDate ketThuc;
    private String trangThai;
    private LocalDateTime ngayTao;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMa() {
        return ma;
    }

    public void setMa(String ma) {
        this.ma = ma;
    }

    public String getLoai() {
        return loai;
    }

    public void setLoai(String loai) {
        this.loai = loai;
    }

    public BigDecimal getGiaTri() {
        return giaTri;
    }

    public void setGiaTri(BigDecimal giaTri) {
        this.giaTri = giaTri;
    }

    public BigDecimal getDonToiThieu() {
        return donToiThieu;
    }

    public void setDonToiThieu(BigDecimal donToiThieu) {
        this.donToiThieu = donToiThieu;
    }

    public BigDecimal getGiamToiDa() {
        return giamToiDa;
    }

    public void setGiamToiDa(BigDecimal giamToiDa) {
        this.giamToiDa = giamToiDa;
    }

    public Integer getSoLanToiDa() {
        return soLanToiDa;
    }

    public void setSoLanToiDa(Integer soLanToiDa) {
        this.soLanToiDa = soLanToiDa;
    }

    public int getSoLanDaDung() {
        return soLanDaDung;
    }

    public void setSoLanDaDung(int soLanDaDung) {
        this.soLanDaDung = soLanDaDung;
    }

    public LocalDate getBatDau() {
        return batDau;
    }

    public void setBatDau(LocalDate batDau) {
        this.batDau = batDau;
    }

    public LocalDate getKetThuc() {
        return ketThuc;
    }

    public void setKetThuc(LocalDate ketThuc) {
        this.ketThuc = ketThuc;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }
}
