package org.quarkus.assignment.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quarkus.assignment.model.Product;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ProductCsvUtilTest {
    private static final String TEST_CSV = "target/unit-csv/products.csv";

    @BeforeEach
    void clean() throws Exception {
        Files.deleteIfExists(Path.of(TEST_CSV));
    }

    private Product product(int id, String name, String desc, Double price, Integer qty) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setDescription(desc);
        p.setPrice(price);
        p.setQuantity(qty);
        return p;
    }

    @Test
    void readAll_when_missing_returns_empty() {
        List<Product> all = ProductCsvUtil.readAllProducts(TEST_CSV);
        assertNotNull(all);
        assertEquals(0, all.size());
    }

    @Test
    void createOnly_then_read_persists_header_and_rows() {
        ProductCsvUtil.createOnlyProducts(TEST_CSV, List.of(
                product(1, "A", "d", 1.2, 3)
        ));

        List<Product> all = ProductCsvUtil.readAllProducts(TEST_CSV);
        assertEquals(1, all.size());
        Product a = all.get(0);
        assertEquals(1, a.getId());
        assertEquals("A", a.getName());
        assertEquals("d", a.getDescription());
        assertEquals(1.2, a.getPrice());
        assertEquals(3, a.getQuantity());
    }

    @Test
    void upsert_overwrites_existing() {
        ProductCsvUtil.createOnlyProducts(TEST_CSV, List.of(product(2, "B", "d", 2.0, 2)));
        ProductCsvUtil.upsertProducts(TEST_CSV, List.of(product(2, "B2", "d2", 5.0, 9)));

        List<Product> all = ProductCsvUtil.readAllProducts(TEST_CSV);
        assertEquals(1, all.size());
        Product p = all.get(0);
        assertEquals("B2", p.getName());
        assertEquals("d2", p.getDescription());
        assertEquals(5.0, p.getPrice());
        assertEquals(9, p.getQuantity());
    }

    @Test
    void patch_updates_non_null_fields_only() {
        ProductCsvUtil.createOnlyProducts(TEST_CSV, List.of(product(3, "C", "old", 1.0, 1)));
        Product patch = product(3, null, "new", null, 7);
        ProductCsvUtil.patchProducts(TEST_CSV, List.of(patch));

        Product p = ProductCsvUtil.readAllProducts(TEST_CSV).get(0);
        assertEquals("C", p.getName());
        assertEquals("new", p.getDescription());
        assertEquals(1.0, p.getPrice());
        assertEquals(7, p.getQuantity());
    }

    @Test
    void deleteById_removes_record_and_returns_flags() {
        ProductCsvUtil.createOnlyProducts(TEST_CSV, List.of(product(9, "Z", "d", 1.0, 1)));
        assertTrue(ProductCsvUtil.deleteById(TEST_CSV, 9));
        assertFalse(ProductCsvUtil.deleteById(TEST_CSV, 9));
        assertEquals(0, ProductCsvUtil.readAllProducts(TEST_CSV).size());
    }
}


