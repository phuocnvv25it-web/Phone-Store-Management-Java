package com.quanlycuahang;

import com.quanlycuahang.config.DatabaseConnection;
import com.quanlycuahang.util.UIUtils;
import com.quanlycuahang.view.BaoHanhSuaChuaPanel;
import com.quanlycuahang.view.BanHangPanel;
import com.quanlycuahang.view.DashboardPanel;
import com.quanlycuahang.view.DienThoaiPanel;
import com.quanlycuahang.view.ImeiPanel;
import com.quanlycuahang.view.KhachHangPanel;
import com.quanlycuahang.view.LoginFrame;
import com.quanlycuahang.view.LuongPanel;
import com.quanlycuahang.view.NhanVienPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Entry point cua ung dung Quan Ly Cua Hang Dien Thoai.
 * Khoi tao JFrame chinh voi JTabbedPane chua 4 man hinh.
 */
public class MainApp extends JFrame {

    private JLabel lblClock;
    private JTabbedPane tabbedPane;

    public MainApp() {
        UIUtils.applyGlobalUI();
        initFrame();
        initComponents();
        connectDatabase();
        startClock();
    }

    // ═══════════════════════════════════════════════════════════
    // INIT
    // ═══════════════════════════════════════════════════════════

    private void initFrame() {
        setTitle("Quản lý cửa hàng điện thoại");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 750));
        setSize(1350, 820);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIUtils.BG_DARK);

        // Icon (dung emoji unicode)
        setIconImage(createIconImage());

        // Dong ung dung: dong ket noi DB
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                DatabaseConnection.closeConnection();
            }
        });
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));

        // ── Header ────────────────────────────────────────────
        add(buildHeader(), BorderLayout.NORTH);

        // ── Tabbed Pane ────────────────────────────────────────
        tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
        tabbedPane.setBackground(UIUtils.BG_DARK);
        tabbedPane.setForeground(UIUtils.TEXT_PRIMARY);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 15));

        tabbedPane.addTab("  Tổng quan  ",  null, new DashboardPanel(), "Theo dõi vận hành cửa hàng");
        tabbedPane.addTab("  Bán hàng  ",   null, new BanHangPanel(),  "Hệ thống bán hàng");
        tabbedPane.addTab("  Khách hàng  ", null, new KhachHangPanel(), "CRM, tích điểm, lịch sử khách");
        tabbedPane.addTab("  IMEI/Serial  ", null, new ImeiPanel(), "Quản lý từng thiết bị theo IMEI");
        tabbedPane.addTab("  Bảo hành  ", null, new BaoHanhSuaChuaPanel(), "Bảo hành và sửa chữa");
        tabbedPane.addTab("  Điện thoại  ", null, new DienThoaiPanel(), "Quản lý sản phẩm điện thoại");
        tabbedPane.addTab("  Nhân viên  ",  null, new NhanVienPanel(), "Quản lý thông tin nhân viên");
        tabbedPane.addTab("  Lương  ",      null, new LuongPanel(),    "Tính lương nhân viên");

        // Style cac tab
        tabbedPane.setBorder(new EmptyBorder(0, 0, 0, 0));

        add(tabbedPane, BorderLayout.CENTER);

        // ── Menu Bar ─────────────────────────────────────────
        setJMenuBar(createMenuBar());
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(UIUtils.BG_CARD);
        menuBar.setForeground(UIUtils.TEXT_PRIMARY);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.BORDER_COLOR));

        JMenu menuHeThong = new JMenu("Hệ Thống");
        menuHeThong.setForeground(UIUtils.TEXT_PRIMARY);

        JMenuItem itemDangXuat = new JMenuItem("Đăng Xuất");
        itemDangXuat.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        JMenuItem itemThoat = new JMenuItem("Thoát");
        itemThoat.addActionListener(e -> System.exit(0));

        menuHeThong.add(itemDangXuat);
        menuHeThong.addSeparator();
        menuHeThong.add(itemThoat);

        JMenu menuTroGiup = new JMenu("Trợ Giúp");
        menuTroGiup.setForeground(UIUtils.TEXT_PRIMARY);
        JMenuItem itemThongTin = new JMenuItem("Thông tin phần mềm");
        itemThongTin.addActionListener(e -> 
            JOptionPane.showMessageDialog(this, "Quản Lý Cửa Hàng Điện Thoại v1.0\nPhiên bản hoàn thiện.", "Thông tin", JOptionPane.INFORMATION_MESSAGE)
        );
        menuTroGiup.add(itemThongTin);

        menuBar.add(menuHeThong);
        menuBar.add(menuTroGiup);
        return menuBar;
    }

    // ═══════════════════════════════════════════════════════════
    // HEADER
    // ═══════════════════════════════════════════════════════════

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setBackground(UIUtils.BG_CARD);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.BORDER_COLOR),
            new EmptyBorder(14, 24, 14, 24)
        ));

        // Logo & title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titlePanel.setBackground(UIUtils.BG_CARD);

        JLabel logo = new JLabel("PS");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        logo.setOpaque(true);
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setPreferredSize(new Dimension(42, 42));
        logo.setBackground(UIUtils.PRIMARY);
        logo.setForeground(Color.WHITE);
        titlePanel.add(logo);

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        textPanel.setBackground(UIUtils.BG_CARD);
        JLabel mainTitle = new JLabel("Quản lý cửa hàng điện thoại");
        mainTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        mainTitle.setForeground(UIUtils.TEXT_PRIMARY);
        JLabel subTitle = new JLabel("Tổng quan | Bán hàng | CRM | IMEI | Bảo hành | Kho");
        subTitle.setFont(UIUtils.FONT_SMALL);
        subTitle.setForeground(UIUtils.TEXT_SECONDARY);
        textPanel.add(mainTitle);
        textPanel.add(subTitle);
        titlePanel.add(textPanel);
        header.add(titlePanel, BorderLayout.WEST);

        // Clock
        lblClock = new JLabel("--:--:--");
        lblClock.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblClock.setForeground(UIUtils.TEXT_PRIMARY);
        lblClock.setHorizontalAlignment(SwingConstants.RIGHT);
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(UIUtils.BG_CARD);
        rightPanel.add(UIUtils.createBadge("Đang hoạt động", UIUtils.SUCCESS));
        rightPanel.add(lblClock);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }



    // ═══════════════════════════════════════════════════════════
    // DATABASE CONNECTION
    // ═══════════════════════════════════════════════════════════

    private void connectDatabase() {
        // Ket noi DB trong background thread de khong block UI
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                try (Connection ignored = DatabaseConnection.getConnection()) {
                    return true;
                } catch (Exception e) {
                    System.err.println("[DB ERROR] " + e.getMessage());
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    if (!ok) {
                        JOptionPane.showMessageDialog(MainApp.this,
                            "Không kết nối được SQL Server!\n\n"
                            + "Kiểm tra:\n"
                            + "1. SQL Server đang chạy chưa?\n"
                            + "2. Tên server trong DatabaseConnection.java đúng chưa?\n"
                            + "3. File mssql-jdbc_auth-13.4.0.x64.dll đã copy vào C:\\Windows\\System32\\ chưa?",
                            "Loi Ket Noi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MainApp.this, "Loi bat ngo: " + e.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // ═══════════════════════════════════════════════════════════
    // CLOCK
    // ═══════════════════════════════════════════════════════════

    private void startClock() {
        Timer timer = new Timer(1000, e -> {
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss  dd/MM/yyyy"));
            lblClock.setText(time);
        });
        timer.start();
    }

    // ═══════════════════════════════════════════════════════════
    // ICON
    // ═══════════════════════════════════════════════════════════

    private Image createIconImage() {
        // Tao icon don gian bang Graphics
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(64, 64,
            java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(UIUtils.PRIMARY);
        g2.fillRoundRect(10, 4, 44, 56, 12, 12);
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(16, 11, 32, 40, 6, 6);
        g2.setColor(UIUtils.PRIMARY_DARK);
        g2.fillOval(29, 53, 6, 6);
        g2.dispose();
        return img;
    }

    // ═══════════════════════════════════════════════════════════
    // MAIN
    // ═══════════════════════════════════════════════════════════

    public static void main(String[] args) {
        // Bat encoding UTF-8 de hien thi tieng Viet
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("sun.stdout.encoding", "UTF-8");

        SwingUtilities.invokeLater(() -> {
            UIUtils.applyGlobalUI();
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        });
    }
}
