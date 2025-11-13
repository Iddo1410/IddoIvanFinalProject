package com.example.iddoivanfinalproject.model;

public class Cart {
    private String name;
    private double price;
    private int quantity;
    private String id;

    public Cart(String name, double price, int quantity, String id) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.id=id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
            return price;
    }

    public void setPrice(double price) {
        if (price>=0) {
            this.price = price;
        }
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Cart{" +
                "name='" + name + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", id='" + id + '\'' +
                '}';
    }
}
