package br.com.muller.beerstock.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BeerStockLessThenZeroException extends Exception{

    public BeerStockLessThenZeroException (Long id, int quantityToDecrement){
        super(String.format("Beers with %s ID to increment informed is less then 0 after decrement: %s", id, quantityToDecrement));
    }

}
