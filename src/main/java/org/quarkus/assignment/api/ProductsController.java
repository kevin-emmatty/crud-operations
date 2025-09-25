package org.quarkus.assignment.api;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.quarkus.assignment.mapper.ProductMapper;
import org.quarkus.assignment.dto.*;
import org.quarkus.assignment.model.Product;
import org.quarkus.assignment.model.SortOrder;
import org.quarkus.assignment.service.ProductService;
import org.quarkus.assignment.util.UriConstants;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST endpoints for managing products.
 */
@Path(UriConstants.PRODUCTS_BASE)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductsController {

    @Inject
    ProductService productService;

    /**
     * Creates new products.
     * @param products list of products to create
     * @return response containing summary and created items
     */
    @POST
    public Uni<Response> createProducts(List<Product> products) {
        if (products == null || products.isEmpty()) {
            throw new BadRequestException("Body must be a non-empty array of products");
        }
        boolean anyInvalid = products.stream().anyMatch(p -> p == null || p.getId() == null || p.getId() == 0 || p.getName() == null || p.getPrice() == null || p.getQuantity() == null);
        if (anyInvalid) {
            throw new BadRequestException("Each product requires field(s) [id, name, price, quantity]");
        }
        return productService.createProducts(products)
            .onItem().transform(summary -> {
                CreateProductsResponse body = CreateProductsResponse.builder()
                        .summary(summary)
                        .items(products.stream().map(ProductMapper::toResponse).collect(Collectors.toList()))
                        .build();
                if (summary.getCreated() == 0) {
                    return Response.status(Response.Status.CONFLICT).entity(body).build();
                }
                return Response.status(Response.Status.CREATED).entity(body).build();
            });
    }

    /**
     * Returns all products or a single product by id.
     * @param id optional product id to fetch
     * @return response with list or single product
     */
    @GET
    public Uni<Response> getProducts(@QueryParam("id") Long id) {
        if (id == null) {
            return productService.getAllProducts()
                .onItem().transform(list -> Response.ok(list.stream().map(ProductMapper::toResponse).collect(Collectors.toList())).build());
        }
        return productService.getProductById(id)
            .onItem().ifNull().failWith(() -> new NotFoundException("Product id " + id + " doesn't exist"))
            .onItem().transform(p -> Response.ok(ProductMapper.toResponse(p)).build());
    }

    /**
     * Upserts a product by id.
     * @param id product id path parameter
     * @param product product payload to upsert
     * @return response with summary and item
     */
    @PUT
    @Path(UriConstants.ID)
    public Uni<Response> upsertProduct(@PathParam("id") long id, Product product) {
        if (product == null) {
            throw new BadRequestException("Body must be a product object");
        }
        if (product.getId() != null && !product.getId().equals(id)) {
            throw new BadRequestException("Body id must match path id");
        }
        product.setId(id);
        return productService.patchProducts(java.util.List.of(product))
            .onItem().transform(summary -> {
                UpsertProductResponse body = UpsertProductResponse.builder()
                        .summary(summary)
                        .item(ProductMapper.toResponse(product))
                        .build();
                return Response.ok(body).build();
            });
    }

    /**
     * Deletes a product by id.
     * @param id product id to delete
     * @return 204 No Content on success
     */
    @DELETE
    @Path(UriConstants.ID)
    public Uni<Response> delete(@PathParam("id") long id) {
        return productService.deleteByIdOrThrow(id).replaceWith(Response.noContent().build());
    }

    /**
     * Checks availability of a product for a requested count.
     * @param id product id to check
     * @param count requested quantity
     * @return response with availability details
     */
    @GET
    @Path(UriConstants.ID_AVAILABILITY)
    public Uni<Response> checkAvailability(@PathParam("id") long id, @QueryParam("count") int count) {
        if (count <= 0) {
            throw new BadRequestException("The value of count field must be positive");
        }
        return productService.getAvailableQuantity(id)
            .onItem().transformToUni(availableQty -> productService.isAvailable(id, count)
                .onItem().transform(available -> Response.ok(AvailabilityResponse.builder()
                        .id(id)
                        .requested(count)
                        .available(available)
                        .availableQuantity(availableQty)
                        .build()).build()));
    }

    /**
     * Lists products sorted by price.
     * @param order sort order (ASC or DESC)
     * @return response with sorted list
     */
    @GET
    @Path(UriConstants.SORTED_PRICE)
    public Uni<Response> listSortedByPrice(@QueryParam("order") @DefaultValue("ASC") SortOrder order) {
        return productService.getAllSortedByPrice(order)
            .onItem().transform(list -> Response.ok(list.stream().map(ProductMapper::toResponse).collect(Collectors.toList())).build());
    }
}
