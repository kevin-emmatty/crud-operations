package org.quarkus.assignment.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DtoBuildersTest {

    @Test
    void productResponse_builder_and_values() {
        ProductResponse p = ProductResponse.builder()
                .id(1).name("A").description("d").price(2.5).quantity(3).build();
        assertEquals(1, p.getId());
        assertEquals("A", p.getName());
        assertEquals("d", p.getDescription());
        assertEquals(2.5, p.getPrice());
        assertEquals(3, p.getQuantity());
    }

    @Test
    void createProductsResponse_builder_with_singular_items() {
        ProductResponse pr = ProductResponse.builder().id(1).build();
        CreateProductsResponse r = CreateProductsResponse.builder()
                .summary(SummaryDto.builder().created(1).updated(0).duplicates(0).total(1).build())
                .item(pr)
                .build();
        assertEquals(1, r.getSummary().getCreated());
        assertEquals(1, r.getItems().size());
        assertEquals(1, r.getItems().get(0).getId());
    }

    @Test
    void upsertProductResponse_builder() {
        UpsertProductResponse r = UpsertProductResponse.builder()
                .summary(SummaryDto.builder().created(0).updated(1).duplicates(0).total(1).build())
                .item(ProductResponse.builder().id(10).build())
                .build();
        assertEquals(1, r.getSummary().getUpdated());
        assertEquals(10, r.getItem().getId());
    }

    @Test
    void availabilityResponse_builder() {
        AvailabilityResponse a = AvailabilityResponse.builder()
                .id(5).requested(2).available(true).availableQuantity(3).build();
        assertTrue(a.isAvailable());
        assertEquals(3, a.getAvailableQuantity());
    }

    @Test
    void errorResponse_builder() {
        ErrorResponse e = ErrorResponse.builder()
                .status(400).error("Bad Request").message("m").path("/p").timestamp("t").build();
        assertEquals(400, e.getStatus());
        assertEquals("/p", e.getPath());
        assertEquals("t", e.getTimestamp());
    }
}


