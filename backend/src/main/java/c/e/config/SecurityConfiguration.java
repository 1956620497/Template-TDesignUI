package c.e.config;

import c.e.entity.RestBean;
import c.e.entity.dto.Account;
import c.e.entity.vo.response.AuthorizeVO;
import c.e.filter.JwtAuthorizeFilter;
import c.e.service.AccountService;
import c.e.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.PrintWriter;

//配置安全框架
@Configuration
public class SecurityConfiguration {

    //导入jwt工具
    @Resource
    JwtUtils utils;

    //jwt过滤器
    @Resource
    JwtAuthorizeFilter jwtAuthorizeFilter;

    @Resource
    AccountService accountService;

    /**
     * Security的过滤器链
     * @return
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //返回一个lambda表达式
        return http
                //配置HTTP请求的权限规则
                .authorizeHttpRequests(conf -> conf
                        //放行登录模块
                        .requestMatchers("/api/auth/**","/api/test/**","error").permitAll()
                        //其他的请求需要登录之后才允许访问
                        .anyRequest().authenticated()
                )
                //配置登录相关
                .formLogin(conf -> conf
                        //配置登录接口
                        .loginProcessingUrl("/api/auth/login")
                        //配置登录成功的处理
                        .successHandler(this::onAuthenticationSuccess)
                        //配置登录失败的处理
                        .failureHandler(this::onAuthenticationFailure)
                )
                //退出登录配置
                .logout(conf -> conf
                        //退出登录接口
                        .logoutUrl("/api/auth/logout")
                        //退出登录成功的处理
                        .logoutSuccessHandler(this::onLogoutSuccess)
                )
                //处理一下没有登录的情况
                .exceptionHandling(conf -> conf
                        //没有登录的情况下
                        .authenticationEntryPoint(this::onUnauthorized)
                        //没有权限的情况下
                        .accessDeniedHandler(this::onAccessDeny)
                )
                //csrf配置，固定配置
                .csrf(AbstractHttpConfigurer::disable)
                //现在是无状态的前后端分离，跟有状态的区别是session不需要维护用户信息的 将session改为无状态，security不用去处理session了
                .sessionManagement(conf -> conf
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                //加入自己的过滤器,将自己的过滤器加在验证的过滤器之前
                .addFilterBefore(jwtAuthorizeFilter, UsernamePasswordAuthenticationFilter.class)


                .build();
    }



    //配置登录成功处理
    public void onAuthenticationSuccess(HttpServletRequest request,   //请求
                                        HttpServletResponse response,  //响应
                                        Authentication authentication  //验证信息
                                        ) throws IOException {
        //配置返回值类型为JSON格式和配置字符编码
        response.setContentType("application/json;charset=utf-8");

        //通过Authentication拿到用户的详细信息  注意这里的User是SpringSecurity中的
        User user = (User) authentication.getPrincipal();
        //从数据库中查找用户信息
        Account account = accountService.findAccountByNameOrEmail(user.getUsername());
        //构建令牌
        String token = utils.createJwt(user,account.getId(),account.getUsername());
        //封装用户信息    因为手动实现了BeanUtils.copyProperties()，所以直接调用asViewObject方法就可以将同属性名的复制进去
        AuthorizeVO vo = account.asViewObject(AuthorizeVO.class,v -> {
            v.setExpire(utils.expireTime());
            v.setToken(token);
        });
        //直接复制属性
//        BeanUtils.copyProperties(account,vo);

        //返回登录成功
        response.getWriter().write(RestBean.success(vo).asJsonString());
    }

    //配置登录失败的处理
    public void onAuthenticationFailure(HttpServletRequest request,  // 请求
                                        HttpServletResponse response,  //响应
                                        AuthenticationException exception  //异常信息
                                        ) throws IOException {
        //配置返回值类型为JSON格式和配置字符编码
        response.setContentType("application/json;charset=utf-8");
        //返回失败信息
        response.getWriter().write(RestBean.unauthorized(exception.getMessage()).asJsonString());
    }

    //权限不够时
    public void onAccessDeny(HttpServletRequest request,
                             HttpServletResponse response,
                             AccessDeniedException accessDeniedException) throws IOException {
        //配置返回值类型为JSON格式和配置字符编码
        response.setContentType("application/json;charset=utf-8");
        //没有登录时，直接返回没有登录
        response.getWriter().write(RestBean.forbidden(accessDeniedException.getMessage()).asJsonString());
    }

    //未验证时
    public void onUnauthorized(HttpServletRequest request,  // 请求
                               HttpServletResponse response,  //响应
                               AuthenticationException exception  //异常信息
    ) throws IOException {
        //配置返回值类型为JSON格式和配置字符编码
        response.setContentType("application/json;charset=utf-8");
        //没有登录时，直接返回没有登录
        response.getWriter().write(RestBean.unauthorized(exception.getMessage()).asJsonString());
    }


    //退出登录成功的处理
    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws IOException {
        //配置返回值类型为JSON格式和配置字符编码
        response.setContentType("application/json;charset=utf-8");
        //将返回参数提取出来
        PrintWriter writer = response.getWriter();
        //获取到验证信息
        String authorization = request.getHeader("Authorization");
        //让请求头中的jwt失效
        if (utils.invalidateJwt(authorization)){
            //失效成功时
            writer.write(RestBean.success().asJsonString());
        }else{
            writer.write(RestBean.failure(400,"退出登录失败").asJsonString());
        }
    }

}
