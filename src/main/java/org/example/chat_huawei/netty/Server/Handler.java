package org.example.chat_huawei.netty.Server;

//import com.influxdb.query.FluxRecord;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.apache.juli.logging.Log;
import org.example.chat_huawei.entity.ChatMessages;
import org.example.chat_huawei.entity.MessageRecipients;
import org.example.chat_huawei.entity.OfflineMessage;
import org.example.chat_huawei.entity.User;
import org.example.chat_huawei.service.IChatMessagesService;
import org.example.chat_huawei.service.IMessageRecipientsService;
import org.example.chat_huawei.service.IOfflineMessageService;
import org.example.chat_huawei.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;

/**
 * 自定义处理器
 */


@Component

@Scope("prototype")//原型，声明为每次需要都创建新的
public class Handler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private final Logger log = Logger.getLogger(Handler.class.getName());

    // 存储用户id跟通道的映射表
    private static final HashMap<Integer, Channel> uidChannels = new HashMap<>();

    //存储发起者id跟对应的视频聊天房间的成员的id列表
    private static final HashMap<Integer, List<Integer>> userId_nameLink = new HashMap<>();

    //存储用户id跟其所在房间的映射
    private static final HashMap<Integer, Integer> idRoom = new HashMap<>();

    private final StringBuffer sb;

    public Handler() {
        sb = new StringBuffer();//缓冲区，每个实例都有其自己的缓冲区
        log.info("这是第" + uidChannels.size() + "个实例");

    }

    //图片的保存路径
    private String path = null;


    //自动装配所需的业务类
    //用户表
    @Autowired
    IUserService userInfoService;
    //接收者表
    @Autowired
    IMessageRecipientsService messageRecipientService;
    //离线信息表
    @Autowired
    IOfflineMessageService messageOfflineService;

    //消息主表
    @Autowired
    IChatMessagesService messageChatService;


    @Override//有消息时的处理操作
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
//        System.out.println(textWebSocketFrame.text());//以字符串显示,当文本信息时
        String text = textWebSocketFrame.text();

        //处理具有结束标识的信息
        if (text.endsWith(":MESSAGE_DATA_END!")) {
            //切割结束标识
            sb.append(text, 0, text.length() - 18);
            //获取总信息
            String result = sb.toString();
            //分情况进行信息处理
            dealMessage(result, channelHandlerContext);
//           channelHandlerContext.writeAndFlush(new TextWebSocketFrame(result));
            sb.setLength(0);//清空缓存
        } else {//往缓冲区扔数据
            sb.append(text);
        }
    }

    //连接建立的监听
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("有连接进来了");
    }

    //断开连接就从map中删除
    @Override//重写客户端断开连接的操作
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //遍历map删除用户
        for (Map.Entry<Integer, Channel> entry : uidChannels.entrySet()) {
            if (Objects.equals(entry.getValue(), ctx.channel())) {
                int id = entry.getKey();
                //关闭通道
                Channel remove = uidChannels.remove(id);
                if (remove != null) {
                    remove.close();
                }
                //日志记录
                //根据id获取用户信息
                User userInfoExt = userInfoService.getUserByid(id);
                log.info(userInfoExt.getUserName() + "已经断开连接");

                //解决重连bug
                dealOccupy(id);

//                //往在线时间表添加在线信息,influxdb的先不管
//                addDuration(entry.getKey());
                break;
            }
        }
    }

    @Override//重写发生异常时的exceptionCaught，处理异常
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 处理异常，打印堆栈信息并关闭通道
        // 记录异常信息
        log.info("Exception caught: " + cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }

    //influxdb的服务类
//    @Resource
//    InfluxdbUserLoginService influxdbUserLoginService;
//    @Resource
//    InfluxdbTimesOnlineService influxdbTimesOnlineService;

    //处理信息
    void dealMessage(String s, ChannelHandlerContext channelHandlerContext) {
//        log.info(s);
        //如果是注册消息就往hashmap中加入
        if (s.startsWith("REGISTER:")) {
            String userName = s.split(":")[1].trim();//拿到用户名
            int userId= Integer.parseInt(s.split(":")[2].trim());
            String clientType = s.split(":")[3].trim();//拿到客户端类型

//            //根据用户名获取用户id
//            int userId = userInfoService.getId(userName);

            //因为肯定是登录之后发的，所以只需要更新内存的通道就行
            //当内存中存在先关闭旧的连接
            if (uidChannels.containsKey(userId)) {
                uidChannels.get(userId).close();
            }
            //再更新uid跟通道建立映射,不需要返回欢迎登录了
            uidChannels.put(userId, channelHandlerContext.channel());

//            channelHandlerContext.writeAndFlush(new TextWebSocketFrame("userid:" + userId + ":" + "from 服务器:" + "欢迎登录 " + userName));//返回响应

            //先查询有无未接收的消息
            List<OfflineMessage> list = messageOfflineService.getOfflineMessageByName(userId);
            //有就接收离线消息
            if (list != null) {
                for (OfflineMessage offlineMessage : list) {
                    //获取消息主体跟消息类型
                    ChatMessages messageById = messageChatService.getMessageById(offlineMessage.getMessageId());
                    //接收离线文本消息
                    if (messageById.getMessageType().equals("文本")) {
                        //根据用户名获取用户id
                        int sourceId = userInfoService.getId(offlineMessage.getSourcename());
                        channelHandlerContext.writeAndFlush(
                                new TextWebSocketFrame("from:private_offline" + offlineMessage.getSourcename() + ":" + sourceId + ":" + messageById.getMessage()));
                    } else {//接收离线图片消息
                        //根据路径读取图片转化为base64格式发送
                        // 读取图片文件为字节数组
                        Path path = Paths.get(messageById.getMessage());
                        byte[] imageBytes = null;
                        try {
                            imageBytes = Files.readAllBytes(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        // 将字节数组编码为Base64字符串
                        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                        //经过测试发现，通过这个转化的消息是没有前缀标识，要加上"data:image/jpeg;base64,"，因为在保存图片时就截取了
                        //发送
                        channelHandlerContext.writeAndFlush(
                                new TextWebSocketFrame("from " + offlineMessage.getSourcename()
                                        + "的离线消息:" + "data:image/jpeg;base64," + base64Image));
                    }

                    //接收之后将该条未接收消息从表中删除
                    messageOfflineService.deleteOfflineMessage(offlineMessage.getId());

                }
            }

            //查询有无没结束的视频聊天
            if (idRoom.containsKey(userId)) {
                //向新客户端发送是否继续的请求
                channelHandlerContext.writeAndFlush(new TextWebSocketFrame("continue"));
            }

            log.info("用户几何：" + uidChannels.size());

//            //往influxdb中存储本次的登录消息
//            addUserLogin(userName);

        }
        //关于视频的消息处理
        else if (s.startsWith("VIDEO:")) {
            //处理视频消息
            dealVideoMessage(s, channelHandlerContext);
        } else {//正常的私发或者群发消息,根据消息格式不同进行不同操作
            //调用方法解析消息的用户名
            String[] split = s.split(":", 6);//最多返回6个，即只进行5次切割
            if (split.length == 6) {
                String explain = split[0].trim();//说明
                String userName = split[1].trim();//来源
                int userId = Integer.parseInt(split[2].trim());//来源id
                String target = split[3].trim();//目标name
                int targetId = Integer.parseInt(split[4].trim());//目标id
                String message = split[5];//消息


                //如果是图片消息先保存下来，目录结构为image/username/图片
                if (message.startsWith("data:image/")) {
                    int num = message.length() % 4;

                    // 解码Base64字符串,要删掉"data:image/jpeg;base64,"的前缀,一共23个字符，从第23位置切割就行
                    byte[] imageBytes = Base64.getDecoder().decode(message.substring(23));
                    // 保存图片到本地,保存位置image/userName/filename.jpg
                    path = getPath(userName);//获取保存图片的名字：当前时间.jpg
                    File file = new File(path);
                    // 确保文件路径的目录存在
                    File parentDir = file.getParentFile();
                    if (!parentDir.exists()) {
                        boolean created = parentDir.mkdirs();
                        if (!created) {
                            System.err.println("Failed to create directory: " + parentDir.getAbsolutePath());
                            return;
                        }
                    }
                    // 将解码后的字节写入文件
                    try {
                        Files.write(Paths.get(path), imageBytes);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                //处理消息
                if (explain.equals("Private")) {//私发
                    //保存该条消息,分类型保存
                    long messageId;
                    if (message.startsWith("data:image/")) {//图片保存
                        messageId = perserveMessage_Private(userId, path, "图片");
                    } else {//文本信息保存
                        messageId = perserveMessage_Private(userId, message, "文本");
                    }

                    //往接收者表中添加数据
                    addMessageRecipients(messageId, targetId, userId);


                    Channel channel = uidChannels.get(targetId);//获取对应的通道
                    if (channel != null) {
                        //转发消息,from_private:来源用户:来源id:message
                        channel.writeAndFlush(new TextWebSocketFrame("from:private:" + userName + ":" + userId + ":" + message));
                        //响应成功
                        channelHandlerContext.writeAndFlush(new TextWebSocketFrame("向" + target + ":" + "发送信息成功"));
                    } else {//不在线的处理逻辑，存储到离线消息表
                        //往离线消息表添加数据
                        messageOfflineService.addOfflineMessage(messageId, target, targetId, userName);
                        log.info("添加离线消息成功");
                        //返回响应消息
                        channelHandlerContext.writeAndFlush(new TextWebSocketFrame("From 服务器:" + target + " 不存在/不在线,已发送离线消息"));
                    }


                } else {//群发

                    //保存该条消息,分类型保存
                    long messageId;//消息主表的该条消息id
                    if (message.startsWith("data:image/")) {//图片保存
                        messageId = perserveMessage_Group(userId, target, path, "IMAGE");
                    } else {
                        messageId = perserveMessage_Group(userId, target, message, "TEXT");
                    }

//                    //先判断该用户有无加入该群组,需要吗？
//                    //获取群组id
//                    int groupId=groupService.getGroupByName(target).getId();
//                    //获取群组关联表中该群组的所有成员id列表
//                    List<GroupUserRelation> userIdList = groupUserRelationService.getUserIdList(groupId);
//
//                    //往该群组里面的所有用户传输消息
//                    //遍历id列表给每个用户发送消息
//                    for (GroupUserRelation groupUserRelation : userIdList) {
//                        int userId=groupUserRelation.getUserId();
//                        //根据用户id获取对应的用户名
//                        User userByid = userService.getUserByid(userId);
//                        //往接收者表中添加数据
//                        addMessageRecipients(messageId, userId);
//                        //发送消息
//                        if (uidChannels.containsKey(userId)) {//往在线的用户发送信息
//                            uidChannels.get(userId).writeAndFlush(new TextWebSocketFrame("From " +target+"的"+ userName + ":" + message));
//                        } else {//对方不在线
//                            //往离线消息表添加数据
//                            offlineMessageService.addOfflineMessage(messageId, userByid.getUserName(), userId, userName);
//                            //返回响应消息
//                            channelHandlerContext.writeAndFlush(new TextWebSocketFrame("From 服务器:" + userByid.getUserName() + " 不存在/不在线"));
//                        }
//                    }
                }
            } else {
                channelHandlerContext.writeAndFlush(new TextWebSocketFrame("from 服务器:消息格式有问题！"));
            }
        }
    }


    //保存历史信息的方法
    //私发的保存,返回新加的消息主表id,传入发送者id，消息，消息类型
    long perserveMessage_Private(int userId, String message, String message_type) {
        //获取来源跟目标的id

        ChatMessages chat = new ChatMessages();
        //私发的对于消息主表只需要发送者id，消息跟类型就行，群组的id为空
        chat.setSenderId(userId);
        chat.setMessage(message);
        chat.setMessageType(message_type);
        //发送时间默认
        //必需
        chat.setDelFlag("01");
        messageChatService.addChatMessage(chat);//添加
        //获取消息主表的id，mybatisplus会回填到对象中
        return chat.getId();
    }

    //群发的保存:发送者id，发送的群组，消息，消息类型
    long perserveMessage_Group(int userId, String target, String message, String message_type) {

//        //获取群组的id
//        int group_id=groupService.getId(target);
//        MessageChat chat=new MessageChat();
//        //群发的对于消息主表只需要发送者id，消息跟类型，群组的id
//        chat.setSenderId(BigInteger.valueOf(userId));
//        chat.setMessage(message);
//        chat.setMessageType(message_type);
//        chat.setGroupId(group_id);
//        chatMessagesService.addChatMessage(chat);//添加
//        return chat.getId();
        return 1;
    }

    //接收者表添加记录:消息主键，接收者id,发送者id
    void addMessageRecipients(long messageId, int userId, int sendId) {
        MessageRecipients mr = new MessageRecipients();
        mr.setMessageId(messageId);
        mr.setReceiveId(userId);
        mr.setSenderId(sendId);
        //必需删除标记位
        mr.setDelFlag("01");
        messageRecipientService.addIMessageRecipients(mr);//添加
    }

    //获取保存图片的路径
    public String getPath(String userName) {
        LocalDateTime now = LocalDateTime.now();
        // 格式化日期时间到毫秒,不能用:跟空格
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss_SSS");
        String formattedDateTime = now.format(formatter);
//        System.out.println(formattedDateTime);
        return "image/" + userName + "/" + formattedDateTime + ".jpg";//返回保存图片的路径
    }


    /**
     * influxdb相关
     *
     * @param s
     * @param ctx
     */

//    //往influxdb中保存当前的登录信息
//    void addUserLogin(String userName){
//        //根据用户名获取id
//        int id = userService.getId(userName);
//        //获取最近一次登录记录
//        FluxRecord userLoginLast = influxdbUserLoginService.getUserLoginLast(userName, String.valueOf(id));
//        //登录的次数
//        long count=1;
//        if(userLoginLast!=null){
//            Object lastcount = userLoginLast.getValueByKey("_value");
//            //更新登录次数
//            count= (long)lastcount + 1;
//            log.info("用户累计登录次数："+String.valueOf(count));
//        }
//        //往用户登录表添加信息
//        influxdbUserLoginService.addUserLogin(userName,id,count);
//    }
//
//    //往在线时间表添加在线信息
//    void addDuration(int id){
//        //根据uid获取用户信息,用户名跟id
//        User userByUid = userService.getUserByid(id);
//        //获取最近一次登录记录
//        FluxRecord userLoginLast = influxdbUserLoginService.getUserLoginLast(userByUid.getUserName(), String.valueOf(id));
//        //拿到登录时间
//        Instant time = userLoginLast.getTime();
//
//        //获取当前时间
//        Instant now = Instant.now();
//        //计算登录时间到现在的持续时间
//        Duration duration = Duration.between(time, now);
////        System.out.println(duration);
//        //格式化持续时间
//        String durationStr = "%d天%d时%d分%d秒%d毫秒 ".formatted(duration.toDays(),
//                duration.toHours()%24,duration.toMinutes()%60,
//                duration.toSeconds()%60,duration.toMillis()%1000);
//        log.info("该用户本次登录时间为："+durationStr);
//        //往在线时间表中插入
//        influxdbTimesOnlineService.addTimesOnlne(userByUid.getUserName(),userByUid.getId(),durationStr,time);
//
//
//    }

    //视频消息的处理
    void dealVideoMessage(String s, ChannelHandlerContext ctx) {
        //去掉video:
        String substring = s.substring(6);

        //分情况处理
        //开始邀请的消息：VIDEO:start:userId:username:groupname:targetnames
        if (substring.startsWith("start")) {
            String[] split = substring.split(":", 5);
            int userId = Integer.parseInt(split[1]);
            String userName = split[2];
            String groupName = split[3];
            String targetLink = split[4];

            //如果当前的视频聊天房间没有发起者就先添加
            if (!userId_nameLink.containsKey(userId)) {
                List<Integer> list = new ArrayList<>();
                list.add(userId);
                userId_nameLink.put(userId, list);

                //同步维护该发起者跟所在房间的map
                idRoom.put(userId, userId);
            }
            //然后遍历targetLink分别发送邀请通话请求
            String[] names = targetLink.split(",");
            for (String name : names) {
                //根据用户名获取对应的id发送
                int id = userInfoService.getId(name);
                //该聊天房间没有这个人就发
                if (!isHaving(id, userId)) {
                    Channel channel = uidChannels.get(id);//获取对应的通道

                    //保存该条邀请消息
                    String message = "request_call:" + groupName + ":" + userName + ":" + userId;
                    long messageId = perserveMessage_Private(userId, message, "文本");

                    if (channel != null) {
                        //转发消息,根据消息格式返回request_call:群组name:发起人name
                        channel.writeAndFlush(new TextWebSocketFrame(message));
                        //响应成功
                        ctx.writeAndFlush(new TextWebSocketFrame("向" + name + ":" + "发送邀请成功"));
                    } else {//不在线的处理逻辑，存储到离线消息表
                        //往离线消息表添加数据
                        messageOfflineService.addOfflineMessage(messageId, name, id, userName);
                        log.info("添加离线消息成功");
                        //返回响应消息
                        ctx.writeAndFlush(new TextWebSocketFrame("From 服务器:" + name + " 不存在/不在线"));
                    }
                } else {
                    //返回响应消息
                    ctx.writeAndFlush(new TextWebSocketFrame("From 服务器:" + name + " 已经在聊天房间了"));
                }

            }
            //返回当前房间人数
            List<Integer> list = userId_nameLink.get(userId);
            returnSum(list);

        }
        //接受邀请的消息，格式  VIDEO`:accept:`startId:acceptId:acceptName
        else if (substring.startsWith("accept")) {
            String[] split = substring.split(":", 4);
            int startId = Integer.parseInt(split[1]);
            int acceptId = Integer.parseInt(split[2]);
            String acceptName = split[3];

            //判断当前用户是否已经存在通话房间
            if (!idRoom.containsKey(acceptId)) {
                //同步维护该接收者跟所在房间的map
                idRoom.put(acceptId, startId);

                //从存储视频聊天的房间里选择该id标识的所有成员id
                if (userId_nameLink.containsKey(startId)) {
                    List<Integer> list = userId_nameLink.get(startId);
                    log.info(list.toString());

                    //向成员列表广播新用户要进来
                    radioWelcome(list, acceptName);

                    //返回当前聊天房间的成员列表：return_idLink:当前对应视频聊天室的成员id列表（直接返回list）
                    ctx.writeAndFlush(new TextWebSocketFrame("return_idLink:" + list.toString()));
                    //把该接受邀请者也添加到聊天房间
                    list.add(acceptId);
                    //看看有无新加
                    log.info(userId_nameLink.get(startId).toString());

                    //向所有参与者返回当前房间人数
                    returnSum(list);

                } else {
                    log.info("该发起者不存在");
                    ctx.writeAndFlush(new TextWebSocketFrame("frow 服务器:该通话已经结束!"));
                }
            } else {
                ctx.writeAndFlush(new TextWebSocketFrame("只能同时存在一个视频聊天！"));
            }

        }
        //挂断消息
        else if (substring.startsWith("leave")) {
            String[] split = substring.split(":", 3);
            int userId = Integer.parseInt(split[1]);
            String userName = split[2];
            //广播离开
            radioLeave(userId, userName);

        }
        //同意重连的信息,格式 VIDEO:continue:userId:userName
        else if (substring.startsWith("continue")) {
            String[] split = substring.split(":", 3);
            int userId = Integer.parseInt(split[1]);
            String userName = split[2];

            //获取该用户对应的房间
            int roomId = idRoom.get(userId);
            //获取对应房间的参与者列表
            List<Integer> users = userId_nameLink.get(roomId);

            //注意这个房间是包含该userId的，但是我们需要返回的应该是不包含的，先删除返回，再添加
            //获取新建一个list返回,或者在jsnb+不是自己的再新建webrtc连接都行
            //根据包装类删除list中的自己
            users.remove(Integer.valueOf(userId));
            String message = "return_idLink:" + users.toString();
            ctx.writeAndFlush(new TextWebSocketFrame(message));
            //再添加回去
            users.add(userId);

            //向所有参与者返回当前房间人数
            returnSum(users);
        }
        //其他交换控制信息的消息，格式 Private:userId:targetId:message
        //返回的消息格式：control:来源id:message
        else {
            String[] split = substring.split(":", 4);
            String userId = split[1];
            String targetId = split[2];
            //message就包括的描述跟具体消息
            String message = split[3];
//
            //向目标uid转发消息,不存在不用管因为这时控制信息的交换，不用存储
            if (uidChannels.containsKey(Integer.parseInt(targetId))) {
                Channel channel = uidChannels.get(Integer.parseInt(targetId));
                channel.writeAndFlush(new TextWebSocketFrame("control:" + userId + ":" + message));
            } else {
                log.info(userInfoService.getUserByid(Integer.parseInt(userId)).getUserName() + "不在线");
            }
        }
    }

    //根据id判断该用户是否在参与者列表
    boolean isHaving(int id, int startId) {
        List<Integer> integers = userId_nameLink.get(startId);
        for (int i : integers) {
            if (i == id) return true;
        }
        return false;
    }

    //向房间参与者列表广播新成员
    void radioWelcome(List<Integer> list, String acceptName) {
        if (list != null && !list.isEmpty()) {
            for (int i : list) {
                Channel channel = uidChannels.get(i);
                if (channel != null) {
                    channel.writeAndFlush(new TextWebSocketFrame("from 服务器: " + acceptName + "正在进入通话房间..."));
                }
            }
        }
    }

    //向房间参与者列表广播成员离开
    void radioLeave(int leaveId, String leaveName) {
        //获取对应房间的标识
        int roomId = idRoom.get(leaveId);
        //获取对应房间的参与者
        List<Integer> users = userId_nameLink.get(roomId);
        //将该参与者从参与者列表删除,以及从存储idRoom中删除
        if (!users.isEmpty()) {
            //注意使用包装类删除,直接传入是根据索引删除
            users.remove(Integer.valueOf(leaveId));
            //当最后一个人离开房间就删除房间
            if (users.isEmpty()) {
                userId_nameLink.remove(roomId);
            }
        }
        idRoom.remove(leaveId);

        //获取删除之后的人数
        int sum = users.size();

        //向剩下的参与者广播该用户离开的消息
        if (!users.isEmpty()) {
            for (int i : users) {
                Channel channel = uidChannels.get(i);
                if (channel != null) {
                    channel.writeAndFlush(new TextWebSocketFrame("from 服务器: " + leaveName + "已经离开通话房间..."));
                    //告知人数
                    channel.writeAndFlush(new TextWebSocketFrame("sum:" + sum));
                }
            }
        }
    }

    //向参与者返回当前房间统计人数
    void returnSum(List<Integer> list) {
        int sum = list.size();
        if (list != null && !list.isEmpty()) {
            for (int i : list) {
                Channel channel = uidChannels.get(i);
                if (channel != null) {
                    channel.writeAndFlush(new TextWebSocketFrame("sum:" + sum));
                }
            }
        }
    }

    //解决重连bug:一直占用房间的问题
    void dealOccupy(int id){
        //如果存在房间
        if(idRoom.containsKey(id)){
            int roomId = idRoom.get(id);
            //获取对应房间参与者列表
            List<Integer> list = userId_nameLink.get(roomId);
            //判断房间是否有效
            boolean effective = isEffective(list);
            //房间无效进入清除模式
            if(!effective){
                log.info("该房间:"+roomId+"无效，已经清除");
                //先清除房间
                userId_nameLink.remove(roomId);
                //再清除idRoom的映射
                if(list != null && !list.isEmpty()){
                    for (int i : list) {
                        idRoom.remove(i);
                    }
                }
            }

        }
    }
    //判断房间是否有效
    boolean isEffective(List<Integer> list){
        if(list != null && !list.isEmpty()){
            for (int i : list) {
                if(uidChannels.containsKey(i)){
                    //如果房间存在活动的客户端就有效
                    return true;
                }
            }
            return false;
        }
        else {
            return false;
        }
    }
}
