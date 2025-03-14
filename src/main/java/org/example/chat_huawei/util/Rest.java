package org.example.chat_huawei.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;

import java.util.HashMap;

@Data
public class Rest {
    private int code;//状态码
    HashMap<String,Object> data;//返回数据
    private String message;//描述信息

    private Rest(int code, HashMap<String,Object> data, String message) {//构造方法
        this.code = code;
        this.data = data;
        this.message = message;
    }

    //构造方法工具类
    public static Rest success(HashMap<String,Object> data) {
        return new Rest(200, data, "响应成功");
    }
    public static Rest success(HashMap<String,Object> data,String message) {
        return new Rest(200, data, message);
    }
    //成功时,加static<T>变成类方法
    public static Rest success(String message) {
        return new Rest(200, null, message);
    }

    //失败时
    public static Rest fail(int code,String message) {
        return new Rest(code, null, message);
    }
    //失败时
    public static Rest fail(String message) {
        return new Rest(400, null, message);
    }
    //将当前对象转换为JSON格式的字符串用于返回，先导依赖
    public String ToJsonString() {
        return JSON.toJSONString(this);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public HashMap<String, Object> getData() {
        return data;
    }

    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
