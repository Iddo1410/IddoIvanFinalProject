package com.example.iddoivanfinalproject.model;

public class Item {
    private String id;
    private String name;
    private String type;
    private String brand;
    private String model;
    private double price;
    private String year;
    private String details;
    private double height;
    private double width;
    private double depth;
    private double volume;

    private String pic;

    public Item(String id, String name, String type, String brand, String model, double price, String year, String details, String pic) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.brand = brand;
        this.model = model;
        this.price = price;
        this.year = year;
        this.details = details;
        this.pic = pic;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    @Override
    public String toString() {
        return "Item{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", price=" + price +
                ", year='" + year + '\'' +
                ", details='" + details + '\'' +
                ", pic='" + pic + '\'' +
                '}';
    }
}
