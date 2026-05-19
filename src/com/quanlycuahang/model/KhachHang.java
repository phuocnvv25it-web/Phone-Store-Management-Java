package com.quanlycuahang.model;

import java.time.LocalDateTime;

public class KhachHang {
    private String maKH;
    private String hoTen;
    private String soDienThoai;
    private String email;
    private String diaChi;
    private int diemTichLuy;
    private String ghiChu;
    private LocalDateTime ngayTao;

    public KhachHang() {}

    public KhachHang(String maKH, String hoTen, String soDienThoai, String email,
                     String diaChi, int diemTichLuy, String ghiChu) {
        this.maKH = maKH;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.diaChi = diaChi;
        this.diemTichLuy = diemTichLuy;
        this.ghiChu = ghiChu;
    }

    public String getMaKH() { return maKH; }
    public void setMaKH(String maKH) { this.maKH = maKH; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public int getDiemTichLuy() { return diemTichLuy; }
    public void setDiemTichLuy(int diemTichLuy) { this.diemTichLuy = diemTichLuy; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }

    public Object[] toTableRow() {
        return new Object[]{
            maKH,
            hoTen,
            soDienThoai,
            email != null ? email : "",
            diaChi != null ? diaChi : "",
            diemTichLuy,
            ghiChu != null ? ghiChu : ""
        };
    }
}
