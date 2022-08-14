package com.bruce.reggie.common;

/**
 * 自定义业务异常
 */
public class CustomException extends RuntimeException{
    //构造方法
    public CustomException(String s) {
        super(s);
    }
}
