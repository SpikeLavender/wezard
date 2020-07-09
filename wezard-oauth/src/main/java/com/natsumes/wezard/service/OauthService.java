package com.natsumes.wezard.service;


import com.natsumes.wezard.entity.Response;
import com.natsumes.wezard.entity.form.UserRegisterForm;
import com.natsumes.wezard.pojo.Users;

public interface OauthService {

    Response register(UserRegisterForm userRegisterForm);

    Response<Users> login(String username, String password);

    Response<Users> wxLogin(String userCode);
}
