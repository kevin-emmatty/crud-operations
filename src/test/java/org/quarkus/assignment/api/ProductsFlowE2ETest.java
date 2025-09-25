package org.quarkus.assignment.api;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheQuery;
import io.quarkus.panache.common.Sort;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.quarkus.assignment.model.Product;
import org.quarkus.assignment.persistence.ProductRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.*;

@QuarkusTest
class ProductsFlowE2ETest {

    @InjectMock
    ProductRepository repository;

    @Test
    void fullCrudFlow_endToEnd_fast() {
        // POST /products → created
        ReactivePanacheQuery<Product> emptyQuery = Mockito.mock(ReactivePanacheQuery.class);
        Mockito.when(repository.find(eq("_id in ?1"), any(Object[].class))).thenReturn(emptyQuery);
        Mockito.when(emptyQuery.list()).thenReturn(Uni.createFrom().item(List.of()));
        Mockito.when(repository.persist(any(List.class))).thenReturn(Uni.createFrom().voidItem());

        List<Map<String, Object>> body = List.of(Map.of(
                "id", 101,
                "name", "ZX",
                "price", 25.5,
                "quantity", 4
        ));

        given().contentType(ContentType.JSON).body(body)
                .when().post("/products")
                .then().statusCode(201)
                .body("summary.created", Matchers.is(1));

        // GET /products?id=101 → 200 with item
        Product stored = new Product(); stored.setId(101L); stored.setName("ZX"); stored.setPrice(25.5); stored.setQuantity(4);
        Mockito.when(repository.findById(101L)).thenReturn(Uni.createFrom().item(stored));

        given().when().get("/products?id=101")
                .then().statusCode(200)
                .body("id", Matchers.is(101))
                .body("name", Matchers.is("ZX"));

        // PUT /products/101 → update name
        ReactivePanacheQuery<Product> foundQuery = Mockito.mock(ReactivePanacheQuery.class);
        Mockito.when(repository.find(eq("_id in ?1"), any(Object[].class))).thenReturn(foundQuery);
        Mockito.when(foundQuery.list()).thenReturn(Uni.createFrom().item(List.of(stored)));
        Mockito.when(repository.persistOrUpdate(any(List.class))).thenReturn(Uni.createFrom().voidItem());

        Map<String, Object> update = Map.of(
                "id", 101,
                "name", "ZX-NEW",
                "price", 25.5,
                "quantity", 4
        );

        given().contentType(ContentType.JSON).body(update)
                .when().put("/products/101")
                .then().statusCode(200)
                .body("summary.updated", Matchers.greaterThanOrEqualTo(1));

        // GET availability
        stored.setQuantity(10);
        Mockito.when(repository.findById(101L)).thenReturn(Uni.createFrom().item(stored));

        given().when().get("/products/101/availability?count=3")
                .then().statusCode(200)
                .body("available", Matchers.is(true))
                .body("availableQuantity", Matchers.is(10));

        // GET sorted price endpoints
        ReactivePanacheQuery<Product> sortQuery = Mockito.mock(ReactivePanacheQuery.class);
        Mockito.when(repository.findAll(any(Sort.class))).thenReturn(sortQuery);
        Mockito.when(sortQuery.list()).thenReturn(Uni.createFrom().item(List.of(stored)));

        given().when().get("/products/sorted/price?order=ASC")
                .then().statusCode(200)
                .body("size()", Matchers.greaterThanOrEqualTo(1));

        // DELETE /products/101
        Mockito.when(repository.deleteById(101L)).thenReturn(Uni.createFrom().item(true));

        given().when().delete("/products/101")
                .then().statusCode(204);
    }
}


