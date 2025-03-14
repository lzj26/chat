package org.example.chat_huawei.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

public class FileDeal {

    //读取文件转化为base64字符串,携带前缀
    public static String encodeFileToBase64(File file,String path) throws IOException {
        // 读取文件字节
        byte[] fileContent = Files.readAllBytes(file.toPath());

        // 使用 Base64 编码这些字节
        String text= Base64.getEncoder().encodeToString(fileContent);
        //添加前缀
        String type=path.substring(path.indexOf(".")+1);
        return "data:image/"+type+";base64,"+text;
    }
}
