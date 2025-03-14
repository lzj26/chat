package org.example.chat_huawei.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;

import org.example.chat_huawei.entity.User;
import org.example.chat_huawei.mapper.UserMapper;
import org.example.chat_huawei.service.IUserService;
import org.example.chat_huawei.util.Encryption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static net.sf.jsqlparser.util.validation.metadata.NamedObject.user;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lzj
 * @since 2024-10-15
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private UserMapper userMapper;


    @Override
    public User findByName(String name) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", name);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public int addUser(String name,String password,String email) {
        System.out.println("新增用户："+name);
        User user = new User();
        user.setUserName(name);
        //加密后插入密码
        user.setPassword(Encryption.hashPassword(password));
        user.setEmail(email);
        int num=userMapper.insert(user);//插入
        if(num==1){//插入成功返回该用户id
            return user.getId();
        }
        else {//插入失败
            return -1;
        }
    }

    @Override
    public int getId(String name) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", name)
                .select("id");
        return userMapper.selectOne(queryWrapper).getId();
    }

    @Override
    public User getUserByid(int id) {
        return userMapper.selectById(id);
    }

    @Override
    public List<User> getUserLink(String keyWord) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        //模糊查询名字
        queryWrapper.like("user_name",keyWord);
        List<User> users = userMapper.selectList(queryWrapper);

        QueryWrapper<User> queryWrapper2 = new QueryWrapper<>();
        //模糊查询邮箱
        queryWrapper2.like("email",keyWord);
        List<User> users1 = userMapper.selectList(queryWrapper2);
        users.addAll(users1);
        return users;
    }

    @Override
    public User getUserByEmail(String email) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email",email);

        return userMapper.selectOne(queryWrapper);
    }

}
