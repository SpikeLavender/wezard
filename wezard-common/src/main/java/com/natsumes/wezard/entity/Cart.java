package com.natsumes.wezard.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class Cart implements Serializable {

    private static final long serialVersionUID = 109421235264497974L;
    private Integer productId;

    /**
     * 购买的数量
     */
    private Integer quantity;

    /**
     * 商品是否选中
     */
    private Boolean productSelected;


    public Cart() {
    }

    public Cart(Integer productId, Integer quantity, Boolean productSelected) {
        this.productId = productId;
        this.quantity = quantity;
        this.productSelected = productSelected;
    }
}
