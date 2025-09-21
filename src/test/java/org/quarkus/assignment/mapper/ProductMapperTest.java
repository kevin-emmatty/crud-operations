package org.quarkus.assignment.mapper;

import org.junit.jupiter.api.Test;
import org.quarkus.assignment.dto.ProductResponse;
import org.quarkus.assignment.dto.SummaryDto;
import org.quarkus.assignment.model.Product;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ProductMapperTest {

    @Test
    void toResponse_maps_all_fields() {
        Product p = new Product();
        p.setId(42);
        p.setName("N");
        p.setDescription("D");
        p.setPrice(9.99);
        p.setQuantity(7);

        ProductResponse r = ProductMapper.toResponse(p);

        assertEquals(42, r.getId());
        assertEquals("N", r.getName());
        assertEquals("D", r.getDescription());
        assertEquals(9.99, r.getPrice());
        assertEquals(7, r.getQuantity());
    }

    @Test
    void toSummary_uses_map_with_defaults() {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("created", 2);
        counts.put("duplicates", 1);
        // omit updated and total to exercise defaults

        SummaryDto s = ProductMapper.toSummary(counts, 5);
        assertEquals(2, s.getCreated());
        assertEquals(0, s.getUpdated());
        assertEquals(1, s.getDuplicates());
        assertEquals(5, s.getTotal());
    }
}


