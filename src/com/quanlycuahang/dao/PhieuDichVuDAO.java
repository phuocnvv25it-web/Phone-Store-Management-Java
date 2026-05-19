package com.quanlycuahang.dao;

import com.quanlycuahang.config.DatabaseConnection;
import com.quanlycuahang.model.PhieuDichVu;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PhieuDichVuDAO {

    public boolean them(PhieuDichVu phieu) throws SQLException {
        String sql = "INSERT INTO PhieuDichVu (MaPhieu, Loai, IMEI, TenKhachHang, SDTKhach, HenTra, TrangThai, ChiPhi, GhiChu) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, phieu.getMaPhieu());
            ps.setNString(2, phieu.getLoai());
            ps.setNString(3, blankToNull(phieu.getImei()));
            ps.setNString(4, phieu.getTenKhachHang());
            ps.setNString(5, blankToNull(phieu.getSdtKhach()));
            if (phieu.getHenTra() != null) ps.setDate(6, Date.valueOf(phieu.getHenTra()));
            else ps.setNull(6, Types.DATE);
            ps.setNString(7, phieu.getTrangThai());
            ps.setBigDecimal(8, phieu.getChiPhi());
            ps.setNString(9, phieu.getGhiChu());
            boolean ok = ps.executeUpdate() > 0;
            if (ok) capNhatTinhTrangImei(phieu.getImei(), trangThaiImeiTheoLoai(phieu.getLoai()));
            return ok;
        }
    }

    public boolean capNhat(PhieuDichVu phieu) throws SQLException {
        String sql = "UPDATE PhieuDichVu SET Loai=?, IMEI=?, TenKhachHang=?, SDTKhach=?, HenTra=?, "
                   + "TrangThai=?, ChiPhi=?, GhiChu=? WHERE MaPhieu=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, phieu.getLoai());
            ps.setNString(2, blankToNull(phieu.getImei()));
            ps.setNString(3, phieu.getTenKhachHang());
            ps.setNString(4, blankToNull(phieu.getSdtKhach()));
            if (phieu.getHenTra() != null) ps.setDate(5, Date.valueOf(phieu.getHenTra()));
            else ps.setNull(5, Types.DATE);
            ps.setNString(6, phieu.getTrangThai());
            ps.setBigDecimal(7, phieu.getChiPhi());
            ps.setNString(8, phieu.getGhiChu());
            ps.setNString(9, phieu.getMaPhieu());
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                String imeiStatus = "HoanThanh".equals(phieu.getTrangThai()) || "DaTra".equals(phieu.getTrangThai())
                    ? "DaBan" : trangThaiImeiTheoLoai(phieu.getLoai());
                capNhatTinhTrangImei(phieu.getImei(), imeiStatus);
            }
            return ok;
        }
    }

    public boolean xoa(String maPhieu) throws SQLException {
        String sql = "DELETE FROM PhieuDichVu WHERE MaPhieu=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, maPhieu);
            return ps.executeUpdate() > 0;
        }
    }

    public List<PhieuDichVu> layTatCa() throws SQLException {
        List<PhieuDichVu> list = new ArrayList<>();
        String sql = baseSelect() + " ORDER BY P.NgayNhan DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<PhieuDichVu> timKiem(String tuKhoa) throws SQLException {
        List<PhieuDichVu> list = new ArrayList<>();
        String sql = baseSelect()
                   + " WHERE P.MaPhieu LIKE ? OR P.IMEI LIKE ? OR P.TenKhachHang LIKE ? OR P.SDTKhach LIKE ? "
                   + " ORDER BY P.NgayNhan DESC";
        String pattern = "%" + tuKhoa + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, pattern);
            ps.setNString(2, pattern);
            ps.setNString(3, pattern);
            ps.setNString(4, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public String sinhMaPhieu(String loai) throws SQLException {
        String prefix = "BaoHanh".equals(loai) ? "BH" : "SC";
        String sql = "SELECT TOP 1 MaPhieu FROM PhieuDichVu WHERE MaPhieu LIKE ? ORDER BY MaPhieu DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String last = rs.getNString("MaPhieu");
                    int next = Integer.parseInt(last.replaceAll("[^0-9]", "")) + 1;
                    return String.format("%s%03d", prefix, next);
                }
            }
        }
        return prefix + "001";
    }

    private void capNhatTinhTrangImei(String imei, String tinhTrang) throws SQLException {
        if (imei == null || imei.trim().isEmpty()) return;
        String sql = "UPDATE ImeiMay SET TinhTrang=? WHERE IMEI=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, tinhTrang);
            ps.setNString(2, imei.trim());
            ps.executeUpdate();
        }
    }

    private String trangThaiImeiTheoLoai(String loai) {
        return "BaoHanh".equals(loai) ? "BaoHanh" : "SuaChua";
    }

    private String baseSelect() {
        return "SELECT P.MaPhieu, P.Loai, P.IMEI, DT.TenMay, P.TenKhachHang, P.SDTKhach, "
             + "P.NgayNhan, P.HenTra, P.TrangThai, P.ChiPhi, P.GhiChu "
             + "FROM PhieuDichVu P "
             + "LEFT JOIN ImeiMay I ON P.IMEI = I.IMEI "
             + "LEFT JOIN DienThoai DT ON I.MaMay = DT.MaMay";
    }

    private String blankToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private PhieuDichVu map(ResultSet rs) throws SQLException {
        PhieuDichVu phieu = new PhieuDichVu();
        phieu.setMaPhieu(rs.getNString("MaPhieu"));
        phieu.setLoai(rs.getNString("Loai"));
        phieu.setImei(rs.getNString("IMEI"));
        phieu.setTenMay(rs.getNString("TenMay"));
        phieu.setTenKhachHang(rs.getNString("TenKhachHang"));
        phieu.setSdtKhach(rs.getNString("SDTKhach"));
        Timestamp ngayNhan = rs.getTimestamp("NgayNhan");
        Date henTra = rs.getDate("HenTra");
        if (ngayNhan != null) phieu.setNgayNhan(ngayNhan.toLocalDateTime());
        if (henTra != null) phieu.setHenTra(henTra.toLocalDate());
        phieu.setTrangThai(rs.getNString("TrangThai"));
        phieu.setChiPhi(rs.getBigDecimal("ChiPhi"));
        phieu.setGhiChu(rs.getNString("GhiChu"));
        return phieu;
    }
}
