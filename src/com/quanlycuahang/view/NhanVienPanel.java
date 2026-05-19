package com.quanlycuahang.view;

import com.quanlycuahang.dao.NhanVienDAO;
import com.quanlycuahang.model.NhanVien;
import com.quanlycuahang.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * Panel Quan ly Nhan Vien — CRUD + Tim kiem.
 */
public class NhanVienPanel extends JPanel {

    private final NhanVienDAO dao = new NhanVienDAO();

    private final String[] COLUMNS = {"Mã NV", "Họ Tên", "Chức Vụ", "SĐT", "Địa Chỉ", "Lương CB", "Ngày Vào"};
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    private final JTextField tfMaNV       = UIUtils.createTextField();
    private final JTextField tfHoTen      = UIUtils.createTextField();
    private final JTextField tfChucVu     = UIUtils.createTextField();
    private final JTextField tfSDT        = UIUtils.createTextField();
    private final JTextField tfDiaChi     = UIUtils.createTextField();
    private final JTextField tfLuongCoBan = UIUtils.createTextField();
    private final JTextField tfSearch     = UIUtils.createSearchField("Tìm theo tên, mã, chức vụ...");

    private final JButton btnThem    = UIUtils.createSuccessButton("Thêm");
    private final JButton btnSua     = UIUtils.createWarningButton("Cập nhật");
    private final JButton btnXoa     = UIUtils.createDangerButton("Xóa");
    private final JButton btnLamMoi  = UIUtils.createInfoButton("Làm mới");
    private final JButton btnTimKiem = UIUtils.createPrimaryButton("Tìm");

    public NhanVienPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(UIUtils.BG_DARK);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildTable(),     BorderLayout.CENTER);
        add(buildFormPanel(), BorderLayout.EAST);

        registerEvents();
        loadData();
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(UIUtils.BG_DARK);
        bar.add(UIUtils.createTitleLabel("Quản lý nhân viên"), BorderLayout.WEST);

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
        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(150);
        table.getColumnModel().getColumn(5).setPreferredWidth(120);
        table.getColumnModel().getColumn(6).setPreferredWidth(100);
        return UIUtils.createScrollPane(table);
    }

    private JPanel buildFormPanel() {
        JPanel form = UIUtils.createCard("Thông tin nhân viên");
        form.setPreferredSize(new Dimension(280, 0));

        JPanel fields = new JPanel(new GridLayout(0, 1, 0, 6));
        fields.setBackground(UIUtils.BG_CARD);

        addFormRow(fields, "Mã NV *", tfMaNV);
        addFormRow(fields, "Họ Tên *", tfHoTen);
        addFormRow(fields, "Chức Vụ *", tfChucVu);
        addFormRow(fields, "Số Điện Thoại", tfSDT);
        addFormRow(fields, "Địa Chỉ", tfDiaChi);
        addFormRow(fields, "Lương Cơ Bản *", tfLuongCoBan);

        form.add(fields, BorderLayout.CENTER);
        form.add(buildButtonPanel(), BorderLayout.SOUTH);
        return form;
    }

    private void addFormRow(JPanel parent, String lbl, JTextField tf) {
        JPanel row = new JPanel(new GridLayout(2, 1, 0, 2));
        row.setBackground(UIUtils.BG_CARD);
        row.add(UIUtils.createLabel(lbl));
        row.add(tf);
        parent.add(row);
    }

    private JPanel buildButtonPanel() {
        JPanel p = new JPanel(new GridLayout(2, 2, 8, 8));
        p.setBackground(UIUtils.BG_CARD);
        p.setBorder(new EmptyBorder(12, 0, 0, 0));
        p.add(btnThem); p.add(btnSua); p.add(btnXoa);
        JButton btnClear = UIUtils.createButton("Xóa form", UIUtils.BG_PANEL);
        btnClear.addActionListener(e -> clearForm());
        p.add(btnClear);
        return p;
    }

    private void registerEvents() {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0)
                fillFormFromTable(table.getSelectedRow());
        });
        btnThem.addActionListener(e -> them());
        btnSua.addActionListener(e -> sua());
        btnXoa.addActionListener(e -> xoa());
        btnLamMoi.addActionListener(e -> { loadData(); clearForm(); });
        btnTimKiem.addActionListener(e -> timKiem());
        tfSearch.addActionListener(e -> timKiem());
        UIUtils.addDebouncedTextChangeListener(tfSearch, 250, this::timKiem);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try {
            for (NhanVien nv : dao.layTatCa())
                tableModel.addRow(nv.toTableRow());
        } catch (SQLException ex) {
            UIUtils.showError(this, "Loi tai du lieu:\n" + ex.getMessage());
        }
    }

    private void timKiem() {
        String kw = tfSearch.getText().trim();
        if (kw.isEmpty() || kw.startsWith("Tìm")) { loadData(); return; }
        tableModel.setRowCount(0);
        try {
            for (NhanVien nv : dao.timKiem(kw)) tableModel.addRow(nv.toTableRow());
        } catch (SQLException ex) {
            UIUtils.showError(this, "Loi tim kiem:\n" + ex.getMessage());
        }
    }

    private void them() {
        if (!validateForm()) return;
        try {
            if (dao.maTonTai(tfMaNV.getText().trim())) {
                UIUtils.showError(this, "Mã NV đã tồn tại!"); return;
            }
            if (dao.themNhanVien(buildFromForm())) {
                UIUtils.showSuccess(this, "Thêm nhân viên thành công!");
                loadData(); clearForm();
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Loi them:\n" + ex.getMessage());
        }
    }

    private void sua() {
        if (table.getSelectedRow() < 0) {
            UIUtils.showError(this, "Vui lòng chọn nhân viên cần sửa!"); return;
        }
        if (!validateForm()) return;
        try {
            if (dao.capNhat(buildFromForm())) {
                UIUtils.showSuccess(this, "Cập nhật thành công!");
                loadData(); clearForm();
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Loi cap nhat:\n" + ex.getMessage());
        }
    }

    private void xoa() {
        int row = table.getSelectedRow();
        if (row < 0) { UIUtils.showError(this, "Vui lòng chọn nhân viên cần xóa!"); return; }
        String maNV = tableModel.getValueAt(row, 0).toString();
        String hoTen = tableModel.getValueAt(row, 1).toString();
        if (!UIUtils.showConfirm(this, "Xóa nhân viên: " + hoTen + "?")) return;
        try {
            if (dao.xoa(maNV)) {
                UIUtils.showSuccess(this, "Đã xóa nhân viên: " + hoTen);
                loadData(); clearForm();
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Loi xoa:\n" + ex.getMessage());
        }
    }

    private void fillFormFromTable(int row) {
        tfMaNV.setText(tableModel.getValueAt(row, 0).toString());
        tfMaNV.setEditable(false);
        tfHoTen.setText(tableModel.getValueAt(row, 1).toString());
        tfChucVu.setText(tableModel.getValueAt(row, 2).toString());
        tfSDT.setText(tableModel.getValueAt(row, 3).toString());
        tfDiaChi.setText(tableModel.getValueAt(row, 4).toString());
        String luong = tableModel.getValueAt(row, 5).toString()
            .replace(" VND", "").replace(",", "");
        tfLuongCoBan.setText(luong);
    }

    private NhanVien buildFromForm() {
        String lcb = tfLuongCoBan.getText().trim().replace(",", "").replace(".", "");
        return new NhanVien(
            tfMaNV.getText().trim(),
            tfHoTen.getText().trim(),
            tfChucVu.getText().trim(),
            tfSDT.getText().trim(),
            tfDiaChi.getText().trim(),
            new BigDecimal(lcb)
        );
    }

    private boolean validateForm() {
        if (tfMaNV.getText().trim().isEmpty())   { UIUtils.showError(this, "Nhập Mã NV!"); return false; }
        if (tfHoTen.getText().trim().isEmpty())  { UIUtils.showError(this, "Nhập Họ Tên!"); return false; }
        if (tfChucVu.getText().trim().isEmpty()) { UIUtils.showError(this, "Nhập Chức Vụ!"); return false; }
        try {
            new BigDecimal(tfLuongCoBan.getText().trim().replace(",", "").replace(".", ""));
        } catch (NumberFormatException ex) {
            UIUtils.showError(this, "Lương cơ bản phải là số!"); return false;
        }
        return true;
    }

    private void clearForm() {
        tfMaNV.setText(""); tfHoTen.setText(""); tfChucVu.setText("");
        tfSDT.setText(""); tfDiaChi.setText(""); tfLuongCoBan.setText("");
        tfMaNV.setEditable(true);
        table.clearSelection();
    }
}
