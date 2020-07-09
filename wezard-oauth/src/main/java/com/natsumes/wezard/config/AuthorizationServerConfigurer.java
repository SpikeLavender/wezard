package com.natsumes.wezard.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import javax.sql.DataSource;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfigurer extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private StefanieAccessTokenConverter stefanieAccessTokenConverter;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Value("${sign_key}")
    private String signKey;


    /**
     * 认证服务器最终是以api接⼝的⽅式对外提供服务（校验合法性并⽣成令牌、校验令牌等）
     * 那么，以api接⼝⽅式对外的话，就涉及到接⼝的访问权限，我们需要在这⾥进⾏必要的配
     置
     * @param security
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        super.configure(security);
        security.allowFormAuthenticationForClients()    // 允许客户端表单认证
                .tokenKeyAccess("permitAll()")  // 开启端口/oauth/token_key的访问权限（允许）
                .checkTokenAccess("permitAll()");   // 开启端口/oauth/check_token的访问权限（允许）
    }

    /**
     * 客户端详情配置，
     * ⽐如client_id，secret
     * 当前这个服务就如同QQ平台，本网站作为客户端需要qq平台进⾏登录授权认证等，提前需到QQ平台注册，QQ平台会给我们
     * 颁发client_id等必要参数，表明客户端是谁
     * @param clients
     * @throws Exception
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        super.configure(clients);

        // 从内存中加载客户端详情

//        clients.inMemory()// 客户端信息存储在什么地方，可以在内存中，可以在数据库里
//                .withClient("client_lagou")  // 添加一个client配置,指定其client_id
//                .secret("abcxyz")                   // 指定客户端的密码/安全码
//                .resourceIds("order")         // 指定客户端所能访问资源id清单，此处的资源id是需要在具体的资源服务器上也配置一样
//                // 认证类型/令牌颁发模式，可以配置多个在这里，但是不一定都用，具体使用哪种方式颁发token，需要客户端调用的时候传递参数指定
//                .authorizedGrantTypes("password","refresh_token", "authorization_code")
//                // 客户端的权限范围，此处配置为all全部即可
//                .scopes("all");

        clients.withClientDetails(createJdbcClientDetailsService());
    }

    /**
     * 认证服务器是玩转token的，那么这⾥配置token令牌管理相关（token此时就是⼀个字符
     串，当下的token需要在服务器端存储，
     * 那么存储在哪⾥呢？都是在这⾥配置）
     * @param endpoints
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        super.configure(endpoints);
        endpoints.tokenStore(tokenStore())  // 指定token的存储方法
                // token服务的一个描述，可以认为是token生成细节的描述，比如有效时间多少等
                .tokenServices(authorizationServerTokenServices())
                .authenticationManager(authenticationManager)   // 指定认证管理器，随后注入一个到当前类使用即可
                .allowedTokenEndpointRequestMethods(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE);

    }

    public TokenStore tokenStore() {
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    /**
     * 返回jwt令牌转换器（帮助我们生成jwt令牌的）
     * 在这里，我们可以把签名密钥传递进去给转换器对象
     * @return
     */
    private JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
        jwtAccessTokenConverter.setSigningKey(signKey);  // 签名密钥
        jwtAccessTokenConverter.setVerifier(new MacSigner(signKey));  // 验证时使用的密钥，和签名密钥保持一致
        jwtAccessTokenConverter.setAccessTokenConverter(stefanieAccessTokenConverter);

        return jwtAccessTokenConverter;
    }

    /**
     * 该方法用户获取一个token服务对象（该对象描述了token有效期等信息）
     */
    public AuthorizationServerTokenServices authorizationServerTokenServices() {
        // 使用默认实现
        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setSupportRefreshToken(true); // 是否开启令牌刷新
        defaultTokenServices.setTokenStore(tokenStore());

        // 针对jwt令牌的添加
        defaultTokenServices.setTokenEnhancer(jwtAccessTokenConverter());

        // 设置令牌有效时间（一般设置为2个小时）
        defaultTokenServices.setAccessTokenValiditySeconds(2 * 60 * 60); // access_token就是我们请求资源需要携带的令牌
        // 设置刷新令牌的有效时间
        defaultTokenServices.setRefreshTokenValiditySeconds(259200); // 3天

        return defaultTokenServices;
    }

    @Bean
    public JdbcClientDetailsService createJdbcClientDetailsService() {
        return new JdbcClientDetailsService(dataSource);
    }
}
