package org.quarkus.assignment.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UpsertProductResponse {
	SummaryDto summary;
	ProductResponse item;
}
