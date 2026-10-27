package model;

/**
 * Represents a Customer in the Electricity Bill Management System.
 */
public class Customer {
    private String customerId;
    private String name;
    private String address;
    private String phone;
    private String email;
    private String meterNumber;
    private double previousReading;
    private double currentReading;

    public Customer(String customerId, String name, String address, String phone,
                    String email, String meterNumber, double previousReading, double currentReading) {
        this.customerId = customerId;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.meterNumber = meterNumber;
        this.previousReading = previousReading;
        this.currentReading = currentReading;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMeterNumber() {
        return meterNumber;
    }

    public void setMeterNumber(String meterNumber) {
        this.meterNumber = meterNumber;
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

    /**
     * Serializes this Customer to a pipe-delimited string.
     */
    public String toFileString() {
        return customerId + "|" +
                name + "|" +
                address + "|" +
                phone + "|" +
                email + "|" +
                meterNumber + "|" +
                previousReading + "|" +
                currentReading;
    }

    /**
     * Deserializes a pipe-delimited string into a Customer object.
     */
    public static Customer fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        String[] parts = line.split("\\|", -1);
        if (parts.length >= 8) {
            try {
                String id = parts[0];
                String name = parts[1];
                String address = parts[2];
                String phone = parts[3];
                String email = parts[4];
                String meterNo = parts[5];
                double prev = Double.parseDouble(parts[6]);
                double curr = Double.parseDouble(parts[7]);
                return new Customer(id, name, address, phone, email, meterNo, prev, curr);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("Customer[ID=%s, Name=%s, Meter=%s, Phone=%s, Readings=%.1f->%.1f]",
                customerId, name, meterNumber, phone, previousReading, currentReading);
    }
}
