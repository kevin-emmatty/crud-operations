package org.quarkus.assignment.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProductResponse {
	Integer id;
	String name;
	String description;
	Double price;
	Integer quantity;
}
