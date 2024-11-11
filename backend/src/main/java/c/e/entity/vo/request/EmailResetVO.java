package c.e.entity.vo.request;

import jakarta.validation.constraints.Email;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 重置密码的参数信息实体类
 */
@Data
public class EmailResetVO {

    @Email
    String email;
    @Length(max = 6,min = 6)
    String code;
    @Length(max = 20,min = 6)
    String password;

}
