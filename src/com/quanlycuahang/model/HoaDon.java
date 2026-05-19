package com.quanlycuahang.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Model tuong ung voi bang HoaDon + ChiTietHoaDon.
 */
public class HoaDon {

    private String maHD;
    private String maNV;
    private String hoTenNV;          // join de hien thi
    private String tenKhachHang;
    private String sdtKhach;
    private BigDecimal tongTien;
    private LocalDateTime ngayBan;
    private String ghiChu;

    // Danh sach chi tiet san pham trong hoa don
    private List<ChiTietHoaDon> danhSachChiTiet = new ArrayList<>();

    // ----------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------

    public HoaDon() {}

    public HoaDon(String maHD, String maNV, String tenKhachHang,
                  String sdtKhach, String ghiChu) {
        this.maHD          = maHD;
        this.maNV          = maNV;
        this.tenKhachHang  = tenKhachHang;
        this.sdtKhach      = sdtKhach;
        this.ghiChu        = ghiChu;
        this.tongTien      = BigDecimal.ZERO;
    }

    // ----------------------------------------------------------------
    // Getters & Setters
    // ----------------------------------------------------------------

    public String getMaHD()                            { return maHD; }
    public void setMaHD(String maHD)                   { this.maHD = maHD; }

    public String getMaNV()                            { return maNV; }
    public void setMaNV(String maNV)                   { this.maNV = maNV; }

    public String getHoTenNV()                         { return hoTenNV; }
    public void setHoTenNV(String hoTenNV)             { this.hoTenNV = hoTenNV; }

    public String getTenKhachHang()                    { return tenKhachHang; }
    public void setTenKhachHang(String tenKhachHang)   { this.tenKhachHang = tenKhachHang; }

    public String getSdtKhach()                        { return sdtKhach; }
    public void setSdtKhach(String sdtKhach)           { this.sdtKhach = sdtKhach; }

    public BigDecimal getTongTien()                    { return tongTien; }
    public void setTongTien(BigDecimal tongTien)       { this.tongTien = tongTien; }

    public LocalDateTime getNgayBan()                  { return ngayBan; }
    public void setNgayBan(LocalDateTime ngayBan)      { this.ngayBan = ngayBan; }

    public String getGhiChu()                          { return ghiChu; }
    public void setGhiChu(String ghiChu)               { this.ghiChu = ghiChu; }

    public List<ChiTietHoaDon> getDanhSachChiTiet()    { return danhSachChiTiet; }
    public void setDanhSachChiTiet(List<ChiTietHoaDon> ds) { this.danhSachChiTiet = ds; }

    // ----------------------------------------------------------------
    // Utility
    // ----------------------------------------------------------------

    /** Tinh lai tong tien tu danh sach chi tiet */
    public void tinhLaiTongTien() {
        this.tongTien = danhSachChiTiet.stream()
            .map(ChiTietHoaDon::getThanhTien)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void themChiTiet(ChiTietHoaDon ct) {
        danhSachChiTiet.add(ct);
        tinhLaiTongTien();
    }

    public Object[] toTableRow() {
        return new Object[]{
            maHD,
            hoTenNV != null ? hoTenNV : maNV,
            tenKhachHang != null ? tenKhachHang : "Khach le",
            sdtKhach != null ? sdtKhach : "",
            String.format("%,.0f VND", tongTien),
            ngayBan != null ? ngayBan.toString().replace("T", " ").substring(0, 19) : ""
        };
    }
}
