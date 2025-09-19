package org.quarkus.assignment.api;

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
import java.util.Optional;
import java.util.stream.Collectors;

@Path(UriConstants.PRODUCTS_BASE)
public class ProductsController {

	@Inject
	ProductService productService;

	/**
	 * Creates new products from the provided array.
	 * @param products array of products to create
	 * @return 201 with summary/items or 409 if all duplicates
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createProducts(List<Product> products) {
		if (products == null || products.isEmpty()) {
			throw new BadRequestException("Body must be a non-empty array of products");
		}
		boolean anyInvalid = products.stream().anyMatch(p -> p == null || p.getId() == null || p.getId() == 0 || p.getName() == null || p.getPrice() == null || p.getQuantity() == null);
		if (anyInvalid) {
			throw new BadRequestException("Each product requires field(s) [id, name, price, quantity]");
		}
		SummaryDto summary = productService.createProducts(products);
		CreateProductsResponse body = CreateProductsResponse.builder()
				.summary(summary)
				.items(products.stream().map(ProductMapper::toResponse).collect(Collectors.toList()))
				.build();
		if (summary.getCreated() == 0) {
			return Response.status(Response.Status.CONFLICT).entity(body).build();
		}
		return Response.status(Response.Status.CREATED).entity(body).build();
	}

	/**
	 * Returns all products or a single product when id is provided.
	 * @param id optional id to fetch a single product
	 * @return 200 with list or single item, 404 if id not found
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProducts(@QueryParam("id") Integer id) {
		if (id == null) {
			List<ProductResponse> all = productService.getAllProducts().stream().map(ProductMapper::toResponse).collect(Collectors.toList());
			return Response.ok(all).build();
		}
		Product p = productService.getRequiredProductById(id);
		return Response.ok(ProductMapper.toResponse(p)).build();
	}

	/**
	 * Applies merge update for the given product id.
	 * @param id target product id in path
	 * @param product product payload with fields to update
	 * @return 200 with summary and updated item
	 */
	@PUT
	@Path(UriConstants.ID)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response upsertProduct(@PathParam("id") int id, Product product) {
		if (product == null) {
			throw new BadRequestException("Body must be a product object");
		}
		if (product.getId() != null && !product.getId().equals(id)) {
			throw new BadRequestException("Body id must match path id");
		}
		product.setId(id);
		SummaryDto summary = productService.patchProducts(java.util.List.of(product));
		UpsertProductResponse body = UpsertProductResponse.builder()
				.summary(summary)
				.item(ProductMapper.toResponse(product))
				.build();
		return Response.ok(body).build();
	}

	/**
	 * Deletes a product by id.
	 * @param id product id to delete
	 * @return 204 on success
	 */
	@DELETE
	@Path(UriConstants.ID)
	public Response delete(@PathParam("id") int id) {
		productService.deleteByIdOrThrow(id);
		return Response.noContent().build();
	}

	/**
	 * Checks availability for requested count and returns current quantity.
	 * @param id product id to check
	 * @param count requested quantity to verify
	 * @return 200 with availability payload
	 */
	@GET
	@Path(UriConstants.ID_AVAILABILITY)
	@Produces(MediaType.APPLICATION_JSON)
	public Response checkAvailability(@PathParam("id") int id, @QueryParam("count") int count) {
		if (count <= 0) {
			throw new BadRequestException("The value of count field must be positive");
		}
		int availableQty = productService.getAvailableQuantity(id);
		boolean available = productService.isAvailable(id, count);
		AvailabilityResponse body = AvailabilityResponse.builder()
				.id(id)
				.requested(count)
				.available(available)
				.availableQuantity(availableQty)
				.build();
		return Response.ok(body).build();
	}

	/**
	 * Lists products sorted by price in requested order.
	 * @param order sort order ASC or DESC
	 * @return 200 with sorted list
	 */
	@GET
	@Path(UriConstants.SORTED_PRICE)
	@Produces(MediaType.APPLICATION_JSON)
	public Response listSortedByPrice(@QueryParam("order") @DefaultValue("ASC") SortOrder order) {
		List<ProductResponse> list = productService.getAllSortedByPrice(order).stream().map(ProductMapper::toResponse).collect(Collectors.toList());
		return Response.ok(list).build();
	}
}
