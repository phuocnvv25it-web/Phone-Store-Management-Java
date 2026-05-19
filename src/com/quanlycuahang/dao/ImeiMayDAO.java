package com.quanlycuahang.dao;

import com.quanlycuahang.config.DatabaseConnection;
import com.quanlycuahang.model.ImeiMay;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ImeiMayDAO {

    public boolean them(ImeiMay item) throws SQLException {
        String sql = "INSERT INTO ImeiMay (IMEI, MaMay, TinhTrang, BaoHanhThang, GhiChu) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, item.getImei());
            ps.setNString(2, item.getMaMay());
            ps.setNString(3, item.getTinhTrang());
            ps.setInt(4, item.getBaoHanhThang());
            ps.setNString(5, item.getGhiChu());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean capNhat(ImeiMay item) throws SQLException {
        String sql = "UPDATE ImeiMay SET MaMay=?, TinhTrang=?, BaoHanhThang=?, GhiChu=? WHERE IMEI=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, item.getMaMay());
            ps.setNString(2, item.getTinhTrang());
            ps.setInt(3, item.getBaoHanhThang());
            ps.setNString(4, item.getGhiChu());
            ps.setNString(5, item.getImei());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean capNhatTinhTrang(String imei, String tinhTrang) throws SQLException {
        if (imei == null || imei.trim().isEmpty()) return false;
        String sql = "UPDATE ImeiMay SET TinhTrang=? WHERE IMEI=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, tinhTrang);
            ps.setNString(2, imei.trim());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean xoa(String imei) throws SQLException {
        String sql = "DELETE FROM ImeiMay WHERE IMEI=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, imei);
            return ps.executeUpdate() > 0;
        }
    }

    public List<ImeiMay> layTatCa() throws SQLException {
        List<ImeiMay> list = new ArrayList<>();
        String sql = baseSelect() + " ORDER BY I.NgayNhap DESC, DT.TenMay";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<ImeiMay> timKiem(String tuKhoa) throws SQLException {
        List<ImeiMay> list = new ArrayList<>();
        String sql = baseSelect()
                   + " WHERE I.IMEI LIKE ? OR I.MaMay LIKE ? OR DT.TenMay LIKE ? OR I.MaHD LIKE ? "
                   + " ORDER BY I.NgayNhap DESC";
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

    public int demTheoMaMay(String maMay) throws SQLException {
        String sql = "SELECT COUNT(*) FROM ImeiMay WHERE MaMay=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, maMay);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public List<String> layTrongKhoTheoMaMay(String maMay, int soLuong) throws SQLException {
        List<String> list = new ArrayList<>();
        int limit = Math.max(1, soLuong);
        String sql = "SELECT TOP " + limit + " IMEI FROM ImeiMay "
                   + "WHERE MaMay=? AND TinhTrang=N'TrongKho' ORDER BY NgayNhap";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, maMay);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rs.getNString("IMEI"));
            }
        }
        return list;
    }

    public void danhDauDaBan(String maHD, List<String> imeis) throws SQLException {
        if (imeis == null || imeis.isEmpty()) return;
        String sql = "UPDATE ImeiMay SET TinhTrang=N'DaBan', MaHD=?, NgayBan=GETDATE() "
                   + "WHERE IMEI=? AND TinhTrang=N'TrongKho'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String imei : imeis) {
                ps.setNString(1, maHD);
                ps.setNString(2, imei);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private String baseSelect() {
        return "SELECT I.IMEI, I.MaMay, DT.TenMay, I.TinhTrang, I.MaHD, "
             + "I.BaoHanhThang, I.NgayNhap, I.NgayBan, I.GhiChu "
             + "FROM ImeiMay I JOIN DienThoai DT ON I.MaMay = DT.MaMay";
    }

    private ImeiMay map(ResultSet rs) throws SQLException {
        ImeiMay item = new ImeiMay();
        item.setImei(rs.getNString("IMEI"));
        item.setMaMay(rs.getNString("MaMay"));
        item.setTenMay(rs.getNString("TenMay"));
        item.setTinhTrang(rs.getNString("TinhTrang"));
        item.setMaHD(rs.getNString("MaHD"));
        item.setBaoHanhThang(rs.getInt("BaoHanhThang"));
        item.setGhiChu(rs.getNString("GhiChu"));
        Timestamp ngayNhap = rs.getTimestamp("NgayNhap");
        Timestamp ngayBan = rs.getTimestamp("NgayBan");
        if (ngayNhap != null) item.setNgayNhap(ngayNhap.toLocalDateTime());
        if (ngayBan != null) item.setNgayBan(ngayBan.toLocalDateTime());
        return item;
    }
}
