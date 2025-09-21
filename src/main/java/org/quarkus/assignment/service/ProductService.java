package org.quarkus.assignment.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;
import org.quarkus.assignment.dto.SummaryDto;
import org.quarkus.assignment.model.Product;
import org.quarkus.assignment.model.SortOrder;
import org.quarkus.assignment.util.ProductCsvUtil;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProductService {

	private static final Logger LOG = Logger.getLogger(ProductService.class);

	@ConfigProperty(name = "app.csv.path", defaultValue = "data/products.csv")
	String csvPath;

	/**
	 * Returns all products from the CSV store.
	 * @return list of products
	 */
	public List<Product> getAllProducts() {
		return ProductCsvUtil.readAllProducts(csvPath);
	}

	/**
	 * Finds a product by id if present.
	 * @param id product id
	 * @return optional product
	 */
	public Optional<Product> getProductById(int id) {
		return getAllProducts().stream().filter(p -> p.getId() != null && p.getId() == id).findFirst();
	}

	/**
	 * Returns product by id or throws 404.
	 * @param id product id
	 * @return found product
	 */
	public Product getRequiredProductById(int id) {
		return getProductById(id).orElseThrow(NotFoundException::new);
	}

	/**
	 * Creates only new products by id.
	 * @param products products to create
	 * @return creation summary
	 */
	public SummaryDto createProducts(List<Product> products) {
		List<Product> existing = ProductCsvUtil.readAllProducts(csvPath);
		Set<Integer> existingIds = new HashSet<>();
		for (Product p : existing) existingIds.add(p.getId());
		int created = 0;
		int duplicates = 0;
		for (Product p : products) {
			if (existingIds.contains(p.getId())) duplicates++; else created++;
		}
		List<Product> onlyNew = products.stream().filter(p -> !existingIds.contains(p.getId())).collect(Collectors.toList());
		ProductCsvUtil.createOnlyProducts(csvPath, onlyNew);
		return SummaryDto.builder().created(created).duplicates(duplicates).updated(0).total(products.size()).build();
	}

	/**
	 * Overwrites existing and creates missing products.
	 * @param products products to upsert
	 * @return update summary
	 */
	public SummaryDto updateProducts(List<Product> products) {
		List<Product> existing = ProductCsvUtil.readAllProducts(csvPath);
		Set<Integer> existingIds = existing.stream().map(Product::getId).collect(Collectors.toSet());
		int updated = 0;
		int created = 0;
		for (Product p : products) {
			if (existingIds.contains(p.getId())) updated++; else created++;
		}
		ProductCsvUtil.upsertProducts(csvPath, products);
		return SummaryDto.builder().created(created).updated(updated).duplicates(0).total(products.size()).build();
	}

	/**
	 * Applies partial updates to products by id.
	 * @param products products with fields to patch
	 * @return patch summary
	 */
	public SummaryDto patchProducts(List<Product> products) {
		List<Product> existing = ProductCsvUtil.readAllProducts(csvPath);
		Set<Integer> existingIds = existing.stream().map(Product::getId).collect(Collectors.toSet());
		int updated = 0;
		int created = 0;
		for (Product p : products) {
			if (existingIds.contains(p.getId())) updated++; else created++;
		}
		ProductCsvUtil.patchProducts(csvPath, products);
		return SummaryDto.builder().created(created).updated(updated).duplicates(0).total(products.size()).build();
	}

	/**
	 * Deletes a product by id or throws 404.
	 * @param id product id
	 */
	public void deleteByIdOrThrow(int id) {
		boolean deleted = ProductCsvUtil.deleteById(csvPath, id);
		if (!deleted) throw new NotFoundException("Requested id not found for deletion");
	}

	/**
	 * Returns true when requested count is available.
	 * @param id product id
	 * @param count requested quantity
	 * @return availability flag
	 */
	public boolean isAvailable(int id, int count) {
		Product p = getRequiredProductById(id);
		return p.getQuantity() != null && p.getQuantity() >= count;
	}

	/**
	 * Returns current available quantity for product.
	 * @param id product id
	 * @return available quantity or 0
	 */
	public int getAvailableQuantity(int id) {
		Product p = getRequiredProductById(id);
		return p.getQuantity() != null ? p.getQuantity() : 0;
	}

	/**
	 * Returns products sorted by price.
	 * @param order sort order ASC or DESC
	 * @return sorted list
	 */
	public List<Product> getAllSortedByPrice(SortOrder order) {
		Comparator<Product> cmp = Comparator.comparing(Product::getPrice, Comparator.nullsLast(Double::compareTo));
		if (order == SortOrder.DESC) cmp = cmp.reversed();
		return getAllProducts().stream().sorted(cmp).collect(Collectors.toList());
	}
}
