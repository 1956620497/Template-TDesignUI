package c.e.listener;

import c.e.utils.Const;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 消费者
 * 邮件消费队列
 * 监听mail队列，如果有消息进来时，对该消息进行消费
 */
@Component
@RabbitListener(queues = Const.MQ_MAIL)
public class MailQueueListener {

    //邮件发送框架引入
    @Resource
    JavaMailSender sender;

    @Value("${spring.mail.username}")
    String username;

    //处理邮件发送
    //发送时用什么类型存储的这里就用什么类型接收
    @RabbitHandler
    public void sendMailMessage(Map<String,Object> data){
        //取出邮件信息
        String email = (String) data.get("email");
        Integer code = (Integer) data.get("code");
        String type = (String) data.get("type");
        //处理邮件类型
        SimpleMailMessage message = switch (type){
            case "register" ->
                    createMessage("欢迎注册我的网站",
                            "您的邮件注册验证码为:" + code + ",有效时间3分钟，为了保证您账号的安全，请勿向他人泄露验证码信息。"
                            ,email);
            case "reset" ->
                    createMessage("您的密码重置邮件",
                            "您正在进行重置密码操作，验证码:" + code + ",有效时间3分钟，如非本人操作，请无视此邮件。"
                            ,email);
            default -> null;
        };
        if (message == null) return;
        //发送邮件
        sender.send(message);
    }



    //构建邮件
    private SimpleMailMessage createMessage(String title,String content,String email){
        //构建邮件对象
        SimpleMailMessage message = new SimpleMailMessage();
        //邮件标题
        message.setSubject(title);
        //邮件内容
        message.setText(content);
        //发送目标
        message.setTo(email);
        //发送者
        message.setFrom(username);
        return message;
    }

}
