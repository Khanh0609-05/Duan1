package View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.border.*;

public class LoginView extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin, btnForgotPassword;
    private JLabel lblStatus;
    private Connection conn;
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=BanHangTaiQuay;encrypt=true;trustServerCertificate=true";
    private static final String USER = "sa"; // Replace with your username
    private static final String PASS = "123"; // Replace with your password

    public LoginView() {
        // Set up the frame
        setTitle("Đăng nhập hệ thống");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Initialize connection
        initializeConnection();

        // Create components with modern design
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(245, 245, 245));

        // Header panel with logo
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(new Color(245, 245, 245));
        JLabel logoLabel = new JLabel("SHOP QUẢN LÝ", JLabel.CENTER);
        logoLabel.setFont(new Font("Arial", Font.BOLD, 24));
        logoLabel.setForeground(new Color(41, 128, 185));
        headerPanel.add(logoLabel);

        // Login panel
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridBagLayout());
        loginPanel.setBackground(new Color(245, 245, 245));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Username
        JLabel lblUsername = new JLabel("Tài khoản:");
        lblUsername.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        loginPanel.add(lblUsername, gbc);

        txtUsername = new JTextField(20);
        txtUsername.setFont(new Font("Arial", Font.PLAIN, 14));
        txtUsername.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        gbc.gridx = 1;
        gbc.gridy = 0;
        loginPanel.add(txtUsername, gbc);

        // Password
        JLabel lblPassword = new JLabel("Mật khẩu:");
        lblPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        loginPanel.add(lblPassword, gbc);

        txtPassword = new JPasswordField(20);
        txtPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        gbc.gridx = 1;
        gbc.gridy = 1;
        loginPanel.add(txtPassword, gbc);

        // Status label
        lblStatus = new JLabel("");
        lblStatus.setForeground(Color.RED);
        lblStatus.setFont(new Font("Arial", Font.PLAIN, 12));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        loginPanel.add(lblStatus, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(new Color(245, 245, 245));

        btnLogin = new JButton("Đăng nhập");
        btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
        btnLogin.setBackground(new Color(41, 128, 185));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorder(new EmptyBorder(8, 15, 8, 15));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> login());

        btnForgotPassword = new JButton("Quên mật khẩu");
        btnForgotPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        btnForgotPassword.setBackground(new Color(245, 245, 245));
        btnForgotPassword.setForeground(new Color(41, 128, 185));
        btnForgotPassword.setBorderPainted(false);
        btnForgotPassword.setContentAreaFilled(false);
        btnForgotPassword.setFocusPainted(false);
        btnForgotPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnForgotPassword.addActionListener(e -> forgotPassword());

        buttonPanel.add(btnLogin);
        buttonPanel.add(btnForgotPassword);

        // Add all panels to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(loginPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add main panel to frame
        add(mainPanel);

        // Add key listener for Enter key
        KeyAdapter enterKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    login();
                }
            }
        };
        txtUsername.addKeyListener(enterKeyListener);
        txtPassword.addKeyListener(enterKeyListener);
    }

    private void initializeConnection() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (ClassNotFoundException | SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Không thể kết nối đến cơ sở dữ liệu: " + e.getMessage(),
                    "Lỗi kết nối",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void login() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            lblStatus.setText("Vui lòng nhập đầy đủ thông tin đăng nhập!");
            return;
        }

        try {
            // Thêm trường trạng thái vào câu truy vấn
            String query = "SELECT * FROM NhanVien WHERE MaNhanVien = ? AND MatKhau = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, username);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                // Kiểm tra trạng thái nhân viên
                int trangThai = rs.getInt("TrangThai"); // Giả sử có cột TrangThai trong DB

                if (trangThai == 0) { // 0 = không hoạt động hoặc đã nghỉ việc
                    lblStatus.setText("Tài khoản của bạn đã bị vô hiệu hóa!");
                    return;
                }

                int userRole = rs.getInt("IDChucVu");
                String employeeName = rs.getString("TenNhanVien");

                // Store user info in session or global variables if needed
                UserSession.getInstance().setCurrentUser(rs.getInt("ID"), employeeName, userRole);

                lblStatus.setText("");
                JOptionPane.showMessageDialog(this, "Đăng nhập thành công! Xin chào " + employeeName);

                // Open main application
                openMainApplication();
            } else {
                lblStatus.setText("Sai tên đăng nhập hoặc mật khẩu!");
            }

            rs.close();
            pst.close();
        } catch (SQLException e) {
            lblStatus.setText("Lỗi đăng nhập: " + e.getMessage());
        }
    }

    private void forgotPassword() {
        JTextField usernameField = new JTextField();
        JTextField phoneField = new JTextField();

        Object[] message = {
            "Vui lòng nhập thông tin để lấy lại mật khẩu:",
            "Mã nhân viên:", usernameField,
            "Số điện thoại:", phoneField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Quên mật khẩu", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String phone = phoneField.getText();

            if (username.trim().isEmpty() || phone.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
                return;
            }

            try {
                String query = "SELECT * FROM NhanVien WHERE MaNhanVien = ? AND SDT = ?";
                PreparedStatement pst = conn.prepareStatement(query);
                pst.setString(1, username);
                pst.setString(2, phone);
                ResultSet rs = pst.executeQuery();

                if (rs.next()) {
                    // Found the user, allow resetting password
                    showResetPasswordDialog(rs.getInt("ID"));
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Không tìm thấy thông tin nhân viên!",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }

                rs.close();
                pst.close();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Lỗi truy vấn: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showResetPasswordDialog(int userId) {
        JPasswordField newPassField = new JPasswordField();
        JPasswordField confirmPassField = new JPasswordField();

        Object[] message = {
            "Đặt lại mật khẩu:",
            "Mật khẩu mới:", newPassField,
            "Xác nhận mật khẩu:", confirmPassField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Đặt lại mật khẩu", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String newPass = new String(newPassField.getPassword());
            String confirmPass = new String(confirmPassField.getPassword());

            if (newPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Mật khẩu không được để trống!");
                return;
            }

            if (!newPass.equals(confirmPass)) {
                JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp!");
                return;
            }

            try {
                String query = "UPDATE NhanVien SET MatKhau = ? WHERE ID = ?";
                PreparedStatement pst = conn.prepareStatement(query);
                pst.setString(1, newPass);
                pst.setInt(2, userId);
                int result = pst.executeUpdate();

                if (result > 0) {
                    JOptionPane.showMessageDialog(this,
                            "Đặt lại mật khẩu thành công! Vui lòng đăng nhập lại.",
                            "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Không thể đặt lại mật khẩu!",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }

                pst.close();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Lỗi cập nhật: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openMainApplication() {
        this.dispose(); // Close login window
        SwingUtilities.invokeLater(() -> {
            MainMenu.main(new String[0]); // Start main application
        });
    }

    public static void logout() {
        // Clear user session
        UserSession.getInstance().clearSession();

        // Close all windows - đảm bảo đóng tất cả các cửa sổ hiện tại
        for (Window window : Window.getWindows()) {
            window.dispose();
        }

        // Show login screen again - đảm bảo chạy trong EDT (Event Dispatch Thread)
        SwingUtilities.invokeLater(() -> {
            new LoginView().setVisible(true);
        });
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            LoginView loginView = new LoginView();
            loginView.setVisible(true);
        });
    }
}
