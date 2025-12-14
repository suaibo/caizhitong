package com.mint.caizhitong.common.exception;


import cn.dev33.satoken.exception.NotRoleException;
import com.mint.caizhitong.common.resp.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result exceptionHandler(Exception e){
        e.printStackTrace();
        // 传递异常的message，如果不为空，否则使用固定信息
        String msg = e.getMessage() != null && !e.getMessage().isEmpty() ? e.getMessage() : "服务器内部错误";
        return Result.error50000(msg);
    }

    @ExceptionHandler(NotRoleException.class)
    @ResponseBody
    public Result notRoleException(NotRoleException e){
        e.printStackTrace();
        //返回无权限信息
        return Result.error40003("权限不足，缺少角色：" + e.getRole());
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseBody
    public Result resourceNotFoundException(ResourceNotFoundException e){
        e.printStackTrace();
        //返回无权限信息
        return Result.error40004(e.getMessage());
    }

    @ExceptionHandler(BusinessConflictException.class)
    @ResponseBody
    public Result businessConflictException(BusinessConflictException e){
        e.printStackTrace();
        //返回无权限信息
        return Result.error40001(e.getMessage());
    }

}
