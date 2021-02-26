package br.com.muller.beerstock.service;

import br.com.muller.beerstock.dto.BeerDTO;
import br.com.muller.beerstock.entity.Beer;
import br.com.muller.beerstock.exception.*;
import br.com.muller.beerstock.mapper.BeerMapper;
import br.com.muller.beerstock.repository.BeerRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class BeerService {
    private final BeerRepository beerRepository;
    private final BeerMapper beerMapper = BeerMapper.INSTANCE;

    public BeerDTO createBeer(BeerDTO beerDTO) throws BeerAlreadyRegisteredException {
        verifyIfIsAlreadyRegistered(beerDTO.getName());
        Beer beer = beerMapper.toModel(beerDTO);
        Beer savedBeer = beerRepository.save(beer);
        return beerMapper.toDTO(beer);
    }

    public BeerDTO findByName(String name) throws BeerNotFoundException {
        Beer foundBeer = beerRepository.findByName(name)
                .orElseThrow(() -> new BeerNotFoundException(name));
        return beerMapper.toDTO(foundBeer);
    }

    public List<BeerDTO> listAll(){
        return beerRepository.findAll()
                .stream()
                .map(beerMapper::toDTO)
                .collect(Collectors.toList());
    }

    public void deleteById(Long id) throws BeerNotFoundException {
        verifyIfExists(id);
        beerRepository.deleteById(id);
    }

    public void verifyIfIsAlreadyRegistered(String name) throws BeerAlreadyRegisteredException {
        Optional<Beer> foundBeer = beerRepository.findByName(name);
        if(foundBeer.isPresent())
            throw new BeerAlreadyRegisteredException(name);
    }

    public Beer verifyIfExists(Long id) throws BeerNotFoundException {
        return beerRepository.findById(id)
                .orElseThrow(() -> new BeerNotFoundException(id));
    }


    public BeerDTO increment(Long id, int quantityToIncrement) throws BeerNotFoundException, BeerStockExceededException, QuantityLessThanZeroException {
        if(quantityToIncrement<0)
            throw new QuantityLessThanZeroException(quantityToIncrement);
        Beer foundBeer = verifyIfExists(id);
        if (foundBeer.getQuantity()+quantityToIncrement > foundBeer.getMax())
            throw new BeerStockExceededException(id, quantityToIncrement);
        foundBeer.setQuantity(foundBeer.getQuantity()+quantityToIncrement);
        Beer savedBeer = beerRepository.save(foundBeer);
        return beerMapper.toDTO(savedBeer);
    }

    public BeerDTO decrement(Long id, int quantityToDecrement) throws BeerNotFoundException, BeerStockLessThenZeroException, QuantityLessThanZeroException {
        if(quantityToDecrement<0)
            throw new QuantityLessThanZeroException(quantityToDecrement);
        Beer foundBeer = verifyIfExists(id);
        if (foundBeer.getQuantity()-quantityToDecrement < 0)
            throw new BeerStockLessThenZeroException(id, quantityToDecrement);
        foundBeer.setQuantity(foundBeer.getQuantity()-quantityToDecrement);
        Beer savedBeer = beerRepository.save(foundBeer);
        return beerMapper.toDTO(savedBeer);
    }
}
