package com.natsumes.wezard.entity.form;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class OrderCreateForm implements Serializable {

    private static final long serialVersionUID = 3219997854116630149L;
    @NotNull
    private Integer shippingId;

    private Integer productNum;

    private Integer productId;

    private Integer createType;  // 1 - by product method

}
