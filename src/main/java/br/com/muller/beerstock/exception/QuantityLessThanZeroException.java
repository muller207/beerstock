package br.com.muller.beerstock.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class QuantityLessThanZeroException extends Exception {
    public QuantityLessThanZeroException(int quantityToDecrement) {
        super(String.format("Quantity informed is less then 0: %s", quantityToDecrement));
    }
}
