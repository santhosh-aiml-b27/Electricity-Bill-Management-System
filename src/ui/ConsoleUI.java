package ui;

import model.Admin;
import model.Bill;
import model.Customer;
import model.Payment;
import service.*;

import java.util.List;
import java.util.Scanner;

/**
 * Menu-driven Console User Interface for the Electricity Bill Management System.
 */
public class ConsoleUI {
    private final Scanner scanner;
    private final AuthService authService;
    private final CustomerService customerService;
    private final BillingService billingService;
    private final PaymentService paymentService;
    private final ReportService reportService;

    public ConsoleUI(AuthService authService, CustomerService customerService,
                     BillingService billingService, PaymentService paymentService,
                     ReportService reportService) {
        this.scanner = new Scanner(System.in);
        this.authService = authService;
        this.customerService = customerService;
        this.billingService = billingService;
        this.paymentService = paymentService;
        this.reportService = reportService;
    }

    /**
     * Starts the main application loop.
     */
    public void start() {
        printBanner();
        boolean running = true;

        while (running) {
            if (authService.getCurrentAdmin() == null) {
                boolean loggedIn = handleLoginFlow();
                if (!loggedIn) {
                    System.out.println("\nExiting application. Goodbye!");
                    break;
                }
            }

            printMainMenu();
            String choice = prompt("Enter your choice: ");

            switch (choice.trim()) {
                case "1":
                    handleCustomerMenu();
                    break;
                case "2":
                    handleBillingMenu();
                    break;
                case "3":
                    handlePaymentMenu();
                    break;
                case "4":
                    handleReportsMenu();
                    break;
                case "5":
                    handleAdminSettings();
                    break;
                case "0":
                    authService.logout();
                    System.out.println("\n[✓] Logged out successfully.");
                    String exitOrLogin = prompt("Press [Enter] to login again, or type 'exit' to quit: ");
                    if ("exit".equalsIgnoreCase(exitOrLogin.trim())) {
                        running = false;
                        System.out.println("Thank you for using Electricity Bill Management System. Goodbye!");
                    }
                    break;
                default:
                    System.out.println("\n[!] Invalid option. Please choose between 0 and 5.");
            }
        }
    }

    // -------------------------------------------------------------
    // Authentication Flow
    // -------------------------------------------------------------

    private boolean handleLoginFlow() {
        printHeader("ADMINISTRATOR LOGIN");
        System.out.println(" Default credentials: username = admin | password = admin123");
        System.out.println(" (Type 'exit' to quit)\n");

        int attempts = 0;
        final int maxAttempts = 3;

        while (attempts < maxAttempts) {
            String username = prompt("Username: ");
            if ("exit".equalsIgnoreCase(username.trim())) {
                return false;
            }
            String password = prompt("Password: ");

            Admin admin = authService.login(username, password);
            if (admin != null) {
                System.out.println("\n[✓] Welcome back, " + admin.getFullName() + "! (Role: " + admin.getRole() + ")");
                return true;
            } else {
                attempts++;
                System.out.printf("[!] Invalid username or password. (Attempt %d of %d)\n\n", attempts, maxAttempts);
            }
        }

        System.out.println("[!] Maximum login attempts exceeded.");
        return false;
    }

    // -------------------------------------------------------------
    // Menu Displays
    // -------------------------------------------------------------

    private void printBanner() {
        System.out.println("================================================================================");
        System.out.println("               ⚡ ELECTRICITY BILL MANAGEMENT SYSTEM  ⚡                           ");
        System.out.println("              Efficient • Transparent • Reliable Billing                        ");
        System.out.println("================================================================================");
    }

    private void printMainMenu() {
        Admin current = authService.getCurrentAdmin();
        String userTag = (current != null) ? current.getUsername() + " (" + current.getRole() + ")" : "Guest";
        System.out.println("\n+------------------------------------------------------------------------------+");
        System.out.printf("| MAIN MENU                                        Logged in as: %-13s |\n", userTag);
        System.out.println("+------------------------------------------------------------------------------+");
        System.out.println("|  [1] Customer Management  (Add, View, Search, Update, Delete)                |");
        System.out.println("|  [2] Billing Management   (Generate Bill, View History, Slab Calculation)    |");
        System.out.println("|  [3] Payments Management  (Record Payment, View Receipts, History)           |");
        System.out.println("|  [4] Reports & Analytics  (Summary, Pending Bills, Monthly, Top Consumers)   |");
        System.out.println("|  [5] Admin Settings       (Change Password, Account Details)                 |");
        System.out.println("|  [0] Logout / Exit                                                           |");
        System.out.println("+------------------------------------------------------------------------------+");
    }

    private void printHeader(String title) {
        System.out.println("\n================================================================================");
        System.out.println("  " + title.toUpperCase());
        System.out.println("================================================================================");
    }

    // -------------------------------------------------------------
    // 1. Customer Management
    // -------------------------------------------------------------

    private void handleCustomerMenu() {
        boolean inMenu = true;
        while (inMenu) {
            printHeader("CUSTOMER MANAGEMENT");
            System.out.println("  [1] Add New Customer");
            System.out.println("  [2] View All Customers");
            System.out.println("  [3] Search Customer");
            System.out.println("  [4] Update Customer Information");
            System.out.println("  [5] Delete Customer");
            System.out.println("  [0] Back to Main Menu");
            System.out.println("--------------------------------------------------------------------------------");

            String choice = prompt("Select an option: ");
            switch (choice.trim()) {
                case "1":
                    addNewCustomer();
                    break;
                case "2":
                    viewAllCustomers();
                    break;
                case "3":
                    searchCustomer();
                    break;
                case "4":
                    updateCustomerInfo();
                    break;
                case "5":
                    deleteCustomer();
                    break;
                case "0":
                    inMenu = false;
                    break;
                default:
                    System.out.println("[!] Invalid choice. Please try again.");
            }
        }
    }

    private void addNewCustomer() {
        printHeader("ADD NEW CUSTOMER");
        String name = promptRequired("Enter Customer Full Name: ");
        String address = promptRequired("Enter Billing Address: ");
        String phone = promptRequired("Enter Phone Number: ");
        String email = prompt("Enter Email Address (optional): ");
        String meterNo = promptRequired("Enter Meter Number: ");
        double initialReading = promptDouble("Enter Initial Meter Reading (units, default 0): ", 0.0);

        try {
            Customer created = customerService.addCustomer(name, address, phone, email, meterNo, initialReading);
            System.out.println("\n[✓] Customer successfully registered!");
            System.out.println("    Assigned Customer ID : " + created.getCustomerId());
            System.out.println("    Meter Number         : " + created.getMeterNumber());
            System.out.println("    Current Reading      : " + created.getCurrentReading() + " units");
        } catch (IllegalArgumentException e) {
            System.out.println("\n[!] Error: " + e.getMessage());
        }
    }

    private void viewAllCustomers() {
        printHeader("ALL REGISTERED CUSTOMERS");
        List<Customer> list = customerService.getAllCustomers();
        if (list.isEmpty()) {
            System.out.println("No customer records found.");
            return;
        }

        printCustomerTable(list);
    }

    private void printCustomerTable(List<Customer> list) {
        System.out.println("+-----------+----------------------+-----------------+---------------+------------+------------+");
        System.out.println("| Cust ID   | Name                 | Phone           | Meter No.     | Prev Rdg   | Curr Rdg   |");
        System.out.println("+-----------+----------------------+-----------------+---------------+------------+------------+");
        for (Customer c : list) {
            System.out.printf("| %-9s | %-20s | %-15s | %-13s | %10.1f | %10.1f |\n",
                    c.getCustomerId(),
                    truncate(c.getName(), 20),
                    truncate(c.getPhone(), 15),
                    truncate(c.getMeterNumber(), 13),
                    c.getPreviousReading(),
                    c.getCurrentReading());
        }
        System.out.println("+-----------+----------------------+-----------------+---------------+------------+------------+");
        System.out.printf(" Total Customers: %d\n", list.size());
    }

    private void searchCustomer() {
        printHeader("SEARCH CUSTOMER");
        String term = prompt("Enter Search Keyword (ID, Name, Phone, Meter No, Address): ");
        if (term.trim().isEmpty()) {
            System.out.println("[!] Search keyword cannot be empty.");
            return;
        }

        List<Customer> results = customerService.searchCustomers(term);
        if (results.isEmpty()) {
            System.out.println("\nNo customers found matching: \"" + term + "\"");
        } else {
            System.out.println("\nSearch Results (" + results.size() + " matches):");
            printCustomerTable(results);
        }
    }

    private void updateCustomerInfo() {
        printHeader("UPDATE CUSTOMER DETAILS");
        String id = promptRequired("Enter Customer ID to update: ");
        Customer c = customerService.getCustomerById(id);
        if (c == null) {
            System.out.println("[!] Customer not found with ID: " + id);
            return;
        }

        System.out.println("\nCurrent Details:");
        System.out.println("  Name   : " + c.getName());
        System.out.println("  Address: " + c.getAddress());
        System.out.println("  Phone  : " + c.getPhone());
        System.out.println("  Email  : " + c.getEmail());
        System.out.println("\n(Press [Enter] to leave a field unchanged)");

        String newName = prompt("New Name [" + c.getName() + "]: ");
        String newAddr = prompt("New Address [" + c.getAddress() + "]: ");
        String newPhone = prompt("New Phone [" + c.getPhone() + "]: ");
        String newEmail = prompt("New Email [" + c.getEmail() + "]: ");

        boolean updated = customerService.updateCustomer(id, newName, newAddr, newPhone, newEmail);
        if (updated) {
            System.out.println("\n[✓] Customer profile updated successfully.");
        } else {
            System.out.println("\n[!] Failed to update customer profile.");
        }
    }

    private void deleteCustomer() {
        printHeader("DELETE CUSTOMER");
        String id = promptRequired("Enter Customer ID to delete: ");
        Customer c = customerService.getCustomerById(id);
        if (c == null) {
            System.out.println("[!] Customer not found with ID: " + id);
            return;
        }

        System.out.println("\nCustomer Found: " + c.getName() + " (Meter: " + c.getMeterNumber() + ")");
        String confirm = prompt("Are you sure you want to permanently delete this customer? (yes/no): ");
        if ("yes".equalsIgnoreCase(confirm.trim())) {
            boolean deleted = customerService.deleteCustomer(id);
            if (deleted) {
                System.out.println("[✓] Customer successfully deleted.");
            } else {
                System.out.println("[!] Could not delete customer.");
            }
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    // -------------------------------------------------------------
    // 2. Billing Management
    // -------------------------------------------------------------

    private void handleBillingMenu() {
        boolean inMenu = true;
        while (inMenu) {
            printHeader("BILLING MANAGEMENT");
            System.out.println("  [1] Generate New Bill");
            System.out.println("  [2] View All Bills");
            System.out.println("  [3] View Bills by Customer ID");
            System.out.println("  [4] View Bill Details & Tariff Slab Breakdown");
            System.out.println("  [5] View Pending (Unpaid) Bills");
            System.out.println("  [0] Back to Main Menu");
            System.out.println("--------------------------------------------------------------------------------");

            String choice = prompt("Select an option: ");
            switch (choice.trim()) {
                case "1":
                    generateBillFlow();
                    break;
                case "2":
                    viewAllBills();
                    break;
                case "3":
                    viewBillsByCustomer();
                    break;
                case "4":
                    viewBillBreakdown();
                    break;
                case "5":
                    viewPendingBills();
                    break;
                case "0":
                    inMenu = false;
                    break;
                default:
                    System.out.println("[!] Invalid choice. Please try again.");
            }
        }
    }

    private void generateBillFlow() {
        printHeader("GENERATE ELECTRICITY BILL");
        String custId = promptRequired("Enter Customer ID: ");
        Customer customer = customerService.getCustomerById(custId);
        if (customer == null) {
            System.out.println("[!] Customer not found with ID: " + custId);
            return;
        }

        System.out.println("\nCustomer Details:");
        System.out.println("  Name            : " + customer.getName());
        System.out.println("  Meter Number    : " + customer.getMeterNumber());
        System.out.println("  Previous Reading: " + customer.getCurrentReading() + " units");

        String billingMonth = promptRequired("Enter Billing Month (e.g. 2026-10 or Oct 2026): ");
        double newReading = promptDouble("Enter New Meter Reading: ", -1);

        if (newReading < customer.getCurrentReading()) {
            System.out.printf("[!] Error: New reading (%.1f) cannot be less than previous reading (%.1f).\n",
                    newReading, customer.getCurrentReading());
            return;
        }

        double units = newReading - customer.getCurrentReading();
        System.out.println("\n--- TARIFF CALCULATION PREVIEW ---");
        System.out.printf("Units Consumed: %.1f units\n", units);
        System.out.println(BillingService.getTariffBreakdown(units));
        System.out.println("----------------------------------");

        String confirm = prompt("\nConfirm bill generation? (yes/no): ");
        if ("yes".equalsIgnoreCase(confirm.trim())) {
            try {
                Bill bill = billingService.generateBill(customer, billingMonth, newReading, customerService);
                System.out.println("\n[✓] Bill successfully generated!");
                System.out.println("    Bill ID      : " + bill.getBillId());
                System.out.println("    Total Amount : Rs. " + String.format("%.2f", bill.getTotalAmount()));
                System.out.println("    Due Date     : " + bill.getDueDate());
                System.out.println("    Status       : " + bill.getStatus());
            } catch (Exception e) {
                System.out.println("[!] Error generating bill: " + e.getMessage());
            }
        } else {
            System.out.println("Bill generation cancelled.");
        }
    }

    private void viewAllBills() {
        printHeader("ALL GENERATED BILLS");
        List<Bill> list = billingService.getAllBills();
        if (list.isEmpty()) {
            System.out.println("No bills found.");
            return;
        }
        printBillsTable(list);
    }

    private void printBillsTable(List<Bill> list) {
        System.out.println("+-----------+-----------+------------+------------+------------+------------+-------------+---------+------------+");
        System.out.println("| Bill ID   | Cust ID   | Month      | Prev Rdg   | Curr Rdg   | Units      | Total (Rs.) | Status  | Due Date   |");
        System.out.println("+-----------+-----------+------------+------------+------------+------------+-------------+---------+------------+");
        for (Bill b : list) {
            System.out.printf("| %-9s | %-9s | %-10s | %10.1f | %10.1f | %10.1f | %11.2f | %-7s | %-10s |\n",
                    b.getBillId(),
                    b.getCustomerId(),
                    truncate(b.getBillingMonth(), 10),
                    b.getPreviousReading(),
                    b.getCurrentReading(),
                    b.getUnitsConsumed(),
                    b.getTotalAmount(),
                    b.getStatus(),
                    b.getDueDate());
        }
        System.out.println("+-----------+-----------+------------+------------+------------+------------+-------------+---------+------------+");
        System.out.printf(" Total Bills: %d\n", list.size());
    }

    private void viewBillsByCustomer() {
        printHeader("BILLS BY CUSTOMER");
        String custId = promptRequired("Enter Customer ID: ");
        Customer c = customerService.getCustomerById(custId);
        if (c != null) {
            System.out.println("Customer: " + c.getName() + " | Meter: " + c.getMeterNumber());
        }
        List<Bill> bills = billingService.getBillsByCustomer(custId);
        if (bills.isEmpty()) {
            System.out.println("No bills recorded for Customer ID: " + custId);
            return;
        }
        printBillsTable(bills);
    }

    private void viewBillBreakdown() {
        printHeader("BILL DETAILS & TARIFF BREAKDOWN");
        String billId = promptRequired("Enter Bill ID: ");
        Bill b = billingService.getBillById(billId);
        if (b == null) {
            System.out.println("[!] Bill not found with ID: " + billId);
            return;
        }

        Customer c = customerService.getCustomerById(b.getCustomerId());
        String cName = (c != null) ? c.getName() : "Unknown";
        String cMeter = (c != null) ? c.getMeterNumber() : "Unknown";

        System.out.println("+------------------------------------------------------------------------------+");
        System.out.printf("| BILL STATEMENT : %-59s |\n", b.getBillId());
        System.out.println("+------------------------------------------------------------------------------+");
        System.out.printf("| Customer ID     : %-25s Customer Name : %-20s |\n", b.getCustomerId(), truncate(cName, 20));
        System.out.printf("| Meter Number    : %-25s Billing Month: %-20s |\n", cMeter, b.getBillingMonth());
        System.out.printf("| Bill Date       : %-25s Due Date     : %-20s |\n", b.getBillDate(), b.getDueDate());
        System.out.printf("| Status          : %-25s                                      |\n", b.getStatus());
        System.out.println("+------------------------------------------------------------------------------+");
        System.out.println("| METER CONSUMPTION DETAILS                                                    |");
        System.out.printf("| Previous Reading: %10.1f units  |  Current Reading: %10.1f units    |\n",
                b.getPreviousReading(), b.getCurrentReading());
        System.out.printf("| Total Units Consumed: %10.1f units                                        |\n",
                b.getUnitsConsumed());
        System.out.println("+------------------------------------------------------------------------------+");
        System.out.println("| TARIFF SLAB CALCULATION                                                      |");
        System.out.println(BillingService.getTariffBreakdown(b.getUnitsConsumed()));
        System.out.println("+------------------------------------------------------------------------------+");
        System.out.printf("| FINAL AMOUNT PAYABLE : Rs. %10.2f                                         |\n", b.getTotalAmount());
        System.out.println("+------------------------------------------------------------------------------+");
    }

    private void viewPendingBills() {
        printHeader("PENDING (UNPAID) BILLS");
        List<Bill> pending = billingService.getPendingBills();
        if (pending.isEmpty()) {
            System.out.println("[✓] Great! There are no unpaid bills currently.");
            return;
        }
        printBillsTable(pending);
    }

    // -------------------------------------------------------------
    // 3. Payment Management
    // -------------------------------------------------------------

    private void handlePaymentMenu() {
        boolean inMenu = true;
        while (inMenu) {
            printHeader("PAYMENT MANAGEMENT");
            System.out.println("  [1] Record Payment for Bill");
            System.out.println("  [2] View All Payment Transactions");
            System.out.println("  [3] View Payments by Bill ID");
            System.out.println("  [4] View Payments by Customer ID");
            System.out.println("  [0] Back to Main Menu");
            System.out.println("--------------------------------------------------------------------------------");

            String choice = prompt("Select an option: ");
            switch (choice.trim()) {
                case "1":
                    recordPaymentFlow();
                    break;
                case "2":
                    viewAllPayments();
                    break;
                case "3":
                    viewPaymentsByBill();
                    break;
                case "4":
                    viewPaymentsByCustomer();
                    break;
                case "0":
                    inMenu = false;
                    break;
                default:
                    System.out.println("[!] Invalid choice. Please try again.");
            }
        }
    }

    private void recordPaymentFlow() {
        printHeader("RECORD BILL PAYMENT");
        String billId = promptRequired("Enter Bill ID to Pay: ");
        Bill bill = billingService.getBillById(billId);

        if (bill == null) {
            System.out.println("[!] Bill not found with ID: " + billId);
            return;
        }

        if (bill.isPaid()) {
            System.out.println("[!] Bill " + billId + " has already been PAID.");
            return;
        }

        Customer customer = customerService.getCustomerById(bill.getCustomerId());
        String cName = (customer != null) ? customer.getName() : "Unknown";

        System.out.println("\nBill Details:");
        System.out.println("  Customer       : " + bill.getCustomerId() + " (" + cName + ")");
        System.out.println("  Billing Month  : " + bill.getBillingMonth());
        System.out.println("  Units Consumed : " + bill.getUnitsConsumed());
        System.out.printf("  Amount Due     : Rs. %.2f\n", bill.getTotalAmount());
        System.out.println("  Due Date       : " + bill.getDueDate());

        System.out.println("\nSelect Payment Method:");
        System.out.println("  [1] Cash");
        System.out.println("  [2] Debit / Credit Card");
        System.out.println("  [3] UPI (Google Pay, PhonePe, Paytm, etc.)");
        System.out.println("  [4] Net Banking");
        String modeChoice = prompt("Choose payment mode (1-4): ");

        String paymentMode;
        switch (modeChoice.trim()) {
            case "1": paymentMode = "CASH"; break;
            case "2": paymentMode = "CARD"; break;
            case "3": paymentMode = "UPI"; break;
            case "4": paymentMode = "NET_BANKING"; break;
            default:
                paymentMode = "CASH";
                System.out.println("Defaulting to CASH.");
        }

        String txnRef = prompt("Enter Transaction Reference / Cheque / Note (optional): ");

        try {
            Payment payment = paymentService.recordPayment(billId, paymentMode, txnRef, billingService);
            System.out.println("\n================================================================================");
            System.out.println("                     ⚡  PAYMENT RECEIPT  ⚡                                  ");
            System.out.println("================================================================================");
            System.out.println("  Payment ID       : " + payment.getPaymentId());
            System.out.println("  Bill ID          : " + payment.getBillId());
            System.out.println("  Customer ID      : " + payment.getCustomerId() + " (" + cName + ")");
            System.out.printf("  Amount Paid      : Rs. %.2f\n", payment.getAmount());
            System.out.println("  Payment Mode     : " + payment.getPaymentMethod());
            System.out.println("  Transaction Ref  : " + payment.getTransactionRef());
            System.out.println("  Payment Date     : " + payment.getPaymentDate());
            System.out.println("  Bill Status      : PAID [✓]");
            System.out.println("================================================================================");
        } catch (Exception e) {
            System.out.println("\n[!] Error processing payment: " + e.getMessage());
        }
    }

    private void viewAllPayments() {
        printHeader("ALL PAYMENT TRANSACTIONS");
        List<Payment> list = paymentService.getAllPayments();
        if (list.isEmpty()) {
            System.out.println("No payment transactions recorded yet.");
            return;
        }
        printPaymentsTable(list);
    }

    private void printPaymentsTable(List<Payment> list) {
        System.out.println("+-----------+-----------+-----------+-------------+------------+-------------+----------------------+");
        System.out.println("| Pay ID    | Bill ID   | Cust ID   | Amount (Rs) | Date       | Method      | Reference            |");
        System.out.println("+-----------+-----------+-----------+-------------+------------+-------------+----------------------+");
        for (Payment p : list) {
            System.out.printf("| %-9s | %-9s | %-9s | %11.2f | %-10s | %-11s | %-20s |\n",
                    p.getPaymentId(),
                    p.getBillId(),
                    p.getCustomerId(),
                    p.getAmount(),
                    p.getPaymentDate(),
                    p.getPaymentMethod(),
                    truncate(p.getTransactionRef(), 20));
        }
        System.out.println("+-----------+-----------+-----------+-------------+------------+-------------+----------------------+");
        System.out.printf(" Total Payments: %d\n", list.size());
    }

    private void viewPaymentsByBill() {
        printHeader("PAYMENTS BY BILL ID");
        String billId = promptRequired("Enter Bill ID: ");
        List<Payment> list = paymentService.getPaymentsByBillId(billId);
        if (list.isEmpty()) {
            System.out.println("No payments recorded for Bill ID: " + billId);
            return;
        }
        printPaymentsTable(list);
    }

    private void viewPaymentsByCustomer() {
        printHeader("PAYMENTS BY CUSTOMER ID");
        String custId = promptRequired("Enter Customer ID: ");
        List<Payment> list = paymentService.getPaymentsByCustomerId(custId);
        if (list.isEmpty()) {
            System.out.println("No payments recorded for Customer ID: " + custId);
            return;
        }
        printPaymentsTable(list);
    }

    // -------------------------------------------------------------
    // 4. Reports & Analytics
    // -------------------------------------------------------------

    private void handleReportsMenu() {
        boolean inMenu = true;
        while (inMenu) {
            printHeader("REPORTS & ANALYTICS");
            System.out.println("  [1] Executive Financial & Operational Summary");
            System.out.println("  [2] Pending (Unpaid) Bills Report");
            System.out.println("  [3] Month-wise Billing & Collections Report");
            System.out.println("  [4] Top Consumers Ranking (By Consumption)");
            System.out.println("  [0] Back to Main Menu");
            System.out.println("--------------------------------------------------------------------------------");

            String choice = prompt("Select an option: ");
            switch (choice.trim()) {
                case "1":
                    viewSummaryReport();
                    break;
                case "2":
                    viewPendingBillsReport();
                    break;
                case "3":
                    viewMonthWiseReport();
                    break;
                case "4":
                    viewTopConsumersReport();
                    break;
                case "0":
                    inMenu = false;
                    break;
                default:
                    System.out.println("[!] Invalid choice. Please try again.");
            }
        }
    }

    private void viewSummaryReport() {
        printHeader("EXECUTIVE SUMMARY REPORT");
        ReportService.SummaryData d = reportService.getSummaryData();

        System.out.println("+------------------------------------------------------------------------------+");
        System.out.println("| METRIC                                       | VALUE                         |");
        System.out.println("+------------------------------------------------------------------------------+");
        System.out.printf("| Total Registered Customers                   | %-29d |\n", d.totalCustomers);
        System.out.printf("| Total Bills Generated                        | %-29d |\n", d.totalBills);
        System.out.printf("| Paid Bills Count                             | %-29d |\n", d.paidBillsCount);
        System.out.printf("| Unpaid (Pending) Bills Count                 | %-29d |\n", d.unpaidBillsCount);
        System.out.printf("| Total Electricity Consumed                   | %-23.1f units |\n", d.totalUnitsConsumed);
        System.out.println("+------------------------------------------------------------------------------+");
        System.out.printf("| Total Billed Amount                          | Rs. %-25.2f |\n", d.totalBilledAmount);
        System.out.printf("| Total Amount Collected                       | Rs. %-25.2f |\n", d.totalCollectedAmount);
        System.out.printf("| Total Amount Pending                         | Rs. %-25.2f |\n", d.totalPendingAmount);
        System.out.println("+------------------------------------------------------------------------------+");

        double collectionRate = (d.totalBilledAmount > 0)
                ? (d.totalCollectedAmount / d.totalBilledAmount) * 100.0
                : 0.0;
        System.out.printf(" Overall Collection Efficiency: %.2f%%\n", collectionRate);
    }

    private void viewPendingBillsReport() {
        printHeader("PENDING BILLS AUDIT REPORT");
        List<Bill> pending = billingService.getPendingBills();
        if (pending.isEmpty()) {
            System.out.println("[✓] No pending bills. All generated bills are settled!");
            return;
        }

        double totalOutstanding = 0.0;
        System.out.println("+-----------+-----------+----------------------+------------+------------+---------------+");
        System.out.println("| Bill ID   | Cust ID   | Customer Name        | Due Date   | Units      | Amount Due    |");
        System.out.println("+-----------+-----------+----------------------+------------+------------+---------------+");
        for (Bill b : pending) {
            Customer c = customerService.getCustomerById(b.getCustomerId());
            String name = (c != null) ? c.getName() : "Unknown";
            totalOutstanding += b.getTotalAmount();
            System.out.printf("| %-9s | %-9s | %-20s | %-10s | %10.1f | Rs. %9.2f |\n",
                    b.getBillId(),
                    b.getCustomerId(),
                    truncate(name, 20),
                    b.getDueDate(),
                    b.getUnitsConsumed(),
                    b.getTotalAmount());
        }
        System.out.println("+-----------+-----------+----------------------+------------+------------+---------------+");
        System.out.printf(" Total Pending Bills: %d | Total Outstanding Debt: Rs. %.2f\n",
                pending.size(), totalOutstanding);
    }

    private void viewMonthWiseReport() {
        printHeader("MONTH-WISE BILLING & COLLECTIONS REPORT");
        List<ReportService.MonthlyData> list = reportService.getMonthWiseReport();
        if (list.isEmpty()) {
            System.out.println("No monthly billing data available.");
            return;
        }

        System.out.println("+------------+--------+------------+---------------+---------------+---------------+");
        System.out.println("| Month/Year | Bills  | Units      | Billed (Rs)   | Paid (Rs)     | Pending (Rs)  |");
        System.out.println("+------------+--------+------------+---------------+---------------+---------------+");
        for (ReportService.MonthlyData m : list) {
            System.out.printf("| %-10s | %6d | %10.1f | %13.2f | %13.2f | %13.2f |\n",
                    m.monthYear,
                    m.billCount,
                    m.units,
                    m.billedAmount,
                    m.paidAmount,
                    m.pendingAmount);
        }
        System.out.println("+------------+--------+------------+---------------+---------------+---------------+");
    }

    private void viewTopConsumersReport() {
        printHeader("TOP CONSUMERS RANKING REPORT");
        int limit = promptInt("Enter number of top consumers to view (e.g. 5 or 10): ", 5);
        List<ReportService.TopConsumerData> list = reportService.getTopConsumers(limit);

        if (list.isEmpty()) {
            System.out.println("No consumer consumption data found.");
            return;
        }

        System.out.println("+------+-----------+----------------------+---------------+-------------+---------------+");
        System.out.println("| Rank | Cust ID   | Customer Name        | Meter No.     | Total Units | Billed (Rs.)  |");
        System.out.println("+------+-----------+----------------------+---------------+-------------+---------------+");
        int rank = 1;
        for (ReportService.TopConsumerData tc : list) {
            System.out.printf("| %4d | %-9s | %-20s | %-13s | %11.1f | %13.2f |\n",
                    rank++,
                    tc.customerId,
                    truncate(tc.customerName, 20),
                    truncate(tc.meterNumber, 13),
                    tc.totalUnits,
                    tc.totalBilled);
        }
        System.out.println("+------+-----------+----------------------+---------------+-------------+---------------+");
    }

    // -------------------------------------------------------------
    // 5. Admin Settings
    // -------------------------------------------------------------

    private void handleAdminSettings() {
        boolean inMenu = true;
        while (inMenu) {
            printHeader("ADMINISTRATOR SETTINGS");
            System.out.println("  [1] Change Password");
            System.out.println("  [2] View Current Admin Profile");
            System.out.println("  [3] Register New Administrator");
            System.out.println("  [0] Back to Main Menu");
            System.out.println("--------------------------------------------------------------------------------");

            String choice = prompt("Select an option: ");
            switch (choice.trim()) {
                case "1":
                    changeAdminPassword();
                    break;
                case "2":
                    viewAdminProfile();
                    break;
                case "3":
                    registerNewAdmin();
                    break;
                case "0":
                    inMenu = false;
                    break;
                default:
                    System.out.println("[!] Invalid choice. Please try again.");
            }
        }
    }

    private void changeAdminPassword() {
        printHeader("CHANGE ADMIN PASSWORD");
        Admin current = authService.getCurrentAdmin();
        if (current == null) {
            System.out.println("[!] No admin currently logged in.");
            return;
        }

        String oldPass = promptRequired("Enter Current Password: ");
        String newPass = promptRequired("Enter New Password: ");
        String confirmPass = promptRequired("Confirm New Password: ");

        if (!newPass.equals(confirmPass)) {
            System.out.println("[!] New passwords do not match.");
            return;
        }

        boolean success = authService.changePassword(current.getUsername(), oldPass, newPass);
        if (success) {
            System.out.println("\n[✓] Password changed successfully!");
        } else {
            System.out.println("\n[!] Current password incorrect. Password change failed.");
        }
    }

    private void viewAdminProfile() {
        printHeader("ADMINISTRATOR PROFILE");
        Admin current = authService.getCurrentAdmin();
        if (current != null) {
            System.out.println("  Username  : " + current.getUsername());
            System.out.println("  Full Name : " + current.getFullName());
            System.out.println("  Role      : " + current.getRole());
        }
    }

    private void registerNewAdmin() {
        printHeader("REGISTER NEW ADMINISTRATOR");
        String username = promptRequired("Enter Admin Username: ");
        String password = promptRequired("Enter Password: ");
        String fullName = promptRequired("Enter Full Name: ");
        String role = prompt("Enter Role (e.g. BILLING_OPERATOR, ADMIN, default ADMIN): ");
        if (role.trim().isEmpty()) {
            role = "ADMIN";
        }

        boolean added = authService.addAdmin(username, password, fullName, role);
        if (added) {
            System.out.println("\n[✓] New administrator account created successfully: " + username);
        } else {
            System.out.println("\n[!] Failed to add administrator. Username might already exist.");
        }
    }

    // -------------------------------------------------------------
    // Input Helpers
    // -------------------------------------------------------------

    private String prompt(String message) {
        System.out.print(message);
        if (scanner.hasNextLine()) {
            return scanner.nextLine();
        }
        return "0";
    }

    private String promptRequired(String message) {
        while (scanner.hasNextLine()) {
            System.out.print(message);
            String input = scanner.nextLine();
            if (input != null && !input.trim().isEmpty()) {
                return input.trim();
            }
            System.out.println("  [!] Field cannot be empty. Please enter a value.");
        }
        return "";
    }

    private double promptDouble(String message, double defaultValue) {
        while (scanner.hasNextLine()) {
            System.out.print(message);
            String input = scanner.nextLine();
            if (input.trim().isEmpty()) {
                return defaultValue;
            }
            try {
                return Double.parseDouble(input.trim());
            } catch (NumberFormatException e) {
                System.out.println("  [!] Invalid number format. Please enter a valid decimal number.");
            }
        }
        return defaultValue;
    }

    private int promptInt(String message, int defaultValue) {
        while (scanner.hasNextLine()) {
            System.out.print(message);
            String input = scanner.nextLine();
            if (input.trim().isEmpty()) {
                return defaultValue;
            }
            try {
                return Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                System.out.println("  [!] Invalid integer format. Please enter a whole number.");
            }
        }
        return defaultValue;
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen - 2) + "..";
    }
}
