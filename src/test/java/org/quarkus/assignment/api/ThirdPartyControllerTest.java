package org.quarkus.assignment.api;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.smallrye.mutiny.Uni;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.quarkus.assignment.service.ThirdPartyService;
import org.quarkus.assignment.thirdparty.UserDto;

import java.util.List;

import static io.restassured.RestAssured.given;

@QuarkusTest
class ThirdPartyControllerTest {

    @InjectMock
    ThirdPartyService service;

    @Test
    void listUsers_returnsOk() {
        UserDto u = new UserDto(); u.setId(1); u.setName("Leanne Graham");
        Mockito.when(service.getUsers()).thenReturn(Uni.createFrom().item(List.of(u)));

        given()
            .when().get("/thirdparty/users")
            .then()
            .statusCode(200)
            .body("size()", Matchers.is(1))
            .body("[0].id", Matchers.is(1))
            .body("[0].name", Matchers.is("Leanne Graham"));
    }
}


