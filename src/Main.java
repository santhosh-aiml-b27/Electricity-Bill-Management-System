import service.*;
import ui.ConsoleUI;

/**
 * Main application entry point for the Electricity Bill Management System.
 */
public class Main {
    public static void main(String[] args) {
        // Initialize services
        AuthService authService = new AuthService();
        CustomerService customerService = new CustomerService();
        BillingService billingService = new BillingService();
        PaymentService paymentService = new PaymentService();
        ReportService reportService = new ReportService(customerService, billingService, paymentService);

        // Seed initial sample data if running for the first time
        customerService.seedSampleCustomersIfEmpty();
        billingService.seedSampleBillsIfEmpty(customerService);

        // Launch Console User Interface
        ConsoleUI ui = new ConsoleUI(authService, customerService, billingService, paymentService, reportService);
        ui.start();
    }
}
