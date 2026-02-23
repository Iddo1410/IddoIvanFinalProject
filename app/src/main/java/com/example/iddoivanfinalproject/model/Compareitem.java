package com.example.iddoivanfinalproject.model;

import java.util.ArrayList;

public class Compareitem {

    String id;
    String type;
    String date;
    ArrayList<Item> itemArrayList;


    public Compareitem(String id, String type, String date, ArrayList<Item> itemArrayList) {
        this.id = id;
        this.type = type;
        this.date = date;
        this.itemArrayList = itemArrayList;
    }

    public Compareitem() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public ArrayList<Item> getItemArrayList() {
        return itemArrayList;
    }

    public void setItemArrayList(ArrayList<Item> itemArrayList) {
        this.itemArrayList = itemArrayList;
    }

    @Override
    public String toString() {
        return "Compareitem{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", date='" + date + '\'' +
                ", itemArrayList=" + itemArrayList +
                '}';
    }
}
