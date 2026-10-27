package service;

import model.Bill;
import model.Customer;
import util.DBConnection;
import util.IdGenerator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for calculating electricity tariffs and managing bills via MySQL JDBC.
 */
public class BillingService {
    public static final double FIXED_SERVICE_CHARGE = 50.0;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public BillingService() {
    }

    /**
     * Slab-based tariff calculation:
     * - 0 to 100 units: Rs 5.0 / unit
     * - 101 to 200 units: Rs 7.0 / unit
     * - 201 to 400 units: Rs 10.0 / unit
     * - 401+ units: Rs 12.0 / unit
     */
    public static double calculateEnergyCharges(double units) {
        if (units <= 0) {
            return 0.0;
        }

        double charge = 0.0;

        // Slab 1: 0 - 100 @ 5.0
        if (units <= 100) {
            charge += units * 5.0;
            return charge;
        } else {
            charge += 100 * 5.0;
        }

        // Slab 2: 101 - 200 @ 7.0
        if (units <= 200) {
            charge += (units - 100) * 7.0;
            return charge;
        } else {
            charge += 100 * 7.0;
        }

        // Slab 3: 201 - 400 @ 10.0
        if (units <= 400) {
            charge += (units - 200) * 10.0;
            return charge;
        } else {
            charge += 200 * 10.0;
        }

        // Slab 4: 401+ @ 12.0
        charge += (units - 400) * 12.0;

        return charge;
    }

    /**
     * Generates a descriptive breakdown string for tariff calculation.
     */
    public static String getTariffBreakdown(double units) {
        StringBuilder sb = new StringBuilder();
        double remaining = units;

        double s1Units = Math.min(Math.max(0, remaining), 100);
        double s1Cost = s1Units * 5.0;
        sb.append(String.format("  - Slab 1 (0-100 units)   : %6.1f units @ Rs. 5.0/unit = Rs. %8.2f\n", s1Units, s1Cost));
        remaining -= s1Units;

        if (remaining > 0) {
            double s2Units = Math.min(remaining, 100);
            double s2Cost = s2Units * 7.0;
            sb.append(String.format("  - Slab 2 (101-200 units) : %6.1f units @ Rs. 7.0/unit = Rs. %8.2f\n", s2Units, s2Cost));
            remaining -= s2Units;
        }

        if (remaining > 0) {
            double s3Units = Math.min(remaining, 200);
            double s3Cost = s3Units * 10.0;
            sb.append(String.format("  - Slab 3 (201-400 units) : %6.1f units @ Rs. 10.0/unit = Rs. %8.2f\n", s3Units, s3Cost));
            remaining -= s3Units;
        }

        if (remaining > 0) {
            double s4Units = remaining;
            double s4Cost = s4Units * 12.0;
            sb.append(String.format("  - Slab 4 (401+ units)    : %6.1f units @ Rs. 12.0/unit = Rs. %8.2f\n", s4Units, s4Cost));
        }

        double energyCharges = calculateEnergyCharges(units);
        sb.append(String.format("  Total Energy Charges     : Rs. %.2f\n", energyCharges));
        sb.append(String.format("  Fixed Service Charge     : Rs. %.2f\n", FIXED_SERVICE_CHARGE));
        sb.append(String.format("  Grand Total Payable      : Rs. %.2f", energyCharges + FIXED_SERVICE_CHARGE));

        return sb.toString();
    }

    /**
     * Generates a bill, persists it to MySQL, and updates customer readings.
     */
    public synchronized Bill generateBill(Customer customer, String billingMonth, double newReading, CustomerService customerService) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null.");
        }

        double prevReading = customer.getCurrentReading();
        if (newReading < prevReading) {
            throw new IllegalArgumentException(String.format(
                    "New meter reading (%.1f) cannot be less than previous reading (%.1f).",
                    newReading, prevReading));
        }

        double unitsConsumed = newReading - prevReading;
        double energyCharges = calculateEnergyCharges(unitsConsumed);
        double totalAmount = energyCharges + FIXED_SERVICE_CHARGE;

        LocalDate today = LocalDate.now();
        LocalDate due = today.plusDays(15);

        List<Bill> existing = getAllBills();
        List<String> existingIds = existing.stream()
                .map(Bill::getBillId)
                .collect(Collectors.toList());
        String billId = IdGenerator.nextBillId(existingIds);

        String sql = "INSERT INTO bills (bill_id, customer_id, billing_month, previous_reading, current_reading, " +
                "units_consumed, energy_charges, service_charge, total_amount, status, bill_date, due_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, billId);
            ps.setString(2, customer.getCustomerId());
            ps.setString(3, billingMonth.trim());
            ps.setDouble(4, prevReading);
            ps.setDouble(5, newReading);
            ps.setDouble(6, unitsConsumed);
            ps.setDouble(7, energyCharges);
            ps.setDouble(8, FIXED_SERVICE_CHARGE);
            ps.setDouble(9, totalAmount);
            ps.setString(10, "UNPAID");
            ps.setString(11, today.format(DATE_FORMATTER));
            ps.setString(12, due.format(DATE_FORMATTER));

            ps.executeUpdate();

            // Update customer meter readings
            customerService.updateMeterReadings(customer.getCustomerId(), newReading);

            return new Bill(billId, customer.getCustomerId(), billingMonth, prevReading, newReading,
                    unitsConsumed, energyCharges, FIXED_SERVICE_CHARGE, totalAmount, "UNPAID",
                    today.format(DATE_FORMATTER), due.format(DATE_FORMATTER));
        } catch (SQLException e) {
            throw new RuntimeException("Error generating bill in database: " + e.getMessage(), e);
        }
    }

    public List<Bill> getAllBills() {
        List<Bill> list = new ArrayList<>();
        String sql = "SELECT bill_id, customer_id, billing_month, previous_reading, current_reading, " +
                "units_consumed, energy_charges, service_charge, total_amount, status, bill_date, due_date " +
                "FROM bills ORDER BY bill_id ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapBill(rs));
            }
        } catch (SQLException e) {
            System.err.println("[!] Error fetching bills from DB: " + e.getMessage());
        }
        return list;
    }

    public Bill getBillById(String billId) {
        if (billId == null) return null;
        String sql = "SELECT bill_id, customer_id, billing_month, previous_reading, current_reading, " +
                "units_consumed, energy_charges, service_charge, total_amount, status, bill_date, due_date " +
                "FROM bills WHERE bill_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, billId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapBill(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[!] Error fetching bill by ID: " + e.getMessage());
        }
        return null;
    }

    public List<Bill> getBillsByCustomer(String customerId) {
        if (customerId == null) return Collections.emptyList();
        List<Bill> list = new ArrayList<>();
        String sql = "SELECT bill_id, customer_id, billing_month, previous_reading, current_reading, " +
                "units_consumed, energy_charges, service_charge, total_amount, status, bill_date, due_date " +
                "FROM bills WHERE customer_id = ? ORDER BY bill_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBill(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[!] Error fetching bills by customer: " + e.getMessage());
        }
        return list;
    }

    public List<Bill> getPendingBills() {
        List<Bill> list = new ArrayList<>();
        String sql = "SELECT bill_id, customer_id, billing_month, previous_reading, current_reading, " +
                "units_consumed, energy_charges, service_charge, total_amount, status, bill_date, due_date " +
                "FROM bills WHERE UPPER(status) = 'UNPAID' ORDER BY bill_id ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapBill(rs));
            }
        } catch (SQLException e) {
            System.err.println("[!] Error fetching pending bills: " + e.getMessage());
        }
        return list;
    }

    public synchronized boolean markPaid(String billId) {
        String sql = "UPDATE bills SET status = 'PAID' WHERE bill_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, billId.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[!] Error marking bill paid: " + e.getMessage());
            return false;
        }
    }

    public void seedSampleBillsIfEmpty(CustomerService customerService) {
        if (getAllBills().isEmpty() && !customerService.getAllCustomers().isEmpty()) {
            List<Customer> list = customerService.getAllCustomers();
            if (list.size() >= 2) {
                generateBill(list.get(0), "2026-09", list.get(0).getCurrentReading() + 180.0, customerService);
                generateBill(list.get(1), "2026-09", list.get(1).getCurrentReading() + 350.0, customerService);
            }
        }
    }

    private Bill mapBill(ResultSet rs) throws SQLException {
        return new Bill(
                rs.getString("bill_id"),
                rs.getString("customer_id"),
                rs.getString("billing_month"),
                rs.getDouble("previous_reading"),
                rs.getDouble("current_reading"),
                rs.getDouble("units_consumed"),
                rs.getDouble("energy_charges"),
                rs.getDouble("service_charge"),
                rs.getDouble("total_amount"),
                rs.getString("status"),
                rs.getString("bill_date"),
                rs.getString("due_date")
        );
    }
}
