package org.quarkus.assignment.util;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import org.jboss.logging.Logger;
import org.quarkus.assignment.model.Product;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProductCsvUtil {
	private static final Logger LOG = Logger.getLogger(ProductCsvUtil.class);

	private static final String[] HEADER = new String[]{
			"id","name","description","price","quantity"
	};

	/**
	 * Reads all products from the CSV file.
	 * @param csvPath path to CSV file
	 * @return list of products
	 */
	public static List<Product> readAllProducts(String csvPath) {
		Path path = Paths.get(csvPath);
		if (!Files.exists(path)) {
			LOG.debugf("CSV not found at %s, returning empty list", path.toAbsolutePath());
			return List.of();
		}
		LOG.debugf("Reading products from CSV %s", path.toAbsolutePath());
		List<Product> products = new ArrayList<>();
		try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
		     CSVReader csvReader = new CSVReader(reader)) {
			String[] row;
			boolean first = true;
			while ((row = csvReader.readNext()) != null) {
				if (first) { // skip header
					first = false;
					continue;
				}
				if (row.length == 0) continue;
				Product p = parseRow(row);
				products.add(p);
			}
			LOG.infof("Loaded %d products from CSV", products.size());
		} catch (IOException | CsvValidationException e) {
			LOG.errorf(e, "Failed to read CSV: %s", csvPath);
			throw new RuntimeException("Failed to read CSV: " + csvPath, e);
		}
		return products;
	}

	/**
	 * Upserts products by overwriting or creating records.
	 * @param csvPath path to CSV file
	 * @param incoming products to upsert
	 */
	public static void upsertProducts(String csvPath, List<Product> incoming) {
		mergeAndWrite(csvPath, incoming, false);
	}

	/**
	 * Applies partial updates to products by id.
	 * @param csvPath path to CSV file
	 * @param partialProducts products with fields to patch
	 */
	public static void patchProducts(String csvPath, List<Product> partialProducts) {
		mergeAndWrite(csvPath, partialProducts, true);
	}

	/**
	 * Creates only new products without overwriting existing.
	 * @param csvPath path to CSV file
	 * @param toCreate products to create
	 */
	public static void createOnlyProducts(String csvPath, List<Product> toCreate) {
		ensureParentDirectory(csvPath);
		LOG.debug("Loading existing products before create-only");
		Map<Integer, Product> idToProduct = new LinkedHashMap<>();
		for (Product existing : readAllProducts(csvPath)) {
			idToProduct.put(existing.getId(), existing);
		}
		int created = 0;
		for (Product p : toCreate) {
			if (p == null || p.getId() == null) continue;
			if (!idToProduct.containsKey(p.getId())) {
				idToProduct.put(p.getId(), p);
				created++;
			}
		}
		LOG.infof("Writing %d products to CSV after create-only (created=%d)", idToProduct.size(), created);
		writeAll(csvPath, new ArrayList<>(idToProduct.values()));
	}

	/**
	 * Deletes a product by id and persists changes.
	 * @param csvPath path to CSV file
	 * @param id product id to delete
	 * @return true when a record was removed
	 */
	public static boolean deleteById(String csvPath, int id) {
		List<Product> all = readAllProducts(csvPath);
		int before = all.size();
		List<Product> kept = new ArrayList<>();
		for (Product p : all) {
			if (p.getId() == null || p.getId() != id) {
				kept.add(p);
			}
		}
		if (kept.size() == before) {
			LOG.debugf("No product found to delete with id=%d", id);
			return false;
		}
		LOG.infof("Deleting product with id=%d (remaining=%d)", id, kept.size());
		writeAll(csvPath, kept);
		return true;
	}

	/**
	 * Merges incoming products and writes the CSV.
	 * @param csvPath path to CSV file
	 * @param incoming incoming products
	 * @param isPatch true for patch semantics
	 */
	private static void mergeAndWrite(String csvPath, List<Product> incoming, boolean isPatch) {
		ensureParentDirectory(csvPath);
		LOG.debug("Loading existing products before merge");
		Map<Integer, Product> idToProduct = new LinkedHashMap<>();
		for (Product existing : readAllProducts(csvPath)) {
			idToProduct.put(existing.getId(), existing);
		}
		for (Product p : incoming) {
			if (p == null || p.getId() == null) continue;
			Product current = idToProduct.get(p.getId());
			if (current == null) {
				idToProduct.put(p.getId(), p);
			} else if (isPatch) {
				idToProduct.put(p.getId(), applyPatch(current, p));
			} else {
				idToProduct.put(p.getId(), p);
			}
		}
		LOG.infof("Writing %d products to CSV after %s", idToProduct.size(), isPatch ? "patch" : "upsert");
		writeAll(csvPath, new ArrayList<>(idToProduct.values()));
	}

	/**
	 * Applies non-null fields from patch onto base product.
	 * @param base existing product
	 * @param patch product with fields to copy
	 * @return merged product
	 */
	private static Product applyPatch(Product base, Product patch) {
		if (patch.getName() != null) base.setName(patch.getName());
		if (patch.getDescription() != null) base.setDescription(patch.getDescription());
		if (patch.getPrice() != null) base.setPrice(patch.getPrice());
		if (patch.getQuantity() != null) base.setQuantity(patch.getQuantity());
		return base;
	}

	/**
	 * Writes all products to CSV with header.
	 * @param csvPath path to CSV file
	 * @param products products to write
	 */
	private static void writeAll(String csvPath, List<Product> products) {
		Path path = Paths.get(csvPath);
		try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
		     CSVWriter csvWriter = new CSVWriter(writer)) {
			csvWriter.writeNext(HEADER, false);
			for (Product p : products) {
				String[] row = new String[]{
					String.valueOf(nullSafeInt(p.getId())),
					defaultString(p.getName()),
					defaultString(p.getDescription()),
					String.valueOf(nullSafeDouble(p.getPrice())),
					String.valueOf(nullSafeInt(p.getQuantity()))
				};
				csvWriter.writeNext(row, false);
			}
			LOG.debugf("Finished writing CSV to %s", path.toAbsolutePath());
		} catch (IOException e) {
			LOG.errorf(e, "Failed to write CSV: %s", csvPath);
			throw new RuntimeException("Failed to write CSV: " + csvPath, e);
		}
	}

	/**
	 * Parses a CSV row into a product.
	 * @param row CSV row array
	 * @return product instance
	 */
	private static Product parseRow(String[] row) {
		Product p = new Product();
		p.setId(parseInt(row, 0));
		p.setName(get(row, 1));
		p.setDescription(get(row, 2));
		p.setPrice(parseDouble(row, 3));
		p.setQuantity(parseInt(row, 4));
		return p;
	}

	/**
	 * Safely gets value from a row index.
	 * @param row CSV row array
	 * @param idx index to read
	 * @return value or empty string
	 */
	private static String get(String[] row, int idx) {
		return idx < row.length ? row[idx] : "";
	}

	/**
	 * Parses integer from row value.
	 * @param row CSV row array
	 * @param idx index to parse
	 * @return integer or null
	 */
	private static Integer parseInt(String[] row, int idx) {
		try {
			String v = get(row, idx);
			if (v == null || v.isBlank()) return null;
			return Integer.parseInt(v.trim());
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Parses double from row value.
	 * @param row CSV row array
	 * @param idx index to parse
	 * @return double or null
	 */
	private static Double parseDouble(String[] row, int idx) {
		try {
			String v = get(row, idx);
			if (v == null || v.isBlank()) return null;
			return Double.parseDouble(v.trim());
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Returns empty string for null values.
	 * @param s input string
	 * @return non-null string
	 */
	private static String defaultString(String s) {
		return s == null ? "" : s;
	}

	/**
	 * Converts null Integer to 0.
	 * @param v Integer value
	 * @return primitive int
	 */
	private static int nullSafeInt(Integer v) { return v == null ? 0 : v; }

	/**
	 * Converts null Double to 0.0.
	 * @param v Double value
	 * @return primitive double
	 */
	private static double nullSafeDouble(Double v) { return v == null ? 0.0d : v; }

	/**
	 * Ensures the parent directory exists.
	 * @param csvPath path to CSV file
	 */
	private static void ensureParentDirectory(String csvPath) {
		Path path = Paths.get(csvPath).toAbsolutePath();
		Path parent = path.getParent();
		if (parent != null) {
			try {
				Files.createDirectories(parent);
			} catch (IOException e) {
				LOG.errorf(e, "Failed to create directory for CSV: %s", parent);
				throw new RuntimeException("Failed to create directory for CSV: " + parent, e);
			}
		}
	}
}
