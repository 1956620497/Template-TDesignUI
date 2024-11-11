package c.e.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



/**
 * 配置消息队列
 */

@Configuration
public class RabbitConfiguration {

    //发送邮件的消息队列
    @Bean("emailQueue")
    public Queue emailQueue(){
        return QueueBuilder
                //队列名
                .durable("mail")
                .build();
    }


    //将消息转换器注入容器中
    //将AMQP消息转换为JSON格式进行序列化和反序列化
    @Bean
    public MessageConverter jackson2JsonMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }

}
