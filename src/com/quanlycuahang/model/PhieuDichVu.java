package com.quanlycuahang.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PhieuDichVu {
    private String maPhieu;
    private String loai;
    private String imei;
    private String tenMay;
    private String tenKhachHang;
    private String sdtKhach;
    private LocalDateTime ngayNhan;
    private LocalDate henTra;
    private String trangThai;
    private BigDecimal chiPhi = BigDecimal.ZERO;
    private String ghiChu;

    public PhieuDichVu() {}

    public PhieuDichVu(String maPhieu, String loai, String imei, String tenKhachHang,
                       String sdtKhach, LocalDate henTra, String trangThai,
                       BigDecimal chiPhi, String ghiChu) {
        this.maPhieu = maPhieu;
        this.loai = loai;
        this.imei = imei;
        this.tenKhachHang = tenKhachHang;
        this.sdtKhach = sdtKhach;
        this.henTra = henTra;
        this.trangThai = trangThai;
        this.chiPhi = chiPhi;
        this.ghiChu = ghiChu;
    }

    public String getMaPhieu() { return maPhieu; }
    public void setMaPhieu(String maPhieu) { this.maPhieu = maPhieu; }

    public String getLoai() { return loai; }
    public void setLoai(String loai) { this.loai = loai; }

    public String getImei() { return imei; }
    public void setImei(String imei) { this.imei = imei; }

    public String getTenMay() { return tenMay; }
    public void setTenMay(String tenMay) { this.tenMay = tenMay; }

    public String getTenKhachHang() { return tenKhachHang; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }

    public String getSdtKhach() { return sdtKhach; }
    public void setSdtKhach(String sdtKhach) { this.sdtKhach = sdtKhach; }

    public LocalDateTime getNgayNhan() { return ngayNhan; }
    public void setNgayNhan(LocalDateTime ngayNhan) { this.ngayNhan = ngayNhan; }

    public LocalDate getHenTra() { return henTra; }
    public void setHenTra(LocalDate henTra) { this.henTra = henTra; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public BigDecimal getChiPhi() { return chiPhi; }
    public void setChiPhi(BigDecimal chiPhi) { this.chiPhi = chiPhi; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public Object[] toTableRow() {
        return new Object[]{
            maPhieu,
            hienThiLoai(loai),
            imei != null ? imei : "",
            tenMay != null ? tenMay : "",
            tenKhachHang,
            sdtKhach != null ? sdtKhach : "",
            ngayNhan != null ? ngayNhan.toLocalDate().toString() : "",
            henTra != null ? henTra.toString() : "",
            hienThiTrangThai(trangThai),
            String.format("%,.0f VND", chiPhi),
            ghiChu != null ? ghiChu : ""
        };
    }

    public static String hienThiLoai(String value) {
        if ("BaoHanh".equals(value)) return "Bảo hành";
        if ("SuaChua".equals(value)) return "Sửa chữa";
        return value != null ? value : "";
    }

    public static String hienThiTrangThai(String value) {
        if ("DangXuLy".equals(value)) return "Đang xử lý";
        if ("ChoLinhKien".equals(value)) return "Chờ linh kiện";
        if ("HoanThanh".equals(value)) return "Hoàn thành";
        if ("DaTra".equals(value)) return "Đã trả";
        return value != null ? value : "";
    }
}
