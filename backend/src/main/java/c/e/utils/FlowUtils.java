package c.e.utils;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


/**
 * reids的限流工具
 */
@Component
public class FlowUtils {

    @Resource
    StringRedisTemplate template;

    /**
     * 针对于单次的频率限制
     * @param key   用于检查的key
     * @param blockTime  冷却时间
     * @return
     */
    public boolean limitOnceCheck(String key,int blockTime){
        //如果缓存中有这个key，说明是正在冷却的状态
        if (Boolean.TRUE.equals(template.hasKey(key))){
            //用户被冷却时,不允许用户请求
            return false;
        }else {
            //缓存中没有这个key，就是不在冷却的状态，就往缓存中丢一个key
            //冷却时间为传入的，以秒为单位
            template.opsForValue().set(key,"",blockTime, TimeUnit.SECONDS);
            return true;
        }
    }

}
