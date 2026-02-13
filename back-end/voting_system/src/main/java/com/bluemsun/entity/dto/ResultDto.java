package com.bluemsun.entity.dto;

import lombok.Data;

@Data
public class ResultDto<T> {
    /**
     *  响应码：
     *      2xx：成功
     *      4xx：请求错误
     *      5xx：后端错误
     */
    private Boolean result;   // 响应码
    private String  msg;    // 响应信息
    private T data;         // 响应数据

    public ResultDto(Boolean result, String msg, T data) {
        this.result = result;
        this.msg = msg;
        this.data = data;
    }

    public ResultDto() {
    }
}
