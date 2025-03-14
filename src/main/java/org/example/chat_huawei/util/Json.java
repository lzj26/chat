package org.example.chat_huawei.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

//@Slf4j

public class Json {
   private static Logger log = Logger.getAnonymousLogger();
    /**
     *
     * @Description: 读取请求内容
     *先把request的内容转化为为字符流读取
     */
    public static StringBuilder getStringBuilder(HttpServletRequest request)  {
        //将字节流转化为字符流
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(request.getInputStream(), "utf-8"));
            String line;
            StringBuilder sb = new StringBuilder();
            //把字符流读取到StringBuilder中
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     *
     * @Title: getJsonMap
     * @Description: 将Post流传入的JSON参数转化为Map
     */
   public static Map<String, Object> getJsonMap(HttpServletRequest request)  {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            //获取请求的字符流
            StringBuilder sb = getStringBuilder(request);
            //判断是否为空，并且是不是只包含空字符串
            if (null != sb && !sb.toString().trim().isEmpty()) {
                //调用 Jackson 库来直接将字符串转化为json对象
                ObjectMapper mapper = new ObjectMapper();
                //就是将字符串-->Map<String, Object>,new TypeReference<要转化的类型>()
                map = mapper.readValue(sb.toString(), new TypeReference<Map<String, Object>>() {
                });
            }
        } catch (Exception e) {
            // 记录异常堆栈信息
            log.info("解析请求数据异常：" + e.getMessage());

        }
        return map;
    }

    /**
     * 获取map中的参数
     */
    public static String getJsonString(Map<String, Object> map, String key)  {
        if(map.containsKey(key)) {
            return String.valueOf(map.get(key));
        }
        else return null;
    }
    public static int getJsonInt(Map<String, Object> map, String key)  {
        if(map.containsKey(key)) {
            return Integer.parseInt(String.valueOf(map.get(key)));
        }
        //返回一个最小值
        else return Integer.MIN_VALUE;

    }


}
