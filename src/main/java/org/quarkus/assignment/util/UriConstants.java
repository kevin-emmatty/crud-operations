package org.quarkus.assignment.util;

import lombok.experimental.UtilityClass;

/**
 * URI constants used by REST controllers.
 */
@UtilityClass
public class UriConstants {
    
	/** Base path for product APIs. */
	public static final String PRODUCTS_BASE = "/products";

	/** Path for id parameter segment. */
	public static final String ID = "/{id}";

	/** Path for availability endpoint. */
	public static final String ID_AVAILABILITY = "/{id}/availability";

	/** Path for sorted by price endpoint. */
	public static final String SORTED_PRICE = "/sorted/price";
}
