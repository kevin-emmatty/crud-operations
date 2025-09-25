package org.quarkus.assignment.model;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonId;

@MongoEntity(collection = "products")
@Data
public class Product {
    @BsonId
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer quantity;
}
