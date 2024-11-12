package c.e.service.impl;

import c.e.entity.dto.Account;
import c.e.entity.vo.request.ConfirmResetVO;
import c.e.entity.vo.request.EmailRegisterVO;
import c.e.entity.vo.request.EmailResetVO;
import c.e.mapper.AccountMapper;
import c.e.service.AccountService;
import c.e.utils.Const;
import c.e.utils.FlowUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;


/**
 * 用户登录相关的
 */
@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    //验证邮件发送冷却时间限制，秒为单位
    @Value("${spring.web.verify.mail-limit}")
    int verifyLimit;

    //消息中间件
    @Resource
    AmqpTemplate amqpTemplate;

    //Redis
    @Resource
    StringRedisTemplate stringRedisTemplate;

    //限流工具
    @Resource
    FlowUtils utils;

    //加密工具
    @Resource
    PasswordEncoder encoder;

    /**
     * 从数据库中通过用户名或邮箱查找用户详细信息
     * @param username  用户名
     * @return  用户详细信息
     * @throws UsernameNotFoundException  如果用户未找到则抛出此异常
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //查询用户信息
        Account account = this.findAccountByNameOrEmail(username);
        //判断有没有拿到用户数据,没拿到的话就抛出异常
        if (account == null)
            throw new UsernameNotFoundException("用户名或密码错误");
        return User
                .withUsername(username)
                .password(account.getPassword())
                .roles(account.getRole())
                .build();
    }

    /**
     * 生成注册验证码存入Redis中，并将邮件发送请求提交到消息队列等待发送
     * @param type   类型
     * @param email   邮件地址
     * @param ip   请求IP地址
     * @return  操作结果，null表示正常，否则为错误原因
     */
    @Override
    public String registerEmailVerifyCode(String type, String email, String ip) {
        //检查用户是否被封禁,其实这样检测是线程不安全的，如果压力测试同时进入100条数据，将会出问题
        //这里需要加个线程锁，防止同一请求被多次调用
        synchronized (ip.intern()){
            if (!this.verifyLimit(ip))
                //如果用户没有通过验证
                return "请求频繁，请稍后再试";
            //生成随机的验证码
            Random random = new Random();
            int code = random.nextInt(899999) + 100000;
            //将要丢入消息队列的消息用map存储起来
            Map<String,Object> data = Map.of("type",type,"email",email,"code",code);
            //丢入消息队列
            amqpTemplate.convertAndSend(Const.MQ_MAIL,data);
            //还要在redis中存储验证码,3分钟有效
            stringRedisTemplate.opsForValue()
                    .set(Const.VERIFY_EMAIL_DATA + email,String.valueOf(code),3, TimeUnit.MINUTES);
            return null;
        }
    }

    /**
     * 邮件验证码注册账号操作，需要检查验证码是否正确以及邮箱、用户名是否存在重名
     * @param vo  用户注册信息实体类
     * @return  是否注册成功，null为正常，否则为错误原因
     */
    @Override
    public String registerEmailAccount(EmailRegisterVO vo) {
        //查询一下，确保用户名和邮箱都是唯一的
        String email = vo.getEmail();
        String username = vo.getUsername();
        //直接获取redis中的验证码
        String key = Const.VERIFY_EMAIL_DATA +email;
        String code = stringRedisTemplate.opsForValue().get(key);
        if (code == null) return "请先获取验证码";
        if (!code.equals(vo.getCode())) return "验证码输入错误，请重新输入";
        //判断邮件是否被注册过
        if (this.existsAccountByEmail(email)) return "此电子邮件已被其他用户注册";
        if (this.existsAccountByUsername(username)) return "此用户名已被其他人注册，请更换一个新的用户名";
        String password = encoder.encode(vo.getPassword());
        Account account = new Account(null,username,password,email,Const.ROLE_DEFAULT,new Date());
        if (this.save(account)) {
            stringRedisTemplate.delete(key);
            return null;
        }else {
            return "内部错误，请联系管理员";
        }

    }

    /**
     * 重置密码的确认操作，验证验证码是否正确
     * @param vo   重置密码的请求验证码实体类
     * @return  操作结果，null表示正常，否则为错误原因
     */
    @Override
    public String resetConfirm(ConfirmResetVO vo) {
        //从缓存中取出验证码，判断用户输入的验证码跟缓存是不是相同的验证码
        String email = vo.getEmail();
        String code = stringRedisTemplate.opsForValue().get(Const.VERIFY_EMAIL_DATA + email);
        if (code == null)  return "请先获取验证码";
        if (!code.equals(vo.getCode())) return  "验证码错误,请重新输入";
        if (!this.existsAccountByEmail(email)) return "该邮箱没有用户,请先注册一个账号吧！";
        //没有问题就返回空
        return null;
    }

    /**
     * 重置密码操作
     * @param vo  重置密码的参数信息实体类
     * @return  操作结果，null表示正常，否则为错误原因
     */
    @Override
    public String resetEmailAccountPassword(EmailResetVO vo) {
        String email = vo.getEmail();
        //验证一下验证码是否正确
        String verify = this.resetConfirm(new ConfirmResetVO(email,vo.getCode()));
        //如果不为空，说明验证正确
        if (verify != null) return verify;
        String password = encoder.encode(vo.getPassword());
        //更新密码操作
        boolean update = this.update().
                eq("email",email).set("password",password).update();
        //判断是否更新成功
        if (update){
            //成功就删除缓存中的验证码
            stringRedisTemplate.delete(Const.VERIFY_EMAIL_DATA + email);
        }
        return null;
    }

    /**
     * 通过邮件判断用户是否存在
     * @param email  注册邮箱
     * @return  是否存在
     */
    private boolean existsAccountByEmail(String email){
        return this.baseMapper.exists(Wrappers.<Account>query().eq("email",email));
    }

    /**
     * 通过用户名判断用户是否存在
     * @param username  注册的用户名
     * @return  该用户名是否已经存在
     */
    private boolean existsAccountByUsername(String username){
        return this.baseMapper.exists(Wrappers.<Account>query().eq("username",username));
    }

    /**
     * 查询用户信息
     * @param text  查询的用户信息，要么是用户名要么是邮箱,先这样把后面需要了再回来修改
     * @return  查询出用户的信息
     */
    public Account findAccountByNameOrEmail(String text){
        return this.query()
                //只支持邮箱登录，所以把用户名pass掉
                .eq("email",text).or()
                .eq("username",text)
                .one();
    }

    /**
     * 检查用户是否被封禁
     * @param ip  用于生成唯一的key
     * @return  是否允许用户使用
     */
    private boolean verifyLimit(String ip){
        String key = Const.VERIFY_EMAIL_LIMIT + ip;
        return utils.limitOnceCheck(key,verifyLimit);
    }



}
