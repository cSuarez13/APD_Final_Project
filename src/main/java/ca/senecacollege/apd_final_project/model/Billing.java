package ca.senecacollege.apd_final_project.model;

import javafx.beans.property.*;

import java.time.LocalDateTime;

public class Billing {
    private final IntegerProperty billID = new SimpleIntegerProperty(this, "billID");
    private final IntegerProperty reservationID = new SimpleIntegerProperty(this, "reservationID");
    private final DoubleProperty amount = new SimpleDoubleProperty(this, "amount");
    private final DoubleProperty tax = new SimpleDoubleProperty(this, "tax");
    private final DoubleProperty totalAmount = new SimpleDoubleProperty(this, "totalAmount");
    private final DoubleProperty discount = new SimpleDoubleProperty(this, "discount");
    private final ObjectProperty<LocalDateTime> billingDateTime = new SimpleObjectProperty<>(this, "billingDateTime");
    private final BooleanProperty paid = new SimpleBooleanProperty(this, "paid");

    // Tax rate constant
    public static final double TAX_RATE = 0.13; // 13% tax

    public Billing() {
        // Default constructor
        this.tax.bind(amount.multiply(TAX_RATE));
        this.totalAmount.bind(amount.add(tax).subtract(discount));
    }

    public Billing(int billID, int reservationID, double amount, double discount,
                   LocalDateTime billingDateTime, boolean paid) {
        this.billID.set(billID);
        this.reservationID.set(reservationID);
        this.amount.set(amount);
        this.discount.set(discount);
        this.billingDateTime.set(billingDateTime);
        this.paid.set(paid);

        // Bind tax and total amount calculations
        this.tax.bind(this.amount.multiply(TAX_RATE));
        this.totalAmount.bind(this.amount.add(this.tax).subtract(this.discount));
    }

    public int getBillID() {
        return billID.get();
    }

    public void setBillID(int billID) {
        this.billID.set(billID);
    }

    public int getReservationID() {
        return reservationID.get();
    }

    public void setReservationID(int reservationID) {
        this.reservationID.set(reservationID);
    }

    public double getAmount() {
        return amount.get();
    }

    public void setAmount(double amount) {
        this.amount.set(amount);
    }

    public double getTax() {
        return tax.get();
    }

    public double getTotalAmount() {
        return totalAmount.get();
    }

    public double getDiscount() {
        return discount.get();
    }

    public void setDiscount(double discount) {
        this.discount.set(discount);
    }

    public LocalDateTime getBillingDateTime() {
        return billingDateTime.get();
    }

    public void setBillingDateTime(LocalDateTime billingDateTime) {
        this.billingDateTime.set(billingDateTime);
    }

    public boolean isPaid() {
        return paid.get();
    }

    public void setPaid(boolean paid) {
        this.paid.set(paid);
    }

    // Methods
    public void generateBill(int reservationID, double amount) {
        setReservationID(reservationID);
        setAmount(amount);
        setDiscount(0.0);
        setBillingDateTime(LocalDateTime.now());
        setPaid(false);
    }

    public void applyDiscount(double discountAmount) {
        if (discountAmount <= getAmount()) {
            setDiscount(discountAmount);
        } else {
            throw new IllegalArgumentException("Discount cannot be greater than the amount.");
        }
    }

    public String generateBillSummary() {
        return "Bill #" + getBillID() +
                "\nReservation ID: " + getReservationID() +
                "\nSubtotal: $" + String.format("%.2f", getAmount()) +
                "\nTax (13%): $" + String.format("%.2f", getTax()) +
                "\nDiscount: $" + String.format("%.2f", getDiscount()) +
                "\nTotal Amount: $" + String.format("%.2f", getTotalAmount()) +
                "\nDate/Time: " + getBillingDateTime() +
                "\nStatus: " + (isPaid() ? "Paid" : "Unpaid");
    }

    @Override
    public String toString() {
        return "Bill #" + getBillID() + " - $" + String.format("%.2f", getTotalAmount()) +
                " - " + (isPaid() ? "Paid" : "Unpaid");
    }
}