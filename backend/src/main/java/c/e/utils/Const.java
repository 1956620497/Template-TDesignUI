package c.e.utils;

//配置属性
public class Const {

    //jwt黑名单
    public static final String JWT_BLACK_LIST = "jwt:blacklist:";
    //
    public final static String JWT_FREQUENCY = "jwt:frequency:";

    //邮件验证码相关
    //限流，防止用户频繁请求注册邮件
    public static final String VERIFY_EMAIL_LIMIT = "verify:email:limit:";
    //存储邮件验证码
    public static final String VERIFY_EMAIL_DATA = "verify:email:data:";

    //请求频率限制相关
    //限流，指定ip的计数器
    public static final String FLOW_LIMIT_COUNTER = "flow:counter:";
    //限流，封禁ip的标志
    public static final String FLOW_LIMIT_BLOCK = "flow:block:";

    //跨域过滤器优先级
    public static final int ORDER_CORS = -102;
    //限流过滤器优先级
    public static final int ORDER_LIMIT = -101;

    //邮件消息队列
    public final static String MQ_MAIL = "mail";

    //请求自定义属性
    public final static String ATTR_USER_ID = "userId";

    //用户角色
    public final static String ROLE_DEFAULT = "user";

}
