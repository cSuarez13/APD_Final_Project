package ca.senecacollege.apd_final_project.model;

public enum RoomType {
    SINGLE("Single Room", 100.0, 2),
    DOUBLE("Double Room", 180.0, 4),
    DELUXE("Deluxe Room", 250.0, 2),
    PENT_HOUSE("Pent House", 400.0, 2);

    private final String displayName;
    private final double basePrice;
    private final int maxOccupancy;

    RoomType(String displayName, double basePrice, int maxOccupancy) {
        this.displayName = displayName;
        this.basePrice = basePrice;
        this.maxOccupancy = maxOccupancy;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public int getMaxOccupancy() {
        return maxOccupancy;
    }

    @Override
    public String toString() {
        return displayName;
    }
}