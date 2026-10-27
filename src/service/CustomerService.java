package service;

import model.Customer;
import util.DBConnection;
import util.IdGenerator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing Customer accounts and meter details via MySQL JDBC.
 */
public class CustomerService {

    public CustomerService() {
    }

    /**
     * Adds a new customer to the MySQL database.
     */
    public synchronized Customer addCustomer(String name, String address, String phone,
                                            String email, String meterNumber, double initialReading) {
        if (getCustomerByMeterNumber(meterNumber) != null) {
            throw new IllegalArgumentException("Meter number '" + meterNumber + "' is already assigned to another customer.");
        }

        List<Customer> current = getAllCustomers();
        List<String> existingIds = current.stream()
                .map(Customer::getCustomerId)
                .collect(Collectors.toList());
        String newId = IdGenerator.nextCustomerId(existingIds);

        String sql = "INSERT INTO customers (customer_id, name, address, phone, email, meter_number, previous_reading, current_reading) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newId);
            ps.setString(2, name.trim());
            ps.setString(3, address.trim());
            ps.setString(4, phone.trim());
            ps.setString(5, (email != null) ? email.trim() : "");
            ps.setString(6, meterNumber.trim());
            ps.setDouble(7, initialReading);
            ps.setDouble(8, initialReading);

            ps.executeUpdate();
            return new Customer(newId, name.trim(), address.trim(), phone.trim(),
                    (email != null) ? email.trim() : "", meterNumber.trim(), initialReading, initialReading);
        } catch (SQLException e) {
            throw new RuntimeException("Error adding customer to database: " + e.getMessage(), e);
        }
    }

    /**
     * Returns a list of all registered customers from MySQL database.
     */
    public List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT customer_id, name, address, phone, email, meter_number, previous_reading, current_reading FROM customers ORDER BY customer_id ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapCustomer(rs));
            }
        } catch (SQLException e) {
            System.err.println("[!] Error fetching customers from DB: " + e.getMessage());
        }
        return list;
    }

    /**
     * Finds a customer by their unique Customer ID.
     */
    public Customer getCustomerById(String customerId) {
        if (customerId == null) return null;
        String sql = "SELECT customer_id, name, address, phone, email, meter_number, previous_reading, current_reading FROM customers WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapCustomer(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[!] Error fetching customer by ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Finds a customer by their assigned meter number.
     */
    public Customer getCustomerByMeterNumber(String meterNumber) {
        if (meterNumber == null) return null;
        String sql = "SELECT customer_id, name, address, phone, email, meter_number, previous_reading, current_reading FROM customers WHERE meter_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, meterNumber.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapCustomer(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[!] Error fetching customer by meter number: " + e.getMessage());
        }
        return null;
    }

    /**
     * Searches customers matching keyword in ID, name, phone, meter number, or address.
     */
    public List<Customer> searchCustomers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllCustomers();
        }
        String pattern = "%" + keyword.trim().toLowerCase() + "%";
        String sql = "SELECT customer_id, name, address, phone, email, meter_number, previous_reading, current_reading " +
                "FROM customers WHERE LOWER(customer_id) LIKE ? OR LOWER(name) LIKE ? OR LOWER(phone) LIKE ? OR LOWER(meter_number) LIKE ? OR LOWER(address) LIKE ? " +
                "ORDER BY customer_id ASC";

        List<Customer> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 1; i <= 5; i++) {
                ps.setString(i, pattern);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapCustomer(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[!] Error searching customers: " + e.getMessage());
        }
        return list;
    }

    /**
     * Updates an existing customer's contact information.
     */
    public synchronized boolean updateCustomer(String customerId, String name, String address, String phone, String email) {
        Customer c = getCustomerById(customerId);
        if (c == null) {
            return false;
        }

        String updName = (name != null && !name.trim().isEmpty()) ? name.trim() : c.getName();
        String updAddr = (address != null && !address.trim().isEmpty()) ? address.trim() : c.getAddress();
        String updPhone = (phone != null && !phone.trim().isEmpty()) ? phone.trim() : c.getPhone();
        String updEmail = (email != null && !email.trim().isEmpty()) ? email.trim() : c.getEmail();

        String sql = "UPDATE customers SET name = ?, address = ?, phone = ?, email = ? WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, updName);
            ps.setString(2, updAddr);
            ps.setString(3, updPhone);
            ps.setString(4, updEmail);
            ps.setString(5, customerId.trim());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[!] Error updating customer: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates customer readings when a new bill is generated.
     */
    public synchronized boolean updateMeterReadings(String customerId, double newReading) {
        Customer c = getCustomerById(customerId);
        if (c == null) {
            return false;
        }

        String sql = "UPDATE customers SET previous_reading = current_reading, current_reading = ? WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newReading);
            ps.setString(2, customerId.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[!] Error updating meter readings: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes a customer by ID.
     */
    public synchronized boolean deleteCustomer(String customerId) {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerId.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[!] Error deleting customer: " + e.getMessage());
            return false;
        }
    }

    /**
     * Seeds initial sample customers if the dataset is empty.
     */
    public void seedSampleCustomersIfEmpty() {
        if (getAllCustomers().isEmpty()) {
            addCustomer("Rajesh Sharma", "Flat 402, Green Valley Apts, Delhi", "9876543210", "rajesh@example.com", "MTR-101", 1200.0);
            addCustomer("Priya Patel", "12 Lotus Colony, Ahmedabad", "9812345678", "priya@example.com", "MTR-102", 850.0);
            addCustomer("Amit Verma", "78 Civil Lines, Jaipur", "9923456789", "amit@example.com", "MTR-103", 2100.0);
            addCustomer("Sneha Roy", "3B South City, Kolkata", "9734567890", "sneha@example.com", "MTR-104", 1540.0);
        }
    }

    private Customer mapCustomer(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getString("customer_id"),
                rs.getString("name"),
                rs.getString("address"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("meter_number"),
                rs.getDouble("previous_reading"),
                rs.getDouble("current_reading")
        );
    }
}
