package cf.vsing.community.config;

import cf.vsing.community.entity.Result;
import cf.vsing.community.entity.ResultCode;
import cf.vsing.community.entity.User;
import cf.vsing.community.service.UserService;
import cf.vsing.community.util.*;
import cn.hutool.jwt.JWTException;
import com.alibaba.fastjson2.JSON;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static cf.vsing.community.util.MyException.LoginException;
import static cf.vsing.community.util.StatusUtil.Authority.*;
import static cf.vsing.community.util.StatusUtil.Status;
import static cf.vsing.community.util.StatusUtil.Status.*;

@Configuration
@EnableWebSecurity    // 添加 security 过滤器
@EnableMethodSecurity    // 启用方法级别的权限认证
public class SpringSecurityConfig  {
    private final UserService userService;
    private final RedisTemplate<String,Object> redisTemplate;
    private final HostHolderUtil hostHolder;

    @Autowired
    SpringSecurityConfig(UserService userService, RedisTemplate<String,Object> redisTemplate,HostHolderUtil hostHolder) {
        this.userService = userService;
        this.redisTemplate = redisTemplate;
        this.hostHolder=hostHolder;
    }

    /**
     * 配置通行资源路径
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web
                .ignoring()
                .requestMatchers("/resources/static/**", "/resources/templates/**");
    }

    /**
     * 自定义 登录逻辑 Provider
     */
    class MyAuthenticationProvider implements AuthenticationProvider {
        @Override
        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            //验证验证码
            captchaVerify(request);

            boolean remember = Boolean.parseBoolean(request.getParameter("remember"));
            String username = request.getParameter("username").trim();
            String password = request.getParameter("password").trim();
            String emailCheck = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
            String phoneCheck = "^1(3\\d|4[5-9]|5[0-35-9]|6[567]|7[0-8]|8\\d|9[0-35-9])\\d{8}$";
            User user;
            if(username.matches(emailCheck)){
                user=userService.find(FieldUtil.User.email,username);
            }else if(username.matches(phoneCheck)){
                user=userService.find(FieldUtil.User.phone,username);
            }else {
                user=userService.find(FieldUtil.User.name,username);
            }
            if (user == null || Status.of(user.getStatus()) == DELETED) {
                throw new LoginException(Result.fail("用户不存在，请先注册！","usernameMsg"));
            } else if (Status.of(user.getStatus()) == UNACTIVATED) {

                throw new LoginException(Result.fail("用户未激活！","usernameMsg"));
            } else if (Status.of(user.getStatus()) == LOCKED) {
                throw new LoginException(Result.fail("用户封禁中！","usernameMsg"));
            } else {
                password = DigestUtils.md5DigestAsHex((password + user.getSalt()).getBytes());
                if (!password.equals(user.getPassword())) {
                    throw new LoginException(Result.fail("密码错误！","passwordMsg"));
                }
                hostHolder.setUser(user,remember ? 7 * 24 *3600000: 3600000);
                String jwt = CommunityUtil.generateJWT(user.getId(), user.getAuthority(), remember ? 7 * 24 *3600000 : 3600000);

                return new UsernamePasswordAuthenticationToken(user.getId(), jwt, getAuthorities(user.getAuthority()));
            }
        }
        private void captchaVerify(HttpServletRequest request) throws LoginException {
            String captcha = request.getParameter("captcha");
            String captchaId = CookieUtil.getCookie(request,"captchaId");
            String realCaptcha = captchaId == null ? "" : (String) redisTemplate.opsForValue().get(RedisKeyUtil.getCaptcha(captchaId));
            redisTemplate.delete(RedisKeyUtil.getCaptcha(captchaId));
            if (StringUtils.isBlank(realCaptcha)) {
                throw new LoginException(Result.fail("验证码失效，请重试！","captchaMsg"));
            } else if (StringUtils.isBlank(captcha)) {
                throw new LoginException(Result.fail("验证码不能为空！","captchaMsg"));
            } else if (!realCaptcha.equalsIgnoreCase(captcha)) {
                throw new LoginException(Result.fail("验证码错误！","captchaMsg"));
            }
        }
        @Override
        public boolean supports(Class<?> authentication) {
            return UsernamePasswordAuthenticationToken.class.equals(authentication);
        }
    }

    /**
     * 自定义 jwt登录凭证 Filter
     */
    class JwtAuthFilter extends OncePerRequestFilter{
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
            String jwt = CookieUtil.getCookie(request, "jwt");
            // 如果请求头中没有Authorization信息则直接放行了,交给后续鉴权Filter处理
            if (jwt == null || StringUtils.isBlank(jwt)) {
                chain.doFilter(request, response);
            }else {
                // 如果请求头中有token，则进行解析，并且设置认证信息
                DecodedJWT claims = CommunityUtil.verifyJWT(jwt);
                if (claims == null) {
                    throw new JWTException("token 异常");
                }
                //从缓存中验证token的存在性
                int userId = claims.getClaim("i").asInt();
                if (!hostHolder.checkLogin(userId)) {
                    throw new JWTException("token 过期");
                }
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userId, jwt, getAuthorities(claims));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                chain.doFilter(request, response);
            }
        }
    }


    private final JwtAuthFilter jwtAuthFilter=new JwtAuthFilter();

    /**
     * 主配置
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.rememberMe().disable();
        http.httpBasic().disable();
        //  禁用CSRF防护
        http.csrf().disable();
        //  清除页面跳转缓存
        http.requestCache().requestCache(new NullRequestCache());

        ProviderManager MyProviderManager=new ProviderManager(new MyAuthenticationProvider());

        //  认证后不清空credentials
        MyProviderManager.setEraseCredentialsAfterAuthentication(false);


        //  认证配置
        //  认证 Provider
        http.authenticationManager(MyProviderManager);
        //  jwt鉴权Filter
        http.addFilterBefore(jwtAuthFilter,UsernamePasswordAuthenticationFilter.class);
        //  登录配置
        http.formLogin()
                //  登录页面，GET 请求
                .loginPage("/login")
                //  登录处理，POST 请求
                .loginProcessingUrl("/login")
                //  登录成功处理器
                .successHandler((request, response, auth) -> {
                    response.setContentType("application/json;charset=utf-8");
                    boolean remember = Boolean.parseBoolean(request.getParameter("remember"));
                    Cookie cookie = new Cookie("jwt", (String) auth.getCredentials());
                    cookie.setPath("/");
                    cookie.setMaxAge(remember ? 7 * 24 *3600: 3600);
                    response.addCookie(cookie);
                    response.getWriter().write(JSON.toJSONString(Result.succ("登陆成功！","/")));
                })
                //  登录失败处理器
                .failureHandler((request, response, e) -> {
                    response.setContentType("application/json;charset=utf-8");
                    response.getWriter().write(JSON.toJSONString(((LoginException) e).getResult()));
                });
        //  登出配置
        http.logout()
                //  注销URL POST 请求
                .logoutUrl("/logout")
                //  注销成功处理器
                .logoutSuccessHandler((request, response, authentication) -> {
                    hostHolder.delete(request);
                    Cookie cookie = new Cookie("jwt", "");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                    response.sendRedirect("/");
                });

        //  权限配置
        //      鉴权 Filter
        //  权限控制
        http.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/user/setting",
                                "/user/follower",
                                "/user/followee",
                                "/setting",
                                "/message",
                                "/logout",
                                "/like",
                                "/info",
                                "/comment/add",
                                "/article/publish"
                        ).hasAnyAuthority("ROLE_USER","ROLE_ADMIN")
                        .anyRequest().permitAll()
                )
                //  权限异常处理
                .exceptionHandling(exception -> exception
                        //  未登录
                        .authenticationEntryPoint((request, response, authException) -> {
                            String ajax = request.getHeader("x-requested-with");
                            if (ajax != null && ajax.equals("XMLHttpRequest")) {
                                response.setContentType("application/json;charset=utf-8");
                                response.setStatus(200);
                                response.getWriter().write(JSON.toJSONString(Result.fail(ResultCode.AUTH_NOT_LOGIN)));
                            } else {
                                response.sendRedirect(request.getContextPath() + "/login");
                            }
                        })
                        //  权限不足
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            String ajax = request.getHeader("x-requested-with");
                            if (ajax != null && ajax.equals("XMLHttpRequest")) {
                                response.setContentType("application/json;charset=utf-8");
                                response.setStatus(200);
                                response.getWriter().write(JSON.toJSONString(Result.fail(ResultCode.AUTH_DENIED)));
                            } else {
                                response.sendRedirect(request.getContextPath() + "/denied");
                            }
                        })
                );
        return http.build();
    }
    public Collection<? extends GrantedAuthority> getAuthorities(DecodedJWT claims) {
        return getAuthorities(claims.getClaim("a").asInt());
    }
    public Collection<? extends GrantedAuthority> getAuthorities(int auth){
        StatusUtil.Authority authority = switch (auth) {
            case 1 -> RESERVED;
            case 2 -> ADMIN;
            case 3 -> SYSTEM;
            default -> USER;
        };
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + authority.name()));
        return grantedAuthorities;
    }
}
