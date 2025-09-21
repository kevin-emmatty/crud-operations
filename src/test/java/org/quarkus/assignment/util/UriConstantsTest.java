package org.quarkus.assignment.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UriConstantsTest {
    @Test
    void constants_are_non_empty() {
        assertTrue(UriConstants.PRODUCTS_BASE.startsWith("/"));
        assertTrue(UriConstants.ID.contains("{"));
        assertTrue(UriConstants.ID_AVAILABILITY.contains("availability"));
        assertTrue(UriConstants.SORTED_PRICE.contains("price"));
    }
}


