package org.quarkus.assignment.service;

import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quarkus.assignment.util.ProductCsvUtil;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ProductServiceNegativeTest {

    private static class ProductServiceWithPath extends ProductService {
        void setPath(String path) {
            try {
                Field f = ProductService.class.getDeclaredField("csvPath");
                f.setAccessible(true);
                f.set(this, path);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static final String TEST_CSV = "target/unit-csv/service-negative.csv";
    private ProductServiceWithPath service;

    @BeforeEach
    void setup() {
        service = new ProductServiceWithPath();
        service.setPath(TEST_CSV);
        // ensure empty CSV store
        ProductCsvUtil.createOnlyProducts(TEST_CSV, List.of());
    }

    @Test
    void getRequiredProductById_throws_not_found() {
        assertThrows(NotFoundException.class, () -> service.getAvailableQuantity(999));
    }

    @Test
    void deleteByIdOrThrow_throws_when_missing() {
        assertThrows(NotFoundException.class, () -> service.deleteByIdOrThrow(123));
    }
}


