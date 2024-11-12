package c.e.controller;


import c.e.entity.RestBean;
import c.e.entity.vo.request.ConfirmResetVO;
import c.e.entity.vo.request.EmailRegisterVO;
import c.e.entity.vo.request.EmailResetVO;
import c.e.service.AccountService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 注册登录相关
 */
@Validated
@RestController
@RequestMapping("/api/auth")
@Tag(name="登录校验相关",description = "包括用户登录、注册、验证码请求等操作。")
public class AuthorizeController {

    @Resource
    AccountService service;

    /**
     * 请求邮件验证码
     * @param email   请求邮件的邮箱
     * @param type  邮件类型
     * @param request  请求
     * @return   是否请求成功
     */
    @GetMapping("/ask-code")
    public RestBean<Void> askVerifyCode(@RequestParam @Email String email,
                                        @RequestParam @Pattern(regexp = "(register|reset)") String type,
                                        HttpServletRequest request){
        //执行发送邮件任务
//        String message = service.registerEmailVerifyCode(type,email,request.getRemoteAddr());
        //判断一下邮件是否发送成功
//        return message == null ? RestBean.success() : RestBean.failure(400,message);
        //优化代码
        return this.messageHandle(() -> service.registerEmailVerifyCode(type,email,request.getRemoteAddr()));
    }

    /**
     * 用户注册
     * @param vo  用户注册信息
     * @return  是否注册成功
     */
    @PostMapping("/register")
    public RestBean<Void> register(@RequestBody @Valid EmailRegisterVO vo){
        return this.messageHandle(() -> service.registerEmailAccount(vo));
    }

    /**
     * 验证重置密码的验证码是否正确
     * @param vo  密码重置信息
     * @return   是否操作成功
     */
    @PostMapping("/reset-confirm")
    public RestBean<Void> resetConfirm(@RequestBody @Valid ConfirmResetVO vo){
        // 旧方法
//        return this.messageHandle(() -> service.resetConfirm(vo));
        //新方法
        return this.messageHandle(vo,service::resetConfirm);
    }

    /**
     * 修改密码操作
     * @param vo  密码重置信息
     * @return  是否操作成功
     */
    @PostMapping("/reset-password")
    public RestBean<Void> resetPassword(@RequestBody @Valid EmailResetVO vo){
        return this.messageHandle(vo,service::resetEmailAccountPassword);
    }

    /**
     * 再次优化代码
     * @param vo    数据对象
     * @param function   执行的方法
     * @return   执行结果
     * @param <T>  任何类型都有可能返回
     */
    private <T> RestBean<Void>  messageHandle(T vo, Function<T,String> function){
        return messageHandle(() -> function.apply(vo));
    }

    /**
     * 优化一下代码
     * 针对于返回值为String作为错误信息的方法进行统一处理
     * @param action  需要调用的代码，也就是具体操作
     * @return 调用结果
     */
    private RestBean<Void> messageHandle(Supplier<String> action){
        //获取调用的方法
        String message = action.get();
        //执行并且返回结果
        return message == null ? RestBean.success() : RestBean.failure(400,message);
    }



}
