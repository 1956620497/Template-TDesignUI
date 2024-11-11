package c.e.entity.vo.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class EmailRegisterVO {

    @Email
    @Length(min = 4)
            @NotEmpty
    String email;
    @Length(max = 6,min = 6)
    String code;
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9]+$")
            @Length(min = 1,max = 15)
    String username;
    @Length(max = 20,min = 6)
    String password;

}
