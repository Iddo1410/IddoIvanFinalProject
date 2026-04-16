package com.example.iddoivanfinalproject.model;

import java.util.List;

public class Order {
    private String id;
    private String userId;
    private String userEmail;
    private List<Cart> items;
    private double totalPrice;
    private long timestamp;

    public Order() {}

    public Order(String userId, String userEmail, List<Cart> items, double totalPrice, long timestamp) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.items = items;
        this.totalPrice = totalPrice;
        this.timestamp = timestamp;
    }


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public String getUserEmail() { return userEmail; }
    public List<Cart> getItems() { return items; }
    public double getTotalPrice() { return totalPrice; }
    public long getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", items=" + items +
                ", totalPrice=" + totalPrice +
                ", timestamp=" + timestamp +
                '}';
    }
}