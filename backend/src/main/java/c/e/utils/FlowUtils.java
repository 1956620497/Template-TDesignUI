package c.e.utils;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;


/**
 * reids的限流工具
 * 针对不同的限流操作，支持限流升级
 */
@Slf4j
@Component
public class FlowUtils {

    @Resource
    StringRedisTemplate template;

    /**
     * 针对于单次的频率限制
     * @param key   用于检查的key
     * @param blockTime  冷却时间
     * @return  是否通过限流检查
     */
    public boolean limitOnceCheck(String key,int blockTime){
        //如果缓存中有这个key，说明是正在冷却的状态
        if (Boolean.TRUE.equals(template.hasKey(key))){
            //用户被冷却时,不允许用户请求
            return false;
        }else {
            //缓存中没有这个key，就是不在冷却的状态，就往缓存中丢一个key
            //冷却时间为传入的，以秒为单位
            template.opsForValue().set(key,"请求冷却",blockTime, TimeUnit.SECONDS);
            return true;
        }
//        return this.internalCheck(key, 1, blockTime, (overclock) -> false);
    }

    /**
     * 针对于单次频率限制，请求成功后，在冷却时间内不得再次进行请求
     * 如3秒内不能再次发起请求，如果不听劝阻继续发起请求，将限制更长时间
     * @param key 键
     * @param frequency 请求频率
     * @param baseTime 基础限制时间
     * @param upgradeTime 升级限制时间
     * @return 是否通过限流检查
     */
    public boolean limitOnceUpgradeCheck(String key, int frequency, int baseTime, int upgradeTime){
        return this.internalCheck(key, frequency, baseTime, (overclock) -> {
            if (overclock)
                template.opsForValue().set(key, "1", upgradeTime, TimeUnit.SECONDS);
            return false;
        });
    }

    /**
     * 针对于在时间段内多次请求限制，如3秒内限制请求20次，超出频率则封禁一段时间
     * @param counterKey 计数键
     * @param blockKey 封禁键
     * @param blockTime 封禁时间
     * @param frequency 请求频率
     * @param period 计数周期
     * @return 是否通过限流检查
     */
    public boolean limitPeriodCheck(String counterKey, String blockKey, int blockTime, int frequency, int period){
        //判断是否通过了检查，如果返回为false
        return this.internalCheck(counterKey, frequency, period, (overclock) -> {
            if (overclock)
                template.opsForValue().set(blockKey, "", blockTime, TimeUnit.SECONDS);
            return !overclock;
        });
    }

    /**
     * 内部使用请求限制主要逻辑
     * @param key 计数键
     * @param frequency 请求频率
     * @param period 计数周期
     * @param action 限制行为与策略
     * @return 是否通过限流检查
     */
    private boolean internalCheck(String key, int frequency, int period, LimitAction action){
        //从缓存中获取key的计数
        String count = template.opsForValue().get(key);
        //如果不为空
        if (count != null) {
            //将计数增加1
            long value = Optional.ofNullable(template.opsForValue().increment(key)).orElse(0L);
            //获取之前得到的计数
            int c = Integer.parseInt(count);
            //判断计数增加1后是否与之前的计数新增相同
            if(value != c + 1)
                //重新设定该key的过期时间
                template.expire(key, period, TimeUnit.SECONDS);
            //返回该key的计数是否大于允许的请求频率
            return action.run(value > frequency);
        } else {
            //新增一个计数键
            template.opsForValue().set(key, "1", period, TimeUnit.SECONDS);
            return true;
        }
    }

    /**
     * 内部使用，限制行为与策略
     */
    private interface LimitAction {
        boolean run(boolean overclock);
    }

}
