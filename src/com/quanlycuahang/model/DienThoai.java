package com.quanlycuahang.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Model tuong ung voi bang DienThoai trong SQL Server.
 */
public class DienThoai {

    private String maMay;
    private String tenMay;
    private String hangSX;
    private BigDecimal gia;
    private int soLuong;
    private String moTa;
    private LocalDateTime ngayNhap;

    // ----------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------

    public DienThoai() {}

    /** Constructor day du — dung khi doc tu DB */
    public DienThoai(String maMay, String tenMay, String hangSX,
                     BigDecimal gia, int soLuong, String moTa, LocalDateTime ngayNhap) {
        this.maMay    = maMay;
        this.tenMay   = tenMay;
        this.hangSX   = hangSX;
        this.gia      = gia;
        this.soLuong  = soLuong;
        this.moTa     = moTa;
        this.ngayNhap = ngayNhap;
    }

    /** Constructor tao moi (khong can ngayNhap — DB tu dien GETDATE()) */
    public DienThoai(String maMay, String tenMay, String hangSX,
                     BigDecimal gia, int soLuong, String moTa) {
        this(maMay, tenMay, hangSX, gia, soLuong, moTa, null);
    }

    // ----------------------------------------------------------------
    // Getters & Setters
    // ----------------------------------------------------------------

    public String getMaMay()                     { return maMay; }
    public void setMaMay(String maMay)           { this.maMay = maMay; }

    public String getTenMay()                    { return tenMay; }
    public void setTenMay(String tenMay)         { this.tenMay = tenMay; }

    public String getHangSX()                    { return hangSX; }
    public void setHangSX(String hangSX)         { this.hangSX = hangSX; }

    public BigDecimal getGia()                   { return gia; }
    public void setGia(BigDecimal gia)           { this.gia = gia; }

    public int getSoLuong()                      { return soLuong; }
    public void setSoLuong(int soLuong)          { this.soLuong = soLuong; }

    public String getMoTa()                      { return moTa; }
    public void setMoTa(String moTa)             { this.moTa = moTa; }

    public LocalDateTime getNgayNhap()           { return ngayNhap; }
    public void setNgayNhap(LocalDateTime dt)    { this.ngayNhap = dt; }

    // ----------------------------------------------------------------
    // Utility
    // ----------------------------------------------------------------

    /**
     * Chuyen gia thanh mang de hien thi trong JTable.
     * Thu tu: MaMay, TenMay, HangSX, Gia, SoLuong, MoTa
     */
    public Object[] toTableRow() {
        return new Object[]{
            maMay,
            tenMay,
            hangSX,
            String.format("%,.0f VND", gia),
            soLuong,
            moTa != null ? moTa : ""
        };
    }

    @Override
    public String toString() {
        return tenMay + " (" + maMay + ") - " + String.format("%,.0f", gia) + " VND";
    }
}
