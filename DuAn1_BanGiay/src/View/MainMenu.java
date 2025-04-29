package View;

import javax.swing.*;
import java.awt.*;

public class MainMenu extends JFrame {

    private static MainMenu mainFrame;
    private static JPanel currentContent;

    public MainMenu() {
        setTitle("Phần mềm quản lý - " + UserSession.getInstance().getUserName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        mainFrame = this;

        initComponents();

        setVisible(true);
    }

    private void initComponents() {
        setView(new BanHangView().getContent());

        JMenuBar menuBar = new JMenuBar();
        JMenu salesMenu = new JMenu("Bán hàng");
        JMenu productMenu = new JMenu("Sản phẩm");
        JMenu invoiceMenu = new JMenu("Hóa đơn");
        JMenu staffMenu = new JMenu("Nhân viên"); // Menu dành cho quản lý nhân viên
        JMenu discountMenu = new JMenu("Phiếu giảm giá");
        JMenu customerMenu = new JMenu("Khách hàng");
        JMenu statisticMenu = new JMenu("Thống kê");

        JMenuItem banHangItem = new JMenuItem("Bán Hàng");
        banHangItem.addActionListener(e -> setView(new BanHangView().getContent()));
        JMenuItem sanPhamItem = new JMenuItem("Sản Phẩm");
        sanPhamItem.addActionListener(e -> setView(new SanPhamView().getContent()));

        JMenuItem hoaDonItem = new JMenuItem("Hóa Đơn");
        hoaDonItem.addActionListener(e -> setView(new HoaDonView().getContent()));

        JMenuItem nhanVienItem = new JMenuItem("Nhân Viên");
        nhanVienItem.addActionListener(e -> setView(new NhanVienView())); // Quản lý nhân viên

        JMenuItem phieuGiamGiaItem = new JMenuItem("Phiếu Giảm Giá");
        phieuGiamGiaItem.addActionListener(e -> setView(new PhieuGiamGiaView().getContent()));

        JMenuItem khachHangItem = new JMenuItem("Quản Lý Khách Hàng");
        khachHangItem.addActionListener(e -> setView(new QuanLyKhachHangView().getContent()));

        JMenuItem thongKeItem = new JMenuItem("Thống Kê");
        thongKeItem.addActionListener(e -> setView(new ThongKeView().getContent()));

        salesMenu.add(banHangItem);
        productMenu.add(sanPhamItem);
        invoiceMenu.add(hoaDonItem);
        staffMenu.add(nhanVienItem);  // Mục "Nhân Viên" chỉ dành cho admin
        discountMenu.add(phieuGiamGiaItem);
        customerMenu.add(khachHangItem);
        statisticMenu.add(thongKeItem);

        menuBar.add(salesMenu); // Bán hàng luôn có
        menuBar.add(invoiceMenu); // Hóa đơn luôn có
        menuBar.add(customerMenu); // Khách hàng luôn có
        menuBar.add(statisticMenu); // Thống kê luôn có

        if (UserSession.getInstance().isAdmin()) {
            menuBar.add(productMenu); // Sản phẩm - chỉ admin
            menuBar.add(staffMenu);   // Nhân viên - chỉ admin
            menuBar.add(discountMenu); // Phiếu giảm giá - chỉ admin
        }
        JMenu accountMenu = new JMenu("Tài khoản");

        JMenuItem userInfoItem = new JMenuItem("Thông tin tài khoản");
        userInfoItem.addActionListener(e -> showUserInfo());

        JMenuItem logoutItem = new JMenuItem("Đăng xuất");
        logoutItem.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(mainFrame,
                    "Bạn có chắc chắn muốn đăng xuất?",
                    "Xác nhận đăng xuất",
                    JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                LoginView.logout();
            }
        });

        JMenuItem exitItem = new JMenuItem("Thoát ứng dụng");
        exitItem.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(mainFrame,
                    "Bạn có chắc chắn muốn thoát ứng dụng?",
                    "Xác nhận thoát",
                    JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        accountMenu.add(userInfoItem);
        accountMenu.add(logoutItem);
        accountMenu.addSeparator();
        accountMenu.add(exitItem);

        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(accountMenu);

        setJMenuBar(menuBar);
    }

    private void showUserInfo() {
        UserSession session = UserSession.getInstance();
        String roleText = session.isAdmin() ? "Quản lý" : "Nhân viên";
        String statusText = session.getUserStatusText();

        JOptionPane.showMessageDialog(this,
                "ID: " + session.getUserId() + "\n"
                + "Tên: " + session.getUserName() + "\n"
                + "Chức vụ: " + roleText + "\n"
                + "Trạng thái: " + statusText,
                "Thông tin tài khoản",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Check if user is logged in
        if (!UserSession.getInstance().isLoggedIn()) {
            // If not logged in, show login form
            SwingUtilities.invokeLater(() -> {
                new LoginView().setVisible(true);
            });
            return;
        }

        // If logged in, show main application
        SwingUtilities.invokeLater(() -> {
            mainFrame = new MainMenu();
            mainFrame.setVisible(true);
        });
    }

    private static void setView(JPanel newContent) {
        if (currentContent != null) {
            mainFrame.remove(currentContent);
        }
        currentContent = newContent;
        mainFrame.add(currentContent, BorderLayout.CENTER);
        mainFrame.pack();
        mainFrame.revalidate();
        mainFrame.repaint();
    }
}
