package service;

import model.Bill;
import model.Customer;
import model.Payment;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for computing analytics, financial summaries, and operational reports.
 */
public class ReportService {
    private final CustomerService customerService;
    private final BillingService billingService;
    private final PaymentService paymentService;

    public ReportService(CustomerService customerService, BillingService billingService, PaymentService paymentService) {
        this.customerService = customerService;
        this.billingService = billingService;
        this.paymentService = paymentService;
    }

    /**
     * Financial & Operational Summary metrics.
     */
    public static class SummaryData {
        public int totalCustomers;
        public int totalBills;
        public int paidBillsCount;
        public int unpaidBillsCount;
        public double totalBilledAmount;
        public double totalCollectedAmount;
        public double totalPendingAmount;
        public double totalUnitsConsumed;
    }

    public SummaryData getSummaryData() {
        SummaryData data = new SummaryData();
        List<Customer> customers = customerService.getAllCustomers();
        List<Bill> bills = billingService.getAllBills();
        List<Payment> payments = paymentService.getAllPayments();

        data.totalCustomers = customers.size();
        data.totalBills = bills.size();

        for (Bill b : bills) {
            data.totalBilledAmount += b.getTotalAmount();
            data.totalUnitsConsumed += b.getUnitsConsumed();
            if (b.isPaid()) {
                data.paidBillsCount++;
            } else {
                data.unpaidBillsCount++;
                data.totalPendingAmount += b.getTotalAmount();
            }
        }

        for (Payment p : payments) {
            data.totalCollectedAmount += p.getAmount();
        }

        return data;
    }

    /**
     * Month-wise billing & collection aggregation.
     */
    public static class MonthlyData {
        public String monthYear;
        public int billCount;
        public double units;
        public double billedAmount;
        public double paidAmount;
        public double pendingAmount;
    }

    public List<MonthlyData> getMonthWiseReport() {
        List<Bill> bills = billingService.getAllBills();
        Map<String, MonthlyData> map = new LinkedHashMap<>();

        for (Bill b : bills) {
            String m = (b.getBillingMonth() != null && !b.getBillingMonth().trim().isEmpty())
                    ? b.getBillingMonth()
                    : "Unknown";

            MonthlyData md = map.computeIfAbsent(m, k -> {
                MonthlyData obj = new MonthlyData();
                obj.monthYear = k;
                return obj;
            });

            md.billCount++;
            md.units += b.getUnitsConsumed();
            md.billedAmount += b.getTotalAmount();
            if (b.isPaid()) {
                md.paidAmount += b.getTotalAmount();
            } else {
                md.pendingAmount += b.getTotalAmount();
            }
        }

        return new ArrayList<>(map.values());
    }

    /**
     * Top consumers aggregation.
     */
    public static class TopConsumerData {
        public String customerId;
        public String customerName;
        public String meterNumber;
        public double totalUnits;
        public double totalBilled;
        public double totalPaid;
        public int billsCount;
    }

    public List<TopConsumerData> getTopConsumers(int limit) {
        List<Customer> customers = customerService.getAllCustomers();
        List<Bill> bills = billingService.getAllBills();

        Map<String, TopConsumerData> map = new HashMap<>();

        for (Customer c : customers) {
            TopConsumerData cd = new TopConsumerData();
            cd.customerId = c.getCustomerId();
            cd.customerName = c.getName();
            cd.meterNumber = c.getMeterNumber();
            map.put(c.getCustomerId(), cd);
        }

        for (Bill b : bills) {
            TopConsumerData cd = map.get(b.getCustomerId());
            if (cd != null) {
                cd.billsCount++;
                cd.totalUnits += b.getUnitsConsumed();
                cd.totalBilled += b.getTotalAmount();
                if (b.isPaid()) {
                    cd.totalPaid += b.getTotalAmount();
                }
            }
        }

        List<TopConsumerData> list = new ArrayList<>(map.values());
        // Sort descending by total units consumed, then total billed
        list.sort((a, b) -> {
            int cmp = Double.compare(b.totalUnits, a.totalUnits);
            if (cmp != 0) return cmp;
            return Double.compare(b.totalBilled, a.totalBilled);
        });

        if (limit > 0 && list.size() > limit) {
            return list.subList(0, limit);
        }
        return list;
    }
}
