package com.quanlycuahang.dao;

import com.quanlycuahang.config.DatabaseConnection;
import com.quanlycuahang.model.NhanVien;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO xu ly cac thao tac CRUD voi bang NhanVien.
 */
public class NhanVienDAO {

    // ----------------------------------------------------------------
    // CREATE
    // ----------------------------------------------------------------

    public boolean themNhanVien(NhanVien nv) throws SQLException {
        String sql = "INSERT INTO NhanVien (MaNV, HoTen, ChucVu, SoDienThoai, DiaChi, LuongCoBan) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setNString(1, nv.getMaNV());
            ps.setNString(2, nv.getHoTen());
            ps.setNString(3, nv.getChucVu());
            ps.setNString(4, nv.getSoDienThoai());
            ps.setNString(5, nv.getDiaChi());
            ps.setBigDecimal(6, nv.getLuongCoBan());

            return ps.executeUpdate() > 0;
        }
    }

    // ----------------------------------------------------------------
    // READ — Tat ca
    // ----------------------------------------------------------------

    public List<NhanVien> layTatCa() throws SQLException {
        List<NhanVien> ds = new ArrayList<>();
        String sql = "SELECT MaNV, HoTen, ChucVu, SoDienThoai, DiaChi, LuongCoBan, NgayVaoLam "
                   + "FROM NhanVien ORDER BY HoTen";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ds.add(mapResultSet(rs));
            }
        }
        return ds;
    }

    // ----------------------------------------------------------------
    // READ — Tim theo ma
    // ----------------------------------------------------------------

    public NhanVien timTheoMa(String maNV) throws SQLException {
        String sql = "SELECT MaNV, HoTen, ChucVu, SoDienThoai, DiaChi, LuongCoBan, NgayVaoLam "
                   + "FROM NhanVien WHERE MaNV = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setNString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSet(rs);
            }
        }
        return null;
    }

    // ----------------------------------------------------------------
    // READ — Tim kiem
    // ----------------------------------------------------------------

    public List<NhanVien> timKiem(String tuKhoa) throws SQLException {
        List<NhanVien> ds = new ArrayList<>();
        String sql = "SELECT MaNV, HoTen, ChucVu, SoDienThoai, DiaChi, LuongCoBan, NgayVaoLam "
                   + "FROM NhanVien "
                   + "WHERE HoTen LIKE ? OR MaNV LIKE ? OR ChucVu LIKE ? "
                   + "ORDER BY HoTen";
        String pattern = "%" + tuKhoa + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setNString(1, pattern);
            ps.setNString(2, pattern);
            ps.setNString(3, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(mapResultSet(rs));
            }
        }
        return ds;
    }

    // ----------------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------------

    public boolean capNhat(NhanVien nv) throws SQLException {
        String sql = "UPDATE NhanVien SET HoTen=?, ChucVu=?, SoDienThoai=?, DiaChi=?, LuongCoBan=? "
                   + "WHERE MaNV=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setNString(1, nv.getHoTen());
            ps.setNString(2, nv.getChucVu());
            ps.setNString(3, nv.getSoDienThoai());
            ps.setNString(4, nv.getDiaChi());
            ps.setBigDecimal(5, nv.getLuongCoBan());
            ps.setNString(6, nv.getMaNV());

            return ps.executeUpdate() > 0;
        }
    }

    // ----------------------------------------------------------------
    // DELETE
    // ----------------------------------------------------------------

    public boolean xoa(String maNV) throws SQLException {
        String sql = "DELETE FROM NhanVien WHERE MaNV=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setNString(1, maNV);
            return ps.executeUpdate() > 0;
        }
    }

    // ----------------------------------------------------------------
    // Kiem tra ton tai
    // ----------------------------------------------------------------

    public boolean maTonTai(String maNV) throws SQLException {
        String sql = "SELECT 1 FROM NhanVien WHERE MaNV=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ----------------------------------------------------------------
    // Helper
    // ----------------------------------------------------------------

    private NhanVien mapResultSet(ResultSet rs) throws SQLException {
        NhanVien nv = new NhanVien();
        nv.setMaNV(rs.getNString("MaNV"));
        nv.setHoTen(rs.getNString("HoTen"));
        nv.setChucVu(rs.getNString("ChucVu"));
        nv.setSoDienThoai(rs.getNString("SoDienThoai"));
        nv.setDiaChi(rs.getNString("DiaChi"));
        nv.setLuongCoBan(rs.getBigDecimal("LuongCoBan"));

        Date d = rs.getDate("NgayVaoLam");
        if (d != null) nv.setNgayVaoLam(d.toLocalDate());

        return nv;
    }
}
