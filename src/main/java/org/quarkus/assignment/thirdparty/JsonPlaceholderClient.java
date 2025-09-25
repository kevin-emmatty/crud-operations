package org.quarkus.assignment.thirdparty;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@RegisterRestClient(configKey = "org.quarkus.assignment.thirdparty.JsonPlaceholderClient")
@Path("/users")
public interface JsonPlaceholderClient {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Uni<List<UserDto>> getUsers();
}


