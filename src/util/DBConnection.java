package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages JDBC connections to MySQL database (electricity_db).
 */
public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/electricity_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "Arrowverse";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            initDatabase();
        } catch (ClassNotFoundException e) {
            System.err.println("[!] MySQL JDBC Driver not found in classpath. Make sure lib/mysql-connector-j-*.jar is included.");
        }
    }

    /**
     * Gets a connection to the electricity_db database.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Ensures all required tables exist in electricity_db.
     */
    public static void initDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Admins table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS admins (" +
                    "username VARCHAR(50) PRIMARY KEY, " +
                    "password VARCHAR(255) NOT NULL, " +
                    "full_name VARCHAR(100) NOT NULL, " +
                    "role VARCHAR(50) NOT NULL)");

            // Customers table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS customers (" +
                    "customer_id VARCHAR(20) PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "address VARCHAR(255) NOT NULL, " +
                    "phone VARCHAR(20) NOT NULL, " +
                    "email VARCHAR(100), " +
                    "meter_number VARCHAR(50) UNIQUE NOT NULL, " +
                    "previous_reading DOUBLE NOT NULL DEFAULT 0.0, " +
                    "current_reading DOUBLE NOT NULL DEFAULT 0.0)");

            // Bills table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS bills (" +
                    "bill_id VARCHAR(20) PRIMARY KEY, " +
                    "customer_id VARCHAR(20) NOT NULL, " +
                    "billing_month VARCHAR(20) NOT NULL, " +
                    "previous_reading DOUBLE NOT NULL, " +
                    "current_reading DOUBLE NOT NULL, " +
                    "units_consumed DOUBLE NOT NULL, " +
                    "energy_charges DOUBLE NOT NULL, " +
                    "service_charge DOUBLE NOT NULL DEFAULT 50.0, " +
                    "total_amount DOUBLE NOT NULL, " +
                    "status VARCHAR(20) NOT NULL DEFAULT 'UNPAID', " +
                    "bill_date VARCHAR(20) NOT NULL, " +
                    "due_date VARCHAR(20) NOT NULL, " +
                    "FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE)");

            // Payments table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS payments (" +
                    "payment_id VARCHAR(20) PRIMARY KEY, " +
                    "bill_id VARCHAR(20) NOT NULL, " +
                    "customer_id VARCHAR(20) NOT NULL, " +
                    "amount DOUBLE NOT NULL, " +
                    "payment_date VARCHAR(20) NOT NULL, " +
                    "payment_method VARCHAR(30) NOT NULL, " +
                    "transaction_ref VARCHAR(100), " +
                    "FOREIGN KEY (bill_id) REFERENCES bills(bill_id) ON DELETE CASCADE)");

        } catch (SQLException e) {
            System.err.println("[!] Warning: Could not initialize database tables: " + e.getMessage());
        }
    }
}
