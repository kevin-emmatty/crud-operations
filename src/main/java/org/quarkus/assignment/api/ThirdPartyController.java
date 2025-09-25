package org.quarkus.assignment.api;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.quarkus.assignment.service.ThirdPartyService;
import org.quarkus.assignment.thirdparty.UserDto;

import java.util.List;

/**
 * Reactive endpoint for fetching users from a third-party API.
 */
@Path("/thirdparty/users")
@Produces(MediaType.APPLICATION_JSON)
public class ThirdPartyController {

    @Inject
    ThirdPartyService service;

    /**
     * Retrieves the list of users from JSONPlaceholder.
     * @return Uni emitting the users list
     */
    @GET
    public Uni<List<UserDto>> list() {
        return service.getUsers();
    }
}


