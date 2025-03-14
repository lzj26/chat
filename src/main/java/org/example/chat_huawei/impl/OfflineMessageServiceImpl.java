package org.example.chat_huawei.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;

import org.example.chat_huawei.entity.OfflineMessage;
import org.example.chat_huawei.mapper.OfflineMessageMapper;
import org.example.chat_huawei.service.IOfflineMessageService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lzj
 * @since 2024-10-22
 */
@Service
public class OfflineMessageServiceImpl extends ServiceImpl<OfflineMessageMapper, OfflineMessage> implements IOfflineMessageService {

    @Resource
    private OfflineMessageMapper offlineMessageMapper;

    @Override
    public int addOfflineMessage(long messageId, String name, int uid,String sourcename) {
        OfflineMessage offlineMessage = new OfflineMessage();
        offlineMessage.setMessageId(messageId);
        offlineMessage.setUserId(uid);
        offlineMessage.setUsername(name);
        offlineMessage.setSourcename(sourcename);
        return   offlineMessageMapper.insert(offlineMessage);//插入

    }

    @Override
    public List<OfflineMessage> getOfflineMessageByName(int id) {
        QueryWrapper<OfflineMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", id);
        return offlineMessageMapper.selectList(queryWrapper);
    }

    @Override
    public int deleteOfflineMessage(int id) {
        return offlineMessageMapper.deleteById(id);
    }
}
