package com.bruce.reggie.common;

/**
 * 基于ThreadLocal封装工具类，维护上下文使用的同一个ThreadLocal对象，基于该对象维护一个线程变量，保存当前登录员工id
 */
public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 向线程中设置值
     * @param id
     */
    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    /**
     * 向线程中获取值
     * @return
     */
    public static Long getCurrentId() {
        return threadLocal.get();
    }
}
