package com.quanlycuahang.dao;

import com.quanlycuahang.config.DatabaseConnection;
import com.quanlycuahang.model.Luong;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO xu ly tính lương và CRUD bang Luong.
 */
public class LuongDAO {

    // ----------------------------------------------------------------
    // CREATE — Them ban ghi luong moi
    // ----------------------------------------------------------------

    /**
     * Them ban ghi luong moi vao DB.
     * LuongCoBan phai duoc truyen vao de luu snapshot.
     */
    public boolean themLuong(Luong l) throws SQLException {
        String sql = "INSERT INTO Luong (MaNV, Thang, Nam, LuongCoBan, Thuong, Phat, GhiChu) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setNString(1, l.getMaNV());
            ps.setInt(2, l.getThang());
            ps.setInt(3, l.getNam());
            ps.setBigDecimal(4, l.getLuongCoBan());
            ps.setBigDecimal(5, l.getThuong());
            ps.setBigDecimal(6, l.getPhat());
            ps.setNString(7, l.getGhiChu());

            return ps.executeUpdate() > 0;
        }
    }

    // ----------------------------------------------------------------
    // READ — Lay toan bo (join NhanVien de lay ten)
    // ----------------------------------------------------------------

    public List<Luong> layTatCa() throws SQLException {
        List<Luong> ds = new ArrayList<>();
        String sql = "SELECT L.MaLuong, L.MaNV, NV.HoTen, L.Thang, L.Nam, "
                   + "       L.LuongCoBan, L.Thuong, L.Phat, L.LuongThucNhan, L.GhiChu, L.NgayTao "
                   + "FROM Luong L "
                   + "JOIN NhanVien NV ON L.MaNV = NV.MaNV "
                   + "ORDER BY L.Nam DESC, L.Thang DESC, NV.HoTen";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) ds.add(mapResultSet(rs));
        }
        return ds;
    }

    // ----------------------------------------------------------------
    // READ — Loc theo nhan vien
    // ----------------------------------------------------------------

    public List<Luong> layTheoNhanVien(String maNV) throws SQLException {
        List<Luong> ds = new ArrayList<>();
        String sql = "SELECT L.MaLuong, L.MaNV, NV.HoTen, L.Thang, L.Nam, "
                   + "       L.LuongCoBan, L.Thuong, L.Phat, L.LuongThucNhan, L.GhiChu, L.NgayTao "
                   + "FROM Luong L "
                   + "JOIN NhanVien NV ON L.MaNV = NV.MaNV "
                   + "WHERE L.MaNV = ? "
                   + "ORDER BY L.Nam DESC, L.Thang DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setNString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(mapResultSet(rs));
            }
        }
        return ds;
    }

    // ----------------------------------------------------------------
    // READ — Loc theo thang/nam
    // ----------------------------------------------------------------

    public List<Luong> layTheoThangNam(int thang, int nam) throws SQLException {
        List<Luong> ds = new ArrayList<>();
        String sql = "SELECT L.MaLuong, L.MaNV, NV.HoTen, L.Thang, L.Nam, "
                   + "       L.LuongCoBan, L.Thuong, L.Phat, L.LuongThucNhan, L.GhiChu, L.NgayTao "
                   + "FROM Luong L "
                   + "JOIN NhanVien NV ON L.MaNV = NV.MaNV "
                   + "WHERE L.Thang = ? AND L.Nam = ? "
                   + "ORDER BY NV.HoTen";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, thang);
            ps.setInt(2, nam);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(mapResultSet(rs));
            }
        }
        return ds;
    }

    // ----------------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------------

    public boolean capNhat(Luong l) throws SQLException {
        String sql = "UPDATE Luong SET LuongCoBan=?, Thuong=?, Phat=?, GhiChu=? WHERE MaLuong=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBigDecimal(1, l.getLuongCoBan());
            ps.setBigDecimal(2, l.getThuong());
            ps.setBigDecimal(3, l.getPhat());
            ps.setNString(4, l.getGhiChu());
            ps.setInt(5, l.getMaLuong());

            return ps.executeUpdate() > 0;
        }
    }

    // ----------------------------------------------------------------
    // DELETE
    // ----------------------------------------------------------------

    public boolean xoa(int maLuong) throws SQLException {
        String sql = "DELETE FROM Luong WHERE MaLuong=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maLuong);
            return ps.executeUpdate() > 0;
        }
    }

    // ----------------------------------------------------------------
    // Kiem tra trung thang/nam
    // ----------------------------------------------------------------

    public boolean daCoLuong(String maNV, int thang, int nam) throws SQLException {
        String sql = "SELECT 1 FROM Luong WHERE MaNV=? AND Thang=? AND Nam=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, maNV);
            ps.setInt(2, thang);
            ps.setInt(3, nam);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ----------------------------------------------------------------
    // Helper
    // ----------------------------------------------------------------

    private Luong mapResultSet(ResultSet rs) throws SQLException {
        Luong l = new Luong();
        l.setMaLuong(rs.getInt("MaLuong"));
        l.setMaNV(rs.getNString("MaNV"));
        l.setHoTenNV(rs.getNString("HoTen"));
        l.setThang(rs.getInt("Thang"));
        l.setNam(rs.getInt("Nam"));
        l.setLuongCoBan(rs.getBigDecimal("LuongCoBan"));
        l.setThuong(rs.getBigDecimal("Thuong"));
        l.setPhat(rs.getBigDecimal("Phat"));
        l.setLuongThucNhan(rs.getBigDecimal("LuongThucNhan"));
        l.setGhiChu(rs.getNString("GhiChu"));

        Timestamp ts = rs.getTimestamp("NgayTao");
        if (ts != null) l.setNgayTao(ts.toLocalDateTime());

        return l;
    }
}
