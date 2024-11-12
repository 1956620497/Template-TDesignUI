package c.e.filter;


import c.e.utils.Const;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
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

    //允许跨域的站点
    @Value("${spring.web.cors.origin}")
    String origin;

    //是否允许用户携带凭证允许跨域访问
    @Value("${spring.web.cors.credentials}")
    boolean credentials;

    @Value("${spring.web.cors.methods}")
    String methods;

    @Override
    protected void doFilter(HttpServletRequest request,
                            HttpServletResponse response,
                            FilterChain chain) throws IOException, ServletException {
        //在响应头中添加跨域信息就可以了
        this.addCorsHeader(request,response);
        // 直接放行全部请求
        chain.doFilter(request,response);
    }

    /**
     * 在响应头中添加跨域信息
     * @param request  请求
     * @param response  响应
     */
    private void addCorsHeader(HttpServletRequest request,
                               HttpServletResponse response){
        //允许哪些地址跨域访问
        // 在请求头中将Origin，也就是原始站点取出来添加到请求头中就可以进行跨域访问了。
//        response.addHeader("Access-Control-Allow-Origin",request.getHeader("Origin"));
        //只能我自己的前端才能跨域访问我的接口
//        response.addHeader("Access-Control-Allow-Origin","http://127.0.0.1:5173");
        response.addHeader("Access-Control-Allow-Origin", this.resolveOrigin(request));
        // 允许的请求的方法
//        response.addHeader("Access-Control-Allow-Methods","GET,POST,PUT,DELETE,OPTIONS");
        response.addHeader("Access-Control-Allow-Methods", this.resolveMethod());
        // 配置一下请求头
        response.addHeader("Access-Control-Allow-Headers","Authorization,Content-Type");

        //
        if(credentials) {
            //添加响应头，并将值设置为true。这个设置允许跨域请求携带凭证，使客户端在跨域请求中可以访问到受保护的资源。
            response.addHeader("Access-Control-Allow-Credentials", "true");
        }
    }

    /**
     * 解析配置文件中的请求方法
     * @return  解析得到的请求头值
     */
    private String resolveMethod(){
        return methods.equals("*") ? "GET, HEAD, POST, PUT, DELETE, OPTIONS, TRACE, PATCH" : methods;
    }

    /**
     * 解析配置文件中的请求原始站点
     * @param request 请求
     * @return 解析得到的请求头值
     */
    private String resolveOrigin(HttpServletRequest request){
        return origin.equals("*") ? request.getHeader("Origin") : origin;
    }

}
