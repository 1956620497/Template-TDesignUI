package c.e.entity.dto;

import c.e.entity.BaseData;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

/**
 * 用户信息实体类
 */
@Data
@TableName("db_account")
@AllArgsConstructor
public class Account implements BaseData {

    @TableId(type = IdType.AUTO)
    Integer id;
    String username;
    String password;
    String email;
    //用户角色
    String role;
    //注册时间
    Date register;

}
