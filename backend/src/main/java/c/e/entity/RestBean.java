package c.e.entity;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;

/**
 *
 * @param code  状态码
 * @param data  传递的数据
 * @param message   通知信息
 * @param <T>
 */
public record RestBean<T>(int code,T data,String message) {

    //处理成功返回
    public static <T> RestBean<T> success(T data){
        return new RestBean<>(200,data,"请求成功");
    }

    public static <T> RestBean<T> success(){
        return success(null);
    }

    //处理失败返回
    //失败一般没有数据，只有请求码
    public static <T> RestBean<T> failure(int code,String message){
        return new RestBean<>(code,null,message);
    }

    //未登录时
    public static <T> RestBean<T> unauthorized(String message){
        return failure(401,message);
    }
    //权限不够时
    public static <T> RestBean<T> forbidden(String message){
        return failure(403,message);
    }

    //将传递进来的值变成JSON字符串的形式返回回去
    public String asJsonString(){
        return JSONObject.toJSONString(this, JSONWriter.Feature.WriteNulls);
    }

}
