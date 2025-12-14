package com.mint.caizhitong.common.resp;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Result<T> {
    //0成功，1失败
    private Integer code;
    //提示信息
    private String message;
    //响应数据
    private T data;
    //返回操作成功响应结果（带响应数据）
    public static <E> Result<E> success(E data) {return new Result<>(0,"ok",data);}
    //无响应数据
    public static Result success(){return new Result(0,"ok",null);}
    //返回操作失败响应结果
    public static Result error(String message) {return new Result(1,message,null);}

    public static Result error40001(String m) {return new Result(40001,m,null);}
    public static Result error40003(String m) {return new Result(40003,m,null);}
    public static Result error40004(String m) {return new Result(40004,m,null);}
    public static Result error50000(String m) {return new Result(50000,m,null);}
}
