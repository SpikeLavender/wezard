package com.natsumes.wezard.entity.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class UserBankForm implements Serializable {
    private static final long serialVersionUID = -2222086103677565638L;

    @NotBlank
    private String bank;

    @NotBlank
    private String bankNo;

    @NotBlank
    private String name;
}
