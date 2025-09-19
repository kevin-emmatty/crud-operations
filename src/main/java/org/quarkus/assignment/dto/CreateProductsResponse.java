package org.quarkus.assignment.dto;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class CreateProductsResponse {
	SummaryDto summary;
	@Singular("item")
	List<ProductResponse> items;
}
