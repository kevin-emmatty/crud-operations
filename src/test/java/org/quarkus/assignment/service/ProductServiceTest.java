package org.quarkus.assignment.service;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheQuery;
import io.quarkus.panache.common.Sort;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.quarkus.assignment.dto.SummaryDto;
import org.quarkus.assignment.model.Product;
import org.quarkus.assignment.model.SortOrder;
import org.quarkus.assignment.persistence.ProductRepository;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@QuarkusTest
class ProductServiceTest {

    @Inject
    ProductService service;

    @InjectMock
    ProductRepository repository;

    @Test
    void createProducts_countsCreatedAndDuplicates() {
        Product existing = new Product(); existing.setId(1L);
        ReactivePanacheQuery<Product> query = Mockito.mock(ReactivePanacheQuery.class);
        Mockito.when(repository.find(eq("_id in ?1"), any(Object[].class))).thenReturn(query);
        Mockito.when(query.list()).thenReturn(Uni.createFrom().item(List.of(existing)));
        Mockito.when(repository.persist(any(List.class))).thenReturn(Uni.createFrom().voidItem());

        Product p1 = new Product(); p1.setId(1L);
        Product p2 = new Product(); p2.setId(2L);
        SummaryDto summary = service.createProducts(List.of(p1, p2)).await().indefinitely();
        assertEquals(1, summary.getCreated());
        assertEquals(1, summary.getDuplicates());
        assertEquals(0, summary.getUpdated());
        assertEquals(2, summary.getTotal());
    }

    @Test
    void updateProducts_countsCreatedAndUpdated() {
        Product existing = new Product(); existing.setId(1L);
        ReactivePanacheQuery<Product> query = Mockito.mock(ReactivePanacheQuery.class);
        Mockito.when(repository.find(eq("_id in ?1"), any(Object[].class))).thenReturn(query);
        Mockito.when(query.list()).thenReturn(Uni.createFrom().item(List.of(existing)));
        Mockito.when(repository.persistOrUpdate(any(List.class))).thenReturn(Uni.createFrom().voidItem());

        Product p1 = new Product(); p1.setId(1L);
        Product p2 = new Product(); p2.setId(2L);
        SummaryDto summary = service.updateProducts(List.of(p1, p2)).await().indefinitely();
        assertEquals(1, summary.getCreated());
        assertEquals(1, summary.getUpdated());
        assertEquals(0, summary.getDuplicates());
        assertEquals(2, summary.getTotal());
    }

    @Test
    void patchProducts_mergesAndCreates() {
        Product existing = new Product(); existing.setId(1L); existing.setName("Old");
        ReactivePanacheQuery<Product> query = Mockito.mock(ReactivePanacheQuery.class);
        Mockito.when(repository.find(eq("_id in ?1"), any(Object[].class))).thenReturn(query);
        Mockito.when(query.list()).thenReturn(Uni.createFrom().item(List.of(existing)));
        Mockito.when(repository.persistOrUpdate(any(List.class))).thenReturn(Uni.createFrom().voidItem());

        Product patch1 = new Product(); patch1.setId(1L); patch1.setName("New");
        Product patch2 = new Product(); patch2.setId(3L); patch2.setName("X");
        SummaryDto summary = service.patchProducts(List.of(patch1, patch2)).await().indefinitely();
        assertEquals(1, summary.getCreated());
        assertEquals(1, summary.getUpdated());
    }

    @Test
    void deleteById_succeedsOrThrows() {
        Mockito.when(repository.deleteById(1L)).thenReturn(Uni.createFrom().item(true));
        Mockito.when(repository.deleteById(2L)).thenReturn(Uni.createFrom().item(false));

        assertDoesNotThrow(() -> service.deleteByIdOrThrow(1L).await().indefinitely());
        assertThrows(NotFoundException.class, () -> service.deleteByIdOrThrow(2L).await().indefinitely());
    }

    @Test
    void availabilityChecks() {
        Product p = new Product(); p.setId(5L); p.setQuantity(10);
        Mockito.when(repository.findById(5L)).thenReturn(Uni.createFrom().item(p));

        assertTrue(service.isAvailable(5L, 3).await().indefinitely());
        assertEquals(10, service.getAvailableQuantity(5L).await().indefinitely());
    }

    @Test
    void sortedByPrice_usesRepositorySort() {
        ReactivePanacheQuery<Product> query = Mockito.mock(ReactivePanacheQuery.class);
        Mockito.when(repository.findAll(any(Sort.class))).thenReturn(query);
        Mockito.when(query.list()).thenReturn(Uni.createFrom().item(List.of()));
        assertNotNull(service.getAllSortedByPrice(SortOrder.ASC).await().indefinitely());
    }
}


