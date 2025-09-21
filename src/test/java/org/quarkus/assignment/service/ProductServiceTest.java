package org.quarkus.assignment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quarkus.assignment.model.Product;
import org.quarkus.assignment.model.SortOrder;
import org.quarkus.assignment.util.ProductCsvUtil;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ProductServiceTest {

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

    private static final String TEST_CSV = "target/unit-csv/service.csv";
    private ProductServiceWithPath service;

    private Product product(int id, double price, int qty) {
        Product p = new Product();
        p.setId(id);
        p.setName("N"+id);
        p.setDescription("D"+id);
        p.setPrice(price);
        p.setQuantity(qty);
        return p;
    }

    @BeforeEach
    void setup() throws Exception {
        service = new ProductServiceWithPath();
        service.setPath(TEST_CSV);
        // ensure clean file
        ProductCsvUtil.createOnlyProducts(TEST_CSV, List.of());
    }

    @Test
    void create_update_patch_delete_and_queries() {
        // create
        var createSummary = service.createProducts(List.of(product(1, 3.0, 5)));
        assertEquals(1, createSummary.getCreated());
        assertEquals(1, service.getAllProducts().size());

        // update (upsert)
        var updateSummary = service.updateProducts(List.of(product(1, 9.0, 2)));
        assertEquals(1, updateSummary.getUpdated());
        assertEquals(9.0, service.getAllProducts().get(0).getPrice());

        // patch
        Product patch = new Product();
        patch.setId(1);
        patch.setQuantity(7);
        var patchSummary = service.patchProducts(List.of(patch));
        assertEquals(1, patchSummary.getUpdated());
        assertEquals(7, service.getAllProducts().get(0).getQuantity());

        // availability
        assertTrue(service.isAvailable(1, 5));
        assertEquals(7, service.getAvailableQuantity(1));

        // sorting
        service.createProducts(List.of(product(2, 1.0, 1)));
        var asc = service.getAllSortedByPrice(SortOrder.ASC);
        var desc = service.getAllSortedByPrice(SortOrder.DESC);
        assertTrue(asc.get(0).getPrice() <= asc.get(1).getPrice());
        assertTrue(desc.get(0).getPrice() >= desc.get(1).getPrice());

        // delete
        service.deleteByIdOrThrow(1);
        assertEquals(1, service.getAllProducts().size());
    }
}


