package service;

import model.Admin;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing Administrator authentication via MySQL JDBC.
 */
public class AuthService {
    private Admin currentAdmin = null;

    public AuthService() {
        seedDefaultAdminIfEmpty();
    }

    private void seedDefaultAdminIfEmpty() {
        String countSql = "SELECT COUNT(*) FROM admins";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                addAdmin("admin", "admin123", "System Administrator", "SUPER_ADMIN");
            }
        } catch (SQLException e) {
            System.err.println("[!] Error checking admins table: " + e.getMessage());
        }
    }

    /**
     * Authenticates an admin against the MySQL database.
     */
    public Admin login(String username, String password) {
        if (username == null || password == null) {
            return null;
        }

        String sql = "SELECT username, password, full_name, role FROM admins WHERE LOWER(username) = LOWER(?) AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Admin admin = new Admin(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("full_name"),
                            rs.getString("role")
                    );
                    this.currentAdmin = admin;
                    return admin;
                }
            }
        } catch (SQLException e) {
            System.err.println("[!] Database login error: " + e.getMessage());
        }

        return null;
    }

    public void logout() {
        this.currentAdmin = null;
    }

    public Admin getCurrentAdmin() {
        return currentAdmin;
    }

    /**
     * Changes password in MySQL database.
     */
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        String sql = "UPDATE admins SET password = ? WHERE LOWER(username) = LOWER(?) AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setString(2, username.trim());
            ps.setString(3, oldPassword);

            int updated = ps.executeUpdate();
            if (updated > 0) {
                if (currentAdmin != null && currentAdmin.getUsername().equalsIgnoreCase(username.trim())) {
                    currentAdmin.setPassword(newPassword);
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[!] Database change password error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Inserts a new admin into the database.
     */
    public boolean addAdmin(String username, String password, String fullName, String role) {
        String sql = "INSERT INTO admins (username, password, full_name, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            ps.setString(2, password);
            ps.setString(3, fullName.trim());
            ps.setString(4, role.trim());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[!] Error registering admin: " + e.getMessage());
            return false;
        }
    }

    public List<Admin> getAllAdmins() {
        List<Admin> list = new ArrayList<>();
        String sql = "SELECT username, password, full_name, role FROM admins";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Admin(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("full_name"),
                        rs.getString("role")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[!] Error fetching admins: " + e.getMessage());
        }
        return list;
    }
}
