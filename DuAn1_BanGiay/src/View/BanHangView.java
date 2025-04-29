package View;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
import Repo.KhachHangRepo;
import Model.HoaDon;
import Model.HoaDonChiTiet;
import Model.KhachHang;
import duan1_bangiay.model.DanhSachDonHang;
import Model.PhieuGiamGia;
import Repo.GioHangRepo;
import Repo.DanhSachDonHangRepo;
import Repo.HoaDonChiTietRepo;
import Repo.HoaDonRepo;
import Repo.NhanVienRepo;
import Repo.PhieuGiamGiaRepo;
import Repo.ThongTinSanPhamRepo;

import Until.DBConnect;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import duan1_bangiay.model.NhanVien;
import java.awt.Desktop;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import javax.swing.JOptionPane;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.Date;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import javax.swing.table.DefaultTableCellRenderer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import javax.swing.JPanel;

/**
 *
 * @author quanr
 */
public class BanHangView extends javax.swing.JFrame {

    private String maHoaDonHienTai = null;
    PhieuGiamGiaRepo pggRepo = new PhieuGiamGiaRepo();

    public BanHangView() {
        initComponents();

        tblHoaDonChiTiet.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            private DecimalFormat formatter = new DecimalFormat("###,###,###");

            @Override
            protected void setValue(Object value) {
                if (value instanceof BigDecimal) {
                    String formatted = formatter.format(((BigDecimal) value).setScale(0, RoundingMode.HALF_UP));
                    setText(formatted.replace(",", "."));
                    super.setValue(value);
                }
            }
        });

// Định dạng cột Thành Tiền
        tblHoaDonChiTiet.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            private DecimalFormat formatter = new DecimalFormat("###,###,###");

            @Override
            protected void setValue(Object value) {
                if (value instanceof BigDecimal) {
                    String formatted = formatter.format(((BigDecimal) value).setScale(0, RoundingMode.HALF_UP));
                    setText(formatted.replace(",", ".")); // Thay dấu phẩy bằng dấu chấm
                } else {
                    super.setValue(value);
                }
            }
        });
        loadTables();
        loadPhieuGiamGia();
        searchListener();
        donHangListener();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        txtNgayTao.setText(now.format(formatter));
        Timer timer = new Timer(1000, e -> {
            LocalDateTime currentTime = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            txtNgayTao.setText(currentTime.format(formatter));
        });
        timer.start();

        txtNgayTao.setText(java.time.LocalDateTime.now().toString());
        txtMaNhanVien.setText(txtMaNhanVien.getText());
        txtSoDienThoai.addActionListener(e -> checkSdtKhachHang(txtSoDienThoai.getText()));
        txtSoTienTra.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                tinhTienDu();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                tinhTienDu();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                tinhTienDu();
            }
        });
        txtTongTien.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                capNhatThanhTien();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                capNhatThanhTien();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                capNhatThanhTien();
            }
        });
        cboPGG.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                capNhatThanhTien();
            }
        });
    }

    private void capNhatThanhTien() {
        try {
            String tongTienStr = txtTongTien.getText().trim().replace(".", "");
            if (tongTienStr.isEmpty()) {
                txtThanhTien.setText("");
                return;
            }
            BigDecimal tongTien = new BigDecimal(tongTienStr);
            BigDecimal giamGia = BigDecimal.ZERO;
            String selectedMaPGG = (String) cboPGG.getSelectedItem();

            if (selectedMaPGG != null && !selectedMaPGG.equals("Không chọn") && !selectedMaPGG.equals(" ")) {
                PhieuGiamGia pgg = pggRepo.getActivePhieuGiamGia().stream()
                        .filter(p -> p.getMaPhieuGiamGia().equals(selectedMaPGG))
                        .findFirst()
                        .orElse(null);

                if (pgg != null) {
                    BigDecimal hoaDonToiThieu = BigDecimal.valueOf(pgg.getHoaDonToiThieu());
                    if (tongTien.compareTo(hoaDonToiThieu) < 0) {
                        JOptionPane.showMessageDialog(null,
                                "Hóa đơn chưa đạt giá trị tối thiểu " + formatVND(hoaDonToiThieu) + " để sử dụng phiếu giảm giá!");
                        cboPGG.setSelectedItem("Không chọn");
                        txtThanhTien.setText(formatVND(tongTien));
                        return;
                    }

                    if (pgg.isKieuGiam()) {
                        giamGia = tongTien.multiply(BigDecimal.valueOf(pgg.getMucGiam()))
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                        BigDecimal mucGiamToiDa = BigDecimal.valueOf(pgg.getMucGiamToiDa());
                        if (giamGia.compareTo(mucGiamToiDa) > 0) {
                            giamGia = mucGiamToiDa;
                        }
                    } else {
                        giamGia = BigDecimal.valueOf(pgg.getMucGiam());
                    }
                }
            }

            BigDecimal thanhTien = tongTien.subtract(giamGia).max(BigDecimal.ZERO);
            txtThanhTien.setText(formatVND(thanhTien));

        } catch (NumberFormatException ex) {
            txtThanhTien.setText("");
            JOptionPane.showMessageDialog(null, "Vui lòng nhập số hợp lệ cho tổng tiền!");
        }
    }

    private void searchListener() {
        Timer searchTimer = new Timer(300, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchAndUpdateTable();
            }
        });
        searchTimer.setRepeats(false); // Chỉ chạy một lần sau khi dừng nhập

        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                searchTimer.restart();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                searchTimer.restart();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                searchTimer.restart();
            }
        });
    }

    private void donHangListener() {
        tblChuaThanhToan.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int selectedRow = tblChuaThanhToan.getSelectedRow();
                if (selectedRow != -1) {
                    handleOrderRowClick(selectedRow, tblChuaThanhToan);
                    dayThongTinLenCacO(selectedRow, tblChuaThanhToan); // Truyền bảng
                }
            }
        });
        tblDaThanhToan.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int selectedRow = tblDaThanhToan.getSelectedRow();
                if (selectedRow != -1) {
                    handleOrderRowClick(selectedRow, tblDaThanhToan);
                    dayThongTinLenCacO(selectedRow, tblDaThanhToan); // Truyền bảng
                }
            }
        });
    }

    private void clearGioHang() {
        DefaultTableModel tblGioHang = (DefaultTableModel) tblHoaDonChiTiet.getModel();
        tblGioHang.setRowCount(0);
    }

    private void clearTextFields() {
        txtMaHoaDon.setText("");
        txtSoDienThoai.setText("");
        txtTenKhachHang.setText("");
        txtMaNhanVien.setText("");
        txtTongTien.setText("");
        txtThanhTien.setText("");
        txtSoTienTra.setText("");
        txtTienDu.setText("");
        txtTimKiem.setText("");
        rdoNam.setSelected(false);
        rdoNu.setSelected(false);

    }

    private void dayThongTinLenCacO(int selectedRow, JTable sourceTable) {
        try {
            DefaultTableModel model = (DefaultTableModel) sourceTable.getModel();
            String maHoaDon = model.getValueAt(selectedRow, 0).toString();

            String sql = "SELECT hd.MaHoaDon, hd.TongTien, hd.ThanhTien, kh.SDT, kh.TenKhachHang, kh.GioiTinh, nv.MaNhanVien, hd.IDPhieuGiamGia "
                    + "FROM HoaDon hd "
                    + "JOIN KhachHang kh ON hd.IDKhachHang = kh.ID "
                    + "JOIN NhanVien nv ON hd.IDNhanVien = nv.ID "
                    + "WHERE hd.MaHoaDon = ?";

            try (Connection connection = DBConnect.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, maHoaDon);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        txtMaHoaDon.setText(rs.getString("MaHoaDon"));
                        txtSoDienThoai.setText(rs.getString("SDT"));
                        txtTenKhachHang.setText(rs.getString("TenKhachHang"));
                        rdoNam.setSelected(rs.getInt("GioiTinh") == 1);
                        rdoNu.setSelected(rs.getInt("GioiTinh") == 0);
                        txtMaNhanVien.setText(rs.getString("MaNhanVien"));
                        BigDecimal tongTien = rs.getBigDecimal("TongTien");
                        BigDecimal thanhTien = rs.getBigDecimal("ThanhTien");
                        // Định dạng giá trị trước khi gán vào txtTongTien và txtThanhTien
                        txtTongTien.setText(tongTien != null ? formatVND(tongTien) : "");
                        txtThanhTien.setText(thanhTien != null ? formatVND(thanhTien) : "");

                        Integer idPhieuGiamGia = (Integer) rs.getObject("IDPhieuGiamGia");

                        if (idPhieuGiamGia != null) {
                            String sqlPGG = "SELECT MaPhieuGiamGia FROM PhieuGiamGia WHERE ID = ?";
                            try (PreparedStatement psPGG = connection.prepareStatement(sqlPGG)) {
                                psPGG.setInt(1, idPhieuGiamGia);
                                try (ResultSet rsPGG = psPGG.executeQuery()) {
                                    if (rsPGG.next()) {
                                        String maPhieuGiamGia = rsPGG.getString("MaPhieuGiamGia");
                                        cboPGG.setSelectedItem(maPhieuGiamGia);
                                    } else {
                                        cboPGG.setSelectedItem(" ");
                                    }
                                }
                            }
                        } else {
                            cboPGG.setSelectedItem(" ");
                        }

                        boolean daThanhToan = kiemTraTrangThaiHoaDon(maHoaDon);
                        txtMaHoaDon.setEditable(false);
                        txtSoDienThoai.setEditable(false);
                        txtTenKhachHang.setEditable(false);
                        txtMaNhanVien.setEditable(false);
                        txtTongTien.setEditable(false);
                        txtThanhTien.setEditable(false);
                        txtSoTienTra.setEnabled(!daThanhToan);
                        txtTienDu.setEnabled(!daThanhToan);
                        rdoNam.setEnabled(!daThanhToan);
                        rdoNu.setEnabled(!daThanhToan);
                        cboPGG.setEnabled(!daThanhToan);
                    } else {
                        JOptionPane.showMessageDialog(null, "Không tìm thấy hóa đơn!");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi khi truy vấn thông tin hóa đơn: " + e.getMessage());
        }
    }

    private void handleOrderRowClick(int selectedRow, JTable sourceTable) {
        try {
            DefaultTableModel sourceModel = (DefaultTableModel) sourceTable.getModel();
            maHoaDonHienTai = sourceModel.getValueAt(selectedRow, 0).toString(); // Lấy mã hóa đơn hiện tại

            // Cập nhật bảng tblHoaDonChiTiet
            capNhatChiTietHoaDon(maHoaDonHienTai);
            tinhTongTienTuTblHoaDonChiTiet();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi khi xử lý hàng đơn: " + e.getMessage());
        }
    }

    private void capNhatChiTietHoaDon(String maHoaDon) {
        List<Object[]> danhSachChiTiet = layDanhSachChiTietHoaDonTuCSDL(maHoaDon);

        DefaultTableModel model = (DefaultTableModel) tblHoaDonChiTiet.getModel();
        model.setRowCount(0);

        for (Object[] dong : danhSachChiTiet) {
            model.addRow(dong);
        }

    }

 
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblHoaDonChiTiet = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblSanPham = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        btnHuyDon = new javax.swing.JButton();
        btnTaoDon = new javax.swing.JButton();
        btnThanhToan = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        txtTimKiem = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        btnInBill = new javax.swing.JButton();
        tblHoaDon = new javax.swing.JTabbedPane();
        jScrollPane4 = new javax.swing.JScrollPane();
        tblChuaThanhToan = new javax.swing.JTable();
        jScrollPane5 = new javax.swing.JScrollPane();
        tblDaThanhToan = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        txtNgayTao = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtSoDienThoai = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtTenKhachHang = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtThanhTien = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtTienDu = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtSoTienTra = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        txtTongTien = new javax.swing.JTextField();
        rdoNam = new javax.swing.JRadioButton();
        rdoNu = new javax.swing.JRadioButton();
        txtMaNhanVien = new javax.swing.JTextField();
        cboPGG = new javax.swing.JComboBox<>();
        jLabel18 = new javax.swing.JLabel();
        txtMaHoaDon = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        lblTongTien = new javax.swing.JLabel();
        btnLamMoi = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setAutoRequestFocus(false);
        setBackground(new java.awt.Color(204, 204, 204));

        tblHoaDonChiTiet.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "STT", "Tên Hàng Hóa", "Đơn Giá", "Số lượng", "Thành Tiền"
            }
        ));
        tblHoaDonChiTiet.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblHoaDonChiTietMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tblHoaDonChiTiet);

        tblSanPham.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "STT", "Mã SP", "Tên SP", "Thương Hiệu", "Giá Bán", "Số Lượng", "Size", "Màu Sắc"
            }
        ));
        tblSanPham.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblSanPhamMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(tblSanPham);
        if (tblSanPham.getColumnModel().getColumnCount() > 0) {
            tblSanPham.getColumnModel().getColumn(0).setHeaderValue("STT");
            tblSanPham.getColumnModel().getColumn(1).setHeaderValue("Mã SP");
            tblSanPham.getColumnModel().getColumn(2).setHeaderValue("Tên SP");
            tblSanPham.getColumnModel().getColumn(3).setHeaderValue("Thương Hiệu");
            tblSanPham.getColumnModel().getColumn(4).setHeaderValue("Giá Bán");
            tblSanPham.getColumnModel().getColumn(5).setHeaderValue("Số Lượng");
            tblSanPham.getColumnModel().getColumn(6).setHeaderValue("Size");
            tblSanPham.getColumnModel().getColumn(7).setHeaderValue("Màu Sắc");
        }

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 30)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 153, 153));
        jLabel1.setText("QUẢN LÍ BÁN HÀNG");

        btnHuyDon.setText("HỦY ĐƠN");
        btnHuyDon.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 0, 0)));
        btnHuyDon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHuyDonActionPerformed(evt);
            }
        });

        btnTaoDon.setText("TẠO ĐƠN");
        btnTaoDon.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 255, 51)));
        btnTaoDon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTaoDonActionPerformed(evt);
            }
        });

        btnThanhToan.setText("THANH TOÁN");
        btnThanhToan.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 255, 255)));
        btnThanhToan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnThanhToanActionPerformed(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel10.setText("Giỏ Hàng");

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel11.setText("Thông Tin Sản Phẩm");

        jLabel12.setText("Tìm Kiếm");

        txtTimKiem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTimKiemActionPerformed(evt);
            }
        });

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel13.setText("Danh sách đơn hàng");

        btnInBill.setText("IN BILL");
        btnInBill.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 0)));
        btnInBill.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInBillActionPerformed(evt);
            }
        });

        tblChuaThanhToan.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Mã Hóa Đơn", "Mã Khách Hàng", "Mã Nhân Viên", "Mã Giảm Giá", "Ngày Mua", "Tổng Tiền"
            }
        ));
        tblChuaThanhToan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblChuaThanhToanMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(tblChuaThanhToan);

        tblHoaDon.addTab("Chưa thanh toán", jScrollPane4);

        tblDaThanhToan.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Mã Hóa Đơn", "Mã Khách Hàng", "Mã Nhân Viên", "Mã Giảm Giá", "Ngày Mua", "Tổng Tiền", "Thành tiền"
            }
        ));
        tblDaThanhToan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblDaThanhToanMouseClicked(evt);
            }
        });
        jScrollPane5.setViewportView(tblDaThanhToan);

        tblHoaDon.addTab("Đã thanh toán", jScrollPane5);

        jLabel2.setText("Mã Hóa Đơn");

        txtNgayTao.setEditable(false);

        jLabel3.setText("Số Điện Thoại");

        jLabel4.setText("Tên Khách Hàng");

        jLabel5.setText("Mã Nhân Viên");

        jLabel6.setText("Thành Tiền");

        txtThanhTien.setEditable(false);

        jLabel7.setText("Tiền Dư");

        txtTienDu.setEditable(false);

        jLabel8.setText("Số Tiền Trả");

        jLabel9.setText("Phiếu Giảm Giá");

        jLabel14.setText("Tổng Tiền");

        jLabel15.setText("Giới Tính");

        txtTongTien.setEditable(false);

        buttonGroup1.add(rdoNam);
        rdoNam.setText("Nam");
        rdoNam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdoNamActionPerformed(evt);
            }
        });

        buttonGroup1.add(rdoNu);
        rdoNu.setText("Nữ");

        jLabel18.setText("Ngày Tạo");

        txtMaHoaDon.setEditable(false);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(txtMaHoaDon, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(txtSoDienThoai, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel15))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 24, Short.MAX_VALUE)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(rdoNam)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(rdoNu))
                                    .addComponent(txtTenKhachHang, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(18, 18, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel18))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtMaNhanVien, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtNgayTao, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(17, 17, 17)))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel14)
                        .addGap(47, 47, 47)
                        .addComponent(txtTongTien, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                            .addComponent(jLabel9)
                            .addGap(18, 18, 18)
                            .addComponent(cboPGG, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                            .addGap(1, 1, 1)
                            .addComponent(jLabel7)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtTienDu, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel8)
                                .addComponent(jLabel6))
                            .addGap(41, 41, 41)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtSoTienTra, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtThanhTien, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(25, 25, 25))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(txtMaHoaDon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(txtSoDienThoai, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(23, 23, 23)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(txtTenKhachHang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6)
                            .addComponent(txtThanhTien, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel14)
                            .addComponent(txtTongTien, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9)
                            .addComponent(cboPGG, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(45, 45, 45)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(jLabel8)
                    .addComponent(txtSoTienTra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rdoNam)
                    .addComponent(rdoNu))
                .addGap(28, 28, 28)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel7)
                        .addComponent(txtTienDu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtMaNhanVien, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel5))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel18)
                    .addComponent(txtNgayTao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(23, 23, 23))
        );

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel17.setText("Tổng Tiền :");

        btnLamMoi.setText("LÀM MỚI");
        btnLamMoi.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 255, 51)));
        btnLamMoi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLamMoiActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(494, 494, 494)
                        .addComponent(jLabel1))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 673, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel17)
                                .addGap(9, 9, 9)
                                .addComponent(lblTongTien)))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(516, 516, 516)
                                .addComponent(btnHuyDon, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 620, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel12)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtTimKiem, javax.swing.GroupLayout.PREFERRED_SIZE, 312, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 673, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel13)
                            .addComponent(tblHoaDon, javax.swing.GroupLayout.PREFERRED_SIZE, 620, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnTaoDon, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnThanhToan, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnInBill, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnLamMoi, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jLabel11))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(jLabel1)
                .addGap(12, 12, 12)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 306, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel17)
                            .addComponent(lblTongTien))
                        .addGap(31, 31, 31)
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnLamMoi, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(btnInBill, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnHuyDon, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnThanhToan, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnTaoDon, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel12)
                            .addComponent(txtTimKiem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 261, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(tblHoaDon, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    private void btnHuyDonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHuyDonActionPerformed
 
        String maHoaDon = txtMaHoaDon.getText().trim();
        if (maHoaDon.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Vui lòng chọn mã hóa đơn cần xóa!");
            return;
        }

        if (kiemTraTrangThaiHoaDon(maHoaDon)) {
            JOptionPane.showMessageDialog(null, "Hóa đơn đã thanh toán, không thể hủy!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(null,
                "Bạn có chắc chắn muốn xóa đơn hàng này khỏi cơ sở dữ liệu?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try (Connection connection = DBConnect.getConnection()) {
            connection.setAutoCommit(false);
            String selectChiTietSql = "SELECT IDSanPham, SoLuong FROM ChiTietHoaDon WHERE IDHoaDon = (SELECT ID FROM HoaDon WHERE MaHoaDon = ?)";
            try (PreparedStatement psSelect = connection.prepareStatement(selectChiTietSql)) {
                psSelect.setString(1, maHoaDon);
                try (ResultSet rs = psSelect.executeQuery()) {
                    while (rs.next()) {
                        int idSanPham = rs.getInt("IDSanPham");
                        int soLuong = rs.getInt("SoLuong");
                        capNhatSoLuongSanPham(idSanPham, -soLuong);
                    }
                }
            }
            String deleteChiTietSql = "DELETE FROM ChiTietHoaDon WHERE IDHoaDon = (SELECT ID FROM HoaDon WHERE MaHoaDon = ?)";
            try (PreparedStatement psChiTiet = connection.prepareStatement(deleteChiTietSql)) {
                psChiTiet.setString(1, maHoaDon);
                psChiTiet.executeUpdate();
            }
            String deleteHoaDonSql = "DELETE FROM HoaDon WHERE MaHoaDon = ?";
            try (PreparedStatement psHoaDon = connection.prepareStatement(deleteHoaDonSql)) {
                psHoaDon.setString(1, maHoaDon);
                int rowsAffected = psHoaDon.executeUpdate();

                if (rowsAffected > 0) {
                    connection.commit();

                    btnLamMoiActionPerformed(evt);
                } else {
                    connection.rollback();
                    JOptionPane.showMessageDialog(null, "Không tìm thấy đơn hàng để xóa!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi khi xóa hóa đơn: " + e.getMessage());
        }
    }//GEN-LAST:event_btnHuyDonActionPerformed

    private void btnTaoDonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTaoDonActionPerformed
        // TODO add your handling code here:
        taoHoaDon();
        loadTables();
    }//GEN-LAST:event_btnTaoDonActionPerformed

    private void btnThanhToanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnThanhToanActionPerformed

        try {
            if (maHoaDonHienTai == null) {
                JOptionPane.showMessageDialog(null, "Vui lòng chọn một hóa đơn từ danh sách chưa thanh toán!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (kiemTraTrangThaiHoaDon(maHoaDonHienTai)) {
                JOptionPane.showMessageDialog(null, "Không thể thanh toán hóa đơn đã thanh toán!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String maHoaDon = txtMaHoaDon.getText().trim();
            String thanhTienStr = txtThanhTien.getText().trim().replace(".", "");
            String soTienTraStr = txtSoTienTra.getText().trim().replace(".", "");

            if (maHoaDon.isEmpty() || thanhTienStr.isEmpty() || soTienTraStr.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Vui lòng điền đầy đủ thông tin trước khi thanh toán!");
                return;
            }

            BigDecimal thanhTien = new BigDecimal(thanhTienStr);
            BigDecimal soTienTra = new BigDecimal(soTienTraStr);

            BigDecimal tienDu = soTienTra.subtract(thanhTien);
            if (tienDu.compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(null, "Số tiền trả không đủ để thanh toán hóa đơn!");
                return;
            }

            txtTienDu.setText(formatVND(tienDu));

            String selectedMaPGG = (String) cboPGG.getSelectedItem();
            BigDecimal giamGia = BigDecimal.ZERO;
            Integer idPhieuGiamGia = null;

            if (selectedMaPGG != null && !selectedMaPGG.equals(" ") && !selectedMaPGG.equals("Không chọn")) {
                PhieuGiamGia pgg = pggRepo.getActivePhieuGiamGia().stream()
                        .filter(p -> p.getMaPhieuGiamGia().equals(selectedMaPGG))
                        .findFirst()
                        .orElse(null);

                if (pgg != null) {
                    if (pgg.getDaDung() >= pgg.getSoLuong()) {
                        JOptionPane.showMessageDialog(null, "Phiếu giảm giá đã hết lượt sử dụng!");
                        cboPGG.setSelectedItem(" ");
                        return;
                    }

                    BigDecimal tongTien = new BigDecimal(txtTongTien.getText().trim().replace(".", ""));
                    BigDecimal hoaDonToiThieu = BigDecimal.valueOf(pgg.getHoaDonToiThieu());
                    if (tongTien.compareTo(hoaDonToiThieu) < 0) {
                        JOptionPane.showMessageDialog(null,
                                "Hóa đơn chưa đạt giá trị tối thiểu " + formatVND(hoaDonToiThieu) + " để sử dụng phiếu giảm giá!");
                        cboPGG.setSelectedItem(" ");
                        return;
                    }

                    if (pgg.isKieuGiam()) {
                        giamGia = tongTien.multiply(BigDecimal.valueOf(pgg.getMucGiam()))
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                        BigDecimal mucGiamToiDa = BigDecimal.valueOf(pgg.getMucGiamToiDa());
                        if (giamGia.compareTo(mucGiamToiDa) > 0) {
                            giamGia = mucGiamToiDa;
                        }
                    } else {
                        giamGia = BigDecimal.valueOf(pgg.getMucGiam());
                    }
                    idPhieuGiamGia = pgg.getId();
                }
            }

            try (Connection connection = DBConnect.getConnection()) {
                connection.setAutoCommit(false);

                String sql = "UPDATE HoaDon SET TongTien = ?, GiamGia = ?, ThanhTien = ?, TrangThai = ?, IDPhieuGiamGia = ? WHERE MaHoaDon = ?";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setBigDecimal(1, new BigDecimal(txtTongTien.getText().trim().replace(".", "")));
                    ps.setBigDecimal(2, giamGia);
                    ps.setBigDecimal(3, thanhTien);
                    ps.setInt(4, 1); // Đã thanh toán
                    ps.setObject(5, idPhieuGiamGia, Types.INTEGER);
                    ps.setString(6, maHoaDon);

                    int rowsAffected = ps.executeUpdate();
                    if (rowsAffected > 0) {
                        if (idPhieuGiamGia != null) {
                            String updatePGG = "UPDATE PhieuGiamGia SET DaDung = DaDung + 1 WHERE ID = ?";
                            try (PreparedStatement psPGG = connection.prepareStatement(updatePGG)) {
                                psPGG.setInt(1, idPhieuGiamGia);
                                psPGG.executeUpdate();
                            }
                        }
                        connection.commit();
                        JOptionPane.showMessageDialog(null, "Thanh toán thành công!");
                        loadTables(); // Làm mới bảng
                        clearTextFields();
                        clearGioHang();
                        cboPGG.setSelectedItem(" ");
                    } else {
                        connection.rollback();
                        JOptionPane.showMessageDialog(null, "Không tìm thấy hóa đơn để cập nhật!");
                    }
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Số tiền không hợp lệ! Vui lòng nhập số.");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi khi cập nhật hóa đơn: " + e.getMessage());
        }

    }//GEN-LAST:event_btnThanhToanActionPerformed

    private void txtTimKiemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTimKiemActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTimKiemActionPerformed

    private void btnInBillActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInBillActionPerformed
        // TODO add your handling code here:
        int index = tblDaThanhToan.getSelectedRow();
        if (index >= 0) {
            String maHoaDon = tblDaThanhToan.getValueAt(index, 0).toString();
            HoaDonChiTietRepo repoHDCT = new HoaDonChiTietRepo();
            HoaDonRepo repoHD = new HoaDonRepo();
            NhanVienRepo repoNV = new NhanVienRepo();
            KhachHangRepo repoKH = new KhachHangRepo();
            PhieuGiamGiaRepo repoPGG = new PhieuGiamGiaRepo();

            ArrayList<HoaDonChiTiet> danhSachChiTiet = repoHDCT.getChiTietHoaDon(maHoaDon);
            HoaDon hoaDon = repoHD.getHoaDonByMa(maHoaDon);

            if (danhSachChiTiet != null && !danhSachChiTiet.isEmpty() && hoaDon != null) {
                int option = JOptionPane.showConfirmDialog(this, "Bạn có muốn xuất hóa đơn ra PDF?", "Xác nhận", JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {
                    try {
                        Document document = new Document(PageSize.A4);
                        String pdfFilePath = "HoaDon_" + maHoaDon + ".pdf";
                        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfFilePath));
                        document.open();

                        // Font chữ
                        BaseFont bf = BaseFont.createFont("c:/windows/fonts/times.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                        Font fontTitle = new Font(bf, 18, Font.BOLD);
                        Font fontHeader = new Font(bf, 12, Font.BOLD);
                        Font fontData = new Font(bf, 11, Font.NORMAL);
                        Font fontBold = new Font(bf, 11, Font.BOLD);
                        Font fontItalic = new Font(bf, 10, Font.ITALIC);
                        Font fontSmall = new Font(bf, 9, Font.NORMAL);

                        // Logo và tên cửa hàng
                        Paragraph brand = new Paragraph("SHOEHUB", new Font(bf, 24, Font.BOLD));
                        brand.setAlignment(Element.ALIGN_CENTER);
                        document.add(brand);

                        Paragraph slogan = new Paragraph("Thời trang cho bước chân của bạn", new Font(bf, 12, Font.ITALIC));
                        slogan.setAlignment(Element.ALIGN_CENTER);
                        document.add(slogan);

                        document.add(new Paragraph("-------------------------------------------------------------------------------------------------------------------------------------", fontData));

                        // Thông tin hóa đơn
                        Paragraph invoiceTitle = new Paragraph("HÓA ĐƠN BÁN HÀNG", fontTitle);
                        invoiceTitle.setAlignment(Element.ALIGN_CENTER);
                        document.add(invoiceTitle);

                        NhanVien nhanVien = repoNV.getNhanVienByMa(hoaDon.getMaNhanVien());
                        KhachHang khachHang = repoKH.getKhachHangByMa(hoaDon.getMaKhachHang());

                        // Thông tin chung của hóa đơn
                        PdfPTable infoTable = new PdfPTable(2);
                        infoTable.setWidthPercentage(100);
                        infoTable.setSpacingBefore(10);
                        infoTable.setSpacingAfter(10);

                        // Cột trái - thông tin hóa đơn
                        PdfPCell leftCell = new PdfPCell();
                        leftCell.setBorder(Rectangle.NO_BORDER);
                        leftCell.addElement(new Phrase("Mã hóa đơn: " + maHoaDon, fontBold));
                        if (hoaDon.getNgayTao() != null) {
                            // Chuyển LocalDateTime sang Date
                            LocalDateTime localDateTime = hoaDon.getNgayTao();
                            Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());

                            // Định dạng ngày tháng theo định dạng mong muốn
                            String formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(date);

                            // Thêm vào cell
                            leftCell.addElement(new Phrase("Ngày tạo: " + formattedDate, fontData));
                        } else {
                            leftCell.addElement(new Phrase("Ngày tạo: N/A", fontData));
                        }
                        leftCell.addElement(new Phrase("Nhân viên: " + nhanVien.getTenNhanVien() + " (" + nhanVien.getMaNhanVien() + ")", fontData));
                        infoTable.addCell(leftCell);

                        // Cột phải - thông tin khách hàng
                        PdfPCell rightCell = new PdfPCell();
                        rightCell.setBorder(Rectangle.NO_BORDER);
                        rightCell.addElement(new Phrase("Khách hàng: " + khachHang.getTenKhachHang(), fontBold));
                        rightCell.addElement(new Phrase("SĐT: " + khachHang.getSdt(), fontData));
                        rightCell.addElement(new Phrase("Địa chỉ: " + khachHang.getDiaChi(), fontData));
                        infoTable.addCell(rightCell);

                        document.add(infoTable);

                        // Chi tiết sản phẩm
                        Paragraph productTitle = new Paragraph("CHI TIẾT SẢN PHẨM", fontHeader);
                        productTitle.setAlignment(Element.ALIGN_CENTER);
                        document.add(productTitle);

                        PdfPTable pdfTable = new PdfPTable(5);
                        pdfTable.setWidthPercentage(100);
                        pdfTable.setSpacingBefore(10);
                        pdfTable.setWidths(new float[]{15f, 30f, 15f, 20f, 20f});

                        // Xóa viền cho bảng chi tiết sản phẩm
                        pdfTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

                        // Header của bảng
                        PdfPCell cell1 = new PdfPCell(new Phrase("Mã SP", fontHeader));
                        PdfPCell cell2 = new PdfPCell(new Phrase("Tên Sản Phẩm", fontHeader));
                        PdfPCell cell3 = new PdfPCell(new Phrase("Số Lượng", fontHeader));
                        PdfPCell cell4 = new PdfPCell(new Phrase("Đơn Giá (VNĐ)", fontHeader));
                        PdfPCell cell5 = new PdfPCell(new Phrase("Thành Tiền (VNĐ)", fontHeader));

                        cell1.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        cell2.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        cell3.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        cell4.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        cell5.setBackgroundColor(BaseColor.LIGHT_GRAY);

                        // Xóa viền cho header của bảng sản phẩm
                        cell1.setBorder(Rectangle.NO_BORDER);
                        cell2.setBorder(Rectangle.NO_BORDER);
                        cell3.setBorder(Rectangle.NO_BORDER);
                        cell4.setBorder(Rectangle.NO_BORDER);
                        cell5.setBorder(Rectangle.NO_BORDER);

                        cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell4.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell5.setHorizontalAlignment(Element.ALIGN_CENTER);

                        pdfTable.addCell(cell1);
                        pdfTable.addCell(cell2);
                        pdfTable.addCell(cell3);
                        pdfTable.addCell(cell4);
                        pdfTable.addCell(cell5);

                        // Data của bảng
                        DecimalFormat df = new DecimalFormat("#,###");
                        for (HoaDonChiTiet cthd : danhSachChiTiet) {
                            PdfPCell dataCell1 = new PdfPCell(new Phrase(cthd.getMaSanPham(), fontData));
                            PdfPCell dataCell2 = new PdfPCell(new Phrase(cthd.getTenSanPham(), fontData));
                            PdfPCell dataCell3 = new PdfPCell(new Phrase(String.valueOf(cthd.getSoLuong()), fontData));
                            PdfPCell dataCell4 = new PdfPCell(new Phrase(df.format(cthd.getDonGia()), fontData));
                            PdfPCell dataCell5 = new PdfPCell(new Phrase(df.format(cthd.getSoLuong() * cthd.getDonGia()), fontData));

                            // Xóa viền cho các ô dữ liệu
                            dataCell1.setBorder(Rectangle.NO_BORDER);
                            dataCell2.setBorder(Rectangle.NO_BORDER);
                            dataCell3.setBorder(Rectangle.NO_BORDER);
                            dataCell4.setBorder(Rectangle.NO_BORDER);
                            dataCell5.setBorder(Rectangle.NO_BORDER);

                            dataCell3.setHorizontalAlignment(Element.ALIGN_CENTER);
                            dataCell4.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            dataCell5.setHorizontalAlignment(Element.ALIGN_RIGHT);

                            pdfTable.addCell(dataCell1);
                            pdfTable.addCell(dataCell2);
                            pdfTable.addCell(dataCell3);
                            pdfTable.addCell(dataCell4);
                            pdfTable.addCell(dataCell5);
                        }

                        document.add(pdfTable);

                        // Thông tin thanh toán
                        PdfPTable summaryTable = new PdfPTable(2);
                        summaryTable.setWidthPercentage(50);
                        summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        summaryTable.setSpacingBefore(10);
                        summaryTable.setWidths(new float[]{50f, 50f});

                        // Xóa viền cho bảng thông tin thanh toán
                        summaryTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

                        // Hiển thị thông tin giảm giá
                        PhieuGiamGia phieuGiamGia = null;
                        phieuGiamGia = repoPGG.getPhieuGiamGiaByMa(hoaDon.getMaPhieuGiamGia());

                        // Xóa viền cho các ô trong bảng thông tin thanh toán
                        PdfPCell tongTienLabel = new PdfPCell(new Phrase("Tổng tiền:", fontBold));
                        PdfPCell tongTienValue = new PdfPCell(new Phrase(df.format(hoaDon.getTongTien()) + " VNĐ", fontData));
                        tongTienLabel.setBorder(Rectangle.NO_BORDER);
                        tongTienValue.setBorder(Rectangle.NO_BORDER);
                        tongTienValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        summaryTable.addCell(tongTienLabel);
                        summaryTable.addCell(tongTienValue);

                        if (phieuGiamGia != null) {
                            PdfPCell maGiamGiaLabel = new PdfPCell(new Phrase("Mã giảm giá:", fontBold));
                            PdfPCell maGiamGiaValue = new PdfPCell(new Phrase(phieuGiamGia.getMaPhieuGiamGia(), fontData));
                            maGiamGiaLabel.setBorder(Rectangle.NO_BORDER);
                            maGiamGiaValue.setBorder(Rectangle.NO_BORDER);
                            maGiamGiaValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            summaryTable.addCell(maGiamGiaLabel);
                            summaryTable.addCell(maGiamGiaValue);
                        }

                        PdfPCell giamGiaLabel = new PdfPCell(new Phrase("Giảm giá:", fontBold));
                        PdfPCell giamGiaValue = new PdfPCell(new Phrase(df.format(hoaDon.getGiamGia()) + " VNĐ", fontData));
                        giamGiaLabel.setBorder(Rectangle.NO_BORDER);
                        giamGiaValue.setBorder(Rectangle.NO_BORDER);
                        giamGiaValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        summaryTable.addCell(giamGiaLabel);
                        summaryTable.addCell(giamGiaValue);

                        // Xóa viền cho dòng thành tiền nhưng giữ màu nền
                        PdfPCell thanhTienLabel = new PdfPCell(new Phrase("Thành tiền:", new Font(bf, 12, Font.BOLD)));
                        PdfPCell thanhTienValue = new PdfPCell(new Phrase(df.format(hoaDon.getThanhToan()) + " VNĐ", new Font(bf, 12, Font.BOLD)));
                        thanhTienLabel.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        thanhTienValue.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        thanhTienLabel.setBorder(Rectangle.NO_BORDER);
                        thanhTienValue.setBorder(Rectangle.NO_BORDER);
                        thanhTienValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        summaryTable.addCell(thanhTienLabel);
                        summaryTable.addCell(thanhTienValue);

                        document.add(summaryTable);

                        // Thêm lời cảm ơn
                        document.add(new Paragraph("\n", fontData));
                        Paragraph thankYou = new Paragraph("Cảm ơn Quý khách đã mua hàng tại SHOE HUB!", fontItalic);
                        thankYou.setAlignment(Element.ALIGN_CENTER);
                        document.add(thankYou);

                        Paragraph returnPolicy = new Paragraph("Hàng đã mua không được đổi trả nếu không có lỗi từ nhà sản xuất.", fontSmall);
                        returnPolicy.setAlignment(Element.ALIGN_CENTER);
                        document.add(returnPolicy);

                        // Thông tin liên hệ
                        document.add(new Paragraph("\n", fontSmall));
                        Paragraph contactInfo = new Paragraph("Hotline: 18009098 - Website: www.shoehub.com.vn", fontSmall);
                        contactInfo.setAlignment(Element.ALIGN_CENTER);
                        document.add(contactInfo);

                        // Ngày giờ in hóa đơn
                        document.close();
                       
                        // Mở file PDF
                        File pdfFile = new File(pdfFilePath);
                        if (pdfFile.exists() && Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().open(pdfFile);
                        }
                    } catch (DocumentException | IOException e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Lỗi khi xuất PDF: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy chi tiết hóa đơn!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "vui lòng chọn hóa đơn đã thanh toán", "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_btnInBillActionPerformed

    private void rdoNamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdoNamActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rdoNamActionPerformed

    private void tblSanPhamMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblSanPhamMouseClicked

        if (maHoaDonHienTai == null) {
            JOptionPane.showMessageDialog(null, "Chưa chọn hóa đơn để thêm sản phẩm!");
            return;
        }

        boolean daThanhToan = kiemTraTrangThaiHoaDon(maHoaDonHienTai);
        if (daThanhToan) {
            JOptionPane.showMessageDialog(null, "Không thể thêm sản phẩm vào hóa đơn đã thanh toán!");
            return;
        }

        int row = tblSanPham.getSelectedRow();
        if (row != -1) {
            try {
                String maSP = tblSanPham.getValueAt(row, 1).toString();
                String tenSP = tblSanPham.getValueAt(row, 2).toString();
                String giaBanStr = tblSanPham.getValueAt(row, 4).toString().replace(".", "");
                Integer soLuongTon = Integer.parseInt(tblSanPham.getValueAt(row, 5).toString());

                if (soLuongTon == null || soLuongTon < 0) {
                    JOptionPane.showMessageDialog(null, "Số lượng tồn kho không hợp lệ!");
                    return;
                }

                BigDecimal donGia;
                try {
                    donGia = new BigDecimal(giaBanStr);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Giá bán của sản phẩm không hợp lệ!");
                    return;
                }

                String soLuongStr = JOptionPane.showInputDialog("Nhập số lượng cho sản phẩm: " + tenSP);
                if (soLuongStr == null || soLuongStr.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Bạn chưa nhập số lượng!");
                    return;
                }

                soLuongStr = soLuongStr.trim();
                if (!soLuongStr.matches("\\d+")) {
                    JOptionPane.showMessageDialog(null, "Số lượng phải là số nguyên dương!");
                    return;
                }

                int soLuong = Integer.parseInt(soLuongStr);
                if (soLuong <= 0) {
                    JOptionPane.showMessageDialog(null, "Số lượng phải lớn hơn 0!");
                    return;
                }
                if (soLuong > soLuongTon) {
                    JOptionPane.showMessageDialog(null, "Không thể thêm sản phẩm \"" + tenSP + "\". Số lượng tồn kho chỉ còn: " + soLuongTon);
                    return;
                }

                boolean productExists = kiemTraSanPhamTonTaiTrongHoaDon(maHoaDonHienTai, maSP);
                if (productExists) {
                    capNhatSoLuongSanPhamTrongHoaDon(maHoaDonHienTai, maSP, soLuong);
                } else {
                    themSanPhamVaoChiTietHoaDon(maHoaDonHienTai, maSP, soLuong, donGia);
                }

                int idSanPham = getIdSanPhamFromMaSP(maSP);
                capNhatSoLuongSanPham(idSanPham, soLuong);

                capNhatChiTietHoaDon(maHoaDonHienTai);
                // Chỉ làm mới tblSanPham
                DefaultTableModel modelSanPham = (DefaultTableModel) tblSanPham.getModel();
                ThongTinSanPhamRepo sanPhamRepository = new ThongTinSanPhamRepo();
                fillToTableSanPham(modelSanPham, sanPhamRepository.getAllSanPham());

                capNhatTongTienChoHoaDon();
                JOptionPane.showMessageDialog(null, "Đã thêm sản phẩm \"" + tenSP + "\" thành công và cập nhật số lượng!");
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Số lượng không hợp lệ! Vui lòng nhập số nguyên.");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Đã xảy ra lỗi khi thêm sản phẩm: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(null, "Vui lòng chọn sản phẩm để thêm!");
        }
    }

    public boolean kiemTraSanPhamTonTaiTrongHoaDon(String maHoaDon, String maSP) {
        String sql = "SELECT COUNT(*) FROM ChiTietHoaDon WHERE IDHoaDon = (SELECT ID FROM HoaDon WHERE MaHoaDon = ?) AND IDSanPham = (SELECT ID FROM SanPham WHERE MaSanPham = ?)";
        try (Connection connection = DBConnect.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, maHoaDon);
            ps.setString(2, maSP);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Returns true if the product exists
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi khi kiểm tra sản phẩm: " + e.getMessage());
        }
        return false; // Return false if no match is found
    }

    public void capNhatSoLuongSanPhamTrongHoaDon(String maHoaDon, String maSP, int soLuongThem) {
        String sql = "UPDATE ChiTietHoaDon SET SoLuong = SoLuong + ? WHERE IDHoaDon = (SELECT ID FROM HoaDon WHERE MaHoaDon = ?) AND IDSanPham = (SELECT ID FROM SanPham WHERE MaSanPham = ?)";
        try (Connection connection = DBConnect.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, soLuongThem); // Increase the quantity
            ps.setString(2, maHoaDon); // Invoice ID
            ps.setString(3, maSP);     // Product ID

            ps.executeUpdate();
            capNhatChiTietHoaDon(maHoaDon);
            capNhatTongTienChoHoaDon();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi khi cập nhật số lượng sản phẩm trong hóa đơn: " + e.getMessage());
        }
    }

    private int getIdSanPhamFromMaSP(String maSP) {
        // Query to retrieve ID corresponding to MaSP
        String sql = "SELECT ID FROM SanPham WHERE MaSanPham = ?";
        try (Connection connection = DBConnect.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, maSP);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("ID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi khi lấy ID cho sản phẩm: " + e.getMessage());
        }
        return -1; // Return -1 if ID not found


    }//GEN-LAST:event_tblSanPhamMouseClicked
    public void themSanPhamVaoChiTietHoaDon(String maHoaDon, String maSP, int soLuong, BigDecimal donGia) {
        String sql = "INSERT INTO ChiTietHoaDon (IDHoaDon, IDSanPham, SoLuong, DonGia, TrangThai) "
                + "VALUES ((SELECT ID FROM HoaDon WHERE MaHoaDon = ?), "
                + "(SELECT ID FROM SanPham WHERE MaSanPham = ?), ?, ?, 1)";

        try (Connection connection = DBConnect.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, maHoaDon);
            ps.setString(2, maSP);
            ps.setInt(3, soLuong);
            ps.setBigDecimal(4, donGia);
            ps.executeUpdate();
            // Sau khi thêm sản phẩm, cập nhật lại tổng tiền của hóa đơn
            capNhatChiTietHoaDon(maHoaDon);
            capNhatTongTienChoHoaDon();

            // Cập nhật giao diện (bảng chi tiết hóa đơn và tổng tiền)
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi khi thêm sản phẩm: " + e.getMessage());
        }
    }

    public void capNhatSoLuongSanPham(int idSanPham, int soLuongThayDoi) {
        // SQL to update stock
        String sql = "UPDATE ChiTietSanPham SET SoLuong = CASE WHEN SoLuong >= ? THEN SoLuong - ? ELSE SoLuong END WHERE ID = ?";
        try (Connection connection = DBConnect.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, soLuongThayDoi); // Ensure enough stock exists
            ps.setInt(2, soLuongThayDoi); // Deduct stock
            ps.setInt(3, idSanPham);      // Reference product by ID

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {

                ThongTinSanPhamRepo sanPhamRepository = new ThongTinSanPhamRepo();
                sanPhamRepository.getAllSanPham();
                loadTables();
            } else {
                JOptionPane.showMessageDialog(null, "Cập nhật thất bại! Sản phẩm không đủ số lượng hoặc không tồn tại.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi khi cập nhật số lượng sản phẩm: " + e.getMessage());
        }
    }

    public void capNhatSoLuongSanPhamSetQuantity(int idSanPham, int soLuongThem) {
        // SQL to increase stock quantity
        String sql = "UPDATE ChiTietSanPham SET SoLuong = SoLuong + ? WHERE ID = ?";
        try (Connection connection = DBConnect.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, soLuongThem); // Add stock
            ps.setInt(2, idSanPham);   // Reference product by ID

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null, "Số lượng sản phẩm đã được cập nhật thành công!");
            } else {
                JOptionPane.showMessageDialog(null, "Không tìm thấy sản phẩm để cập nhật số lượng.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi khi cập nhật số lượng sản phẩm: " + e.getMessage());
        }
    }

    public List<Object[]> layDanhSachChiTietHoaDonTuCSDL(String maHoaDon) {
        List<Object[]> danhSachChiTiet = new ArrayList<>();
        String sql = "SELECT ROW_NUMBER() OVER (ORDER BY cthd.ID) AS STT, "
                + "sp.TenSanPham, cthd.DonGia, cthd.SoLuong, "
                + "(cthd.DonGia * cthd.SoLuong) AS ThanhTien "
                + "FROM ChiTietHoaDon cthd "
                + "JOIN SanPham sp ON cthd.IDSanPham = sp.ID "
                + "JOIN HoaDon hd ON cthd.IDHoaDon = hd.ID "
                + "WHERE hd.MaHoaDon = ?";

        try (Connection connection = DBConnect.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, maHoaDon);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal donGia = rs.getBigDecimal("DonGia");
                    BigDecimal thanhTien = rs.getBigDecimal("ThanhTien");
                    danhSachChiTiet.add(new Object[]{
                        rs.getInt("STT"),
                        rs.getString("TenSanPham"),
                        donGia, // Lưu giá trị gốc (BigDecimal)
                        rs.getInt("SoLuong"),
                        thanhTien 
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi khi lấy chi tiết hóa đơn: " + e.getMessage());
        }
        return danhSachChiTiet;
    }
    private void tblChuaThanhToanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblChuaThanhToanMouseClicked
        // TODO add your handling code here:

    }//GEN-LAST:event_tblChuaThanhToanMouseClicked

    private void tblHoaDonChiTietMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblHoaDonChiTietMouseClicked

        if (maHoaDonHienTai == null) {
            JOptionPane.showMessageDialog(null, "Chưa chọn hóa đơn!");
            return;
        }

        if (kiemTraTrangThaiHoaDon(maHoaDonHienTai)) {
            JOptionPane.showMessageDialog(null, "Hóa đơn đã thanh toán, không thể chỉnh sửa sản phẩm!");
            return;
        }

        int row = tblHoaDonChiTiet.getSelectedRow();
        if (row != -1) {
            try {
                String tenHangHoa = tblHoaDonChiTiet.getValueAt(row, 1).toString();
                int soLuongHienTai = Integer.parseInt(tblHoaDonChiTiet.getValueAt(row, 3).toString());

                Object[] options = {"Xóa sản phẩm", "Cập nhật số lượng"};
                int choice = JOptionPane.showOptionDialog(null,
                        "Bạn muốn làm gì với sản phẩm \"" + tenHangHoa + "\"?",
                        "Tùy chọn",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]);

                int idSanPham = getIdSanPhamFromTenSanPham(tenHangHoa);
                if (idSanPham == -1) {
                    JOptionPane.showMessageDialog(null, "Không tìm thấy sản phẩm!");
                    return;
                }

                int soLuongTonKho = SoLuongTonKho(idSanPham);
                int soLuongKhaDung = soLuongTonKho + soLuongHienTai;

                if (choice == 0) {
                    int confirm = JOptionPane.showConfirmDialog(null,
                            "Bạn có chắc chắn muốn xóa sản phẩm \"" + tenHangHoa + "\" khỏi giỏ hàng?",
                            "Xác nhận xóa sản phẩm", JOptionPane.YES_NO_OPTION);
                    if (confirm != JOptionPane.YES_OPTION) {
                        return;
                    }

                    xoaSanPhamKhoiChiTietHoaDon(maHoaDonHienTai, tenHangHoa);
                    capNhatSoLuongSanPhamSetQuantity(idSanPham, soLuongHienTai);
                    capNhatChiTietHoaDon(maHoaDonHienTai);
                    capNhatTongTienChoHoaDon();

                } else if (choice == 1) {
                    String soLuongMoiStr = JOptionPane.showInputDialog(null,
                            "Nhập số lượng mới cho sản phẩm \"" + tenHangHoa + "\":",
                            soLuongHienTai);
                    if (soLuongMoiStr == null || soLuongMoiStr.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Bạn chưa nhập số lượng!");
                        return;
                    }

                    int soLuongMoi = Integer.parseInt(soLuongMoiStr.trim());
                    if (soLuongMoi < 0) {
                        JOptionPane.showMessageDialog(null, "Số lượng không được âm!");
                        return;
                    }

                    if (soLuongMoi > soLuongKhaDung) {
                        JOptionPane.showMessageDialog(null, "Số lượng nhập vượt quá số lượng tồn kho khả dụng! Tồn kho hiện tại: " + soLuongKhaDung);
                        return;
                    }

                    if (soLuongMoi == 0) {
                        // Nếu số lượng mới là 0, xóa sản phẩm
                        xoaSanPhamKhoiChiTietHoaDon(maHoaDonHienTai, tenHangHoa);
                        capNhatSoLuongSanPhamSetQuantity(idSanPham, soLuongHienTai);
                        JOptionPane.showMessageDialog(null,
                                "Số lượng bằng 0, sản phẩm \"" + tenHangHoa + "\" đã được xóa khỏi giỏ hàng!");
                    } else {
                        // Cập nhật số lượng trong chi tiết hóa đơn
                        capNhatSoLuongSanPhamTrongChiTietHoaDon(maHoaDonHienTai, tenHangHoa, soLuongMoi);

                        // Điều chỉnh số lượng trong kho
                        int soLuongThayDoi = soLuongMoi - soLuongHienTai;
                        if (soLuongThayDoi > 0) {
                            // Giảm số lượng trong kho
                            capNhatSoLuongSanPham(idSanPham, soLuongThayDoi);
                        } else if (soLuongThayDoi < 0) {
                            // Tăng số lượng trong kho
                            capNhatSoLuongSanPhamSetQuantity(idSanPham, -soLuongThayDoi);
                        }

                    }

                    // Cập nhật bảng chi tiết hóa đơn và tổng tiền
                    capNhatChiTietHoaDon(maHoaDonHienTai);
                    capNhatTongTienChoHoaDon();
                }

                // Làm mới danh sách sản phẩm
                ThongTinSanPhamRepo sanPhamRepository = new ThongTinSanPhamRepo();
                sanPhamRepository.getAllSanPham();
                loadTables();

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Số lượng không hợp lệ! Vui lòng nhập số nguyên.");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Đã xảy ra lỗi: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(null, "Vui lòng chọn sản phẩm để chỉnh sửa!");
        }

    }//GEN-LAST:event_tblHoaDonChiTietMouseClicked

    public int SoLuongTonKho(int idSanPham) {
        String sql = "SELECT SoLuong FROM ChiTietSanPham WHERE ID = ?";
        try (Connection connection = DBConnect.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idSanPham);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("SoLuong");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi khi lấy số lượng tồn kho: " + e.getMessage());
        }
        return 0; // Trả về 0 nếu có lỗi hoặc không tìm thấy
    }

    public void capNhatSoLuongSanPhamTrongChiTietHoaDon(String maHoaDon, String tenHangHoa, int soLuongMoi) {
        String sql = "UPDATE ChiTietHoaDon SET SoLuong = ? "
                + "WHERE IDHoaDon = (SELECT ID FROM HoaDon WHERE MaHoaDon = ?) "
                + "AND IDSanPham = (SELECT ID FROM SanPham WHERE TenSanPham = ?)";
        try (Connection connection = DBConnect.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, soLuongMoi); // Số lượng mới
            ps.setString(2, maHoaDon); // Mã hóa đơn
            ps.setString(3, tenHangHoa); // Tên sản phẩm

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                // Không cần thông báo ở đây vì thông báo sẽ được hiển thị trong tblHoaDonChiTietMouseClicked
            } else {
                JOptionPane.showMessageDialog(null, "Không tìm thấy sản phẩm \"" + tenHangHoa + "\" trong hóa đơn!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi khi cập nhật số lượng sản phẩm: " + e.getMessage());
        }
    }


    private void btnLamMoiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLamMoiActionPerformed

        // Clear all text fields
        clearTextFields();

        // Clear the shopping cart (Gio Hang) table
        clearGioHang();

        // Reset the current invoice code to null
        maHoaDonHienTai = null;

        // Reset the total amount label and fields
        lblTongTien.setText("0 VND");
        txtTongTien.setText("");
        txtThanhTien.setText("");
        txtSoTienTra.setText("");
        txtTienDu.setText("");

        // Đặt lại trạng thái có thể chỉnh sửa của các thành phần giao diện
        txtMaHoaDon.setEditable(true);
        txtSoDienThoai.setEditable(true);
        txtTenKhachHang.setEditable(true);
        txtMaNhanVien.setEditable(true);
        txtTongTien.setEditable(false); // Tổng tiền thường không cho phép chỉnh sửa thủ công
        txtThanhTien.setEditable(false); // Thành tiền không cho phép chỉnh sửa thủ công
        txtSoTienTra.setEditable(true);
        txtTienDu.setEditable(false); // Tiền dư không cho phép chỉnh sửa thủ công
        rdoNam.setEnabled(true);
        rdoNu.setEnabled(true);
        cboPGG.setEnabled(true);

        // Đặt tab "Chưa thanh toán" làm tab được chọn
        tblHoaDon.setSelectedIndex(0); // 0 là chỉ số của tab "Chưa thanh toán"

        // Optionally, reload the tables to refresh data from the database
        loadTables();


    }//GEN-LAST:event_btnLamMoiActionPerformed

    private void tblDaThanhToanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblDaThanhToanMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_tblDaThanhToanMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(BanHangView.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(BanHangView.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(BanHangView.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(BanHangView.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new BanHangView().setVisible(true);
            }
        });
    }

    public JPanel getContent() {
        return (JPanel) this.getContentPane();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnHuyDon;
    private javax.swing.JButton btnInBill;
    private javax.swing.JButton btnLamMoi;
    private javax.swing.JButton btnTaoDon;
    private javax.swing.JButton btnThanhToan;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox<String> cboPGG;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JLabel lblTongTien;
    private javax.swing.JRadioButton rdoNam;
    private javax.swing.JRadioButton rdoNu;
    private javax.swing.JTable tblChuaThanhToan;
    private javax.swing.JTable tblDaThanhToan;
    private javax.swing.JTabbedPane tblHoaDon;
    private javax.swing.JTable tblHoaDonChiTiet;
    private javax.swing.JTable tblSanPham;
    private javax.swing.JTextField txtMaHoaDon;
    private javax.swing.JTextField txtMaNhanVien;
    private javax.swing.JTextField txtNgayTao;
    private javax.swing.JTextField txtSoDienThoai;
    private javax.swing.JTextField txtSoTienTra;
    private javax.swing.JTextField txtTenKhachHang;
    private javax.swing.JTextField txtThanhTien;
    private javax.swing.JTextField txtTienDu;
    private javax.swing.JTextField txtTimKiem;
    private javax.swing.JTextField txtTongTien;
    // End of variables declaration//GEN-END:variables
    private void loadTables() {
        // Models for the HoaDon tables
        DefaultTableModel modelChuaThanhToan = (DefaultTableModel) tblChuaThanhToan.getModel();
        DefaultTableModel modelDaThanhToan = (DefaultTableModel) tblDaThanhToan.getModel();

        // Model for the SanPham table
        DefaultTableModel modelSanPham = (DefaultTableModel) tblSanPham.getModel();

        // Create repository instances
        DanhSachDonHangRepo hoaDonRepository = new DanhSachDonHangRepo();
        ThongTinSanPhamRepo sanPhamRepository = new ThongTinSanPhamRepo();

        // Get data for HoaDon tables
        List<DanhSachDonHang> chuaThanhToanList = hoaDonRepository.getHoaDonChuaThanhToan();
        List<DanhSachDonHang> daThanhToanList = hoaDonRepository.getHoaDonDaThanhToan();

        // Get data for SanPham table
        List<Object[]> sanPhamList = sanPhamRepository.getAllSanPham();

        // Fill data into HoaDon tables
        fillToTableHoaDon(chuaThanhToanList, modelChuaThanhToan);
        fillToTableHoaDon(daThanhToanList, modelDaThanhToan);

        // Fill data into SanPham table
        fillToTableSanPham(modelSanPham, sanPhamList);
    }

    private void fillToTableHoaDon(List<DanhSachDonHang> hoaDonList, DefaultTableModel tableModel) {
        tableModel.setRowCount(0); // Xóa dữ liệu cũ

        if (hoaDonList != null) {
            for (DanhSachDonHang hoaDon : hoaDonList) {
                String maPhieuGiamGia = hoaDon.getMaPhieuGiamGia() != null ? hoaDon.getMaPhieuGiamGia() : "Không có";
                BigDecimal giamGia = hoaDon.getGiamGia() != null ? hoaDon.getGiamGia() : BigDecimal.ZERO;
                BigDecimal tongTien = hoaDon.getTongTien() != null ? hoaDon.getTongTien() : BigDecimal.ZERO;
                BigDecimal thanhTien = tongTien.subtract(giamGia);

                tableModel.insertRow(0, new Object[]{
                    hoaDon.getMaHoaDon(),
                    hoaDon.getMaKhachHang(),
                    hoaDon.getMaNhanVien(),
                    maPhieuGiamGia,
                    hoaDon.getNgayTao(),
                    formatVND(tongTien), // Định dạng Tổng Tiền
                    formatVND(thanhTien) // Định dạng Thành Tiền
                });
            }
        }
    }

    private void fillToTableSanPham(DefaultTableModel tableModel, List<Object[]> productData) {
        tableModel.setRowCount(0);

        if (productData != null && !productData.isEmpty()) {
            for (Object[] row : productData) {
                if (row.length != 8) {
                    JOptionPane.showMessageDialog(null, "Dữ liệu sản phẩm không đúng định dạng!");
                    continue;
                }

                Object[] newRow = new Object[8];
                newRow[0] = row[0]; 
                newRow[1] = row[1];
                newRow[2] = row[2];
                newRow[3] = row[3];
                newRow[4] = formatVND((BigDecimal) row[4]);
                newRow[5] = row[5];
                newRow[6] = row[6];
                newRow[7] = row[7];

                tableModel.addRow(newRow);
            }
        }
    }

    private void searchAndUpdateTable() {
        
        String keyword = txtTimKiem.getText().trim();
        DefaultTableModel modelSanPham = (DefaultTableModel) tblSanPham.getModel();
        ThongTinSanPhamRepo repository = new ThongTinSanPhamRepo();
        List<Object[]> searchResults = repository.searchSanPham(keyword);
        fillToTableSanPham(modelSanPham, searchResults);
    }

    private void checkSdtKhachHang(String soDienThoai) {
        String sql = "SELECT TenKhachHang, GioiTinh FROM KhachHang WHERE SDT = ?";
        try (Connection connection = DBConnect.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, soDienThoai);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    txtTenKhachHang.setText(rs.getString("TenKhachHang"));
                    int gioiTinh = rs.getInt("GioiTinh");
                    if (gioiTinh == 1) {
                        rdoNam.setSelected(true);
                    } else if (gioiTinh == 0) {
                        rdoNu.setSelected(true);
                    } else {
                        rdoNam.setSelected(false);
                        rdoNu.setSelected(false);
                    }
                } else {
                    txtTenKhachHang.setText("");
                    rdoNam.setSelected(false);
                    rdoNu.setSelected(false);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void luuKhachHang(String soDienThoai, String tenKhachHang, boolean isNam) {
        String sql = "INSERT INTO KhachHang (SDT, TenKhachHang, GioiTinh) VALUES (?, ?, ?)";

        try (Connection connection = DBConnect.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, soDienThoai);
            ps.setString(2, tenKhachHang);
            ps.setInt(3, isNam ? 1 : 0); // 1 for Nam, 0 for Nu

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void taoHoaDon() {
        String maHoaDon = txtMaHoaDon.getText();
        String soDienThoai = txtSoDienThoai.getText();
        String tenKhachHang = txtTenKhachHang.getText();
        boolean isNam = rdoNam.isSelected();
        String maNhanVien = txtMaNhanVien.getText();

        
        LocalDateTime ngayTao = LocalDateTime.now();
        String formattedNgayTao = ngayTao.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if (maNhanVien.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Vui lòng điền mã nhân viên."); // Thông báo nếu mã nhân viên trống
            return;
        }

        if (!txtMaHoaDon.getText().trim().isEmpty()) {
            txtMaHoaDon.setText("");
            JOptionPane.showMessageDialog(null, "hóa đơn đã tồn tại vui lòng tạo lại đơn");
            return;
        }

        if (!soDienThoai.isEmpty()) {
            if (!soDienThoai.matches("\\d{8,11}")) {
                JOptionPane.showMessageDialog(null, "Số điện thoại chỉ được chứa các chữ số (0-9) và có độ dài từ 8 đến 11 ký tự!");
                return;
            }
        }
        if (!tenKhachHang.isEmpty()) {
        
        String regex = "^[\\p{L}\\s]*$";
        if (!tenKhachHang.matches(regex)) {
            JOptionPane.showMessageDialog(null, "Tên khách hàng không được chứa ký tự đặc biệt!");
            txtTenKhachHang.setText(""); // Xóa nội dung không hợp lệ
            return;
        }
    }
        try (Connection connection = DBConnect.getConnection()) {
            int customerId = timSdtKhachHang(soDienThoai); 
            if (customerId == -1) {
                String customerSql = "INSERT INTO KhachHang (SDT, TenKhachHang, GioiTinh) VALUES (?, ?, ?)";
                try (PreparedStatement customerPs = connection.prepareStatement(customerSql, Statement.RETURN_GENERATED_KEYS)) {
                    customerPs.setString(1, soDienThoai);
                    customerPs.setString(2, tenKhachHang);
                    customerPs.setInt(3, isNam ? 1 : 0);

                    customerPs.executeUpdate();
                    ResultSet generatedKeys = customerPs.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        customerId = generatedKeys.getInt(1);
                    }
                }
            }

            int employeeId;
            try {
                employeeId = Integer.parseInt(maNhanVien);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Mã nhân viên không hợp lệ! Vui lòng nhập số.");
                return;
            }

            String checkEmployeeSql = "SELECT COUNT(*) FROM NhanVien WHERE ID = ?";
            try (PreparedStatement checkPs = connection.prepareStatement(checkEmployeeSql)) {
                checkPs.setInt(1, employeeId);
                ResultSet rs = checkPs.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) { // Nếu không tìm thấy nhân viên
                    JOptionPane.showMessageDialog(null, "Không có nhân viên này!");
                    return;
                }
            }
        String kiemTraTrangThaiNhanVien = "SELECT TrangThai FROM NhanVien WHERE ID = ?";
        try (PreparedStatement trangThaiPs = connection.prepareStatement(kiemTraTrangThaiNhanVien)) {
            trangThaiPs.setInt(1, employeeId);
            ResultSet rs = trangThaiPs.executeQuery();
            if (rs.next()) {
                boolean trangThai = rs.getBoolean("TrangThai");
                if (!trangThai) {
                    JOptionPane.showMessageDialog(null, "Nhân viên này đã nghỉ làm, không thể tạo hóa đơn!");
                    return;
                }
            }
        }
            
            String hoaDon = "INSERT INTO HoaDon (MaHoaDon, IDKhachHang, IDNhanVien, NgayTao, TrangThai, TongTien) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement invoicePs = connection.prepareStatement(hoaDon)) {
                invoicePs.setString(1, maHoaDon);
                invoicePs.setInt(2, customerId);
                invoicePs.setInt(3, Integer.parseInt(maNhanVien));
                invoicePs.setTimestamp(4, Timestamp.valueOf(formattedNgayTao));
                invoicePs.setBoolean(5, false);
                invoicePs.setBigDecimal(6, null);

                invoicePs.executeUpdate();
                JOptionPane.showMessageDialog(null, "Tạo hóa đơn thành công!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Tạo hóa đơn lỗi: " + e.getMessage());
        }
    }

    private int timSdtKhachHang(String soDienThoai) throws SQLException {
        String sql = "SELECT ID FROM KhachHang WHERE SDT = ?";
        try (Connection connection = DBConnect.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, soDienThoai);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("ID");
            }
        }
        return -1;
    }

    public void addToInvoiceDetails(String maSP, String tenSP, BigDecimal giaBan, int soLuong) {
      
        GioHangRepo hoaDonChiTietRepository = new GioHangRepo();
        hoaDonChiTietRepository.insertIntoChiTietHoaDon(maSP, maSP, soLuong, giaBan);
    }

    private BigDecimal tinhTongTienTuTblHoaDonChiTiet() {
        BigDecimal tongTien = BigDecimal.ZERO;
        DefaultTableModel model = (DefaultTableModel) tblHoaDonChiTiet.getModel();
        if (model.getRowCount() == 0) {
            lblTongTien.setText("0 VND");
            txtTongTien.setText("");
            txtThanhTien.setText("");
            return tongTien;
        }

        for (int i = 0; i < model.getRowCount(); i++) {
            Object thanhTienObj = model.getValueAt(i, 4); // Cột "ThanhTien"
            if (thanhTienObj instanceof BigDecimal) {
                BigDecimal thanhTien = (BigDecimal) thanhTienObj;
                tongTien = tongTien.add(thanhTien);
            }
        }

        lblTongTien.setText(tongTien.compareTo(BigDecimal.ZERO) > 0 ? formatVND(tongTien) + " VND" : "0 VND");
        txtTongTien.setText(formatVND(tongTien));
        txtThanhTien.setText(formatVND(tongTien));

        return tongTien;
    }

    private void luuTongTienVaoHoaDon(BigDecimal tongTien, String maHoaDon) {
        String sql = "UPDATE HoaDon SET TongTien = ? WHERE MaHoaDon = ?";

        try (Connection connection = DBConnect.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setBigDecimal(1, tongTien); // Gán tổng tiền vào tham số đầu tiên
            ps.setString(2, maHoaDon);    // Gán mã hóa đơn vào tham số thứ hai

            int rowsAffected = ps.executeUpdate(); // Thực thi câu lệnh SQL
            if (rowsAffected == 0) {
                JOptionPane.showMessageDialog(null, "Cập nhật thất bại! Mã hóa đơn không tồn tại.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi khi lưu tổng tiền vào hóa đơn: " + e.getMessage());
        }
    }

    private void capNhatTongTienChoHoaDon() {
        if (maHoaDonHienTai == null) {
            JOptionPane.showMessageDialog(null, "Chưa chọn hóa đơn để cập nhật tổng tiền!");
            return;
        }

        // Tính tổng tiền từ tblHoaDonChiTiet
        BigDecimal tongTien = tinhTongTienTuTblHoaDonChiTiet();

        // Lưu tổng tiền vào bảng HoaDon
        luuTongTienVaoHoaDon(tongTien, maHoaDonHienTai);

        // Cập nhật giao diện với định dạng tiền Việt Nam
        txtTongTien.setText(formatVND(tongTien));
        txtThanhTien.setText(formatVND(tongTien));
        lblTongTien.setText(tongTien.compareTo(BigDecimal.ZERO) > 0 ? formatVND(tongTien) + " VND" : "0 VND");
    }

    private void tinhTienDu() {
        try {
            String thanhTienStr = txtThanhTien.getText().trim().replace(".", "");
            String soTienTraStr = txtSoTienTra.getText().trim().replace(".", "");

            
            if (thanhTienStr.isEmpty() || soTienTraStr.isEmpty()) {
                txtTienDu.setText("");
                return;
            }

            BigDecimal thanhTien = new BigDecimal(thanhTienStr);
            BigDecimal soTienTra = new BigDecimal(soTienTraStr);

            BigDecimal tienDu = soTienTra.subtract(thanhTien);

            txtTienDu.setText(formatVND(tienDu));
        } catch (NumberFormatException e) {
            txtTienDu.setText("");
            JOptionPane.showMessageDialog(null, "Số tiền không hợp lệ! Vui lòng nhập số.");
        }
    }

    private boolean kiemTraTrangThaiHoaDon(String maHoaDonHienTai) {
        try {
            Connection conn = DBConnect.getConnection();
            String sql = "SELECT trangThai FROM HoaDon WHERE maHoaDon = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, maHoaDonHienTai);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int trangThai = rs.getInt("trangThai");
                return trangThai == 1;
            }

            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi kiểm tra trạng thái hóa đơn: " + e.getMessage());
            return true;
        }
    }

    public void xoaSanPhamKhoiChiTietHoaDon(String maHoaDon, String tenHangHoa) {
        String sql = "DELETE FROM ChiTietHoaDon WHERE IDHoaDon = (SELECT ID FROM HoaDon WHERE MaHoaDon = ?) "
                + "AND IDSanPham = (SELECT ID FROM SanPham WHERE TenSanPham = ?)";
        try (Connection connection = DBConnect.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
           
            ps.setString(1, maHoaDon);
            ps.setString(2, tenHangHoa);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                JOptionPane.showMessageDialog(null, "Không tìm thấy sản phẩm \"" + tenHangHoa + "\" trong hóa đơn!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi khi xóa sản phẩm: " + e.getMessage());
        }
    }

    public int getIdSanPhamFromTenSanPham(String tenSanPham) {
        String sql = "SELECT ID FROM SanPham WHERE TenSanPham = ?";
        try (Connection connection = DBConnect.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tenSanPham);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("ID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi khi lấy ID cho sản phẩm: " + e.getMessage());
        }
        return -1;
    }

    private void loadPhieuGiamGia() {
        List<PhieuGiamGia> danhSach = pggRepo.getActivePhieuGiamGia();

        cboPGG.addItem(" ");

        Date now = new Date();
        for (PhieuGiamGia pgg : danhSach) {
            if (!pgg.getNgayBatDau().after(now) && !pgg.getNgayKetThuc().before(now)) {
                cboPGG.addItem(pgg.getMaPhieuGiamGia());
            }
        }
    }

    private String formatVND(BigDecimal amount) {
        if (amount == null) {
            return "0";
        }
        DecimalFormat formatter = new DecimalFormat("###,###,###");
        String formatted = formatter.format(amount.setScale(0, RoundingMode.HALF_UP));
        return formatted.replace(",", ".");
    }
}
