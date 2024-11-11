package c.e.service;

import c.e.entity.dto.Account;
import c.e.entity.vo.request.ConfirmResetVO;
import c.e.entity.vo.request.EmailRegisterVO;
import c.e.entity.vo.request.EmailResetVO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * 用户登录相关
 * UserDetailsService 接口用于定义从数据库中加载用户信息的服务。
 * Spring Security 使用该接口来获取用户的认证信息（例如用户名、密码、权限），从而用于身份验证和权限管理。
 */

public interface AccountService extends IService<Account>, UserDetailsService {

    //查询用户信息
    Account findAccountByNameOrEmail(String text);

    //注册邮件验证码
    String registerEmailVerifyCode(String type,String email,String ip);

    //注册用户
    String registerEmailAccount(EmailRegisterVO vo);

    //验证重置密码的验证码是否正确
    String resetConfirm(ConfirmResetVO vo);

    //重置密码操作
    String resetEmailAccountPassword(EmailResetVO vo);

}
