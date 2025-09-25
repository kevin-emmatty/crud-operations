package org.quarkus.assignment.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AvailabilityResponse {
	long id;
	int requested;
	boolean available;
	int availableQuantity;
}
