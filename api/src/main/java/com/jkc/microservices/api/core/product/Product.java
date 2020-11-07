package com.jkc.microservices.api.core.product;

public class Product {
    private int productID;
    private String name;
    private double weight;
    private String serviceAddress;

    public Product() {
        this.productID = 0;
        this.name = null;
        this.weight = 0;
        this.serviceAddress = null;
    }

    public Product(int productID, String name, double weight, String serviceAddress) {
        this.productID = productID;
        this.name = name;
        this.weight = weight;
        this.serviceAddress = serviceAddress;
    }

    public int getProductID() {
        return productID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }
}
