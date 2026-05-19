package com.quanlycuahang.model;

import java.time.LocalDateTime;

public class ImeiMay {
    private String imei;
    private String maMay;
    private String tenMay;
    private String tinhTrang;
    private String maHD;
    private int baoHanhThang;
    private LocalDateTime ngayNhap;
    private LocalDateTime ngayBan;
    private String ghiChu;

    public ImeiMay() {}

    public ImeiMay(String imei, String maMay, String tinhTrang, int baoHanhThang, String ghiChu) {
        this.imei = imei;
        this.maMay = maMay;
        this.tinhTrang = tinhTrang;
        this.baoHanhThang = baoHanhThang;
        this.ghiChu = ghiChu;
    }

    public String getImei() { return imei; }
    public void setImei(String imei) { this.imei = imei; }

    public String getMaMay() { return maMay; }
    public void setMaMay(String maMay) { this.maMay = maMay; }

    public String getTenMay() { return tenMay; }
    public void setTenMay(String tenMay) { this.tenMay = tenMay; }

    public String getTinhTrang() { return tinhTrang; }
    public void setTinhTrang(String tinhTrang) { this.tinhTrang = tinhTrang; }

    public String getMaHD() { return maHD; }
    public void setMaHD(String maHD) { this.maHD = maHD; }

    public int getBaoHanhThang() { return baoHanhThang; }
    public void setBaoHanhThang(int baoHanhThang) { this.baoHanhThang = baoHanhThang; }

    public LocalDateTime getNgayNhap() { return ngayNhap; }
    public void setNgayNhap(LocalDateTime ngayNhap) { this.ngayNhap = ngayNhap; }

    public LocalDateTime getNgayBan() { return ngayBan; }
    public void setNgayBan(LocalDateTime ngayBan) { this.ngayBan = ngayBan; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public Object[] toTableRow() {
        return new Object[]{
            imei,
            maMay,
            tenMay != null ? tenMay : "",
            hienThiTinhTrang(tinhTrang),
            maHD != null ? maHD : "",
            baoHanhThang,
            ngayBan != null ? ngayBan.toLocalDate().toString() : "",
            ghiChu != null ? ghiChu : ""
        };
    }

    public static String hienThiTinhTrang(String value) {
        if ("TrongKho".equals(value)) return "Trong kho";
        if ("DaBan".equals(value)) return "Đã bán";
        if ("BaoHanh".equals(value)) return "Bảo hành";
        if ("SuaChua".equals(value)) return "Sửa chữa";
        if ("DoiTra".equals(value)) return "Đổi trả";
        return value != null ? value : "";
    }
}
