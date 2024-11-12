package c.e.filter;

import c.e.utils.JwtUtils;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 用于对请求头中jwt令牌进行校验的工具，为当前请求添加用户验证信息
 * 并将用户的ID存放在请求对象属性中，方便后续使用
 */
@Component
public class JwtAuthorizeFilter extends OncePerRequestFilter {

    //导入jwt工具
    @Resource
    JwtUtils utils;

    /**
     * 编写自定义验证逻辑
     * @param request  请求
     * @param response  响应
     * @param filterChain  放行对象
     * @throws ServletException  可能的异常
     * @throws IOException   可能的异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        //读取请求头  从Authorization请求头中获取token
        String authorization = request.getHeader("Authorization");
        //验证并解析jwt
        DecodedJWT jwt = utils.resolveJwt(authorization);
        //如果不等于空,
        if (jwt != null){
            //从jwt中解析出用户信息
            UserDetails user = utils.toUser(jwt);
            //手动授权    这个是Security内部的token
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user,null,user.getAuthorities());
            //UsernamePasswordAuthenticationToken这里面是这样写的，照着写就可以了
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            //将写好的验证信息丢进去，这样就相当于通过了security的验证
            SecurityContextHolder.getContext().setAuthentication(authentication);
            //为了方便后续的业务，将一些必要信息放到request中
            request.setAttribute("id",utils.toId(jwt));
        }
        //如果等于空的话直接往下走就可以了
        filterChain.doFilter(request,response);
    }

}
