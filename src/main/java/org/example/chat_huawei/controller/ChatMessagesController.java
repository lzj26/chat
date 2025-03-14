package org.example.chat_huawei.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.chat_huawei.entity.User;
import org.example.chat_huawei.service.IChatMessagesService;
import org.example.chat_huawei.service.IUserService;
import org.example.chat_huawei.util.Json;
import org.example.chat_huawei.util.Rest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 消息主表 前端控制器
 * </p>
 *
 * @author lzj
 * @since 2024-12-9
 */
@RestController
@RequestMapping("/chatMessages")
//@CrossOrigin(origins = "http://localhost:5173") // 指定允许的前端域名
public class ChatMessagesController {

}
