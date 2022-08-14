package com.bruce.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理
 */
@Slf4j
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
public class GlobalExceptionHandler {

    /**
     * SQL异常---处理方法
     * @param ex
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<String> exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        log.error(ex.getMessage()); //日志打印异常信息

        //SQLIntegrityConstraintViolationException异常是一个异常大类，需要根据实际报错类型对应返回结果给前端
        if (ex.getMessage().contains("Duplicate entry")) {
            String[] split = ex.getMessage().split(" ");
            return Result.error(split[2] + "已存在");   //split[2]即报错时指示的重复添加的账号
        }
        return Result.error("添加失败");
    }

    /**
     * 自定义异常---处理方法
     * @param ex
     * @return
     */
    @ExceptionHandler(CustomException.class)
    public Result<String> customExceptionHandler(CustomException ex) {
        log.error(ex.getMessage());
        return Result.error(ex.getMessage());
    }
}
