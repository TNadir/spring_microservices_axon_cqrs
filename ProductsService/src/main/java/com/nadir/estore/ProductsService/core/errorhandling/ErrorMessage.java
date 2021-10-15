package com.nadir.estore.ProductsService.core.errorhandling;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.Date;


@Data
@AllArgsConstructor
public class ErrorMessage {
    private Date timestamp;
    private String message;
    private int status;
}
