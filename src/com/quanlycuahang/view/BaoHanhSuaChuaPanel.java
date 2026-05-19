package com.quanlycuahang.view;

import com.quanlycuahang.dao.PhieuDichVuDAO;
import com.quanlycuahang.model.PhieuDichVu;
import com.quanlycuahang.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;

public class BaoHanhSuaChuaPanel extends JPanel {
    private final PhieuDichVuDAO dao = new PhieuDichVuDAO();

    private final String[] columns = {"Mã phiếu", "Loại", "IMEI", "Máy", "Khách hàng", "SĐT",
        "Ngày nhận", "Hẹn trả", "Trạng thái", "Chi phí", "Ghi chú"};
    private final DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    private final JTextField tfSearch = UIUtils.createSearchField("Tìm IMEI, SĐT, khách hàng...");
    private final JTextField tfMaPhieu = UIUtils.createTextField();
    private final JComboBox<String> cbLoai = new JComboBox<>(new String[]{"BaoHanh", "SuaChua"});
    private final JTextField tfImei = UIUtils.createTextField();
    private final JTextField tfKhachHang = UIUtils.createTextField();
    private final JTextField tfSDT = UIUtils.createTextField();
    private final JTextField tfHenTra = UIUtils.createTextField();
    private final JComboBox<String> cbTrangThai = new JComboBox<>(new String[]{"DangXuLy", "ChoLinhKien", "HoanThanh", "DaTra"});
    private final JTextField tfChiPhi = UIUtils.createTextField();
    private final JTextField tfGhiChu = UIUtils.createTextField();

    private final JButton btnTao = UIUtils.createSuccessButton("Tạo phiếu");
    private final JButton btnSua = UIUtils.createWarningButton("Cập nhật");
    private final JButton btnXoa = UIUtils.createDangerButton("Xóa");
    private final JButton btnTim = UIUtils.createPrimaryButton("Tìm");
    private final JButton btnLamMoi = UIUtils.createInfoButton("Làm mới");

    public BaoHanhSuaChuaPanel() {
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
        bar.add(UIUtils.createTitleLabel("Bảo hành / Sửa chữa"), BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchPanel.setBackground(UIUtils.BG_DARK);
        tfSearch.setPreferredSize(new Dimension(300, 36));
        searchPanel.add(tfSearch);
        searchPanel.add(btnTim);
        searchPanel.add(btnLamMoi);
        bar.add(searchPanel, BorderLayout.EAST);
        return bar;
    }

    private JScrollPane buildTable() {
        UIUtils.styleTable(table);
        int[] widths = {80, 90, 140, 160, 150, 100, 100, 100, 110, 110, 180};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        return UIUtils.createScrollPane(table);
    }

    private JPanel buildForm() {
        JPanel form = UIUtils.createCard("Phiếu dịch vụ");
        form.setPreferredSize(new Dimension(320, 0));
        JPanel fields = new JPanel(new GridLayout(0, 1, 0, 6));
        fields.setBackground(UIUtils.BG_CARD);

        tfMaPhieu.setEditable(false);
        UIUtils.styleComboBox(cbLoai);
        UIUtils.styleComboBox(cbTrangThai);
        addRow(fields, "Mã phiếu", tfMaPhieu);
        addRow(fields, "Loại phiếu", cbLoai);
        addRow(fields, "IMEI/Serial", tfImei);
        addRow(fields, "Khách hàng *", tfKhachHang);
        addRow(fields, "SĐT khách", tfSDT);
        addRow(fields, "Hẹn trả (yyyy-MM-dd)", tfHenTra);
        addRow(fields, "Trạng thái", cbTrangThai);
        addRow(fields, "Chi phí", tfChiPhi);
        addRow(fields, "Ghi chú", tfGhiChu);

        form.add(fields, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(2, 2, 8, 8));
        buttons.setBackground(UIUtils.BG_CARD);
        buttons.setBorder(new EmptyBorder(12, 0, 0, 0));
        buttons.add(btnTao);
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
        btnTao.addActionListener(e -> tao());
        btnSua.addActionListener(e -> sua());
        btnXoa.addActionListener(e -> xoa());
        btnTim.addActionListener(e -> timKiem());
        btnLamMoi.addActionListener(e -> { loadData(); clearForm(); });
        cbLoai.addActionListener(e -> goiYMaPhieu());
        tfSearch.addActionListener(e -> timKiem());
        UIUtils.addDebouncedTextChangeListener(tfSearch, 250, this::timKiem);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try {
            for (PhieuDichVu phieu : dao.layTatCa()) tableModel.addRow(phieu.toTableRow());
        } catch (SQLException ex) {
            UIUtils.showError(this, "Lỗi tải phiếu dịch vụ:\n" + ex.getMessage());
        }
    }

    private void timKiem() {
        String kw = tfSearch.getText().trim();
        if (kw.isEmpty() || kw.equals("Tìm IMEI, SĐT, khách hàng...")) {
            loadData();
            return;
        }
        tableModel.setRowCount(0);
        try {
            for (PhieuDichVu phieu : dao.timKiem(kw)) tableModel.addRow(phieu.toTableRow());
        } catch (SQLException ex) {
            UIUtils.showError(this, "Lỗi tìm phiếu dịch vụ:\n" + ex.getMessage());
        }
    }

    private void tao() {
        if (!validateForm()) return;
        try {
            PhieuDichVu phieu = buildFromForm();
            phieu.setMaPhieu(dao.sinhMaPhieu(phieu.getLoai()));
            if (dao.them(phieu)) {
                UIUtils.showSuccess(this, "Đã tạo phiếu dịch vụ.");
                loadData();
                clearForm();
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Lỗi tạo phiếu:\n" + ex.getMessage());
        }
    }

    private void sua() {
        if (tfMaPhieu.getText().trim().isEmpty()) {
            UIUtils.showError(this, "Chọn phiếu cần cập nhật.");
            return;
        }
        if (!validateForm()) return;
        try {
            if (dao.capNhat(buildFromForm())) {
                UIUtils.showSuccess(this, "Đã cập nhật phiếu dịch vụ.");
                loadData();
                clearForm();
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Lỗi cập nhật phiếu:\n" + ex.getMessage());
        }
    }

    private void xoa() {
        if (tfMaPhieu.getText().trim().isEmpty()) {
            UIUtils.showError(this, "Chọn phiếu cần xóa.");
            return;
        }
        if (!UIUtils.showConfirm(this, "Xóa phiếu " + tfMaPhieu.getText().trim() + "?")) return;
        try {
            if (dao.xoa(tfMaPhieu.getText().trim())) {
                UIUtils.showSuccess(this, "Đã xóa phiếu dịch vụ.");
                loadData();
                clearForm();
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Lỗi xóa phiếu:\n" + ex.getMessage());
        }
    }

    private void fillForm(int row) {
        tfMaPhieu.setText(tableModel.getValueAt(row, 0).toString());
        cbLoai.setSelectedItem(rawLoai(tableModel.getValueAt(row, 1).toString()));
        tfImei.setText(tableModel.getValueAt(row, 2).toString());
        tfKhachHang.setText(tableModel.getValueAt(row, 4).toString());
        tfSDT.setText(tableModel.getValueAt(row, 5).toString());
        tfHenTra.setText(tableModel.getValueAt(row, 7).toString());
        cbTrangThai.setSelectedItem(rawTrangThai(tableModel.getValueAt(row, 8).toString()));
        tfChiPhi.setText(tableModel.getValueAt(row, 9).toString().replace(" VND", "").replace(",", ""));
        tfGhiChu.setText(tableModel.getValueAt(row, 10).toString());
    }

    private PhieuDichVu buildFromForm() {
        String henTra = tfHenTra.getText().trim();
        String chiPhi = tfChiPhi.getText().trim().replace(",", "").replace(".", "");
        return new PhieuDichVu(
            tfMaPhieu.getText().trim(),
            cbLoai.getSelectedItem().toString(),
            tfImei.getText().trim(),
            tfKhachHang.getText().trim(),
            tfSDT.getText().trim(),
            henTra.isEmpty() ? null : LocalDate.parse(henTra),
            cbTrangThai.getSelectedItem().toString(),
            chiPhi.isEmpty() ? BigDecimal.ZERO : new BigDecimal(chiPhi),
            tfGhiChu.getText().trim()
        );
    }

    private boolean validateForm() {
        if (tfKhachHang.getText().trim().isEmpty()) {
            UIUtils.showError(this, "Nhập tên khách hàng.");
            return false;
        }
        try {
            String henTra = tfHenTra.getText().trim();
            if (!henTra.isEmpty()) LocalDate.parse(henTra);
        } catch (Exception ex) {
            UIUtils.showError(this, "Ngày hẹn trả phải theo định dạng yyyy-MM-dd.");
            return false;
        }
        try {
            String chiPhi = tfChiPhi.getText().trim().replace(",", "").replace(".", "");
            if (!chiPhi.isEmpty() && new BigDecimal(chiPhi).compareTo(BigDecimal.ZERO) < 0) {
                throw new NumberFormatException();
            }
        } catch (Exception ex) {
            UIUtils.showError(this, "Chi phí phải là số >= 0.");
            return false;
        }
        return true;
    }

    private void goiYMaPhieu() {
        if (!tfMaPhieu.getText().trim().isEmpty()) return;
        try {
            tfMaPhieu.setText(dao.sinhMaPhieu(cbLoai.getSelectedItem().toString()));
        } catch (SQLException ignored) {
            tfMaPhieu.setText("");
        }
    }

    private String rawLoai(String display) {
        if ("Bảo hành".equals(display)) return "BaoHanh";
        if ("Sửa chữa".equals(display)) return "SuaChua";
        return display;
    }

    private String rawTrangThai(String display) {
        if ("Đang xử lý".equals(display)) return "DangXuLy";
        if ("Chờ linh kiện".equals(display)) return "ChoLinhKien";
        if ("Hoàn thành".equals(display)) return "HoanThanh";
        if ("Đã trả".equals(display)) return "DaTra";
        return display;
    }

    private void clearForm() {
        tfMaPhieu.setText("");
        if (cbLoai.getItemCount() > 0) cbLoai.setSelectedIndex(0);
        tfImei.setText("");
        tfKhachHang.setText("");
        tfSDT.setText("");
        tfHenTra.setText(LocalDate.now().plusDays(7).toString());
        cbTrangThai.setSelectedItem("DangXuLy");
        tfChiPhi.setText("0");
        tfGhiChu.setText("");
        table.clearSelection();
        goiYMaPhieu();
    }
}
