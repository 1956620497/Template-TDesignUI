package c.e.filter;

import c.e.entity.RestBean;
import c.e.utils.Const;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 限流控制过滤器
 * 防止用户高频请求接口，借助redis限流
 */
@Slf4j
@Component
@Order(Const.ORDER_LIMIT)  //优先级要比跨域过滤器低一点，先过跨域再过限流
public class FlowLimitFilter extends HttpFilter {

    @Resource
    StringRedisTemplate template;

    //指定时间内最大请求次数限制
    @Value("${spring.web.flow.limit}")
    int limit;
    //计数时间周期
    @Value("${spring.web.flow.period}")
    int period;
    //超出请求限制封禁时间
    @Value("${spring.web.flow.block}")
    int block;


    @Override
    protected void doFilter(HttpServletRequest request,
                            HttpServletResponse response,
                            FilterChain chain) throws IOException, ServletException {
        //获取用户的ip
        String ip = request.getRemoteAddr();
        //判断请求计数器是否正常计数
        if (this.tryCount(ip)){
            //计数成功，就通过该请求
            chain.doFilter(request, response);
        }else {
            //如果计数失败,就拒绝用户请求并且提示用户访问过快
            this.writeBlockMessage(response);
        }


    }

    /**
     * 为响应编写拦截内容，提示用户操作频繁
     * @param response  响应对象
     */
    private void writeBlockMessage(HttpServletResponse response) throws IOException {
        //拒绝用户访问
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        //提示用户访问过快
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(RestBean.forbidden("操作频繁,请稍后再试").asJsonString());
    }

    /**
     * 对指定ip地址请求计数，如果被限制则无法继续访问
     * @param ip  请求的ip地址
     * @return  是否计数成功，如果计数不成功就说明用户被限流了
     */
    private boolean tryCount(String ip){
        //因为有可能同时判断和同时设置黑名单，所以要加个锁，避免请求太快引发判断错误
        synchronized (ip.intern()){
            //判断缓存中是否有被封禁的key
            if (Boolean.TRUE.equals(template.hasKey(Const.FLOW_LIMIT_BLOCK + ip)))
                //如果已经被封禁
                return false;
            //没有被封禁就尝试增加计数器，如果计数器计数到上限，就会封禁该ip
            return this.limitPeriodCheck(ip);
        }
    }

    /**
     * 请求频率限制
     * @param ip 需要限制的ip地址
     * @return  false为被封禁，不允许通过，true是运行通过
     */
    private boolean limitPeriodCheck(String ip){
        String key = Const.FLOW_LIMIT_COUNTER + ip;
        //这里有个问题，用increment来给key的value增加计数，如果在增加时该key刚好不存在，会自动创建一个持续时间为-1的，也就是永久存在的key
        //但是这个问题影响不是很大，基本上只有很小的概率，或者进行压力测试时会出现，所以到这一步，如果遇到为-1的情况下，直接将该计数器删除，让它重新走判定

        //判断缓存中是否有这个ip的key
        if (Boolean.TRUE.equals(template.hasKey(key))) {
            //如果有key就让计数+1
            //获取该key，因为有可能获取时刚好到期，获取到为空,所以要用Optional.ofNullable()来处理一下
            long increment = Optional.ofNullable(
                    template.opsForValue().increment(key)).orElse(0L);
            //如果该key的请求数大于50，就说明该请求超额了，要被封禁了
            if (increment > limit){
                //将该ip设置到黑名单中，封禁30秒
                template.opsForValue().set(Const.FLOW_LIMIT_BLOCK + ip,"封禁ip",block,TimeUnit.SECONDS);
                return false;
            }
        }else{
            //如果没有key就往缓存中添加一个新的
            //设置该缓存只能存在3秒钟
            template.opsForValue().set(key,"1",period, TimeUnit.SECONDS);
        }
        return true;
    }

}
