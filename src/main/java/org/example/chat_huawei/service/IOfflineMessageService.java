package org.example.chat_huawei.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.chat_huawei.entity.OfflineMessage;


import java.util.List;


/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lzj
 * @since 2024-10-22
 */
public interface IOfflineMessageService extends IService<OfflineMessage> {
    //往离线表添加记录,传入消息id，待接收者名字，待接收者的id，跟消息的发送者
    int addOfflineMessage(long messageId,String name,int id,String sourcename);

    //按待接收的用户id查询有无未接收的消息
    List<OfflineMessage> getOfflineMessageByName(int id);

    //根据id删除该条未接收消息
    int deleteOfflineMessage(int id);

}
