package br.com.muller.beerstock.controller;

import br.com.muller.beerstock.builder.BeerDTOBuilder;
import br.com.muller.beerstock.builder.QuantityDTOBuilder;
import br.com.muller.beerstock.dto.BeerDTO;
import br.com.muller.beerstock.dto.QuantityDTO;
import br.com.muller.beerstock.exception.*;
import br.com.muller.beerstock.service.BeerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.List;

import static br.com.muller.beerstock.utils.JsonConvertionUtils.asJsonString;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class BeerControllerTest {
    private static final String BEER_API_URL_PATH = "/api/v1/beers";
    private static final long VALID_BEER_ID = 1l;
    private static final long INVALID_BEER_ID = 2l;
    private static final String BEER_API_SUBPATH_INCREMENT_URL = "/increment";
    private static final String BEER_API_SUBPATH_DECREMENT_URL = "/decrement";

    @InjectMocks
    private BeerController beerController;

    @Mock
    private BeerService beerService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(beerController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setViewResolvers((s, locale) -> new MappingJackson2JsonView())
                .build();
    }


    @Test
    void whenPOSTIsCalledThenABeerShouldBeCreated() throws Exception {
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        when(beerService.createBeer(beerDTO)).thenReturn(beerDTO);

        mockMvc.perform(post(BEER_API_URL_PATH)
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(beerDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(beerDTO.getName())))
                .andExpect(jsonPath("$.brand", is(beerDTO.getBrand())))
                .andExpect(jsonPath("$.type", is(beerDTO.getType().toString())));
    }

    @Test
    void whenPOSTIsCalledWithoutRequiredFieldThenAnErrorIsReturned() throws Exception {
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        beerDTO.setName(null);

        mockMvc.perform(post(BEER_API_URL_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(beerDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenGETIsCalledWithValidNameThenOKIsReturned() throws Exception {
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        when(beerService.findByName(beerDTO.getName())).thenReturn(beerDTO);

        mockMvc.perform(get(BEER_API_URL_PATH + "/" + beerDTO.getName())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(beerDTO.getName())))
                .andExpect(jsonPath("$.brand", is(beerDTO.getBrand())))
                .andExpect(jsonPath("$.type", is(beerDTO.getType().toString())));
    }

    @Test
    void whenGETIsCalledWithInvalidNameThenAnErrorIsReturned() throws Exception {
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        when(beerService.findByName(beerDTO.getName())).thenThrow(BeerNotFoundException.class);

        mockMvc.perform(get(BEER_API_URL_PATH + "/" + beerDTO.getName())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

    }

    @Test
    void whenGETIsCalledForAllBeersThenListOfAllBeersIsReturned() throws Exception {
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        when(beerService.listAll()).thenReturn(List.of(beerDTO));

        mockMvc.perform(get(BEER_API_URL_PATH)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is(beerDTO.getName())))
                .andExpect(jsonPath("$[0].brand", is(beerDTO.getBrand())))
                .andExpect(jsonPath("$[0].type", is(beerDTO.getType().toString())));

    }

    @Test
    void whenDELETEIsCalledWithValidIdThenNoContentIsReturned() throws Exception {
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        doNothing().when(beerService).deleteById(beerDTO.getId());

        mockMvc.perform(delete(BEER_API_URL_PATH + "/" + beerDTO.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void whenDELETEIsCalledWithInvalidIdThenAnErrorIsReturned() throws Exception {
        doThrow(BeerNotFoundException.class).when(beerService).deleteById(INVALID_BEER_ID);

        mockMvc.perform(delete(BEER_API_URL_PATH + "/" + INVALID_BEER_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

    }

    @Test
    void whenPATCHIncrementIsCalledWithValidIdAndQuantityThenBeerQuantityIsIncremented() throws Exception {
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        QuantityDTO quantityDTO = QuantityDTOBuilder.builder().quantity(10).build().toQuantityDTO();

        BeerDTO incrementedBeerDTO = beerDTO;
        incrementedBeerDTO.setQuantity(beerDTO.getQuantity()+quantityDTO.getQuantity());

        when(beerService.increment(incrementedBeerDTO.getId(),quantityDTO.getQuantity())).thenReturn(incrementedBeerDTO);

        mockMvc.perform(patch(BEER_API_URL_PATH + "/" + beerDTO.getId() + BEER_API_SUBPATH_INCREMENT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(quantityDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(incrementedBeerDTO.getName())))
                .andExpect(jsonPath("$.quantity", is(incrementedBeerDTO.getQuantity())));
    }

    @Test
    void whenPATCHIncrementIsCalledWithValidIdAndQuantityGreaterThanAllowedThenAnErrorIsReturned() throws Exception {
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        QuantityDTO quantityDTO = QuantityDTOBuilder.builder().quantity(50).build().toQuantityDTO();

        BeerDTO incrementedBeerDTO = beerDTO;
        incrementedBeerDTO.setQuantity(beerDTO.getQuantity()+quantityDTO.getQuantity());

        doThrow(BeerStockExceededException.class).when(beerService).increment(incrementedBeerDTO.getId(),quantityDTO.getQuantity());

        mockMvc.perform(patch(BEER_API_URL_PATH + "/" + beerDTO.getId() + BEER_API_SUBPATH_INCREMENT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(quantityDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenPATCHIncrementIsCalledWithValidIdAndQuantityLessThanZeroThenAnErrorIsReturned() throws Exception {
        QuantityDTO quantityDTO = QuantityDTOBuilder.builder().quantity(-1).build().toQuantityDTO();

        mockMvc.perform(patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_INCREMENT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(quantityDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenPATCHIncrementIsCalledWithInvalidIdThenAnErrorIsReturned() throws Exception {
        QuantityDTO quantityDTO = QuantityDTOBuilder.builder().quantity(1).build().toQuantityDTO();

        when(beerService.increment(INVALID_BEER_ID, quantityDTO.getQuantity())).thenThrow(BeerNotFoundException.class);

        mockMvc.perform(patch(BEER_API_URL_PATH + "/" + INVALID_BEER_ID + BEER_API_SUBPATH_INCREMENT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(quantityDTO)))
                .andExpect(status().isNotFound());

    }

    @Test
    void whenPATCHDecrementIsCalledWithValidIdAndQuantityThenBeerQuantityIsDecremented() throws Exception {
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        QuantityDTO quantityDTO = QuantityDTOBuilder.builder().quantity(5).build().toQuantityDTO();

        BeerDTO decrementedBeerDTO = beerDTO;
        decrementedBeerDTO.setQuantity(beerDTO.getQuantity()-quantityDTO.getQuantity());

        when(beerService.decrement(decrementedBeerDTO.getId(),quantityDTO.getQuantity())).thenReturn(decrementedBeerDTO);

        mockMvc.perform(patch(BEER_API_URL_PATH + "/" + beerDTO.getId() + BEER_API_SUBPATH_DECREMENT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(quantityDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(decrementedBeerDTO.getName())))
                .andExpect(jsonPath("$.quantity", is(decrementedBeerDTO.getQuantity())));
    }

    @Test
    void whenPATCHDecrementIsCalledWithValidIdAndQuantityLessThanZeroAfterDecrementThanAllowedThenAnErrorIsReturned() throws Exception {
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        QuantityDTO quantityDTO = QuantityDTOBuilder.builder().quantity(80).build().toQuantityDTO();

        doThrow(BeerStockLessThenZeroException.class).when(beerService).decrement(beerDTO.getId(),quantityDTO.getQuantity());

        mockMvc.perform(patch(BEER_API_URL_PATH + "/" + beerDTO.getId() + BEER_API_SUBPATH_DECREMENT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(quantityDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenPATCHDecrementIsCalledWithValidIdAndQuantityLessThanZeroThenAnErrorIsReturned() throws Exception {
        QuantityDTO quantityDTO = QuantityDTOBuilder.builder().quantity(-1).build().toQuantityDTO();

        mockMvc.perform(patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_DECREMENT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(quantityDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenPATCHDecrementIsCalledWithInvalidIdThenAnErrorIsReturned() throws Exception {
        QuantityDTO quantityDTO = QuantityDTOBuilder.builder().quantity(1).build().toQuantityDTO();

        when(beerService.decrement(INVALID_BEER_ID, quantityDTO.getQuantity())).thenThrow(BeerNotFoundException.class);

        mockMvc.perform(patch(BEER_API_URL_PATH + "/" + INVALID_BEER_ID + BEER_API_SUBPATH_DECREMENT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(quantityDTO)))
                .andExpect(status().isNotFound());

    }
}
