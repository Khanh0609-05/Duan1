/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Repo;

import Model.HoaDon;
import Model.ThongKe;
import Model.ThongKeTongQuan;
import Until.DBConnect;
import static Until.DBConnect.getConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.sql.*;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

/**
 *
 * @author nguye
 */
public class ThongKeRepo {

    private Connection con = null;
    private PreparedStatement ps = null;
    private ResultSet rs = null;
    private String sql = null;

    public ThongKeRepo() {
        con = DBConnect.getConnection();
    }

    public ArrayList<ThongKeTongQuan> getAll(java.util.Date dateFrom, java.util.Date dateTo) {
     ArrayList<ThongKeTongQuan> listTKTQ = new ArrayList<>();
    String sql = "SELECT \n"
            + "    COALESCE(SUM(hd.ThanhTien), 0) AS TongDoanhThu,\n"
            + "    COALESCE(SUM(hdct.SoLuong), 0) AS TongSanPham,\n"
            + "    COALESCE(COUNT(DISTINCT CASE WHEN hd.IDKhachHang IS NOT NULL THEN hd.IDKhachHang END), 0) AS TongKhachHang\n"
            + "FROM HoaDon hd\n"
            + "LEFT JOIN ChiTietHoaDon hdct ON hd.ID = hdct.IDHoaDon\n"
            + "WHERE hd.NgayTao >= ? AND hd.NgayTao <= ?\n"
            + "  AND hd.TrangThai = 1\n"
            + "GROUP BY ()\n"
            + "HAVING COUNT(*) >= 0;";

    try (PreparedStatement ps = con.prepareStatement(sql)) {
        // Đặt múi giờ là ICT (UTC+7) để đồng bộ với cơ sở dữ liệu
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
        Calendar calendar = Calendar.getInstance(timeZone);

        // Sử dụng java.sql.Timestamp để bao gồm cả thời gian
        calendar.setTime(dateFrom);
        ps.setTimestamp(1, new java.sql.Timestamp(dateFrom.getTime()), calendar);

        calendar.setTime(dateTo);
        ps.setTimestamp(2, new java.sql.Timestamp(dateTo.getTime()), calendar);

        // Thực thi truy vấn
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                double tongDoanhThu = rs.getDouble("TongDoanhThu");
                int tongSanPham = rs.getInt("TongSanPham");
                int tongKhachHang = rs.getInt("TongKhachHang");
                listTKTQ.add(new ThongKeTongQuan(tongDoanhThu, tongSanPham, tongKhachHang));
            }
        }
        return listTKTQ;
    } catch (SQLException e) {
        e.printStackTrace();
        return new ArrayList<>();
    }
    }

    public Map<Integer, Double> getDoanhThuTheoThang(int year) {
        Map<Integer, Double> doanhThuTheoThang = new HashMap<>();
        for (int month = 1; month <= 12; month++) {
            doanhThuTheoThang.put(month, 0.0);
        }

        String sql = "SELECT MONTH(hd.NgayTao) AS Thang, "
                + "COALESCE(SUM(hd.ThanhTien), 0) AS TongDoanhThu "
                + "FROM HoaDon hd "
                + "WHERE YEAR(hd.NgayTao) = ? AND hd.TrangThai = 1 "
                + "GROUP BY MONTH(hd.NgayTao)";

        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int thang = rs.getInt("Thang");
                    double tongDoanhThu = rs.getDouble("TongDoanhThu");
                    doanhThuTheoThang.put(thang, tongDoanhThu);
                }
            }
            // Ghi log để kiểm tra dữ liệu
            System.out.println("Doanh thu theo tháng cho năm " + year + ": " + doanhThuTheoThang);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doanhThuTheoThang;
    }

    public List<Map<String, Object>> getTop5SanPhamBanChay(java.util.Date tuNgayTop5, java.util.Date denNgayTop5) {
        List<Map<String, Object>> top5SanPham = new ArrayList<>();

        String sql = "SELECT TOP 5 \n"
                + "    sp.MaSanPham, \n"
                + "    sp.TenSanPham, \n"
                + "    SUM(cthd.SoLuong) AS SoLuong\n"
                + "FROM ChiTietHoaDon cthd\n"
                + "JOIN HoaDon hd ON cthd.IDHoaDon = hd.ID\n"
                + "JOIN SanPham sp ON cthd.IDSanPham = sp.ID\n"
                + "WHERE \n"
                + "    hd.NgayTao BETWEEN ? AND ?\n"
                + "    AND hd.TrangThai = 1\n"
                + "    AND cthd.TrangThai = 1\n"
                + "GROUP BY \n"
                + "    sp.MaSanPham, \n"
                + "    sp.TenSanPham\n"
                + "ORDER BY \n"
                + "    SUM(cthd.SoLuong) DESC";

        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            // Điều chỉnh tuNgayTop5 về 00:00:00 và denNgayTop5 về 23:59:59 ngay trong repository
            Calendar calTu = Calendar.getInstance();
            calTu.setTime(tuNgayTop5);
            calTu.set(Calendar.HOUR_OF_DAY, 0);
            calTu.set(Calendar.MINUTE, 0);
            calTu.set(Calendar.SECOND, 0);
            calTu.set(Calendar.MILLISECOND, 0);
            java.util.Date adjustedTuNgay = calTu.getTime();

            Calendar calDen = Calendar.getInstance();
            calDen.setTime(denNgayTop5);
            calDen.set(Calendar.HOUR_OF_DAY, 23);
            calDen.set(Calendar.MINUTE, 59);
            calDen.set(Calendar.SECOND, 59);
            calDen.set(Calendar.MILLISECOND, 999);
            java.util.Date adjustedDenNgay = calDen.getTime();

            // Sử dụng setTimestamp để truyền thời gian đầy đủ
            ps.setTimestamp(1, new java.sql.Timestamp(adjustedTuNgay.getTime()));
            ps.setTimestamp(2, new java.sql.Timestamp(adjustedDenNgay.getTime()));
            System.out.println("Executing query with: " + new java.sql.Timestamp(adjustedTuNgay.getTime())
                    + " to " + new java.sql.Timestamp(adjustedDenNgay.getTime()));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> sanPham = new HashMap<>();
                String maSanPham = rs.getString("MaSanPham");
                String tenSanPham = rs.getString("TenSanPham");
                int soLuong = rs.getInt("SoLuong");

                // Kiểm tra dữ liệu trước khi thêm
                if (maSanPham != null && tenSanPham != null && soLuong > 0) {
                    sanPham.put("MaSanPham", maSanPham);
                    sanPham.put("TenSanPham", tenSanPham);
                    sanPham.put("SoLuong", soLuong);
                    top5SanPham.add(sanPham);
                    System.out.println("Added: " + maSanPham + ", " + tenSanPham + ", SoLuong: " + soLuong);
                } else {
                    System.out.println("Skipped invalid data: MaSanPham=" + maSanPham + ", TenSanPham=" + tenSanPham + ", SoLuong=" + soLuong);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("Total records returned: " + top5SanPham.size());
        return top5SanPham;
    }

}
