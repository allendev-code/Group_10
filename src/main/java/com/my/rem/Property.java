
package com.my.rem;

public class Property {
    private int propertyId;
    private String location;
    private double price;
    private String status; // Available, Reserved, Sold

    public Property(int propertyId, String location, double price, String status) {
        this.propertyId = propertyId;
        this.location = location;
        this.price = price;
        this.status = status;
    }

    public int getPropertyId() { return propertyId; }
    public String getLocation() { return location; }
    public double getPrice() { return price; }
    public String getStatus() { return status; }

    public void setLocation(String location) { this.location = location; }
    public void setPrice(double price) { this.price = price; }
    public void setStatus(String status) { this.status = status; }
}
