package c.e.filter;


import c.e.utils.Const;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 用来处理浏览器跨域相关
 */
@Component
//指定过滤器链优先级
@Order(Const.ORDER_CORS)
public class CorsFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest request,
                            HttpServletResponse response,
                            FilterChain chain) throws IOException, ServletException {
        //在响应头中添加跨域信息就可以了
        this.addCorsHeader(request,response);
        // 直接放行全部请求
        chain.doFilter(request,response);
    }

    //在响应头中添加跨域信息
    private void addCorsHeader(HttpServletRequest request,
                               HttpServletResponse response){
        //允许哪些地址跨域访问
        // 在请求头中将Origin，也就是原始站点取出来添加到请求头中就可以进行跨域访问了。
//        response.addHeader("Access-Control-Allow-Origin",request.getHeader("Origin"));
        //只能我自己的前端才能跨域访问我的接口
        response.addHeader("Access-Control-Allow-Origin","http://127.0.0.1:5173");
        // 允许的请求的方法
        response.addHeader("Access-Control-Allow-Methods","GET,POST,PUT,DELETE,OPTIONS");
        // 配置一下请求头
        response.addHeader("Access-Control-Allow-Headers","Authorization,Content-Type");
    }

}
