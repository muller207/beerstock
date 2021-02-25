package br.com.muller.beerstock.builder;


import br.com.muller.beerstock.dto.QuantityDTO;
import lombok.Builder;

@Builder
public class QuantityDTOBuilder {
    @Builder.Default
    private int quantity = 10;

    public QuantityDTO toQuantityDTO(){
        return new QuantityDTO(quantity);
    }
}
