package com.natsumes.wezard.entity.form;

import lombok.Data;

import java.io.Serializable;

@Data
public class CartUpdateForm implements Serializable {

    private static final long serialVersionUID = 3944532563893209452L;
    private Integer quantity; //非必填

    private Boolean selected; //非必填

}
