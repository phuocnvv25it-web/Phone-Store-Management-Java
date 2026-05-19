package com.quanlycuahang.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Model tuong ung voi bang NhanVien trong SQL Server.
 */
public class NhanVien {

    private String maNV;
    private String hoTen;
    private String chucVu;
    private String soDienThoai;
    private String diaChi;
    private BigDecimal luongCoBan;
    private LocalDate ngayVaoLam;

    // ----------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------

    public NhanVien() {}

    public NhanVien(String maNV, String hoTen, String chucVu,
                    String soDienThoai, String diaChi,
                    BigDecimal luongCoBan, LocalDate ngayVaoLam) {
        this.maNV         = maNV;
        this.hoTen        = hoTen;
        this.chucVu       = chucVu;
        this.soDienThoai  = soDienThoai;
        this.diaChi       = diaChi;
        this.luongCoBan   = luongCoBan;
        this.ngayVaoLam   = ngayVaoLam;
    }

    /** Constructor tao moi — khong can ngayVaoLam */
    public NhanVien(String maNV, String hoTen, String chucVu,
                    String soDienThoai, String diaChi, BigDecimal luongCoBan) {
        this(maNV, hoTen, chucVu, soDienThoai, diaChi, luongCoBan, LocalDate.now());
    }

    // ----------------------------------------------------------------
    // Getters & Setters
    // ----------------------------------------------------------------

    public String getMaNV()                          { return maNV; }
    public void setMaNV(String maNV)                 { this.maNV = maNV; }

    public String getHoTen()                         { return hoTen; }
    public void setHoTen(String hoTen)               { this.hoTen = hoTen; }

    public String getChucVu()                        { return chucVu; }
    public void setChucVu(String chucVu)             { this.chucVu = chucVu; }

    public String getSoDienThoai()                   { return soDienThoai; }
    public void setSoDienThoai(String sdt)           { this.soDienThoai = sdt; }

    public String getDiaChi()                        { return diaChi; }
    public void setDiaChi(String diaChi)             { this.diaChi = diaChi; }

    public BigDecimal getLuongCoBan()                { return luongCoBan; }
    public void setLuongCoBan(BigDecimal luongCoBan) { this.luongCoBan = luongCoBan; }

    public LocalDate getNgayVaoLam()                 { return ngayVaoLam; }
    public void setNgayVaoLam(LocalDate ngayVaoLam)  { this.ngayVaoLam = ngayVaoLam; }

    // ----------------------------------------------------------------
    // Utility
    // ----------------------------------------------------------------

    /** Chuyen thanh mang de hien thi trong JTable (NhanVien) */
    public Object[] toTableRow() {
        return new Object[]{
            maNV,
            hoTen,
            chucVu,
            soDienThoai != null ? soDienThoai : "",
            diaChi != null ? diaChi : "",
            String.format("%,.0f VND", luongCoBan),
            ngayVaoLam != null ? ngayVaoLam.toString() : ""
        };
    }

    @Override
    public String toString() {
        return hoTen + " [" + maNV + "] - " + chucVu;
    }
}
