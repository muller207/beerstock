package br.com.muller.beerstock.controller;

import br.com.muller.beerstock.dto.BeerDTO;
import br.com.muller.beerstock.dto.QuantityDTO;
import br.com.muller.beerstock.exception.*;
import br.com.muller.beerstock.service.BeerService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/beers")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class BeerController implements BeerControllerDocs{

    private final BeerService beerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BeerDTO createBeer(@RequestBody @Valid BeerDTO beerDTO) throws BeerAlreadyRegisteredException {
        return beerService.createBeer(beerDTO);
    }

    @GetMapping("/{name}")
    public BeerDTO findByName(@PathVariable String name) throws BeerNotFoundException {
        return beerService.findByName(name);
    }


    @GetMapping
    public List<BeerDTO> listBeers() {
        return beerService.listAll();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long id) throws BeerNotFoundException {
        beerService.deleteById(id);
    }

    @PatchMapping("/{id}/increment")
    public BeerDTO increment(@PathVariable Long id,@RequestBody @Valid QuantityDTO quantityToIncrement) throws BeerNotFoundException, BeerStockExceededException, QuantityLessThanZeroException {
        return beerService.increment(id, quantityToIncrement.getQuantity());
    }

    @PatchMapping("/{id}/decrement")
    public BeerDTO decrement(@PathVariable Long id,@RequestBody @Valid QuantityDTO quantityToDecrement) throws BeerNotFoundException, BeerStockLessThenZeroException, QuantityLessThanZeroException  {
        return beerService.decrement(id, quantityToDecrement.getQuantity());
    }


}
