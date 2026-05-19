package com.quanlycuahang.dao;

import com.quanlycuahang.config.DatabaseConnection;
import com.quanlycuahang.model.ChiTietHoaDon;
import com.quanlycuahang.model.HoaDon;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO xu ly HoaDon va ChiTietHoaDon.
 * Goi Stored Procedure sp_BanHang de dam bao Transaction khi ban hang.
 */
public class HoaDonDAO {

    // ----------------------------------------------------------------
    // TAO HOA DON + CHI TIET + GOI SP (toan bo trong 1 transaction)
    // ----------------------------------------------------------------

    /**
     * Tao hoa don hoan chinh:
     *   1. Insert HoaDon header
     *   2. Insert tat ca ChiTietHoaDon
     *   3. Goi sp_BanHang de cap nhat tong tien + ton kho (Transaction trong SP)
     *
     * @param hd doi tuong HoaDon co danh sach chi tiet
     * @throws SQLException neu co loi hoac ton kho khong du
     */
    public void taoHoaDon(HoaDon hd) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Bat dau transaction

            // 1. Insert header hoa don
            String sqlHD = "INSERT INTO HoaDon (MaHD, MaNV, TenKhachHang, SDTKhach, GhiChu) "
                         + "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlHD)) {
                ps.setNString(1, hd.getMaHD());
                ps.setNString(2, hd.getMaNV());
                ps.setNString(3, hd.getTenKhachHang());
                ps.setNString(4, hd.getSdtKhach());
                ps.setNString(5, hd.getGhiChu());
                ps.executeUpdate();
            }

            // 2. Insert tung chi tiet
            String sqlCT = "INSERT INTO ChiTietHoaDon (MaHD, MaMay, SoLuongBan, DonGia) "
                         + "VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlCT)) {
                for (ChiTietHoaDon ct : hd.getDanhSachChiTiet()) {
                    ps.setNString(1, hd.getMaHD());
                    ps.setNString(2, ct.getMaMay());
                    ps.setInt(3, ct.getSoLuongBan());
                    ps.setBigDecimal(4, ct.getDonGia());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // 3. Goi SP de tinh tong tien + cap nhat ton kho
            String sqlSP = "{CALL sp_BanHang(?, ?, ?, ?, ?)}";
            try (CallableStatement cs = conn.prepareCall(sqlSP)) {
                cs.setNString(1, hd.getMaHD());
                cs.setNString(2, hd.getMaNV());
                cs.setNString(3, hd.getTenKhachHang());
                cs.setNString(4, hd.getSdtKhach());
                cs.setNString(5, hd.getGhiChu());
                cs.execute();
            }

            conn.commit(); // Xac nhan toan bo
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /* bo qua */ }
            }
            throw e; // Nem lai de tang View xu ly
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ex) { /* bo qua */ }
                try { conn.close(); } catch (SQLException ex) { /* bo qua */ }
            }
        }
    }

    // ----------------------------------------------------------------
    // READ — Lay tat ca hoa don
    // ----------------------------------------------------------------

    public List<HoaDon> layTatCa() throws SQLException {
        List<HoaDon> ds = new ArrayList<>();
        String sql = "SELECT H.MaHD, H.MaNV, NV.HoTen, H.TenKhachHang, H.SDTKhach, "
                   + "       H.TongTien, H.NgayBan, H.GhiChu "
                   + "FROM HoaDon H "
                   + "JOIN NhanVien NV ON H.MaNV = NV.MaNV "
                   + "ORDER BY H.NgayBan DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) ds.add(mapHoaDon(rs));
        }
        return ds;
    }

    // ----------------------------------------------------------------
    // READ — Lay chi tiet theo ma hoa don
    // ----------------------------------------------------------------

    public List<ChiTietHoaDon> layChiTiet(String maHD) throws SQLException {
        List<ChiTietHoaDon> ds = new ArrayList<>();
        String sql = "SELECT CT.MaCTHD, CT.MaHD, CT.MaMay, DT.TenMay, "
                   + "       CT.SoLuongBan, CT.DonGia, CT.ThanhTien "
                   + "FROM ChiTietHoaDon CT "
                   + "JOIN DienThoai DT ON CT.MaMay = DT.MaMay "
                   + "WHERE CT.MaHD = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setNString(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(mapChiTiet(rs));
            }
        }
        return ds;
    }

    // ----------------------------------------------------------------
    // READ — Lay hoa don theo ma (day du voi chi tiet)
    // ----------------------------------------------------------------

    public HoaDon layTheoMa(String maHD) throws SQLException {
        String sql = "SELECT H.MaHD, H.MaNV, NV.HoTen, H.TenKhachHang, H.SDTKhach, "
                   + "       H.TongTien, H.NgayBan, H.GhiChu "
                   + "FROM HoaDon H "
                   + "JOIN NhanVien NV ON H.MaNV = NV.MaNV "
                   + "WHERE H.MaHD = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setNString(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    HoaDon hd = mapHoaDon(rs);
                    hd.setDanhSachChiTiet(layChiTiet(maHD));
                    return hd;
                }
            }
        }
        return null;
    }

    // ----------------------------------------------------------------
    // Sinh Ma Hoa Don tu dong
    // ----------------------------------------------------------------

    /**
     * Tao ma hoa don moi theo dinh dang HD + so thu tu (HD001, HD002, ...).
     */
    public String sinhMaHoaDon() throws SQLException {
        String sql = "SELECT TOP 1 MaHD FROM HoaDon ORDER BY MaHD DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                String lastMa = rs.getNString("MaHD");
                // Lay phan so (bo "HD" o dau)
                int soThuTu = Integer.parseInt(lastMa.replaceAll("[^0-9]", "")) + 1;
                return String.format("HD%03d", soThuTu);
            }
        }
        return "HD001";
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private HoaDon mapHoaDon(ResultSet rs) throws SQLException {
        HoaDon hd = new HoaDon();
        hd.setMaHD(rs.getNString("MaHD"));
        hd.setMaNV(rs.getNString("MaNV"));
        hd.setHoTenNV(rs.getNString("HoTen"));
        hd.setTenKhachHang(rs.getNString("TenKhachHang"));
        hd.setSdtKhach(rs.getNString("SDTKhach"));
        hd.setTongTien(rs.getBigDecimal("TongTien"));
        hd.setGhiChu(rs.getNString("GhiChu"));

        Timestamp ts = rs.getTimestamp("NgayBan");
        if (ts != null) hd.setNgayBan(ts.toLocalDateTime());

        return hd;
    }

    private ChiTietHoaDon mapChiTiet(ResultSet rs) throws SQLException {
        ChiTietHoaDon ct = new ChiTietHoaDon();
        ct.setMaCTHD(rs.getInt("MaCTHD"));
        ct.setMaHD(rs.getNString("MaHD"));
        ct.setMaMay(rs.getNString("MaMay"));
        ct.setTenMay(rs.getNString("TenMay"));
        ct.setSoLuongBan(rs.getInt("SoLuongBan"));
        ct.setDonGia(rs.getBigDecimal("DonGia"));
        ct.setThanhTien(rs.getBigDecimal("ThanhTien"));
        return ct;
    }
}
