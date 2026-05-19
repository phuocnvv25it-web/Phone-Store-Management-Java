package com.quanlycuahang.view;

import com.quanlycuahang.dao.KhachHangDAO;
import com.quanlycuahang.model.KhachHang;
import com.quanlycuahang.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class KhachHangPanel extends JPanel {
    private final KhachHangDAO dao = new KhachHangDAO();

    private final String[] columns = {"Mã KH", "Họ tên", "SĐT", "Email", "Địa chỉ", "Điểm", "Ghi chú"};
    private final DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    private final JTextField tfSearch = UIUtils.createSearchField("Tìm theo tên, SĐT, mã...");
    private final JTextField tfMaKH = UIUtils.createTextField();
    private final JTextField tfHoTen = UIUtils.createTextField();
    private final JTextField tfSDT = UIUtils.createTextField();
    private final JTextField tfEmail = UIUtils.createTextField();
    private final JTextField tfDiaChi = UIUtils.createTextField();
    private final JTextField tfDiem = UIUtils.createTextField();
    private final JTextField tfGhiChu = UIUtils.createTextField();

    private final JButton btnThem = UIUtils.createSuccessButton("Thêm");
    private final JButton btnSua = UIUtils.createWarningButton("Cập nhật");
    private final JButton btnXoa = UIUtils.createDangerButton("Xóa");
    private final JButton btnLamMoi = UIUtils.createInfoButton("Làm mới");
    private final JButton btnTim = UIUtils.createPrimaryButton("Tìm");

    public KhachHangPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(UIUtils.BG_DARK);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildForm(), BorderLayout.EAST);

        registerEvents();
        clearForm();
        loadData();
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(UIUtils.BG_DARK);
        bar.add(UIUtils.createTitleLabel("Khách hàng / CRM"), BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchPanel.setBackground(UIUtils.BG_DARK);
        tfSearch.setPreferredSize(new Dimension(260, 36));
        searchPanel.add(tfSearch);
        searchPanel.add(btnTim);
        searchPanel.add(btnLamMoi);
        bar.add(searchPanel, BorderLayout.EAST);
        return bar;
    }

    private JScrollPane buildTable() {
        UIUtils.styleTable(table);
        int[] widths = {70, 160, 110, 150, 180, 70, 160};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        return UIUtils.createScrollPane(table);
    }

    private JPanel buildForm() {
        JPanel form = UIUtils.createCard("Thông tin khách hàng");
        form.setPreferredSize(new Dimension(300, 0));
        JPanel fields = new JPanel(new GridLayout(0, 1, 0, 6));
        fields.setBackground(UIUtils.BG_CARD);

        tfMaKH.setEditable(false);
        addRow(fields, "Mã KH", tfMaKH);
        addRow(fields, "Họ tên *", tfHoTen);
        addRow(fields, "SĐT *", tfSDT);
        addRow(fields, "Email", tfEmail);
        addRow(fields, "Địa chỉ", tfDiaChi);
        addRow(fields, "Điểm tích lũy", tfDiem);
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

    private void loadData() {
        tableModel.setRowCount(0);
        try {
            for (KhachHang kh : dao.layTatCa()) tableModel.addRow(kh.toTableRow());
        } catch (SQLException ex) {
            UIUtils.showError(this, "Lỗi tải khách hàng:\n" + ex.getMessage());
        }
    }

    private void timKiem() {
        String kw = tfSearch.getText().trim();
        if (kw.isEmpty() || kw.equals("Tìm theo tên, SĐT, mã...")) {
            loadData();
            return;
        }
        tableModel.setRowCount(0);
        try {
            for (KhachHang kh : dao.timKiem(kw)) tableModel.addRow(kh.toTableRow());
        } catch (SQLException ex) {
            UIUtils.showError(this, "Lỗi tìm kiếm khách hàng:\n" + ex.getMessage());
        }
    }

    private void them() {
        if (!validateForm()) return;
        try {
            if (dao.timTheoSDT(tfSDT.getText().trim()) != null) {
                UIUtils.showError(this, "SĐT này đã có trong danh sách khách hàng.");
                return;
            }
            KhachHang kh = buildFromForm();
            kh.setMaKH(dao.sinhMaKhachHang());
            if (dao.them(kh)) {
                UIUtils.showSuccess(this, "Đã thêm khách hàng.");
                loadData();
                clearForm();
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Lỗi thêm khách hàng:\n" + ex.getMessage());
        }
    }

    private void sua() {
        if (tfMaKH.getText().trim().isEmpty()) {
            UIUtils.showError(this, "Chọn khách hàng cần cập nhật.");
            return;
        }
        if (!validateForm()) return;
        try {
            if (dao.capNhat(buildFromForm())) {
                UIUtils.showSuccess(this, "Đã cập nhật khách hàng.");
                loadData();
                clearForm();
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Lỗi cập nhật khách hàng:\n" + ex.getMessage());
        }
    }

    private void xoa() {
        if (tfMaKH.getText().trim().isEmpty()) {
            UIUtils.showError(this, "Chọn khách hàng cần xóa.");
            return;
        }
        if (!UIUtils.showConfirm(this, "Xóa khách hàng " + tfHoTen.getText().trim() + "?")) return;
        try {
            if (dao.xoa(tfMaKH.getText().trim())) {
                UIUtils.showSuccess(this, "Đã xóa khách hàng.");
                loadData();
                clearForm();
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Lỗi xóa khách hàng:\n" + ex.getMessage());
        }
    }

    private void fillForm(int row) {
        tfMaKH.setText(tableModel.getValueAt(row, 0).toString());
        tfHoTen.setText(tableModel.getValueAt(row, 1).toString());
        tfSDT.setText(tableModel.getValueAt(row, 2).toString());
        tfEmail.setText(tableModel.getValueAt(row, 3).toString());
        tfDiaChi.setText(tableModel.getValueAt(row, 4).toString());
        tfDiem.setText(tableModel.getValueAt(row, 5).toString());
        tfGhiChu.setText(tableModel.getValueAt(row, 6).toString());
    }

    private KhachHang buildFromForm() {
        return new KhachHang(
            tfMaKH.getText().trim(),
            tfHoTen.getText().trim(),
            tfSDT.getText().trim(),
            tfEmail.getText().trim(),
            tfDiaChi.getText().trim(),
            Integer.parseInt(tfDiem.getText().trim()),
            tfGhiChu.getText().trim()
        );
    }

    private boolean validateForm() {
        if (tfHoTen.getText().trim().isEmpty()) {
            UIUtils.showError(this, "Nhập họ tên khách hàng.");
            return false;
        }
        if (tfSDT.getText().trim().isEmpty()) {
            UIUtils.showError(this, "Nhập số điện thoại khách hàng.");
            return false;
        }
        try {
            int diem = Integer.parseInt(tfDiem.getText().trim());
            if (diem < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            UIUtils.showError(this, "Điểm tích lũy phải là số nguyên >= 0.");
            return false;
        }
        return true;
    }

    private void clearForm() {
        tfMaKH.setText("");
        tfHoTen.setText("");
        tfSDT.setText("");
        tfEmail.setText("");
        tfDiaChi.setText("");
        tfDiem.setText("0");
        tfGhiChu.setText("");
        table.clearSelection();
    }
}
