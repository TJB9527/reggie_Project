package com.bruce.reggie.common;

import com.bruce.reggie.entity.Employee;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用返回结果类，后端响应给前端的数据最终都会被封装成此对象
 * @param <T>
 */
@Data
public class Result<T> implements Serializable {
    private Integer code; //编码：1成功，0和其它数字为失败

    private String msg; //错误信息

    private T data; //数据

    private Map map = new HashMap(); //动态数据

    //响应成功：通过静态方法直接封装成功的Result对象
    public static <T> Result<T> success(T object) {
        Result<T> r = new Result<T>();
        r.data = object;
        r.code = 1;
        return r;
    }

    //响应失败：通过静态方法直接封装失败的Result对象
    public static <T> Result<T> error(String msg) {
        Result r = new Result();
        r.msg = msg;
        r.code = 0;
        return r;
    }

    //动态添加数据
    public Result<T> add(String key, Object value) {
        this.map.put(key, value);
        return this;
    }
}