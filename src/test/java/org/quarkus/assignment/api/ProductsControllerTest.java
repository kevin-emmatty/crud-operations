package org.quarkus.assignment.api;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class ProductsControllerTest {

    @BeforeEach
    void cleanCsv() throws Exception {
        Path csv = Path.of("target/test-products.csv");
        Files.deleteIfExists(csv);
    }

    @Test
    void create_and_list_products() {
        String body = "[" +
                "{\"id\":1,\"name\":\"A\",\"description\":\"d\",\"price\":10.5,\"quantity\":5}," +
                "{\"id\":2,\"name\":\"B\",\"description\":\"d\",\"price\":3.2,\"quantity\":8}" +
                "]";

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/products")
                .then()
                .statusCode(anyOf(is(201), is(409)))
                .body("summary.total", is(2));

        given()
                .when()
                .get("/products")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(2));
    }

    @Test
    void get_by_id_not_found_then_create_and_get() {
        given()
                .queryParam("id", 999)
                .when()
                .get("/products")
                .then()
                .statusCode(404);

        String create = "[{\"id\":5,\"name\":\"X\",\"description\":\"d\",\"price\":1.0,\"quantity\":1}]";
        given().contentType(ContentType.JSON).body(create).when().post("/products").then().statusCode(anyOf(is(201), is(409)));

        given()
                .queryParam("id", 5)
                .when()
                .get("/products")
                .then()
                .statusCode(200)
                .body("id", is(5));
    }

    @Test
    void put_patch_semantics_updates_fields() {
        String create = "[{\"id\":10,\"name\":\"P\",\"description\":\"old\",\"price\":5.0,\"quantity\":2}]";
        given().contentType(ContentType.JSON).body(create).when().post("/products").then().statusCode(anyOf(is(201), is(409)));

        String patch = "{\"description\":\"new\"}";
        given()
                .contentType(ContentType.JSON)
                .body(patch)
                .when()
                .put("/products/10")
                .then()
                .statusCode(200)
                .body("item.description", is("new"));
    }

    @Test
    void delete_and_availability() {
        String create = "[{\"id\":20,\"name\":\"C\",\"description\":\"d\",\"price\":7.0,\"quantity\":4}]";
        given().contentType(ContentType.JSON).body(create).when().post("/products").then().statusCode(anyOf(is(201), is(409)));

        given().when().get("/products/20/availability?count=3").then().statusCode(200).body("available", is(true));

        given().when().delete("/products/20").then().statusCode(204);

        given().when().get("/products/20/availability?count=1").then().statusCode(404);
    }

    @Test
    void sorted_by_price_order_param() {
        String create = "[" +
                "{\"id\":31,\"name\":\"L\",\"description\":\"d\",\"price\":2.0,\"quantity\":1}," +
                "{\"id\":32,\"name\":\"H\",\"description\":\"d\",\"price\":9.0,\"quantity\":1}" +
                "]";
        given().contentType(ContentType.JSON).body(create).when().post("/products").then().statusCode(anyOf(is(201), is(409)));

        given().when().get("/products/sorted/price?order=ASC").then().statusCode(200).body("size()", greaterThanOrEqualTo(2));
        given().when().get("/products/sorted/price?order=DESC").then().statusCode(200).body("size()", greaterThanOrEqualTo(2));
    }
}
