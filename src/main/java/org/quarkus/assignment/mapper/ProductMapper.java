package org.quarkus.assignment.mapper;

import org.quarkus.assignment.dto.ProductResponse;
import org.quarkus.assignment.dto.SummaryDto;
import org.quarkus.assignment.model.Product;

public class ProductMapper {
	public static ProductResponse toResponse(Product p) {
		return ProductResponse.builder()
				.id(p.getId())
				.name(p.getName())
				.description(p.getDescription())
				.price(p.getPrice())
				.quantity(p.getQuantity())
				.build();
	}

	public static SummaryDto toSummary(java.util.Map<String, Integer> counts, int defaultTotal) {
		return SummaryDto.builder()
				.created(counts.getOrDefault("created", 0))
				.updated(counts.getOrDefault("updated", 0))
				.duplicates(counts.getOrDefault("duplicates", 0))
				.total(counts.getOrDefault("total", defaultTotal))
				.build();
	}
}
