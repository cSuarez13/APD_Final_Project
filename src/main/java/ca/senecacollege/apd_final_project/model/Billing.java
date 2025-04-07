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
    private final DoubleProperty totalAmount = new SimpleDoubleProperty(this, "totalAmount");
    private final DoubleProperty discount = new SimpleDoubleProperty(this, "discount");
    private final ObjectProperty<LocalDateTime> billingDateTime = new SimpleObjectProperty<>(this, "billingDateTime");
    private final BooleanProperty paid = new SimpleBooleanProperty(this, "paid");

    // List to store the itemized room charges (not stored in database, calculated on-the-fly)
    private final List<BillingItem> billingItems = new ArrayList<>();

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

    public List<BillingItem> getBillingItems() {
        return billingItems;
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