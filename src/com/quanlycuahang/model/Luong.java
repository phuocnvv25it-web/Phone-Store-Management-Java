package com.quanlycuahang.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Model tuong ung voi bang Luong trong SQL Server.
 * LuongThucNhan la computed column: LuongCoBan + Thuong - Phat
 */
public class Luong {

    private int maLuong;
    private String maNV;
    private String hoTenNV;    // join de hien thi
    private int thang;
    private int nam;
    private BigDecimal luongCoBan;
    private BigDecimal thuong;
    private BigDecimal phat;
    private BigDecimal luongThucNhan; // doc tu DB (computed column)
    private String ghiChu;
    private LocalDateTime ngayTao;

    // ----------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------

    public Luong() {}

    public Luong(String maNV, int thang, int nam,
                 BigDecimal luongCoBan, BigDecimal thuong, BigDecimal phat, String ghiChu) {
        this.maNV       = maNV;
        this.thang      = thang;
        this.nam        = nam;
        this.luongCoBan = luongCoBan;
        this.thuong     = thuong;
        this.phat       = phat;
        this.ghiChu     = ghiChu;
        // Tinh thu cong de hien thi ngay (truoc khi luu DB)
        this.luongThucNhan = luongCoBan.add(thuong).subtract(phat);
    }

    // ----------------------------------------------------------------
    // Getters & Setters
    // ----------------------------------------------------------------

    public int getMaLuong()                          { return maLuong; }
    public void setMaLuong(int maLuong)              { this.maLuong = maLuong; }

    public String getMaNV()                          { return maNV; }
    public void setMaNV(String maNV)                 { this.maNV = maNV; }

    public String getHoTenNV()                       { return hoTenNV; }
    public void setHoTenNV(String hoTenNV)           { this.hoTenNV = hoTenNV; }

    public int getThang()                            { return thang; }
    public void setThang(int thang)                  { this.thang = thang; }

    public int getNam()                              { return nam; }
    public void setNam(int nam)                      { this.nam = nam; }

    public BigDecimal getLuongCoBan()                { return luongCoBan; }
    public void setLuongCoBan(BigDecimal luongCoBan) { this.luongCoBan = luongCoBan; }

    public BigDecimal getThuong()                    { return thuong; }
    public void setThuong(BigDecimal thuong)         { this.thuong = thuong; }

    public BigDecimal getPhat()                      { return phat; }
    public void setPhat(BigDecimal phat)             { this.phat = phat; }

    public BigDecimal getLuongThucNhan()             { return luongThucNhan; }
    public void setLuongThucNhan(BigDecimal l)       { this.luongThucNhan = l; }

    public String getGhiChu()                        { return ghiChu; }
    public void setGhiChu(String ghiChu)             { this.ghiChu = ghiChu; }

    public LocalDateTime getNgayTao()                { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao)    { this.ngayTao = ngayTao; }

    // ----------------------------------------------------------------
    // Utility
    // ----------------------------------------------------------------

    public Object[] toTableRow() {
        return new Object[]{
            maLuong,
            maNV,
            hoTenNV != null ? hoTenNV : "",
            "Thang " + thang + "/" + nam,
            String.format("%,.0f", luongCoBan),
            String.format("%,.0f", thuong),
            String.format("%,.0f", phat),
            String.format("%,.0f VND", luongThucNhan),
            ghiChu != null ? ghiChu : ""
        };
    }
}
