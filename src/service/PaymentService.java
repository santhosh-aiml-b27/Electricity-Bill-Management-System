package service;

import model.Bill;
import model.Payment;
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
 * Service for recording and tracking bill payments via MySQL JDBC.
 */
public class PaymentService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public PaymentService() {
    }

    /**
     * Records a payment against an unpaid bill in MySQL.
     */
    public synchronized Payment recordPayment(String billId, String paymentMethod,
                                             String transactionRef, BillingService billingService) {
        Bill bill = billingService.getBillById(billId);
        if (bill == null) {
            throw new IllegalArgumentException("Bill not found with ID: " + billId);
        }

        if (bill.isPaid()) {
            throw new IllegalStateException("Bill " + billId + " is already marked as PAID.");
        }

        List<Payment> existing = getAllPayments();
        List<String> existingIds = existing.stream()
                .map(Payment::getPaymentId)
                .collect(Collectors.toList());
        String paymentId = IdGenerator.nextPaymentId(existingIds);

        String today = LocalDate.now().format(DATE_FORMATTER);
        String ref = (transactionRef == null || transactionRef.trim().isEmpty())
                ? "TXN-" + System.currentTimeMillis() % 1000000
                : transactionRef.trim();

        String sql = "INSERT INTO payments (payment_id, bill_id, customer_id, amount, payment_date, payment_method, transaction_ref) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, paymentId);
            ps.setString(2, bill.getBillId());
            ps.setString(3, bill.getCustomerId());
            ps.setDouble(4, bill.getTotalAmount());
            ps.setString(5, today);
            ps.setString(6, paymentMethod.toUpperCase().trim());
            ps.setString(7, ref);

            ps.executeUpdate();

            // Mark the bill as PAID in database
            billingService.markPaid(bill.getBillId());

            return new Payment(paymentId, bill.getBillId(), bill.getCustomerId(),
                    bill.getTotalAmount(), today, paymentMethod.toUpperCase().trim(), ref);
        } catch (SQLException e) {
            throw new RuntimeException("Error recording payment in database: " + e.getMessage(), e);
        }
    }

    public List<Payment> getAllPayments() {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT payment_id, bill_id, customer_id, amount, payment_date, payment_method, transaction_ref FROM payments ORDER BY payment_id ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapPayment(rs));
            }
        } catch (SQLException e) {
            System.err.println("[!] Error fetching payments from DB: " + e.getMessage());
        }
        return list;
    }

    public List<Payment> getPaymentsByBillId(String billId) {
        if (billId == null) return Collections.emptyList();
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT payment_id, bill_id, customer_id, amount, payment_date, payment_method, transaction_ref FROM payments WHERE bill_id = ? ORDER BY payment_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, billId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapPayment(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[!] Error fetching payments by bill: " + e.getMessage());
        }
        return list;
    }

    public List<Payment> getPaymentsByCustomerId(String customerId) {
        if (customerId == null) return Collections.emptyList();
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT payment_id, bill_id, customer_id, amount, payment_date, payment_method, transaction_ref FROM payments WHERE customer_id = ? ORDER BY payment_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapPayment(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[!] Error fetching payments by customer: " + e.getMessage());
        }
        return list;
    }

    private Payment mapPayment(ResultSet rs) throws SQLException {
        return new Payment(
                rs.getString("payment_id"),
                rs.getString("bill_id"),
                rs.getString("customer_id"),
                rs.getDouble("amount"),
                rs.getString("payment_date"),
                rs.getString("payment_method"),
                rs.getString("transaction_ref")
        );
    }
}
