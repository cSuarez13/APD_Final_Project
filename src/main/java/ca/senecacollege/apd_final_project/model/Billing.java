package ca.senecacollege.apd_final_project.model;

import javafx.beans.property.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Billing {
    private final IntegerProperty billID = new SimpleIntegerProperty(this, "billID");
    private final IntegerProperty reservationID = new SimpleIntegerProperty(this, "reservationID");
    private final DoubleProperty amount = new SimpleDoubleProperty(this, "amount");
    private final DoubleProperty tax = new SimpleDoubleProperty(this, "tax");
    private final DoubleProperty discount = new SimpleDoubleProperty(this, "discount");
    private final DoubleProperty totalAmount = new SimpleDoubleProperty(this, "totalAmount");
    private final StringProperty paymentMethod = new SimpleStringProperty(this, "paymentMethod");
    private final ObjectProperty<LocalDateTime> billingDateTime = new SimpleObjectProperty<>(this, "billingDateTime");
    private final BooleanProperty paid = new SimpleBooleanProperty(this, "paid");

    private final List<BillingItem> billingItems = new ArrayList<>();

    // Add constructors
    public Billing() {
        // Default constructor
    }

    public Billing(int billID, int reservationID, double amount, double tax, double discount,
                   double totalAmount, String paymentMethod, LocalDateTime billingDateTime, boolean paid) {
        this.billID.set(billID);
        this.reservationID.set(reservationID);
        this.amount.set(amount);
        this.tax.set(tax);
        this.discount.set(discount);
        this.totalAmount.set(totalAmount);
        this.paymentMethod.set(paymentMethod);
        this.billingDateTime.set(billingDateTime);
        this.paid.set(paid);
    }

    // Add getters/setters for new fields
    public double getTax() {
        return tax.get();
    }

    public void setTax(double tax) {
        this.tax.set(tax);
    }

    public DoubleProperty taxProperty() {
        return tax;
    }

    /**
     * Calculate the total amount (amount + tax - discount)
     *
     * @return The total amount
     */
    public double getTotalAmount() {
        // If totalAmount is already set, return it
        if (totalAmount.get() > 0) {
            return totalAmount.get();
        }

        // Otherwise calculate it
        double total = getAmount() + getTax() - getDiscount();

        // Update the property
        setTotalAmount(total);

        return total;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount.set(totalAmount);
    }

    public DoubleProperty totalAmountProperty() {
        return totalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod.get();
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod.set(paymentMethod);
    }

    public StringProperty paymentMethodProperty() {
        return paymentMethod;
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


    public void addBillingItem(BillingItem item) {
        billingItems.add(item);
        updateTotalAmount();
    }

    public void removeBillingItem(BillingItem item) {
        billingItems.remove(item);
        updateTotalAmount();
    }

    public void clearBillingItems() {
        billingItems.clear();
        updateTotalAmount();
    }

    private void updateTotalAmount() {
        double total = 0;
        for (BillingItem item : billingItems) {
            total += item.getSubtotal();
        }
        setAmount(total);
    }

    @Override
    public String toString() {
        return "Bill #" + getBillID() + " - $" + String.format("%.2f", getTotalAmount()) +
                " - " + (isPaid() ? "Paid" : "Unpaid");
    }

    /**
     * Represents an individual line item in a bill, typically a room charge
     */
    public static class BillingItem {
        private final int roomId;
        private final String roomType;
        private final int nights;
        private final double pricePerNight;
        private final double subtotal;

        public BillingItem(int roomId, String roomType, int nights, double pricePerNight) {
            this.roomId = roomId;
            this.roomType = roomType;
            this.nights = nights;
            this.pricePerNight = pricePerNight;
            this.subtotal = nights * pricePerNight;
        }

        public int getRoomId() {
            return roomId;
        }

        public String getRoomType() {
            return roomType;
        }

        public int getNights() {
            return nights;
        }

        public double getPricePerNight() {
            return pricePerNight;
        }

        public double getSubtotal() {
            return subtotal;
        }

        @Override
        public String toString() {
            return "Room #" + roomId + " (" + roomType + "): " +
                    nights + " nights × $" + String.format("%.2f", pricePerNight) +
                    " = $" + String.format("%.2f", subtotal);
        }
    }
}