package com.quanlycuahang.view;

import com.quanlycuahang.dao.TaiKhoanDAO;
import com.quanlycuahang.model.TaiKhoan;
import com.quanlycuahang.util.SecurityUtils;
import com.quanlycuahang.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

public class RegisterDialog extends JDialog {
    private final JTextField tfUser = UIUtils.createTextField();
    private final JPasswordField pfPass = new JPasswordField();
    private final JPasswordField pfConfirm = new JPasswordField();
    private final TaiKhoanDAO dao = new TaiKhoanDAO();

    public RegisterDialog(Window owner) {
        super(owner, "Đăng Ký Tài Khoản", ModalityType.APPLICATION_MODAL);
        initUI();
    }

    private void initUI() {
        setSize(350, 420);
        setLocationRelativeTo(getOwner());
        getContentPane().setBackground(UIUtils.BG_DARK);

        // Styling password fields
        pfPass.setFont(UIUtils.FONT_BODY);
        pfPass.setBackground(UIUtils.BG_INPUT);
        pfPass.setForeground(UIUtils.TEXT_PRIMARY);
        pfPass.setCaretColor(UIUtils.TEXT_PRIMARY);
        pfPass.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1),
            new EmptyBorder(6, 10, 6, 10)
        ));

        pfConfirm.setFont(UIUtils.FONT_BODY);
        pfConfirm.setBackground(UIUtils.BG_INPUT);
        pfConfirm.setForeground(UIUtils.TEXT_PRIMARY);
        pfConfirm.setCaretColor(UIUtils.TEXT_PRIMARY);
        pfConfirm.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1),
            new EmptyBorder(6, 10, 6, 10)
        ));

        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 8));
        panel.setBackground(UIUtils.BG_DARK);
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel title = new JLabel("TẠO TÀI KHOẢN MỚI");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(UIUtils.PRIMARY);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(title);
        
        panel.add(new JLabel(" ")); // spacer

        panel.add(UIUtils.createLabel("Tên Đăng Nhập:"));
        panel.add(tfUser);
        panel.add(UIUtils.createLabel("Mật Khẩu:"));
        panel.add(pfPass);
        panel.add(UIUtils.createLabel("Xác Nhận Mật Khẩu:"));
        panel.add(pfConfirm);
        
        panel.add(new JLabel(" ")); // spacer

        JButton btnRegister = UIUtils.createPrimaryButton("Đăng Ký");
        btnRegister.addActionListener(e -> handleRegister());
        panel.add(btnRegister);

        add(panel);
    }

    private void handleRegister() {
        String user = tfUser.getText().trim();
        String pass = new String(pfPass.getPassword());
        String confirm = new String(pfConfirm.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            UIUtils.showError(this, "Vui lòng nhập đủ thông tin!");
            return;
        }
        if (!pass.equals(confirm)) {
            UIUtils.showError(this, "Mật khẩu xác nhận không khớp!");
            return;
        }

        try {
            if (dao.kiemTraTonTai(user)) {
                UIUtils.showError(this, "Tên đăng nhập đã tồn tại!");
                return;
            }

            String hash = SecurityUtils.hashPassword(pass);
            TaiKhoan tk = new TaiKhoan(user, hash, "NhanVien");
            if (dao.dangKy(tk)) {
                UIUtils.showSuccess(this, "Đăng ký thành công!");
                dispose();
            } else {
                UIUtils.showError(this, "Đăng ký thất bại!");
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Lỗi kết nối CSDL: " + ex.getMessage());
        }
    }
}
