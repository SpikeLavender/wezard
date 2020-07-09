package com.natsumes.wezard.controller;

import com.natsumes.wezard.entity.Response;
import com.natsumes.wezard.entity.form.UserLoginForm;
import com.natsumes.wezard.entity.form.UserRegisterForm;
import com.natsumes.wezard.entity.form.WeChartForm;
import com.natsumes.wezard.pojo.Users;
import com.natsumes.wezard.service.OauthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import static com.natsumes.wezard.consts.StefanieConst.CURRENT_USER;

@Slf4j
@RestController
@RequestMapping("/oauth")
public class OauthController {

    @Autowired
    private OauthService oauthService;

    @PostMapping("/register")
    public Response register(@Valid @RequestBody UserRegisterForm userRegisterForm) {

        return oauthService.register(userRegisterForm);
    }

    @PostMapping("/login")
    public Response<Users> login(@Valid @RequestBody UserLoginForm userLoginForm, HttpSession session) {

        Response<Users> userResponseVo = oauthService.login(userLoginForm.getUsername(), userLoginForm.getPassword());

        //设置 Session
        session.setAttribute(CURRENT_USER, userResponseVo.getData());
        log.info("login sessionId = {}", session.getId());
        return userResponseVo;
    }

    @PostMapping("/wechart")
    public Response<Users> wechart(@Valid @RequestBody WeChartForm userForm) {

        return oauthService.wxLogin(userForm.getUserCode());
    }

    @PostMapping("/{userId}/logout")
    public Response<Users> logout(@PathVariable Integer userId) {
        log.info("logout sessionId = {}", userId);

//        session.removeAttribute(CURRENT_USER);
        return Response.success();
    }

}
