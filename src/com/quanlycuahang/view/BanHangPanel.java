package com.quanlycuahang.view;

import com.quanlycuahang.dao.DienThoaiDAO;
import com.quanlycuahang.dao.HoaDonDAO;
import com.quanlycuahang.dao.ImeiMayDAO;
import com.quanlycuahang.dao.KhachHangDAO;
import com.quanlycuahang.dao.NhanVienDAO;
import com.quanlycuahang.model.ChiTietHoaDon;
import com.quanlycuahang.model.DienThoai;
import com.quanlycuahang.model.HoaDon;
import com.quanlycuahang.model.NhanVien;
import com.quanlycuahang.util.BillExporter;
import com.quanlycuahang.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Panel Ban Hang:
 *  - Chon san pham tu danh sach, nhap so luong
 *  - Gio hang (cart) hien thi san pham da chon
 *  - Tinh tong tien tu dong
 *  - Thanh toan: luu DB + cap nhat ton kho
 *  - Xuat bill .txt
 */
public class BanHangPanel extends JPanel {

    private final DienThoaiDAO dtDAO    = new DienThoaiDAO();
    private final HoaDonDAO    hdDAO    = new HoaDonDAO();
    private final ImeiMayDAO   imeiDAO  = new ImeiMayDAO();
    private final KhachHangDAO khDAO    = new KhachHangDAO();
    private final NhanVienDAO  nvDAO    = new NhanVienDAO();

    // ── Danh sach san pham ──────────────────────────────────
    private final String[] SP_COLS = {"Mã", "Tên Máy", "Hãng", "Giá", "Tồn Kho"};
    private final DefaultTableModel spModel = new DefaultTableModel(SP_COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tableSP = new JTable(spModel);

    // ── Gio hang ─────────────────────────────────────────────
    private final String[] CART_COLS = {"Mã", "Tên Máy", "Đơn Giá", "SL", "Thành Tiền"};
    private final DefaultTableModel cartModel = new DefaultTableModel(CART_COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tableCart = new JTable(cartModel);

    // ── Cart data ─────────────────────────────────────────────
    private final List<ChiTietHoaDon> cartItems = new ArrayList<>();

    // ── Controls ──────────────────────────────────────────────
    private final JTextField    tfSearch     = UIUtils.createSearchField("Tìm điện thoại...");
    private final JTextField    tfSoLuong    = UIUtils.createTextField();
    private final JTextField    tfKhachHang  = UIUtils.createTextField();
    private final JTextField    tfSDTKhach   = UIUtils.createTextField();
    private final JTextField    tfGhiChu     = UIUtils.createTextField();
    private final JComboBox<String> cbNhanVien = new JComboBox<>();
    private final JLabel        lblMaHD       = new JLabel("---");
    private final JLabel        lblTongTien   = new JLabel("0 VND");
    private final JLabel        lblSoMatHang  = new JLabel("0");
    private final JLabel        lblTongSoLuong = new JLabel("0");
    private final JCheckBox     chkTuXuatBill = new JCheckBox("Tự xuất bill sau thanh toán", true);

    private final JButton btnThemVaoGio  = UIUtils.createSuccessButton("Thêm vào giỏ");
    private final JButton btnXoaKhoiGio  = UIUtils.createDangerButton("Xóa khỏi giỏ");
    private final JButton btnThanhToan   = UIUtils.createButton("Thanh toán", new Color(124, 58, 237));
    private final JButton btnXuatBill    = UIUtils.createInfoButton("Xuất bill");
    private final JButton btnHoaDonMoi   = UIUtils.createButton("Hóa đơn mới", UIUtils.SUCCESS);

    private HoaDon currentHoaDon = null;

    public BanHangPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(UIUtils.BG_DARK);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(buildTopBar(),   BorderLayout.NORTH);
        add(buildCenter(),   BorderLayout.CENTER);
        add(buildRightPanel(), BorderLayout.EAST);

        loadNhanVienCombo();
        loadSanPham();
        initHoaDonMoi();
        registerEvents();
    }

    // ═══════════════════════════════════════════════════════════
    // BUILD
    // ═══════════════════════════════════════════════════════════

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(UIUtils.BG_DARK);
        bar.setBorder(new EmptyBorder(0, 0, 4, 0));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setBackground(UIUtils.BG_DARK);
        left.add(UIUtils.createTitleLabel("Bán hàng"));

        JPanel maHDPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        maHDPanel.setBackground(UIUtils.BG_DARK);
        maHDPanel.add(UIUtils.createLabel("Mã HD:"));
        lblMaHD.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblMaHD.setForeground(UIUtils.PRIMARY);
        maHDPanel.add(lblMaHD);
        left.add(maHDPanel);

        bar.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setBackground(UIUtils.BG_DARK);
        right.add(btnHoaDonMoi);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JSplitPane buildCenter() {
        // Left: danh sach san pham
        JPanel spPanel = UIUtils.createCard("Danh sách điện thoại");

        JPanel searchBar = new JPanel(new BorderLayout(8, 0));
        searchBar.setBackground(UIUtils.BG_CARD);
        tfSearch.setPreferredSize(new Dimension(0, 34));
        JButton btnSearch = UIUtils.createPrimaryButton("Tìm");
        btnSearch.addActionListener(e -> timKiemSP());
        searchBar.add(tfSearch, BorderLayout.CENTER);
        searchBar.add(btnSearch, BorderLayout.EAST);
        spPanel.add(searchBar, BorderLayout.NORTH);

        UIUtils.styleTable(tableSP);
        tableSP.getColumnModel().getColumn(0).setPreferredWidth(70);
        tableSP.getColumnModel().getColumn(1).setPreferredWidth(200);
        tableSP.getColumnModel().getColumn(2).setPreferredWidth(90);
        tableSP.getColumnModel().getColumn(3).setPreferredWidth(110);
        tableSP.getColumnModel().getColumn(4).setPreferredWidth(80);
        spPanel.add(UIUtils.createScrollPane(tableSP), BorderLayout.CENTER);

        JPanel addBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        addBar.setBackground(UIUtils.BG_CARD);
        addBar.add(UIUtils.createLabel("Số lượng:"));
        tfSoLuong.setPreferredSize(new Dimension(60, 34));
        tfSoLuong.setText("1");
        addBar.add(tfSoLuong);
        addBar.add(btnThemVaoGio);
        spPanel.add(addBar, BorderLayout.SOUTH);

        // Right: gio hang
        JPanel cartPanel = UIUtils.createCard("Giỏ hàng");
        UIUtils.styleTable(tableCart);
        tableCart.getColumnModel().getColumn(0).setPreferredWidth(70);
        tableCart.getColumnModel().getColumn(1).setPreferredWidth(180);
        tableCart.getColumnModel().getColumn(2).setPreferredWidth(110);
        tableCart.getColumnModel().getColumn(3).setPreferredWidth(50);
        tableCart.getColumnModel().getColumn(4).setPreferredWidth(120);
        cartPanel.add(UIUtils.createScrollPane(tableCart), BorderLayout.CENTER);

        JPanel cartBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        cartBottom.setBackground(UIUtils.BG_CARD);
        cartBottom.add(btnXoaKhoiGio);
        cartPanel.add(cartBottom, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, spPanel, cartPanel);
        split.setDividerLocation(540);
        split.setDividerSize(8);
        split.setBorder(null);
        split.setBackground(UIUtils.BG_DARK);
        return split;
    }

    private JPanel buildRightPanel() {
        JPanel panel = UIUtils.createCard("Thanh toán");
        panel.setPreferredSize(new Dimension(300, 0));

        JPanel fields = new JPanel(new GridLayout(0, 1, 0, 8));
        fields.setBackground(UIUtils.BG_CARD);

        // NV
        cbNhanVien.setFont(UIUtils.FONT_BODY);
        UIUtils.styleComboBox(cbNhanVien);
        addFieldRow(fields, "Nhân Viên *", cbNhanVien);

        addFieldRow(fields, "Tên Khách Hàng", tfKhachHang);
        tfKhachHang.setText("Khach le");
        addFieldRow(fields, "SĐT Khách", tfSDTKhach);
        addFieldRow(fields, "Ghi Chú", tfGhiChu);

        // Tong tien
        JPanel tongPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        tongPanel.setBackground(UIUtils.BG_CARD);
        tongPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIUtils.PRIMARY, 1),
            new EmptyBorder(12, 12, 12, 12)
        ));
        JLabel lbl = UIUtils.createLabel("TỔNG TIỀN");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        tongPanel.add(lbl);
        lblTongTien.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTongTien.setForeground(UIUtils.SUCCESS);
        tongPanel.add(lblTongTien);
        fields.add(tongPanel);

        JPanel countPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        countPanel.setBackground(UIUtils.BG_CARD);
        countPanel.add(createMiniMetric("Mặt hàng", lblSoMatHang));
        countPanel.add(createMiniMetric("Tổng SL", lblTongSoLuong));
        fields.add(countPanel);

        chkTuXuatBill.setFont(UIUtils.FONT_SMALL);
        chkTuXuatBill.setForeground(UIUtils.TEXT_SECONDARY);
        chkTuXuatBill.setBackground(UIUtils.BG_CARD);
        chkTuXuatBill.setFocusPainted(false);
        fields.add(chkTuXuatBill);

        panel.add(fields, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 0, 8));
        btnPanel.setBackground(UIUtils.BG_CARD);
        btnPanel.setBorder(new EmptyBorder(12, 0, 0, 0));
        btnThanhToan.setPreferredSize(new Dimension(0, 44));
        btnXuatBill.setPreferredSize(new Dimension(0, 38));
        btnPanel.add(btnThanhToan);
        btnPanel.add(btnXuatBill);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void addFieldRow(JPanel parent, String lbl, JComponent comp) {
        JPanel row = new JPanel(new GridLayout(2, 1, 0, 2));
        row.setBackground(UIUtils.BG_CARD);
        row.add(UIUtils.createLabel(lbl));
        row.add(comp);
        parent.add(row);
    }

    private JPanel createMiniMetric(String title, JLabel valueLabel) {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 2));
        panel.setBackground(UIUtils.BG_PANEL);
        panel.setBorder(new EmptyBorder(8, 10, 8, 10));
        panel.add(UIUtils.createLabel(title));
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(UIUtils.TEXT_PRIMARY);
        panel.add(valueLabel);
        return panel;
    }

    // ═══════════════════════════════════════════════════════════
    // EVENTS
    // ═══════════════════════════════════════════════════════════

    private void registerEvents() {
        btnThemVaoGio.addActionListener(e -> themVaoGio());
        btnXoaKhoiGio.addActionListener(e -> xoaKhoiGio());
        btnThanhToan.addActionListener(e -> thanhToan());
        btnXuatBill.addActionListener(e -> xuatBill());
        btnHoaDonMoi.addActionListener(e -> initHoaDonMoi());
        tfSearch.addActionListener(e -> timKiemSP());
        tfSoLuong.addActionListener(e -> themVaoGio());
        UIUtils.addDebouncedTextChangeListener(tfSearch, 250, this::timKiemSP);
        tableSP.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && tableSP.getSelectedRow() >= 0) {
                    themVaoGio();
                }
            }
        });
    }

    // ═══════════════════════════════════════════════════════════
    // LOGIC
    // ═══════════════════════════════════════════════════════════

    private void initHoaDonMoi() {
        cartItems.clear();
        cartModel.setRowCount(0);
        currentHoaDon = null;
        lblTongTien.setText("0 VND");
        lblSoMatHang.setText("0");
        lblTongSoLuong.setText("0");
        btnThanhToan.setEnabled(false);
        btnXuatBill.setEnabled(false);
        tfKhachHang.setText("Khach le");
        tfSDTKhach.setText("");
        tfGhiChu.setText("");
        tfSoLuong.setText("1");
        try {
            String maHD = hdDAO.sinhMaHoaDon();
            lblMaHD.setText(maHD);
        } catch (SQLException ex) {
            lblMaHD.setText("HD001");
        }
        loadSanPham();
    }

    private void loadSanPham() {
        spModel.setRowCount(0);
        try {
            for (DienThoai dt : dtDAO.layTatCa()) {
                spModel.addRow(new Object[]{
                    dt.getMaMay(), dt.getTenMay(), dt.getHangSX(),
                    String.format("%,.0f", dt.getGia()), dt.getSoLuong()
                });
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Loi tai san pham:\n" + ex.getMessage());
        }
    }

    private void timKiemSP() {
        String kw = tfSearch.getText().trim();
        if (kw.isEmpty() || kw.equals("Tìm điện thoại...")) { loadSanPham(); return; }
        spModel.setRowCount(0);
        try {
            for (DienThoai dt : dtDAO.timKiem(kw)) {
                spModel.addRow(new Object[]{
                    dt.getMaMay(), dt.getTenMay(), dt.getHangSX(),
                    String.format("%,.0f", dt.getGia()), dt.getSoLuong()
                });
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Loi tim kiem:\n" + ex.getMessage());
        }
    }

    private void themVaoGio() {
        if (currentHoaDon != null) {
            UIUtils.showError(this, "Hóa đơn này đã thanh toán. Hãy tạo hóa đơn mới trước khi thêm sản phẩm.");
            return;
        }
        int row = tableSP.getSelectedRow();
        if (row < 0) { UIUtils.showError(this, "Chọn sản phẩm trước!"); return; }

        int tonKho = Integer.parseInt(spModel.getValueAt(row, 4).toString());
        int sl;
        try {
            sl = Integer.parseInt(tfSoLuong.getText().trim());
            if (sl <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            UIUtils.showError(this, "Số lượng phải là số nguyên dương!"); return;
        }
        if (sl > tonKho) {
            UIUtils.showError(this, "Số lượng vượt quá tồn kho (" + tonKho + ")!"); return;
        }

        String maMay = spModel.getValueAt(row, 0).toString();
        String tenMay = spModel.getValueAt(row, 1).toString();
        BigDecimal donGia = new BigDecimal(
            spModel.getValueAt(row, 3).toString().replace(",", "")
        );

        // Kiem tra da co trong gio chua
        for (ChiTietHoaDon ct : cartItems) {
            if (ct.getMaMay().equals(maMay)) {
                int newSL = ct.getSoLuongBan() + sl;
                if (newSL > tonKho) {
                    UIUtils.showError(this, "Tổng số lượng vượt tồn kho!"); return;
                }
                ct.setSoLuongBan(newSL);
                refreshCart();
                tfSoLuong.setText("1");
                tfSearch.requestFocusInWindow();
                return;
            }
        }

        ChiTietHoaDon ct = new ChiTietHoaDon(null, maMay, tenMay, sl, donGia);
        cartItems.add(ct);
        refreshCart();
        tfSoLuong.setText("1");
        tfSearch.requestFocusInWindow();
    }

    private void xoaKhoiGio() {
        if (currentHoaDon != null) {
            UIUtils.showError(this, "Hóa đơn đã thanh toán nên không thể sửa giỏ hàng.");
            return;
        }
        int row = tableCart.getSelectedRow();
        if (row < 0) { UIUtils.showError(this, "Chọn sản phẩm trong giỏ để xóa!"); return; }
        cartItems.remove(row);
        refreshCart();
    }

    private void refreshCart() {
        cartModel.setRowCount(0);
        BigDecimal tong = BigDecimal.ZERO;
        int tongSoLuong = 0;
        for (ChiTietHoaDon ct : cartItems) {
            cartModel.addRow(ct.toTableRow());
            tong = tong.add(ct.getThanhTien());
            tongSoLuong += ct.getSoLuongBan();
        }
        lblTongTien.setText(String.format("%,.0f VND", tong));
        lblSoMatHang.setText(String.valueOf(cartItems.size()));
        lblTongSoLuong.setText(String.valueOf(tongSoLuong));
        if (currentHoaDon == null) {
            btnThanhToan.setEnabled(!cartItems.isEmpty());
        }
    }

    private void thanhToan() {
        if (cartItems.isEmpty()) {
            UIUtils.showError(this, "Giỏ hàng trống! Thêm sản phẩm trước."); return;
        }
        if (cbNhanVien.getSelectedItem() == null) {
            UIUtils.showError(this, "Chọn nhân viên bán hàng!"); return;
        }

        String maNV = cbNhanVien.getSelectedItem().toString().split(" - ")[0];
        String maHD = lblMaHD.getText();
        String khach = tfKhachHang.getText().trim().isEmpty() ? "Khach le" : tfKhachHang.getText().trim();

        HoaDon hd = new HoaDon(maHD, maNV, khach, tfSDTKhach.getText().trim(), tfGhiChu.getText().trim());
        hd.setNgayBan(LocalDateTime.now());
        hd.setDanhSachChiTiet(new ArrayList<>(cartItems));
        hd.tinhLaiTongTien();
        hd.setHoTenNV(cbNhanVien.getSelectedItem().toString().split(" - ").length > 1
            ? cbNhanVien.getSelectedItem().toString().split(" - ")[1] : maNV);

        Map<String, List<String>> imeiDaGan = new HashMap<>();
        String imeiPreview = "";
        try {
            imeiDaGan = chonImeiTuDong();
            if (!imeiDaGan.isEmpty()) {
                imeiPreview = "\nIMEI se tu dong gan: " + formatImeiSummary(imeiDaGan);
            }
        } catch (IllegalStateException ex) {
            UIUtils.showError(this, ex.getMessage());
            return;
        } catch (SQLException ex) {
            imeiPreview = "\nLuu y: chua kiem tra duoc IMEI (" + ex.getMessage() + ")";
        }

        if (!UIUtils.showConfirm(this, "Xác nhận thanh toán hóa đơn " + maHD
            + "\nTổng tiền: " + String.format("%,.0f VND", hd.getTongTien()) + imeiPreview + "?")) return;

        try {
            hdDAO.taoHoaDon(hd);
            currentHoaDon = hd;
            btnThanhToan.setEnabled(false);
            btnXuatBill.setEnabled(true);
            loadSanPham(); // refresh ton kho

            String automationMsg = "";
            try {
                for (List<String> imeis : imeiDaGan.values()) {
                    imeiDAO.danhDauDaBan(maHD, imeis);
                }
                if (!imeiDaGan.isEmpty()) {
                    automationMsg += "\nĐã gán IMEI: " + formatImeiSummary(imeiDaGan);
                }
            } catch (SQLException imeiEx) {
                automationMsg += "\nKhông cập nhật được IMEI: " + imeiEx.getMessage();
            }

            try {
                khDAO.ghiNhanMuaHang(khach, tfSDTKhach.getText().trim(), hd.getTongTien());
                if (!tfSDTKhach.getText().trim().isEmpty()) {
                    automationMsg += "\nĐã cập nhật khách hàng/tích điểm.";
                }
            } catch (SQLException khEx) {
                automationMsg += "\nKhông cập nhật được CRM: " + khEx.getMessage();
            }

            String billMsg = "";
            if (chkTuXuatBill.isSelected()) {
                try {
                    billMsg = "\nBill: " + exportCurrentBill(false);
                } catch (IOException ioEx) {
                    billMsg = "\nKhông xuất được bill tự động: " + ioEx.getMessage();
                }
            }
            UIUtils.showSuccess(this, "Thanh toán thành công!\nMã HD: " + maHD
                + "\nTồn kho đã được cập nhật tự động." + automationMsg + billMsg);
        } catch (SQLException ex) {
            UIUtils.showError(this, "Loi thanh toan:\n" + ex.getMessage());
        }
    }

    private Map<String, List<String>> chonImeiTuDong() throws SQLException {
        Map<String, List<String>> result = new HashMap<>();
        for (ChiTietHoaDon ct : cartItems) {
            int totalTracked = imeiDAO.demTheoMaMay(ct.getMaMay());
            if (totalTracked == 0) continue;

            List<String> available = imeiDAO.layTrongKhoTheoMaMay(ct.getMaMay(), ct.getSoLuongBan());
            if (available.size() < ct.getSoLuongBan()) {
                throw new IllegalStateException("Sản phẩm " + ct.getTenMay()
                    + " đang quản lý theo IMEI nhưng chỉ còn " + available.size()
                    + " IMEI trong kho, cần " + ct.getSoLuongBan() + ".");
            }
            result.put(ct.getMaMay(), available);
        }
        return result;
    }

    private String formatImeiSummary(Map<String, List<String>> imeisByProduct) {
        return imeisByProduct.entrySet().stream()
            .map(entry -> entry.getKey() + "=" + String.join(",", entry.getValue()))
            .collect(Collectors.joining("; "));
    }

    private void xuatBill() {
        if (currentHoaDon == null) {
            UIUtils.showError(this, "Chưa có hóa đơn nào được thanh toán!\nVui lòng thanh toán trước."); return;
        }
        try {
            String filePath = exportCurrentBill(true);
            UIUtils.showSuccess(this, "Đã xuất bill: " + filePath);
        } catch (IOException ex) {
            UIUtils.showError(this, "Loi xuat bill:\n" + ex.getMessage());
        }
    }

    private String exportCurrentBill(boolean openFile) throws IOException {
        File billDir = new File("bills");
        billDir.mkdirs();

        String filePath = "bills/" + currentHoaDon.getMaHD() + ".txt";
        File billFile = new File(filePath);
        BillExporter.xuatBill(currentHoaDon, filePath);
        if (openFile && Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(billFile);
        }
        return billFile.getPath();
    }

    private void loadNhanVienCombo() {
        cbNhanVien.removeAllItems();
        try {
            for (NhanVien nv : nvDAO.layTatCa())
                cbNhanVien.addItem(nv.getMaNV() + " - " + nv.getHoTen());
        } catch (SQLException ex) {
            UIUtils.showError(this, "Loi tai nhan vien:\n" + ex.getMessage());
        }
    }
}
