package c.e.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * web服务相关配置
 */
@Configuration
public class WebConfiguration {

    /**
     * 密码加密器
     * @return  加密后的字符串
     */
    @Bean
    BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}
