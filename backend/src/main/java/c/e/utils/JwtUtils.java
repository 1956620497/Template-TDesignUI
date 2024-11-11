package c.e.utils;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 构建JWT的工具类
 */
@Component
public class JwtUtils {

    //加密秘钥
    @Value("${spring.security.jwt.key}")
    String key;

    //过期时间
    @Value("${spring.security.jwt.expire}")
    int expire;

    @Resource
    StringRedisTemplate template;

    //让token令牌失效
    public boolean invalidateJwt(String headerToken){
        //判断token是否合法
        String token = this.convertToken(headerToken);
        if (token == null) return false;
        //加密算法
        Algorithm algorithm = Algorithm.HMAC256(key);
        //解密jwt
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        //拉黑令牌,把令牌存到redis中
        try{
            //解析jwt，拿到解析出来的jwt的id
            DecodedJWT jwt = jwtVerifier.verify(token);
            String id = jwt.getId();
            //返回拉黑的结果
            return deleteToken(id,jwt.getExpiresAt());
        }catch (JWTVerificationException e){
            return false;
        }
    }

    /**
     * 拉黑JWT,设置一个过期时间，过期时间与jwt的过期时间相同
     * @param uuid  jwt的uuid
     * @param time  jwt过期的剩余时间
     * @return   是否拉黑成功
     */
    private boolean deleteToken(String uuid,Date time){
        //判断是否已经失效
        if (this.isInvalidToken(uuid))
            return false;
        //拿到现在的时间
        Date now = new Date();
        //计算一下具体的失效时间
        // 有个问题，如果jwt令牌已经失效，拿这个jwt来退出的话会产生一个负值
        //所以拿0和减出来的失效时间来比较，如果小于0就说明该令牌已经过期了
        long expire = Math.max(time.getTime() - now.getTime(),0);
        //存入redis
        template.opsForValue().set(Const.JWT_BLACK_LIST + uuid,"退出用户",expire, TimeUnit.MILLISECONDS);
        return true;
    }

    //判断令牌是否已经被拉黑了
    private boolean isInvalidToken(String uuid){
        //查询该令牌是否已经被拉黑了
        return Boolean.TRUE.equals(template.hasKey(Const.JWT_BLACK_LIST + uuid));
    }

    //创建JWT令牌
    public String createJwt(UserDetails details,int id,String username){
        //要在JWT令牌中塞入用户信息什么的,所以先从UserDetails读取用户信息
        //UserDetails里面没有用户ID，只有用户的名字
        //加密算法
        Algorithm algorithm = Algorithm.HMAC256(key);
        //过期时间
        Date expire = expireTime();
        //返回并构建jwt令牌
        return JWT.create()
                //添加一个jwtID
                .withJWTId(UUID.randomUUID().toString())
                //填入id
                .withClaim("id",id)
                //填入用户名
                .withClaim("name",username)
                //填入权限
                .withClaim("authorities",details.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                //设置过期时间
                .withExpiresAt(expire)
                //token颁发时间
                .withIssuedAt(new Date())
                //把算法传进来签名
                .sign(algorithm);

    }

    //计算jwt令牌过期时间
    public Date expireTime(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR,expire * 24);
        return calendar.getTime();
    }

    //解析出原来的Token，并验证该token是否合法
    public DecodedJWT resolveJwt(String headerToken){
        //判断token是否合法
        String token = this.convertToken(headerToken);
        if (token == null) return null;
        //加密算法
        Algorithm algorithm = Algorithm.HMAC256(key);
        //解密jwt
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try{
            //验证jwt是否被用户篡改过,如果被篡改过会抛一个验证异常
            DecodedJWT jwt = jwtVerifier.verify(token);
            //判断jwt令牌是否被拉黑失效了
            if (this.isInvalidToken(jwt.getId()))
                return null;
            //判断令牌是否过期,获取过期的日期
            Date expiresAt = jwt.getExpiresAt();
            //判断现在是否已经超过这个过期的日期了  超过了返回空，没超过返回解析后的jwt
            return new Date().after(expiresAt) ? null : jwt;

            //运行时异常，不会显式的抛出，只能自己去捕获
        }catch (JWTVerificationException e) {
            //如果捕获到异常，就是修改过了，直接验证失败
            return null;
        }
    }

    //从jwt中解析出信息
    public UserDetails toUser(DecodedJWT jwt){
        //取出用户信息
        Map<String,Claim> claims = jwt.getClaims();
        //还原为UserDetails
        return User
                .withUsername(claims.get("name").asString())
                .password("******")
                .authorities(claims.get("authorities").asArray(String.class))
                .build();
    }

    //将用户id解析出来
    public Integer toId(DecodedJWT jwt){
        //取出用户信息
        Map<String,Claim> claims = jwt.getClaims();
        return claims.get("id").asInt();
    }

    //判断token是否合法
    private String convertToken(String headerToken){
        if (headerToken == null || !headerToken.startsWith("Bearer "))
            return null;
        //返回token并且切割前7位
        return headerToken.substring(7);
    }

}
