package com.natsumes.wezard.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.natsumes.wezard.entity.Response;
import com.natsumes.wezard.entity.form.UserRegisterForm;
import com.natsumes.wezard.enums.ResponseEnum;
import com.natsumes.wezard.enums.RoleEnum;
import com.natsumes.wezard.pojo.Users;
import com.natsumes.wezard.service.DubboUsersService;
import com.natsumes.wezard.service.OauthService;
import com.natsumes.wezard.utils.JSONUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

@Service
@Configuration
public class OauthServiceImpl implements OauthService {

    @Value("${wx.openIdUrl}")
    private String openIdUrl;

    @Value("${wx.appId}")
    private String appId;

    @Value("${wx.mchKey}")
    private String mchKey;

    @Autowired
    private RestTemplate restTemplate;

    @Reference
    private DubboUsersService dubboUsersService;

    @Override
    public Response register(UserRegisterForm userRegisterForm) {

        Users users = new Users();
        BeanUtils.copyProperties(userRegisterForm, users);

        //username 不能重复
        int countByUsername = dubboUsersService.countByUsername(users.getUsername());
        if (countByUsername > 0) {
            return Response.error(ResponseEnum.USERNAME_EXIST);
        }

        //todo:放到登陆页面而不是注册页面

        //检验推广父id是否有效
        if (users.getParentId() == null) {
            users.setParentId(0);
        }
        if (users.getParentId() != 0 && dubboUsersService.countById(users.getParentId()) <= 0) {
            //todo: 设置为0，是否需要提示异常待定
            return Response.error(ResponseEnum.PARENT_NO_EXIST, "上级ID错误，请确认并重新填写");
        }

        users.setRole(RoleEnum.ADMIN.getCode());
        //MD5摘要算法(Spring 自带)
        String password = new BCryptPasswordEncoder().encode(users.getPassword());
        users.setPassword(password);

        //写入数据库
        int resultCount = dubboUsersService.insertSelective(users);
        if (resultCount == 0) {
            return Response.error(ResponseEnum.SYSTEM_ERROR, "写入数据库异常, 注册失败");
        }
        return Response.success();
    }

    @Override
    public Response<Users> login(String username, String password) {
        Users users = dubboUsersService.selectByUsername(username);
        if (users == null) {
            //用户不存在(返回: 用户名或密码错误)
            return Response.error(ResponseEnum.USERNAME_OR_PASSWORD_ERROR);
        }
        if (!users.getPassword().equalsIgnoreCase(DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8)))) {
            //密码错误(返回: 用户名或密码错误)
            return Response.error(ResponseEnum.USERNAME_OR_PASSWORD_ERROR);
        }

        users.setPassword("");
        users.setOpenid("");
        return Response.success(users);
    }

    /**
     * 微信登陆
     * @param userCode
     * @return
     */
    @Override
    public Response<Users> wxLogin(String userCode) {
        String url = openIdUrl + "?appid=" + appId + "&secret=" + mchKey + "&js_code="
                + userCode + "&grant_type=authorization_code";

        System.out.println("ssssaaaaaaaaaa"+url);

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);

        if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
            return Response.error(ResponseEnum.SYSTEM_ERROR, "查询微信OpenId失败");
        }

        JSONObject responseBody = JSONUtils.parseObject(responseEntity.getBody());
        if (responseBody.containsKey("errcode")) {
            return Response.error(ResponseEnum.WECHART_LOGIN_ERROR, responseBody.getString("errmsg"));
        }
        String openId = responseBody.getString("openid");
        String sessionKey = responseBody.getString("session_key");

        Users users = dubboUsersService.selectByUsername(openId); //openId为用户的username
        if (users == null) {
            //用户不存在(返回: 用户名或密码错误) , 自动注册
            users = new Users();
            users.setRole(RoleEnum.CUSTOMER.getCode());
            //MD5摘要算法(Spring 自带)
            //存储sessionKey
            users.setPassword(openId); //传送给前端，用一定的加密算法
            users.setParentId(0);
            users.setUsername(openId);
            users.setOpenid(sessionKey);

            //写入数据库
            int resultCount = dubboUsersService.insertSelective(users);
            if (resultCount == 0) {
                return Response.error(ResponseEnum.SYSTEM_ERROR, "写入数据库异常, 注册失败");
            }
        } else {
            //更新sessionKey
            users.setPassword(openId); //到时候做加密
            users.setOpenid(sessionKey);
            users.setUsername(openId);
            dubboUsersService.updateByPrimaryKey(users);
        }
        return Response.success(users);
    }
}
