package com.nadir.estore.ProductsService.command.rest;

import java.math.BigDecimal;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CreateProductRestModel {

	@NotBlank(message = "Product title is a required field")
	private String title;

	@Min(value = 1, message = "Price can't be lower then 1")
	@NotNull(message = "Quantity can't be null")
	private BigDecimal price;

	@Min(value = 1, message = "Quantity can't be lower then 1")
	@Max(value = 20, message = "Quantity can't be greater then 5")
	@NotNull(message = "Quantity can't be null")
	private Integer quantity;
}
