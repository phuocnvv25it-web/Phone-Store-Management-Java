package com.quanlycuahang.dao;

import com.quanlycuahang.config.DatabaseConnection;
import com.quanlycuahang.model.KhachHang;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KhachHangDAO {

    public boolean them(KhachHang kh) throws SQLException {
        String sql = "INSERT INTO KhachHang (MaKH, HoTen, SoDienThoai, Email, DiaChi, DiemTichLuy, GhiChu) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, kh.getMaKH());
            ps.setNString(2, kh.getHoTen());
            ps.setNString(3, kh.getSoDienThoai());
            ps.setNString(4, kh.getEmail());
            ps.setNString(5, kh.getDiaChi());
            ps.setInt(6, kh.getDiemTichLuy());
            ps.setNString(7, kh.getGhiChu());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean capNhat(KhachHang kh) throws SQLException {
        String sql = "UPDATE KhachHang SET HoTen=?, SoDienThoai=?, Email=?, DiaChi=?, DiemTichLuy=?, GhiChu=? "
                   + "WHERE MaKH=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, kh.getHoTen());
            ps.setNString(2, kh.getSoDienThoai());
            ps.setNString(3, kh.getEmail());
            ps.setNString(4, kh.getDiaChi());
            ps.setInt(5, kh.getDiemTichLuy());
            ps.setNString(6, kh.getGhiChu());
            ps.setNString(7, kh.getMaKH());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean xoa(String maKH) throws SQLException {
        String sql = "DELETE FROM KhachHang WHERE MaKH=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, maKH);
            return ps.executeUpdate() > 0;
        }
    }

    public List<KhachHang> layTatCa() throws SQLException {
        List<KhachHang> list = new ArrayList<>();
        String sql = "SELECT MaKH, HoTen, SoDienThoai, Email, DiaChi, DiemTichLuy, GhiChu, NgayTao "
                   + "FROM KhachHang ORDER BY HoTen";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<KhachHang> timKiem(String tuKhoa) throws SQLException {
        List<KhachHang> list = new ArrayList<>();
        String sql = "SELECT MaKH, HoTen, SoDienThoai, Email, DiaChi, DiemTichLuy, GhiChu, NgayTao "
                   + "FROM KhachHang "
                   + "WHERE MaKH LIKE ? OR HoTen LIKE ? OR SoDienThoai LIKE ? "
                   + "ORDER BY HoTen";
        String pattern = "%" + tuKhoa + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, pattern);
            ps.setNString(2, pattern);
            ps.setNString(3, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public KhachHang timTheoSDT(String soDienThoai) throws SQLException {
        String sql = "SELECT MaKH, HoTen, SoDienThoai, Email, DiaChi, DiemTichLuy, GhiChu, NgayTao "
                   + "FROM KhachHang WHERE SoDienThoai=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, soDienThoai);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public boolean maTonTai(String maKH) throws SQLException {
        String sql = "SELECT 1 FROM KhachHang WHERE MaKH=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, maKH);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public String sinhMaKhachHang() throws SQLException {
        String sql = "SELECT TOP 1 MaKH FROM KhachHang ORDER BY MaKH DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String lastMa = rs.getNString("MaKH");
                int soThuTu = Integer.parseInt(lastMa.replaceAll("[^0-9]", "")) + 1;
                return String.format("KH%03d", soThuTu);
            }
        }
        return "KH001";
    }

    public void ghiNhanMuaHang(String tenKhach, String sdtKhach, BigDecimal tongTien) throws SQLException {
        if (sdtKhach == null || sdtKhach.trim().isEmpty()) return;
        String sdt = sdtKhach.trim();
        String ten = tenKhach == null || tenKhach.trim().isEmpty() ? "Khach le" : tenKhach.trim();
        int diemMoi = tongTien == null ? 0
            : tongTien.divide(BigDecimal.valueOf(1_000_000), RoundingMode.DOWN).intValue();

        KhachHang kh = timTheoSDT(sdt);
        if (kh == null) {
            them(new KhachHang(sinhMaKhachHang(), ten, sdt, "", "", Math.max(0, diemMoi), "Tu dong tao tu hoa don"));
            return;
        }

        String sql = "UPDATE KhachHang SET HoTen=?, DiemTichLuy=DiemTichLuy+? WHERE MaKH=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, ten);
            ps.setInt(2, Math.max(0, diemMoi));
            ps.setNString(3, kh.getMaKH());
            ps.executeUpdate();
        }
    }

    private KhachHang map(ResultSet rs) throws SQLException {
        KhachHang kh = new KhachHang();
        kh.setMaKH(rs.getNString("MaKH"));
        kh.setHoTen(rs.getNString("HoTen"));
        kh.setSoDienThoai(rs.getNString("SoDienThoai"));
        kh.setEmail(rs.getNString("Email"));
        kh.setDiaChi(rs.getNString("DiaChi"));
        kh.setDiemTichLuy(rs.getInt("DiemTichLuy"));
        kh.setGhiChu(rs.getNString("GhiChu"));
        Timestamp ts = rs.getTimestamp("NgayTao");
        if (ts != null) kh.setNgayTao(ts.toLocalDateTime());
        return kh;
    }
}
