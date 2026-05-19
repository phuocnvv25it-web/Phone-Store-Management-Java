package com.quanlycuahang.dao;

import com.quanlycuahang.config.DatabaseConnection;
import com.quanlycuahang.model.DienThoai;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO xu ly cac thao tac CRUD voi bang DienThoai.
 */
public class DienThoaiDAO {

    // ----------------------------------------------------------------
    // CREATE
    // ----------------------------------------------------------------

    /**
     * Them moi mot san pham dien thoai vao DB.
     *
     * @param dt doi tuong DienThoai can them
     * @return true neu them thanh cong
     */
    public boolean themDienThoai(DienThoai dt) throws SQLException {
        String sql = "INSERT INTO DienThoai (MaMay, TenMay, HangSX, Gia, SoLuong, MoTa) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setNString(1, dt.getMaMay());
            ps.setNString(2, dt.getTenMay());
            ps.setNString(3, dt.getHangSX());
            ps.setBigDecimal(4, dt.getGia());
            ps.setInt(5, dt.getSoLuong());
            ps.setNString(6, dt.getMoTa());

            return ps.executeUpdate() > 0;
        }
    }

    // ----------------------------------------------------------------
    // READ — Lay tat ca
    // ----------------------------------------------------------------

    /**
     * Lay danh sach toan bo san pham.
     */
    public List<DienThoai> layTatCa() throws SQLException {
        List<DienThoai> ds = new ArrayList<>();
        String sql = "SELECT MaMay, TenMay, HangSX, Gia, SoLuong, MoTa, NgayNhap "
                   + "FROM DienThoai ORDER BY TenMay";
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

    /**
     * Tim san pham theo MaMay.
     *
     * @param maMay ma can tim
     * @return DienThoai hoac null neu khong tim thay
     */
    public DienThoai timTheoMa(String maMay) throws SQLException {
        String sql = "SELECT MaMay, TenMay, HangSX, Gia, SoLuong, MoTa, NgayNhap "
                   + "FROM DienThoai WHERE MaMay = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setNString(1, maMay);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        }
        return null;
    }

    // ----------------------------------------------------------------
    // READ — Tim kiem theo tu khoa (ten hoac hang)
    // ----------------------------------------------------------------

    /**
     * Tim kiem san pham theo tu khoa (ten may hoac hang san xuat).
     *
     * @param tuKhoa tu khoa tim kiem (khong phan biet hoa thuong)
     */
    public List<DienThoai> timKiem(String tuKhoa) throws SQLException {
        List<DienThoai> ds = new ArrayList<>();
        String sql = "SELECT MaMay, TenMay, HangSX, Gia, SoLuong, MoTa, NgayNhap "
                   + "FROM DienThoai "
                   + "WHERE TenMay LIKE ? OR HangSX LIKE ? OR MaMay LIKE ? "
                   + "ORDER BY TenMay";
        String pattern = "%" + tuKhoa + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setNString(1, pattern);
            ps.setNString(2, pattern);
            ps.setNString(3, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(mapResultSet(rs));
                }
            }
        }
        return ds;
    }

    // ----------------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------------

    /**
     * Cap nhat thong tin san pham (khong cap nhat MaMay — la khoa chinh).
     *
     * @param dt doi tuong DienThoai voi thong tin moi
     * @return true neu cap nhat thanh cong
     */
    public boolean capNhat(DienThoai dt) throws SQLException {
        String sql = "UPDATE DienThoai SET TenMay=?, HangSX=?, Gia=?, SoLuong=?, MoTa=? "
                   + "WHERE MaMay=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setNString(1, dt.getTenMay());
            ps.setNString(2, dt.getHangSX());
            ps.setBigDecimal(3, dt.getGia());
            ps.setInt(4, dt.getSoLuong());
            ps.setNString(5, dt.getMoTa());
            ps.setNString(6, dt.getMaMay());

            return ps.executeUpdate() > 0;
        }
    }

    // ----------------------------------------------------------------
    // DELETE
    // ----------------------------------------------------------------

    /**
     * Xoa san pham theo MaMay.
     *
     * @param maMay ma san pham can xoa
     * @return true neu xoa thanh cong
     */
    public boolean xoa(String maMay) throws SQLException {
        String sql = "DELETE FROM DienThoai WHERE MaMay=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setNString(1, maMay);
            return ps.executeUpdate() > 0;
        }
    }

    // ----------------------------------------------------------------
    // Kiem tra trung ma
    // ----------------------------------------------------------------

    /**
     * Kiem tra ma may da ton tai trong DB chua.
     */
    public boolean maTonTai(String maMay) throws SQLException {
        String sql = "SELECT 1 FROM DienThoai WHERE MaMay=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, maMay);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ----------------------------------------------------------------
    // Helper: map ResultSet -> DienThoai
    // ----------------------------------------------------------------

    private DienThoai mapResultSet(ResultSet rs) throws SQLException {
        DienThoai dt = new DienThoai();
        dt.setMaMay(rs.getNString("MaMay"));
        dt.setTenMay(rs.getNString("TenMay"));
        dt.setHangSX(rs.getNString("HangSX"));
        dt.setGia(rs.getBigDecimal("Gia"));
        dt.setSoLuong(rs.getInt("SoLuong"));
        dt.setMoTa(rs.getNString("MoTa"));

        Timestamp ts = rs.getTimestamp("NgayNhap");
        if (ts != null) {
            dt.setNgayNhap(ts.toLocalDateTime());
        }
        return dt;
    }
}
