package model;

/**
 * Represents a payment transaction in the Electricity Bill Management System.
 */
public class Payment {
    private String paymentId;
    private String billId;
    private String customerId;
    private double amount;
    private String paymentDate;
    private String paymentMethod; // "CASH", "CARD", "UPI", "NET_BANKING"
    private String transactionRef;

    public Payment(String paymentId, String billId, String customerId,
                   double amount, String paymentDate, String paymentMethod, String transactionRef) {
        this.paymentId = paymentId;
        this.billId = billId;
        this.customerId = customerId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.transactionRef = transactionRef;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public void setTransactionRef(String transactionRef) {
        this.transactionRef = transactionRef;
    }

    /**
     * Serializes this Payment to a pipe-delimited string.
     */
    public String toFileString() {
        return paymentId + "|" +
                billId + "|" +
                customerId + "|" +
                amount + "|" +
                paymentDate + "|" +
                paymentMethod + "|" +
                transactionRef;
    }

    /**
     * Deserializes a pipe-delimited string into a Payment object.
     */
    public static Payment fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        String[] parts = line.split("\\|", -1);
        if (parts.length >= 7) {
            try {
                String payId = parts[0];
                String billId = parts[1];
                String custId = parts[2];
                double amt = Double.parseDouble(parts[3]);
                String date = parts[4];
                String method = parts[5];
                String ref = parts[6];
                return new Payment(payId, billId, custId, amt, date, method, ref);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("Payment[ID=%s, BillID=%s, CustID=%s, Amount=Rs.%.2f, Date=%s, Mode=%s]",
                paymentId, billId, customerId, amount, paymentDate, paymentMethod);
    }
}
