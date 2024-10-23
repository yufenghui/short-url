package com.yufenghui.shorturl.common;

import lombok.Data;

import java.io.Serializable;

/**
 * Response
 *
 * @author yufenghui
 * @date 2024/10/23 13:30
 */
@Data
public class Response<T> implements Serializable {

    public static final Integer SUCCESS = 200;
    public static final Integer ERROR = 500;
    public static final Integer BAD_REQUEST = 400;

    private Integer code;
    private String message;
    private T data;

    public static <T> Response<T> succeed(T data) {
        Response<T> response = new Response<>();
        response.setCode(SUCCESS);
        response.setData(data);
        return response;
    }

    public static <T> Response<T> fail(String message) {
        Response<T> response = new Response<>();
        response.setCode(ERROR);
        response.setMessage(message);
        return response;
    }

}
