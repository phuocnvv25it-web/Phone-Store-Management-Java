package com.quanlycuahang.view;

import com.quanlycuahang.dao.DienThoaiDAO;
import com.quanlycuahang.dao.HoaDonDAO;
import com.quanlycuahang.dao.NhanVienDAO;
import com.quanlycuahang.model.DienThoai;
import com.quanlycuahang.model.HoaDon;
import com.quanlycuahang.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DashboardPanel extends JPanel {

    private static final int LOW_STOCK_LIMIT = 5;

    private final DienThoaiDAO dienThoaiDAO = new DienThoaiDAO();
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();

    private final JPanel statsPanel = new JPanel(new GridLayout(1, 5, 12, 0));
    private final JLabel lblStatus = UIUtils.createLabel("Đang tải dữ liệu...");
    private final String[] columns = {"Mã", "Sản phẩm", "Hãng", "Tồn", "Giá trị tồn"};
    private final DefaultTableModel lowStockModel = new DefaultTableModel(columns, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable lowStockTable = new JTable(lowStockModel);

    public DashboardPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(UIUtils.BG_DARK);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        loadDashboard();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(UIUtils.BG_DARK);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setBackground(UIUtils.BG_DARK);
        titlePanel.add(UIUtils.createTitleLabel("Tổng quan vận hành"));
        lblStatus.setForeground(UIUtils.TEXT_MUTED);
        titlePanel.add(lblStatus);
        header.add(titlePanel, BorderLayout.WEST);

        JButton btnRefresh = UIUtils.createPrimaryButton("Làm mới");
        btnRefresh.addActionListener(e -> loadDashboard());
        header.add(btnRefresh, BorderLayout.EAST);
        return header;
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(UIUtils.BG_DARK);

        statsPanel.setBackground(UIUtils.BG_DARK);
        content.add(statsPanel, BorderLayout.NORTH);

        JPanel tablePanel = UIUtils.createCard("Cảnh báo tồn kho thấp");
        UIUtils.styleTable(lowStockTable);
        lowStockTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        lowStockTable.getColumnModel().getColumn(1).setPreferredWidth(220);
        lowStockTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        lowStockTable.getColumnModel().getColumn(3).setPreferredWidth(70);
        lowStockTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        tablePanel.add(UIUtils.createScrollPane(lowStockTable), BorderLayout.CENTER);
        content.add(tablePanel, BorderLayout.CENTER);
        return content;
    }

    private void loadDashboard() {
        lblStatus.setText("Đang cập nhật dữ liệu...");
        statsPanel.removeAll();
        statsPanel.add(UIUtils.createStatCard("Sản phẩm", "...", UIUtils.PRIMARY));
        statsPanel.add(UIUtils.createStatCard("Tồn kho", "...", UIUtils.INFO));
        statsPanel.add(UIUtils.createStatCard("Sắp hết", "...", UIUtils.WARNING));
        statsPanel.add(UIUtils.createStatCard("Hóa đơn hôm nay", "...", UIUtils.SUCCESS));
        statsPanel.add(UIUtils.createStatCard("Doanh thu hôm nay", "...", UIUtils.PRIMARY_DARK));
        statsPanel.revalidate();
        statsPanel.repaint();

        SwingWorker<DashboardData, Void> worker = new SwingWorker<>() {
            @Override
            protected DashboardData doInBackground() throws SQLException {
                return fetchData();
            }

            @Override
            protected void done() {
                try {
                    renderData(get());
                } catch (Exception ex) {
                    lblStatus.setText("Không tải được dữ liệu. Kiểm tra kết nối SQL Server.");
                    lowStockModel.setRowCount(0);
                    UIUtils.showError(DashboardPanel.this, "Loi tai tong quan:\n" + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private DashboardData fetchData() throws SQLException {
        List<DienThoai> products = dienThoaiDAO.layTatCa();
        List<HoaDon> invoices = hoaDonDAO.layTatCa();
        int employees = nhanVienDAO.layTatCa().size();

        DashboardData data = new DashboardData();
        data.productCount = products.size();
        data.employeeCount = employees;

        LocalDate today = LocalDate.now();
        for (DienThoai product : products) {
            data.totalStock += product.getSoLuong();
            BigDecimal value = product.getGia().multiply(BigDecimal.valueOf(product.getSoLuong()));
            data.inventoryValue = data.inventoryValue.add(value);
            if (product.getSoLuong() <= LOW_STOCK_LIMIT) {
                data.lowStock.add(product);
            }
        }

        for (HoaDon invoice : invoices) {
            if (invoice.getNgayBan() != null && invoice.getNgayBan().toLocalDate().equals(today)) {
                data.todayInvoices++;
                if (invoice.getTongTien() != null) {
                    data.todayRevenue = data.todayRevenue.add(invoice.getTongTien());
                }
            }
        }
        data.lowStock.sort(Comparator.comparingInt(DienThoai::getSoLuong));
        return data;
    }

    private void renderData(DashboardData data) {
        statsPanel.removeAll();
        statsPanel.add(UIUtils.createStatCard("Sản phẩm", String.valueOf(data.productCount), UIUtils.PRIMARY));
        statsPanel.add(UIUtils.createStatCard("Tồn kho", String.valueOf(data.totalStock), UIUtils.INFO));
        statsPanel.add(UIUtils.createStatCard("Sắp hết", String.valueOf(data.lowStock.size()), UIUtils.WARNING));
        statsPanel.add(UIUtils.createStatCard("Hóa đơn hôm nay", String.valueOf(data.todayInvoices), UIUtils.SUCCESS));
        statsPanel.add(UIUtils.createStatCard("Doanh thu hôm nay", formatMoney(data.todayRevenue), UIUtils.PRIMARY_DARK));
        statsPanel.revalidate();
        statsPanel.repaint();

        lowStockModel.setRowCount(0);
        for (DienThoai product : data.lowStock) {
            BigDecimal value = product.getGia().multiply(BigDecimal.valueOf(product.getSoLuong()));
            lowStockModel.addRow(new Object[]{
                product.getMaMay(),
                product.getTenMay(),
                product.getHangSX(),
                product.getSoLuong(),
                formatMoney(value)
            });
        }
        lblStatus.setText("Cập nhật lúc " + java.time.LocalTime.now().withNano(0)
            + " | Nhân viên: " + data.employeeCount
            + " | Giá trị tồn: " + formatMoney(data.inventoryValue));
    }

    private String formatMoney(BigDecimal value) {
        return String.format("%,.0f VND", value);
    }

    private static class DashboardData {
        int productCount;
        int totalStock;
        int employeeCount;
        int todayInvoices;
        BigDecimal todayRevenue = BigDecimal.ZERO;
        BigDecimal inventoryValue = BigDecimal.ZERO;
        List<DienThoai> lowStock = new ArrayList<>();
    }
}
