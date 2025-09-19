package org.quarkus.assignment.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SummaryDto {
	int created;
	int updated;
	int duplicates;
	int total;
}
