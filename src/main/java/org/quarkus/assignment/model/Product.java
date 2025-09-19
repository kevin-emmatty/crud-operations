package org.quarkus.assignment.model;

import lombok.Data;

@Data
public class Product {
    private Integer id;
    private String name;
    private String description;
    private Double price;
    private Integer quantity;
}
