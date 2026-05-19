package com.quanlycuahang.model;

import java.math.BigDecimal;

/**
 * Model tuong ung voi bang ChiTietHoaDon.
 * ThanhTien = SoLuongBan * DonGia (computed column trong DB, tinh thu cong o day)
 */
public class ChiTietHoaDon {

    private int maCTHD;
    private String maHD;
    private String maMay;
    private String tenMay;   // join de hien thi
    private int soLuongBan;
    private BigDecimal donGia;
    private BigDecimal thanhTien; // soLuongBan * donGia

    // ----------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------

    public ChiTietHoaDon() {}

    public ChiTietHoaDon(String maHD, String maMay, String tenMay,
                          int soLuongBan, BigDecimal donGia) {
        this.maHD       = maHD;
        this.maMay      = maMay;
        this.tenMay     = tenMay;
        this.soLuongBan = soLuongBan;
        this.donGia     = donGia;
        this.thanhTien  = donGia.multiply(BigDecimal.valueOf(soLuongBan));
    }

    // ----------------------------------------------------------------
    // Getters & Setters
    // ----------------------------------------------------------------

    public int getMaCTHD()                         { return maCTHD; }
    public void setMaCTHD(int maCTHD)              { this.maCTHD = maCTHD; }

    public String getMaHD()                        { return maHD; }
    public void setMaHD(String maHD)               { this.maHD = maHD; }

    public String getMaMay()                       { return maMay; }
    public void setMaMay(String maMay)             { this.maMay = maMay; }

    public String getTenMay()                      { return tenMay; }
    public void setTenMay(String tenMay)           { this.tenMay = tenMay; }

    public int getSoLuongBan()                     { return soLuongBan; }
    public void setSoLuongBan(int soLuongBan) {
        this.soLuongBan = soLuongBan;
        if (donGia != null) {
            this.thanhTien = donGia.multiply(BigDecimal.valueOf(soLuongBan));
        }
    }

    public BigDecimal getDonGia()                  { return donGia; }
    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
        this.thanhTien = donGia.multiply(BigDecimal.valueOf(soLuongBan));
    }

    public BigDecimal getThanhTien()               { return thanhTien; }
    public void setThanhTien(BigDecimal thanhTien) { this.thanhTien = thanhTien; }

    // ----------------------------------------------------------------
    // Utility
    // ----------------------------------------------------------------

    /** Mang du lieu cho JTable chi tiet hoa don */
    public Object[] toTableRow() {
        return new Object[]{
            maMay,
            tenMay != null ? tenMay : "",
            String.format("%,.0f", donGia),
            soLuongBan,
            String.format("%,.0f VND", thanhTien)
        };
    }
}
