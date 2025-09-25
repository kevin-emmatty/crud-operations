package org.quarkus.assignment.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.quarkus.assignment.thirdparty.UserDto;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * Reactive service for calling the JSONPlaceholder users API.
 */
@ApplicationScoped
public class ThirdPartyService {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Fetches users from the third-party endpoint.
     * @return Uni emitting the list of users
     */
    public Uni<List<UserDto>> getUsers() {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://jsonplaceholder.typicode.com/users"))
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();
        return Uni.createFrom().completionStage(
                CLIENT.sendAsync(req, HttpResponse.BodyHandlers.ofString())
        ).onItem().transform(resp -> {
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                try {
                    return MAPPER.readValue(resp.body(), new TypeReference<>() {});
                } catch (Exception e) {
                    throw new RuntimeException("Failed to parse users response", e);
                }
            }
            throw new RuntimeException("Failed to fetch users. Status: " + resp.statusCode());
        });
    }
}


