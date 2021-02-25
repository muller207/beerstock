package br.com.muller.beerstock.controller;

import br.com.muller.beerstock.dto.BeerDTO;
import br.com.muller.beerstock.exception.BeerAlreadyRegisteredException;
import br.com.muller.beerstock.exception.BeerNotFoundException;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface BeerControllerDocs {

    BeerDTO createBeer(BeerDTO beerDTO) throws BeerAlreadyRegisteredException;
    BeerDTO findByName(@PathVariable String name) throws BeerNotFoundException;
    List<BeerDTO> listBeers();
    void deleteById(@PathVariable Long id) throws BeerNotFoundException;
}
