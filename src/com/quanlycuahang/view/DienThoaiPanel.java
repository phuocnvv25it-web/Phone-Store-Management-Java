package com.quanlycuahang.view;

import com.quanlycuahang.dao.DienThoaiDAO;
import com.quanlycuahang.model.DienThoai;
import com.quanlycuahang.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
/**
 * Panel quan ly Dien Thoai — CRUD + Tim kiem.
 * Layout: BorderLayout
 *   - NORTH : Thanh cong cu (tim kiem + nut lam moi)
 *   - CENTER: JTable danh sach san pham
 *   - EAST  : Form nhap lieu + cac nut chuc nang
 */
public class DienThoaiPanel extends JPanel {

    private final DienThoaiDAO dao = new DienThoaiDAO();

    // ── Table ──────────────────────────────────────────────────
    private final String[] COLUMNS = {"Mã máy", "Tên máy", "Hãng SX", "Giá (VND)", "Số lượng", "Mô tả"};
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    // ── Form fields ────────────────────────────────────────────
    private final JTextField tfMaMay    = UIUtils.createTextField();
    private final JTextField tfTenMay   = UIUtils.createTextField();
    private final JTextField tfHangSX   = UIUtils.createTextField();
    private final JTextField tfGia      = UIUtils.createTextField();
    private final JTextField tfSoLuong  = UIUtils.createTextField();
    private final JTextField tfMoTa     = UIUtils.createTextField();
    private final JTextField tfSearch   = UIUtils.createSearchField("Tìm theo tên, hãng, mã...");

    // ── Buttons ────────────────────────────────────────────────
    private final JButton btnThem    = UIUtils.createSuccessButton("Thêm");
    private final JButton btnSua     = UIUtils.createWarningButton("Cập nhật");
    private final JButton btnXoa     = UIUtils.createDangerButton("Xóa");
    private final JButton btnLamMoi  = UIUtils.createInfoButton("Làm mới");
    private final JButton btnTimKiem = UIUtils.createPrimaryButton("Tìm");

    public DienThoaiPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(UIUtils.BG_DARK);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(buildTopBar(),  BorderLayout.NORTH);
        add(buildTable(),   BorderLayout.CENTER);
        add(buildFormPanel(), BorderLayout.EAST);

        registerEvents();
        loadData();
    }

    // ═══════════════════════════════════════════════════════════
    // BUILD COMPONENTS
    // ═══════════════════════════════════════════════════════════

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(UIUtils.BG_DARK);
        bar.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel title = UIUtils.createTitleLabel("Quản lý điện thoại");
        bar.add(title, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchPanel.setBackground(UIUtils.BG_DARK);
        tfSearch.setPreferredSize(new Dimension(260, 36));
        searchPanel.add(tfSearch);
        searchPanel.add(btnTimKiem);
        searchPanel.add(btnLamMoi);
        bar.add(searchPanel, BorderLayout.EAST);

        return bar;
    }

    private JScrollPane buildTable() {
        UIUtils.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(150);
        return UIUtils.createScrollPane(table);
    }

    private JPanel buildFormPanel() {
        JPanel form = UIUtils.createCard("Thông tin sản phẩm");
        form.setPreferredSize(new Dimension(280, 0));

        JPanel fields = new JPanel(new GridLayout(0, 1, 0, 6));
        fields.setBackground(UIUtils.BG_CARD);

        tfMaMay.setPreferredSize(new Dimension(240, 34));
        addFormRow(fields, "Mã Máy *", tfMaMay);
        addFormRow(fields, "Tên Máy *", tfTenMay);
        addFormRow(fields, "Hãng SX *", tfHangSX);
        addFormRow(fields, "Giá (VND) *", tfGia);
        addFormRow(fields, "Số Lượng *", tfSoLuong);
        addFormRow(fields, "Mô Tả", tfMoTa);

        form.add(fields, BorderLayout.CENTER);
        form.add(buildButtonPanel(), BorderLayout.SOUTH);
        return form;
    }

    private void addFormRow(JPanel parent, String labelText, JTextField tf) {
        JPanel row = new JPanel(new GridLayout(2, 1, 0, 2));
        row.setBackground(UIUtils.BG_CARD);
        row.add(UIUtils.createLabel(labelText));
        row.add(tf);
        parent.add(row);
    }

    private JPanel buildButtonPanel() {
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        btnPanel.setBackground(UIUtils.BG_CARD);
        btnPanel.setBorder(new EmptyBorder(12, 0, 0, 0));
        btnPanel.add(btnThem);
        btnPanel.add(btnSua);
        btnPanel.add(btnXoa);

        JButton btnClear = UIUtils.createButton("Xóa form", UIUtils.BG_PANEL);
        btnClear.addActionListener(e -> clearForm());
        btnPanel.add(btnClear);
        return btnPanel;
    }

    // ═══════════════════════════════════════════════════════════
    // EVENTS
    // ═══════════════════════════════════════════════════════════

    private void registerEvents() {
        // Chon hang tren table -> dien vao form
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                fillFormFromTable(table.getSelectedRow());
            }
        });

        btnThem.addActionListener(e -> themSanPham());
        btnSua.addActionListener(e -> suaSanPham());
        btnXoa.addActionListener(e -> xoaSanPham());
        btnLamMoi.addActionListener(e -> { loadData(); clearForm(); });
        btnTimKiem.addActionListener(e -> timKiem());

        // Enter trong o tim kiem
        tfSearch.addActionListener(e -> timKiem());
        UIUtils.addDebouncedTextChangeListener(tfSearch, 250, this::timKiem);
    }

    // ═══════════════════════════════════════════════════════════
    // CRUD LOGIC
    // ═══════════════════════════════════════════════════════════

    private void loadData() {
        tableModel.setRowCount(0);
        try {
            for (DienThoai dt : dao.layTatCa()) {
                tableModel.addRow(dt.toTableRow());
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Loi tai du lieu:\n" + ex.getMessage());
        }
    }

    private void timKiem() {
        String kw = tfSearch.getText().trim();
        if (kw.isEmpty() || kw.equals("Tìm theo tên, hãng, mã...")) {
            loadData(); return;
        }
        tableModel.setRowCount(0);
        try {
            for (DienThoai dt : dao.timKiem(kw)) {
                tableModel.addRow(dt.toTableRow());
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Loi tim kiem:\n" + ex.getMessage());
        }
    }

    private void themSanPham() {
        if (!validateForm()) return;
        try {
            if (dao.maTonTai(tfMaMay.getText().trim())) {
                UIUtils.showError(this, "Mã máy đã tồn tại! Vui lòng nhập mã khác.");
                return;
            }
            DienThoai dt = buildFromForm();
            if (dao.themDienThoai(dt)) {
                UIUtils.showSuccess(this, "Thêm sản phẩm thành công!");
                loadData(); clearForm();
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Loi them san pham:\n" + ex.getMessage());
        }
    }

    private void suaSanPham() {
        if (table.getSelectedRow() < 0) {
            UIUtils.showError(this, "Vui lòng chọn sản phẩm cần sửa!"); return;
        }
        if (!validateForm()) return;
        try {
            DienThoai dt = buildFromForm();
            if (dao.capNhat(dt)) {
                UIUtils.showSuccess(this, "Cập nhật thành công!");
                loadData(); clearForm();
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Loi cap nhat:\n" + ex.getMessage());
        }
    }

    private void xoaSanPham() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIUtils.showError(this, "Vui lòng chọn sản phẩm cần xóa!"); return;
        }
        String maMay = tableModel.getValueAt(row, 0).toString();
        String tenMay = tableModel.getValueAt(row, 1).toString();
        if (!UIUtils.showConfirm(this, "Xác nhận xóa: " + tenMay + "?")) return;
        try {
            if (dao.xoa(maMay)) {
                UIUtils.showSuccess(this, "Đã xóa sản phẩm: " + tenMay);
                loadData(); clearForm();
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Loi xoa:\n" + ex.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════

    private void fillFormFromTable(int row) {
        tfMaMay.setText(tableModel.getValueAt(row, 0).toString());
        tfMaMay.setEditable(false);
        tfTenMay.setText(tableModel.getValueAt(row, 1).toString());
        tfHangSX.setText(tableModel.getValueAt(row, 2).toString());
        // Gia dang dinh dang "1,234,567 VND" — can bo chu "VND" va dau phay
        String giaStr = tableModel.getValueAt(row, 3).toString()
            .replace(" VND", "").replace(",", "");
        tfGia.setText(giaStr);
        tfSoLuong.setText(tableModel.getValueAt(row, 4).toString());
        tfMoTa.setText(tableModel.getValueAt(row, 5).toString());
    }

    private DienThoai buildFromForm() {
        String giaStr = tfGia.getText().trim().replace(",", "").replace(".", "");
        return new DienThoai(
            tfMaMay.getText().trim(),
            tfTenMay.getText().trim(),
            tfHangSX.getText().trim(),
            new BigDecimal(giaStr),
            Integer.parseInt(tfSoLuong.getText().trim()),
            tfMoTa.getText().trim()
        );
    }

    private boolean validateForm() {
        if (tfMaMay.getText().trim().isEmpty()) {
            UIUtils.showError(this, "Vui lòng nhập Mã Máy!"); return false;
        }
        if (tfTenMay.getText().trim().isEmpty()) {
            UIUtils.showError(this, "Vui lòng nhập Tên Máy!"); return false;
        }
        if (tfHangSX.getText().trim().isEmpty()) {
            UIUtils.showError(this, "Vui lòng nhập Hãng SX!"); return false;
        }
        try {
            String gia = tfGia.getText().trim().replace(",", "").replace(".", "");
            if (new BigDecimal(gia).compareTo(BigDecimal.ZERO) < 0)
                throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            UIUtils.showError(this, "Giá phải là số dương!"); return false;
        }
        try {
            int sl = Integer.parseInt(tfSoLuong.getText().trim());
            if (sl < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            UIUtils.showError(this, "Số lượng phải là số nguyên >= 0!"); return false;
        }
        return true;
    }

    private void clearForm() {
        tfMaMay.setText(""); tfTenMay.setText(""); tfHangSX.setText("");
        tfGia.setText(""); tfSoLuong.setText(""); tfMoTa.setText("");
        tfMaMay.setEditable(true);
        table.clearSelection();
    }
}
