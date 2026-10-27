package model;

import java.time.LocalDate;

/**
 * Represents an Electricity Bill.
 */
public class Bill {
    private String billId;
    private String customerId;
    private String billingMonth;
    private double previousReading;
    private double currentReading;
    private double unitsConsumed;
    private double energyCharges;
    private double serviceCharge;
    private double totalAmount;
    private String status; // "UNPAID" or "PAID"
    private String billDate;
    private String dueDate;

    public Bill(String billId, String customerId, String billingMonth,
                double previousReading, double currentReading, double unitsConsumed,
                double energyCharges, double serviceCharge, double totalAmount,
                String status, String billDate, String dueDate) {
        this.billId = billId;
        this.customerId = customerId;
        this.billingMonth = billingMonth;
        this.previousReading = previousReading;
        this.currentReading = currentReading;
        this.unitsConsumed = unitsConsumed;
        this.energyCharges = energyCharges;
        this.serviceCharge = serviceCharge;
        this.totalAmount = totalAmount;
        this.status = status;
        this.billDate = billDate;
        this.dueDate = dueDate;
    }

    public String getBillId() {
        return billId;
    }

    public void setBillId(String billId) {
        this.billId = billId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getBillingMonth() {
        return billingMonth;
    }

    public void setBillingMonth(String billingMonth) {
        this.billingMonth = billingMonth;
    }

    public double getPreviousReading() {
        return previousReading;
    }

    public void setPreviousReading(double previousReading) {
        this.previousReading = previousReading;
    }

    public double getCurrentReading() {
        return currentReading;
    }

    public void setCurrentReading(double currentReading) {
        this.currentReading = currentReading;
    }

    public double getUnitsConsumed() {
        return unitsConsumed;
    }

    public void setUnitsConsumed(double unitsConsumed) {
        this.unitsConsumed = unitsConsumed;
    }

    public double getEnergyCharges() {
        return energyCharges;
    }

    public void setEnergyCharges(double energyCharges) {
        this.energyCharges = energyCharges;
    }

    public double getServiceCharge() {
        return serviceCharge;
    }

    public void setServiceCharge(double serviceCharge) {
        this.serviceCharge = serviceCharge;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBillDate() {
        return billDate;
    }

    public void setBillDate(String billDate) {
        this.billDate = billDate;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isPaid() {
        return "PAID".equalsIgnoreCase(this.status);
    }

    /**
     * Serializes this Bill into a pipe-delimited string.
     */
    public String toFileString() {
        return billId + "|" +
                customerId + "|" +
                billingMonth + "|" +
                previousReading + "|" +
                currentReading + "|" +
                unitsConsumed + "|" +
                energyCharges + "|" +
                serviceCharge + "|" +
                totalAmount + "|" +
                status + "|" +
                billDate + "|" +
                dueDate;
    }

    /**
     * Deserializes a pipe-delimited string into a Bill object.
     */
    public static Bill fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        String[] parts = line.split("\\|", -1);
        if (parts.length >= 12) {
            try {
                String billId = parts[0];
                String customerId = parts[1];
                String billingMonth = parts[2];
                double prev = Double.parseDouble(parts[3]);
                double curr = Double.parseDouble(parts[4]);
                double units = Double.parseDouble(parts[5]);
                double energy = Double.parseDouble(parts[6]);
                double service = Double.parseDouble(parts[7]);
                double total = Double.parseDouble(parts[8]);
                String status = parts[9];
                String billDate = parts[10];
                String dueDate = parts[11];

                return new Bill(billId, customerId, billingMonth, prev, curr, units,
                        energy, service, total, status, billDate, dueDate);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("Bill[ID=%s, CustomerID=%s, Month=%s, Units=%.1f, Total=Rs.%.2f, Status=%s]",
                billId, customerId, billingMonth, unitsConsumed, totalAmount, status);
    }
}
