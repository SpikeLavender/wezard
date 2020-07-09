package com.natsumes.wezard.entity.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class UserRegisterForm implements Serializable {

    private static final long serialVersionUID = -4886824318831615133L;
    //@NotBlank 用于 String 判断空格
    //@NotEmpty 用于集合
    //@NotNull
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotNull
    private Integer parentId;
}
