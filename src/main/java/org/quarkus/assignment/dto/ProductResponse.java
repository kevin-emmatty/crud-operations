package org.quarkus.assignment.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProductResponse {
    Long id;
	String name;
	String description;
	Double price;
	Integer quantity;
}
