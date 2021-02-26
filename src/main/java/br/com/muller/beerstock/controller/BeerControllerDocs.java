package br.com.muller.beerstock.controller;

import br.com.muller.beerstock.dto.BeerDTO;
import br.com.muller.beerstock.dto.QuantityDTO;
import br.com.muller.beerstock.entity.Beer;
import br.com.muller.beerstock.exception.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Api("Manages beer stock")
public interface BeerControllerDocs {

    @ApiOperation("Beer creation operation")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Success beer creation"),
            @ApiResponse(code = 400, message = "Missing required fields or wrong field range value.")
    })
    BeerDTO createBeer(BeerDTO beerDTO) throws BeerAlreadyRegisteredException;

    @ApiOperation("Returns a beer found by a given name")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success beer found in the system"),
            @ApiResponse(code = 404, message = "Beer with given name not found.")
    })
    BeerDTO findByName(@PathVariable String name) throws BeerNotFoundException;

    @ApiOperation("List of all beers registered in the system")
    @ApiResponse(code = 200, message = "")
    List<BeerDTO> listBeers();

    @ApiOperation("Delete a beer found by a given valid Id")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Success beer deleted in the system"),
            @ApiResponse(code = 404, message = "Beer with given id not found.")
    })
    void deleteById(@PathVariable Long id) throws BeerNotFoundException;

    @ApiOperation("Increment a beer stock quantity given valid Id and quantity less than max")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success beer incremented in the system"),
            @ApiResponse(code = 400, message = "Quantity to increment is greater than max"),
            @ApiResponse(code = 404, message = "Beer with given id not found"),
    })
    BeerDTO increment(@PathVariable Long id, QuantityDTO quantityToIncrement) throws BeerNotFoundException, BeerStockExceededException, QuantityLessThanZeroException;

    @ApiOperation("Decrement a beer stock quantity given valid Id and quantity greater than 0")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success beer decremented in the system"),
            @ApiResponse(code = 400, message = "Quantity to decrement is less than 0"),
            @ApiResponse(code = 404, message = "Beer with given id not found"),
    })
    BeerDTO decrement(@PathVariable Long id, QuantityDTO quantityToDecrement) throws BeerNotFoundException, BeerStockLessThenZeroException, QuantityLessThanZeroException;
}
