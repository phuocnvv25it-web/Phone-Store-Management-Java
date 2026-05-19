package com.quanlycuahang.view;

import com.quanlycuahang.dao.LuongDAO;
import com.quanlycuahang.dao.NhanVienDAO;
import com.quanlycuahang.model.Luong;
import com.quanlycuahang.model.NhanVien;
import com.quanlycuahang.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Panel Quan ly Luong — Them, sua, xoa, loc theo nhan vien / thang nam.
 */
public class LuongPanel extends JPanel {

    private final LuongDAO    luongDAO   = new LuongDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();

    private final String[] COLUMNS = {"Mã Lương", "Mã NV", "Họ Tên", "Tháng/Năm",
                                       "Lương CB", "Thưởng", "Phạt", "Thực Nhận", "Ghi Chú"};
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    // Form fields
    private final JComboBox<String> cbMaNV     = new JComboBox<>();
    private final JTextField tfThang           = UIUtils.createTextField();
    private final JTextField tfNam             = UIUtils.createTextField();
    private final JTextField tfThuong          = UIUtils.createTextField();
    private final JTextField tfPhat            = UIUtils.createTextField();
    private final JTextField tfGhiChu          = UIUtils.createTextField();
    private final JLabel lblLuongCoBan         = new JLabel("---");
    private final JLabel lblLuongThucNhan      = new JLabel("---");

    // Filter
    private final JComboBox<String> cbFilterNV = new JComboBox<>();
    private final JTextField tfFilterThang     = UIUtils.createSearchField("Tháng");
    private final JTextField tfFilterNam       = UIUtils.createSearchField("Năm");

    // Buttons
    private final JButton btnThem    = UIUtils.createSuccessButton("Tạo");
    private final JButton btnSua     = UIUtils.createWarningButton("Cập nhật");
    private final JButton btnXoa     = UIUtils.createDangerButton("Xóa");
    private final JButton btnLoc     = UIUtils.createPrimaryButton("Lọc");
    private final JButton btnLamMoi  = UIUtils.createInfoButton("Tất cả");

    private int selectedMaLuong = -1;

    public LuongPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(UIUtils.BG_DARK);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildTable(),     BorderLayout.CENTER);
        add(buildFormPanel(), BorderLayout.EAST);

        loadNhanVienCombo();
        registerEvents();
        loadData();
    }

    // ═══════════════════════════════════════════════════════════
    // BUILD
    // ═══════════════════════════════════════════════════════════

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(UIUtils.BG_DARK);
        bar.add(UIUtils.createTitleLabel("Quản lý lương"), BorderLayout.WEST);

        // Filter bar
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterPanel.setBackground(UIUtils.BG_DARK);

        cbFilterNV.setFont(UIUtils.FONT_BODY);
        cbFilterNV.setPreferredSize(new Dimension(180, 36));
        UIUtils.styleComboBox(cbFilterNV);

        tfFilterThang.setPreferredSize(new Dimension(70, 36));
        tfFilterNam.setPreferredSize(new Dimension(80, 36));

        filterPanel.add(UIUtils.createLabel("NV:"));
        filterPanel.add(cbFilterNV);
        filterPanel.add(UIUtils.createLabel("Tháng:"));
        filterPanel.add(tfFilterThang);
        filterPanel.add(UIUtils.createLabel("Năm:"));
        filterPanel.add(tfFilterNam);
        filterPanel.add(btnLoc);
        filterPanel.add(btnLamMoi);
        bar.add(filterPanel, BorderLayout.EAST);
        return bar;
    }

    private JScrollPane buildTable() {
        UIUtils.styleTable(table);
        int[] widths = {70, 70, 140, 100, 110, 100, 100, 130, 120};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        return UIUtils.createScrollPane(table);
    }

    private JPanel buildFormPanel() {
        JPanel form = UIUtils.createCard("Tính lương");
        form.setPreferredSize(new Dimension(290, 0));

        JPanel fields = new JPanel(new GridLayout(0, 1, 0, 6));
        fields.setBackground(UIUtils.BG_CARD);

        // ComboBox NV
        UIUtils.styleComboBox(cbMaNV);
        addFieldRow(fields, "Nhân Viên *", cbMaNV);

        addFieldRow(fields, "Tháng * (1-12)", tfThang);
        tfThang.setText(String.valueOf(LocalDate.now().getMonthValue()));
        addFieldRow(fields, "Năm *", tfNam);
        tfNam.setText(String.valueOf(LocalDate.now().getYear()));

        // Luong co ban (read-only display)
        lblLuongCoBan.setFont(UIUtils.FONT_BODY);
        lblLuongCoBan.setForeground(UIUtils.WARNING);
        addFieldRow(fields, "Lương Cơ Bản", lblLuongCoBan);

        addFieldRow(fields, "Thưởng (VND)", tfThuong);
        tfThuong.setText("0");
        addFieldRow(fields, "Phạt (VND)", tfPhat);
        tfPhat.setText("0");
        addFieldRow(fields, "Ghi Chú", tfGhiChu);

        // Tong luong
        JPanel tongPanel = new JPanel(new BorderLayout(4, 0));
        tongPanel.setBackground(UIUtils.BG_CARD);
        tongPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIUtils.SUCCESS, 1),
            new EmptyBorder(8, 10, 8, 10)
        ));
        JLabel lblTitle = UIUtils.createLabel("LƯƠNG THỰC NHẬN:");
        lblTitle.setForeground(UIUtils.TEXT_SECONDARY);
        lblLuongThucNhan.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblLuongThucNhan.setForeground(UIUtils.SUCCESS);
        tongPanel.add(lblTitle, BorderLayout.WEST);
        tongPanel.add(lblLuongThucNhan, BorderLayout.EAST);
        fields.add(tongPanel);

        form.add(fields, BorderLayout.CENTER);
        form.add(buildButtonPanel(), BorderLayout.SOUTH);
        return form;
    }

    private void addFieldRow(JPanel parent, String lbl, JComponent comp) {
        JPanel row = new JPanel(new GridLayout(2, 1, 0, 2));
        row.setBackground(UIUtils.BG_CARD);
        row.add(UIUtils.createLabel(lbl));
        row.add(comp);
        parent.add(row);
    }

    private JPanel buildButtonPanel() {
        JPanel p = new JPanel(new GridLayout(1, 3, 8, 0));
        p.setBackground(UIUtils.BG_CARD);
        p.setBorder(new EmptyBorder(12, 0, 0, 0));
        p.add(btnThem); p.add(btnSua); p.add(btnXoa);
        return p;
    }

    // ═══════════════════════════════════════════════════════════
    // EVENTS
    // ═══════════════════════════════════════════════════════════

    private void registerEvents() {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0)
                fillFormFromTable(table.getSelectedRow());
        });

        // Tinh luong thu cong khi thay doi so
        Runnable tinhLuong = this::tinhLuongThucNhan;
        tfThuong.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) { tinhLuong.run(); }
        });
        tfPhat.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) { tinhLuong.run(); }
        });
        cbMaNV.addActionListener(e -> capNhatLuongCoBanDisplay());

        btnThem.addActionListener(e -> them());
        btnSua.addActionListener(e -> sua());
        btnXoa.addActionListener(e -> xoa());
        btnLoc.addActionListener(e -> loc());
        btnLamMoi.addActionListener(e -> { loadData(); clearForm(); });
        cbFilterNV.addActionListener(e -> loc());
        UIUtils.addDebouncedTextChangeListener(tfFilterThang, 250, this::loc);
        UIUtils.addDebouncedTextChangeListener(tfFilterNam, 250, this::loc);
    }

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    private void loadNhanVienCombo() {
        cbMaNV.removeAllItems();
        cbFilterNV.removeAllItems();
        cbFilterNV.addItem("-- Tất cả --");
        try {
            for (NhanVien nv : nhanVienDAO.layTatCa()) {
                String item = nv.getMaNV() + " - " + nv.getHoTen();
                cbMaNV.addItem(item);
                cbFilterNV.addItem(item);
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Loi tai danh sach nhan vien:\n" + ex.getMessage());
        }
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try {
            for (Luong l : luongDAO.layTatCa())
                tableModel.addRow(l.toTableRow());
        } catch (SQLException ex) {
            UIUtils.showError(this, "Loi tai du lieu:\n" + ex.getMessage());
        }
    }

    private void loc() {
        tableModel.setRowCount(0);
        try {
            String filterNV = cbFilterNV.getSelectedItem() != null
                ? cbFilterNV.getSelectedItem().toString() : "";
            String filterThang = tfFilterThang.getText().trim();
            String filterNam   = tfFilterNam.getText().trim();

            List<Luong> list;
            boolean hasNV = !filterNV.startsWith("--") && filterNV.contains(" - ");
            boolean hasThang = !filterThang.isEmpty() && !filterThang.equals("Tháng");
            boolean hasNam   = !filterNam.isEmpty() && !filterNam.equals("Năm");

            if (hasThang && hasNam) {
                list = luongDAO.layTheoThangNam(
                    Integer.parseInt(filterThang), Integer.parseInt(filterNam));
            } else if (hasNV) {
                String maNV = filterNV.split(" - ")[0];
                list = luongDAO.layTheoNhanVien(maNV);
            } else {
                list = luongDAO.layTatCa();
            }
            for (Luong l : list) tableModel.addRow(l.toTableRow());
        } catch (Exception ex) {
            UIUtils.showError(this, "Loi loc du lieu:\n" + ex.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════
    // CRUD
    // ═══════════════════════════════════════════════════════════

    private void them() {
        if (!validateForm()) return;
        try {
            Luong l = buildFromForm();
            if (luongDAO.daCoLuong(l.getMaNV(), l.getThang(), l.getNam())) {
                UIUtils.showError(this, "Nhân viên này đã có lương tháng " + l.getThang()
                    + "/" + l.getNam() + "!\nDùng Sửa để chỉnh sửa.");
                return;
            }
            if (luongDAO.themLuong(l)) {
                UIUtils.showSuccess(this, "Đã tạo bảng lương thành công!");
                loadData(); clearForm();
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Loi them luong:\n" + ex.getMessage());
        }
    }

    private void sua() {
        if (selectedMaLuong < 0) {
            UIUtils.showError(this, "Vui lòng chọn bản ghi lương cần sửa!"); return;
        }
        if (!validateForm()) return;
        try {
            Luong l = buildFromForm();
            l.setMaLuong(selectedMaLuong);
            if (luongDAO.capNhat(l)) {
                UIUtils.showSuccess(this, "Cập nhật lương thành công!");
                loadData(); clearForm();
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Loi cap nhat:\n" + ex.getMessage());
        }
    }

    private void xoa() {
        if (selectedMaLuong < 0) {
            UIUtils.showError(this, "Vui lòng chọn bản ghi lương cần xóa!"); return;
        }
        if (!UIUtils.showConfirm(this, "Xác nhận xóa bản ghi lương này?")) return;
        try {
            if (luongDAO.xoa(selectedMaLuong)) {
                UIUtils.showSuccess(this, "Đã xóa bản ghi lương!");
                loadData(); clearForm();
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Loi xoa:\n" + ex.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════

    private void tinhLuongThucNhan() {
        try {
            BigDecimal luongCB = parseLuongCoBan();
            BigDecimal thuong  = parseTien(tfThuong.getText());
            BigDecimal phat    = parseTien(tfPhat.getText());
            BigDecimal thucNhan = luongCB.add(thuong).subtract(phat);
            lblLuongThucNhan.setText(String.format("%,.0f VND", thucNhan));
        } catch (Exception ex) {
            lblLuongThucNhan.setText("---");
        }
    }

    private void capNhatLuongCoBanDisplay() {
        try {
            String maNV = getMaNVSelected();
            if (maNV == null) return;
            NhanVien nv = nhanVienDAO.timTheoMa(maNV);
            if (nv != null) {
                lblLuongCoBan.setText(String.format("%,.0f VND", nv.getLuongCoBan()));
                tinhLuongThucNhan();
            }
        } catch (SQLException ex) { /* ignore */ }
    }

    private BigDecimal parseLuongCoBan() throws SQLException {
        String maNV = getMaNVSelected();
        if (maNV == null) return BigDecimal.ZERO;
        NhanVien nv = nhanVienDAO.timTheoMa(maNV);
        return nv != null ? nv.getLuongCoBan() : BigDecimal.ZERO;
    }

    private BigDecimal parseTien(String s) {
        try { return new BigDecimal(s.trim().replace(",", "").replace(".", "")); }
        catch (Exception e) { return BigDecimal.ZERO; }
    }

    private String getMaNVSelected() {
        Object sel = cbMaNV.getSelectedItem();
        if (sel == null) return null;
        String s = sel.toString();
        return s.contains(" - ") ? s.split(" - ")[0] : s;
    }

    private void fillFormFromTable(int row) {
        selectedMaLuong = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        String maNV = tableModel.getValueAt(row, 1).toString();
        for (int i = 0; i < cbMaNV.getItemCount(); i++) {
            if (cbMaNV.getItemAt(i).startsWith(maNV)) {
                cbMaNV.setSelectedIndex(i); break;
            }
        }
        String thangNam = tableModel.getValueAt(row, 3).toString(); // "Thang X/YYYY"
        String[] parts = thangNam.replace("Thang ", "").split("/");
        tfThang.setText(parts[0]); tfNam.setText(parts[1]);
        tfThuong.setText(tableModel.getValueAt(row, 5).toString().replace(",", ""));
        tfPhat.setText(tableModel.getValueAt(row, 6).toString().replace(",", ""));
        tfGhiChu.setText(tableModel.getValueAt(row, 8).toString());
        capNhatLuongCoBanDisplay();
    }

    private Luong buildFromForm() throws SQLException {
        String maNV = getMaNVSelected();
        BigDecimal lcb = parseLuongCoBan();
        return new Luong(
            maNV,
            Integer.parseInt(tfThang.getText().trim()),
            Integer.parseInt(tfNam.getText().trim()),
            lcb,
            parseTien(tfThuong.getText()),
            parseTien(tfPhat.getText()),
            tfGhiChu.getText().trim()
        );
    }

    private boolean validateForm() {
        if (cbMaNV.getSelectedItem() == null) {
            UIUtils.showError(this, "Chọn nhân viên!"); return false;
        }
        try {
            int t = Integer.parseInt(tfThang.getText().trim());
            if (t < 1 || t > 12) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            UIUtils.showError(this, "Tháng phải từ 1 đến 12!"); return false;
        }
        try {
            Integer.parseInt(tfNam.getText().trim());
        } catch (NumberFormatException ex) {
            UIUtils.showError(this, "Năm không hợp lệ!"); return false;
        }
        return true;
    }

    private void clearForm() {
        selectedMaLuong = -1;
        tfThang.setText(String.valueOf(LocalDate.now().getMonthValue()));
        tfNam.setText(String.valueOf(LocalDate.now().getYear()));
        tfThuong.setText("0"); tfPhat.setText("0"); tfGhiChu.setText("");
        lblLuongCoBan.setText("---"); lblLuongThucNhan.setText("---");
        table.clearSelection();
    }
}
