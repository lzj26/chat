package org.example.chat_huawei.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.chat_huawei.entity.User;


import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lzj
 * @since 2024-10-15
 */
public interface IUserService extends IService<User> {
    //查询该用户名是否存在
    User findByName(String name);

    //往用户表里面新增记录
    int addUser(String name,String password,String email);

    //根据用户名查id
    int getId(String name);


    //根据id查用户消息
    User getUserByid(int id);

    //模糊查询
    List<User> getUserLink(String keyWord);

    //根据邮箱查询用户信息
    User getUserByEmail(String email);


}
