package org.quarkus.assignment.persistence;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.quarkus.assignment.model.Product;

@ApplicationScoped
public class ProductRepository implements ReactivePanacheMongoRepositoryBase<Product, Long> {
}
