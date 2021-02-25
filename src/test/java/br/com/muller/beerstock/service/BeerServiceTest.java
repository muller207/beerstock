package br.com.muller.beerstock.service;

import br.com.muller.beerstock.builder.BeerDTOBuilder;
import br.com.muller.beerstock.dto.BeerDTO;
import br.com.muller.beerstock.entity.Beer;
import br.com.muller.beerstock.exception.BeerAlreadyRegisteredException;
import br.com.muller.beerstock.exception.BeerNotFoundException;
import br.com.muller.beerstock.exception.BeerStockExceededException;
import br.com.muller.beerstock.mapper.BeerMapper;
import br.com.muller.beerstock.repository.BeerRepository;
import br.com.muller.beerstock.service.BeerService;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BeerServiceTest {

    @InjectMocks
    private BeerService beerService;

    @Mock
    private BeerRepository beerRepository;

    private BeerMapper beerMapper = BeerMapper.INSTANCE;
    private static final Long INVALID_BEER_ID = 2l;

    //createBeer
    @Test
    void whenBeerIsInformedThenItShouldBeCreated() throws BeerAlreadyRegisteredException {
        //given
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedSavedBeer = beerMapper.toModel(beerDTO);

        //when
        when(beerRepository.findByName(beerDTO.getName())).thenReturn(Optional.empty());
        when(beerRepository.save(expectedSavedBeer)).thenReturn(expectedSavedBeer);

        //then
        BeerDTO createdBeerDTO = beerService.createBeer(beerDTO);
        assertThat(beerDTO.getId(), is(createdBeerDTO.getId()));
        assertThat(beerDTO.getName(), is(createdBeerDTO.getName()));
        assertThat(beerDTO.getQuantity(), is(createdBeerDTO.getQuantity()));
    }

    @Test
    void whenAlreadyRegisteredBeerIsInformedThenExceptionShouldBeThrown() {
        //given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer duplicatedBeer = beerMapper.toModel(expectedBeerDTO);

        //when
        when(beerRepository.findByName(expectedBeerDTO.getName())).thenReturn(Optional.of(duplicatedBeer));

        //then
        assertThrows(BeerAlreadyRegisteredException.class,() -> beerService.createBeer(expectedBeerDTO));
    }

    //findByName
    @Test
    void whenValidBeerNameIsInformedThenABeerShouldBeReturned() throws BeerNotFoundException {
        //given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        //when
        when(beerRepository.findByName(expectedBeerDTO.getName())).thenReturn(Optional.of(expectedBeer));

        //then
        BeerDTO foundBeerDTO = beerService.findByName(expectedBeerDTO.getName());
        assertThat(expectedBeerDTO, is(equalTo(foundBeerDTO)));
    }

    @Test
    void whenNotRegisteredBeerNameIsInformedThenExceptionShouldBeThrown() {
        //given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        //when
        when(beerRepository.findByName(expectedBeerDTO.getName())).thenReturn(Optional.empty());

        //then
        assertThrows(BeerNotFoundException.class, () -> beerService.findByName(expectedBeerDTO.getName()));
    }

    //listAll
    @Test
    void whenListBeerIsCalledThenReturnAllBeersRegistered(){
        //given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        when(beerRepository.findAll()).thenReturn(List.of(expectedBeer));

        List<BeerDTO> foundbeers = beerService.listAll();
        assertThat(foundbeers, is(not(empty())));
        assertThat(foundbeers.get(0),is(equalTo(expectedBeerDTO)));
    }

    @Test
    void whenListBeerIsCalledThenReturnAndEmptyList(){
        //given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        when(beerRepository.findAll()).thenReturn(Collections.emptyList());

        List<BeerDTO> foundbeers = beerService.listAll();
        assertThat(foundbeers, is(empty()));
    }

    //deleteById
    @Test
    void whenDeleteABeerWithValidIdThenABeerShouldBeDeleted() throws BeerNotFoundException {
        //given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
        doNothing().when(beerRepository).deleteById(expectedBeerDTO.getId());

        beerService.deleteById(expectedBeerDTO.getId());

        verify(beerRepository, times(1)).findById(expectedBeerDTO.getId());
        verify(beerRepository, times(1)).deleteById(expectedBeerDTO.getId());
    }

    @Test
    void whenDeleteABeerWithInvalidIdThenThenExceptionShouldBeThrown() {
        //given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.empty());

        assertThrows(BeerNotFoundException.class,() -> beerService.deleteById(expectedBeerDTO.getId()));
    }

    //increment
    @Test
    void whenIncrementIsCalledThenIncrementBeerStock() throws BeerNotFoundException, BeerStockExceededException {
        //given
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(beerDTO);

        //when
        when(beerRepository.findById(beerDTO.getId())).thenReturn(Optional.of(expectedBeer));
        when(beerRepository.save(expectedBeer)).thenReturn(expectedBeer);

        //then
        int quantityToIncrement = 40;
        int quantityAfterIncrement = beerDTO.getQuantity() + quantityToIncrement;
        BeerDTO incrementedBeerDTO = beerService.increment(beerDTO.getId(), quantityToIncrement);
        assertThat(quantityAfterIncrement, is(equalTo(incrementedBeerDTO.getQuantity())));
        assertThat(quantityAfterIncrement, is(lessThanOrEqualTo(beerDTO.getMax())));
    }

    @Test
    void whenIncrementGreaterThenAllowedIsCalledThenAnExceptionShouldBeThrown() throws BeerNotFoundException, BeerStockExceededException {
        //given
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(beerDTO);

        //when
        when(beerRepository.findById(beerDTO.getId())).thenReturn(Optional.of(expectedBeer));

        //then
        int quantityToIncrement = 80;
        assertThrows(BeerStockExceededException.class,() -> beerService.increment(beerDTO.getId(),quantityToIncrement));

    }

    @Test
    void whenIncrementIsCalledWithInvalidIdThenAnExceptionShouldBeThrown(){
        int quantityToIncrement = 10;

        when(beerRepository.findById(INVALID_BEER_ID)).thenReturn(Optional.empty());

        assertThrows(BeerNotFoundException.class,() -> beerService.increment(INVALID_BEER_ID,quantityToIncrement));
    }

    }
