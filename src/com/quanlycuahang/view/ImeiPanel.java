package com.quanlycuahang.view;

import com.quanlycuahang.dao.DienThoaiDAO;
import com.quanlycuahang.dao.ImeiMayDAO;
import com.quanlycuahang.model.DienThoai;
import com.quanlycuahang.model.ImeiMay;
import com.quanlycuahang.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class ImeiPanel extends JPanel {
    private final ImeiMayDAO imeiDAO = new ImeiMayDAO();
    private final DienThoaiDAO dienThoaiDAO = new DienThoaiDAO();

    private final String[] columns = {"IMEI/Serial", "Mã máy", "Tên máy", "Trạng thái", "Mã HD", "BH", "Ngày bán", "Ghi chú"};
    private final DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    private final JTextField tfSearch = UIUtils.createSearchField("Quét/nhập IMEI, mã máy, hóa đơn...");
    private final JTextField tfImei = UIUtils.createTextField();
    private final JComboBox<String> cbMay = new JComboBox<>();
    private final JComboBox<String> cbTinhTrang = new JComboBox<>(new String[]{"TrongKho", "DaBan", "BaoHanh", "SuaChua", "DoiTra"});
    private final JTextField tfBaoHanh = UIUtils.createTextField();
    private final JTextField tfGhiChu = UIUtils.createTextField();

    private final JButton btnThem = UIUtils.createSuccessButton("Thêm");
    private final JButton btnSua = UIUtils.createWarningButton("Cập nhật");
    private final JButton btnXoa = UIUtils.createDangerButton("Xóa");
    private final JButton btnLamMoi = UIUtils.createInfoButton("Làm mới");
    private final JButton btnTim = UIUtils.createPrimaryButton("Tìm");

    public ImeiPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(UIUtils.BG_DARK);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildForm(), BorderLayout.EAST);

        loadProductCombo();
        registerEvents();
        clearForm();
        loadData();
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(UIUtils.BG_DARK);
        bar.add(UIUtils.createTitleLabel("IMEI / Serial"), BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchPanel.setBackground(UIUtils.BG_DARK);
        tfSearch.setPreferredSize(new Dimension(310, 36));
        searchPanel.add(tfSearch);
        searchPanel.add(btnTim);
        searchPanel.add(btnLamMoi);
        bar.add(searchPanel, BorderLayout.EAST);
        return bar;
    }

    private JScrollPane buildTable() {
        UIUtils.styleTable(table);
        int[] widths = {150, 80, 180, 100, 80, 60, 100, 160};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        return UIUtils.createScrollPane(table);
    }

    private JPanel buildForm() {
        JPanel form = UIUtils.createCard("Thiết bị theo IMEI");
        form.setPreferredSize(new Dimension(310, 0));
        JPanel fields = new JPanel(new GridLayout(0, 1, 0, 6));
        fields.setBackground(UIUtils.BG_CARD);

        UIUtils.styleComboBox(cbMay);
        UIUtils.styleComboBox(cbTinhTrang);
        addRow(fields, "IMEI/Serial *", tfImei);
        addRow(fields, "Sản phẩm *", cbMay);
        addRow(fields, "Trạng thái", cbTinhTrang);
        addRow(fields, "Bảo hành (tháng)", tfBaoHanh);
        addRow(fields, "Ghi chú", tfGhiChu);

        form.add(fields, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(2, 2, 8, 8));
        buttons.setBackground(UIUtils.BG_CARD);
        buttons.setBorder(new EmptyBorder(12, 0, 0, 0));
        buttons.add(btnThem);
        buttons.add(btnSua);
        buttons.add(btnXoa);
        JButton btnClear = UIUtils.createButton("Xóa form", UIUtils.BG_PANEL);
        btnClear.addActionListener(e -> clearForm());
        buttons.add(btnClear);
        form.add(buttons, BorderLayout.SOUTH);
        return form;
    }

    private void addRow(JPanel parent, String label, JComponent input) {
        JPanel row = new JPanel(new GridLayout(2, 1, 0, 2));
        row.setBackground(UIUtils.BG_CARD);
        row.add(UIUtils.createLabel(label));
        row.add(input);
        parent.add(row);
    }

    private void registerEvents() {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) fillForm(table.getSelectedRow());
        });
        btnThem.addActionListener(e -> them());
        btnSua.addActionListener(e -> sua());
        btnXoa.addActionListener(e -> xoa());
        btnTim.addActionListener(e -> timKiem());
        btnLamMoi.addActionListener(e -> { loadData(); clearForm(); });
        tfSearch.addActionListener(e -> timKiem());
        UIUtils.addDebouncedTextChangeListener(tfSearch, 250, this::timKiem);
    }

    private void loadProductCombo() {
        cbMay.removeAllItems();
        try {
            for (DienThoai dt : dienThoaiDAO.layTatCa()) {
                cbMay.addItem(dt.getMaMay() + " - " + dt.getTenMay());
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Lỗi tải sản phẩm:\n" + ex.getMessage());
        }
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try {
            for (ImeiMay item : imeiDAO.layTatCa()) tableModel.addRow(item.toTableRow());
        } catch (SQLException ex) {
            UIUtils.showError(this, "Lỗi tải IMEI:\n" + ex.getMessage());
        }
    }

    private void timKiem() {
        String kw = tfSearch.getText().trim();
        if (kw.isEmpty() || kw.equals("Quét/nhập IMEI, mã máy, hóa đơn...")) {
            loadData();
            return;
        }
        tableModel.setRowCount(0);
        try {
            for (ImeiMay item : imeiDAO.timKiem(kw)) tableModel.addRow(item.toTableRow());
        } catch (SQLException ex) {
            UIUtils.showError(this, "Lỗi tìm IMEI:\n" + ex.getMessage());
        }
    }

    private void them() {
        if (!validateForm()) return;
        try {
            if (imeiDAO.them(buildFromForm())) {
                UIUtils.showSuccess(this, "Đã thêm IMEI/Serial.");
                loadData();
                clearForm();
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Lỗi thêm IMEI:\n" + ex.getMessage());
        }
    }

    private void sua() {
        if (tfImei.isEditable()) {
            UIUtils.showError(this, "Chọn IMEI cần cập nhật.");
            return;
        }
        if (!validateForm()) return;
        try {
            if (imeiDAO.capNhat(buildFromForm())) {
                UIUtils.showSuccess(this, "Đã cập nhật IMEI/Serial.");
                loadData();
                clearForm();
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Lỗi cập nhật IMEI:\n" + ex.getMessage());
        }
    }

    private void xoa() {
        if (tfImei.isEditable()) {
            UIUtils.showError(this, "Chọn IMEI cần xóa.");
            return;
        }
        if (!UIUtils.showConfirm(this, "Xóa IMEI " + tfImei.getText().trim() + "?")) return;
        try {
            if (imeiDAO.xoa(tfImei.getText().trim())) {
                UIUtils.showSuccess(this, "Đã xóa IMEI/Serial.");
                loadData();
                clearForm();
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Lỗi xóa IMEI:\n" + ex.getMessage());
        }
    }

    private void fillForm(int row) {
        tfImei.setText(tableModel.getValueAt(row, 0).toString());
        tfImei.setEditable(false);
        selectProduct(tableModel.getValueAt(row, 1).toString());
        cbTinhTrang.setSelectedItem(rawTinhTrang(tableModel.getValueAt(row, 3).toString()));
        tfBaoHanh.setText(tableModel.getValueAt(row, 5).toString());
        tfGhiChu.setText(tableModel.getValueAt(row, 7).toString());
    }

    private ImeiMay buildFromForm() {
        return new ImeiMay(
            tfImei.getText().trim(),
            getSelectedMaMay(),
            cbTinhTrang.getSelectedItem().toString(),
            Integer.parseInt(tfBaoHanh.getText().trim()),
            tfGhiChu.getText().trim()
        );
    }

    private boolean validateForm() {
        if (tfImei.getText().trim().isEmpty()) {
            UIUtils.showError(this, "Nhập IMEI/Serial.");
            return false;
        }
        if (getSelectedMaMay() == null) {
            UIUtils.showError(this, "Chọn sản phẩm.");
            return false;
        }
        try {
            int bh = Integer.parseInt(tfBaoHanh.getText().trim());
            if (bh < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            UIUtils.showError(this, "Bảo hành phải là số tháng >= 0.");
            return false;
        }
        return true;
    }

    private String getSelectedMaMay() {
        Object selected = cbMay.getSelectedItem();
        if (selected == null) return null;
        String value = selected.toString();
        return value.contains(" - ") ? value.split(" - ")[0] : value;
    }

    private void selectProduct(String maMay) {
        for (int i = 0; i < cbMay.getItemCount(); i++) {
            if (cbMay.getItemAt(i).startsWith(maMay + " - ")) {
                cbMay.setSelectedIndex(i);
                return;
            }
        }
    }

    private String rawTinhTrang(String display) {
        if ("Trong kho".equals(display)) return "TrongKho";
        if ("Đã bán".equals(display)) return "DaBan";
        if ("Bảo hành".equals(display)) return "BaoHanh";
        if ("Sửa chữa".equals(display)) return "SuaChua";
        if ("Đổi trả".equals(display)) return "DoiTra";
        return display;
    }

    private void clearForm() {
        tfImei.setText("");
        tfImei.setEditable(true);
        if (cbMay.getItemCount() > 0) cbMay.setSelectedIndex(0);
        cbTinhTrang.setSelectedItem("TrongKho");
        tfBaoHanh.setText("12");
        tfGhiChu.setText("");
        table.clearSelection();
    }
}
