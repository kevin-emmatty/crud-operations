package org.quarkus.assignment.api.exception;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalExceptionMapperTest {

    private final GlobalExceptionMapper mapper = new GlobalExceptionMapper();

    @Test
    void maps_not_found_to_404() {
        Response r = mapper.toResponse(new NotFoundException("nf"));
        assertEquals(404, r.getStatus());
    }

    @Test
    void maps_bad_request_to_400() {
        Response r = mapper.toResponse(new BadRequestException("br"));
        assertEquals(400, r.getStatus());
    }

    @Test
    void maps_wae_to_pass_through_status() {
        Response r = mapper.toResponse(new WebApplicationException("x", 418));
        assertEquals(418, r.getStatus());
    }

    @Test
    void maps_other_to_500() {
        Response r = mapper.toResponse(new RuntimeException("boom"));
        assertEquals(500, r.getStatus());
    }
}


