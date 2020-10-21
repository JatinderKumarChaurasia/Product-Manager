package com.jkc.microservices.api.core.product;

public class Product {
    private final int productID;
    private final String name;
    private final double weight;
    private final String serviceAddress;

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

    public String getName() {
        return name;
    }

    public double getWeight() {
        return weight;
    }

    public String getServiceAddress() {
        return serviceAddress;
    }
}
