package com.natsumes.wezard.entity.form;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class SearchForm implements Serializable {

    private static final long serialVersionUID = 1222737238449394740L;
    private String name;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private Integer categoryId;

    public SearchForm() {
    }

    public SearchForm(String name, BigDecimal minPrice, BigDecimal maxPrice, Integer categoryId) {
        this.name = name;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.categoryId = categoryId;
    }
}
