package org.quarkus.assignment.api;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.quarkus.assignment.dto.SummaryDto;
import org.quarkus.assignment.model.Product;
import org.quarkus.assignment.model.SortOrder;
import org.quarkus.assignment.service.ProductService;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

@QuarkusTest
class ProductsControllerTest {

    @InjectMock
    ProductService service;

    @Test
    void listAll_returnsOk() {
        Product p = new Product(); p.setId(1L); p.setName("A"); p.setPrice(10.0); p.setQuantity(5);
        Mockito.when(service.getAllProducts()).thenReturn(Uni.createFrom().item(List.of(p)));

        given()
            .when().get("/products")
            .then()
            .statusCode(200)
            .body("size()", Matchers.is(1))
            .body("[0].id", Matchers.is(1));
    }

    @Test
    void getById_found_returnsOk() {
        Product p = new Product(); p.setId(2L); p.setName("B");
        Mockito.when(service.getProductById(2L)).thenReturn(Uni.createFrom().item(p));

        given()
            .when().get("/products?id=2")
            .then()
            .statusCode(200)
            .body("id", Matchers.is(2));
    }

    @Test
    void getById_missing_returns404() {
        Mockito.when(service.getProductById(999L)).thenReturn(Uni.createFrom().nullItem());

        given()
            .when().get("/products?id=999")
            .then()
            .statusCode(404);
    }

    @Test
    void delete_returns204() {
        Mockito.when(service.deleteByIdOrThrow(3L)).thenReturn(Uni.createFrom().voidItem());

        given()
            .when().delete("/products/3")
            .then()
            .statusCode(204);
    }

    @Test
    void sortedByPrice_returnsOk() {
        Mockito.when(service.getAllSortedByPrice(SortOrder.DESC)).thenReturn(Uni.createFrom().item(List.of()));

        given()
            .when().get("/products/sorted/price?order=DESC")
            .then()
            .statusCode(200);
    }

    @Test
    void put_upsert_returnsOk() {
        SummaryDto summary = SummaryDto.builder().created(1).updated(0).duplicates(0).total(1).build();
        Mockito.when(service.patchProducts(Mockito.anyList())).thenReturn(Uni.createFrom().item(summary));

        Map<String, Object> product = Map.of(
            "id", 5,
            "name", "X",
            "price", 12.5,
            "quantity", 3
        );

        given()
            .contentType(ContentType.JSON)
            .body(product)
            .when().put("/products/5")
            .then()
            .statusCode(200)
            .body("summary.created", Matchers.is(1));
    }

    @Test
    void post_create_returns201_or409() {
        SummaryDto created = SummaryDto.builder().created(1).updated(0).duplicates(0).total(1).build();
        SummaryDto conflict = SummaryDto.builder().created(0).updated(0).duplicates(1).total(1).build();

        Mockito.when(service.createProducts(Mockito.anyList()))
            .thenReturn(Uni.createFrom().item(created))
            .thenReturn(Uni.createFrom().item(conflict));

        List<Map<String, Object>> body = List.of(Map.of(
            "id", 7,
            "name", "Y",
            "price", 9.9,
            "quantity", 2
        ));

        given().contentType(ContentType.JSON).body(body)
            .when().post("/products")
            .then().statusCode(201);

        given().contentType(ContentType.JSON).body(body)
            .when().post("/products")
            .then().statusCode(409);
    }
}


