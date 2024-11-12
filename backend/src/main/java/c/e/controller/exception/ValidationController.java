package c.e.controller.exception;

import c.e.entity.RestBean;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 自定义接口校验不通过而反馈的信息
 * 处理异常，并返回一个自定义的
 */
@Slf4j
@RestControllerAdvice
public class ValidationController {


    /**
     * 处理验证接口参数不正确时反馈为中文
     * 与SpringBoot的错误信息保持一致，校验不通过先打印警告信息，而不是直接抛出异常
     * @param exception   验证异常
     * @return   校验结果
     */
    @ExceptionHandler(ValidationException.class)
    public RestBean<Void> validateException(ValidationException exception){
        log.warn("Resolve [{} : {}]",exception.getClass().getName(),exception.getMessage());
        return RestBean.failure(400,"请求参数有误");
    }

}
