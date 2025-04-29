package Repo;

import Model.KhachHang;
import Until.DBConnect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class KhachHangRepo {

    private Connection con = null;
    private PreparedStatement ps = null;
    private ResultSet rs = null;
    private String sql = null;

    public KhachHangRepo() {
        con = DBConnect.getConnection();
    }

    public KhachHang getKhachHangByMa(String ma) {
    KhachHang kh = null;
    String sql = "SELECT ID, MaKhachHang, TenKhachHang, DiaChi, SDT, GioiTinh FROM KhachHang WHERE MaKhachHang = ?";
    try (
        Connection con = DBConnect.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)
    ) {
        ps.setString(1, ma);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                kh = new KhachHang(
                    rs.getInt("ID"),
                    rs.getString("MaKhachHang"),
                    rs.getString("TenKhachHang"),
                    rs.getString("DiaChi"),
                    rs.getString("SDT"),
                    rs.getBoolean("GioiTinh")
                );
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return kh;
}

   
}
