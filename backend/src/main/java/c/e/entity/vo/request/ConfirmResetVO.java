package c.e.entity.vo.request;


import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 重置密码的请求验证码实体类
 */
@Data
@AllArgsConstructor
public class ConfirmResetVO {

    @Email
    String email;
    @Length(max = 6,min = 6)
    String code;

}
