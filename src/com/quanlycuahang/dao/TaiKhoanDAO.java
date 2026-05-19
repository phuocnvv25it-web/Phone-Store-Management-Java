package com.quanlycuahang.dao;

import com.quanlycuahang.config.DatabaseConnection;
import com.quanlycuahang.model.TaiKhoan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TaiKhoanDAO {

    public TaiKhoan dangNhap(String tenDangNhap, String matKhauHash) throws SQLException {
        String sql = "SELECT * FROM TaiKhoan WHERE TenDangNhap = ? AND MatKhau = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenDangNhap);
            stmt.setString(2, matKhauHash);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    TaiKhoan tk = new TaiKhoan();
                    tk.setTenDangNhap(rs.getString("TenDangNhap"));
                    tk.setMatKhau(rs.getString("MatKhau"));
                    tk.setVaiTro(rs.getString("VaiTro"));
                    tk.setNgayTao(rs.getTimestamp("NgayTao").toLocalDateTime());
                    return tk;
                }
            }
        }
        return null;
    }

    public boolean dangKy(TaiKhoan tk) throws SQLException {
        String sql = "INSERT INTO TaiKhoan (TenDangNhap, MatKhau, VaiTro) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tk.getTenDangNhap());
            stmt.setString(2, tk.getMatKhau());
            stmt.setString(3, tk.getVaiTro());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean kiemTraTonTai(String tenDangNhap) throws SQLException {
        String sql = "SELECT 1 FROM TaiKhoan WHERE TenDangNhap = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenDangNhap);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}
