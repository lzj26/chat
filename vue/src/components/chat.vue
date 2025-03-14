<script>
import Header from "@/components/header.vue";


import EmojiPicker from 'vue3-emoji-picker';

import 'vue3-emoji-picker'; // 有些库会自动引入样式

import {path} from '../assets/store.js'
import store from '../assets/store.js'
import Confirm from './confirm.vue'

export default {
  props: ['id', 'name', 'type'],
  data() {
    return {
      newMessage: '',
      // messages: [
      //     //就是以一个信息列表存储信息，每条信息都是对象,对象包含了发生时间，是否自己跟类型信息
      //   { text: '欢迎来到聊天室！', time: new Date(), own: false, type:'TEXT' }
      // ],
      showEmojiPicker: false, // 控制表情包选择器显示/隐藏
      confirm: false,//控制弹窗的出现
      word: '是否要向对方发起语音通话？',
    };
  },
  computed:{//计算属性
    messages(){//返回store里面的消息列表
      let messages=store.getters.getMessageMap
      console.log(messages)
      if(messages[this.id]){
        console.log("历史消息为")
        console.log(messages[this.id])
        //调整视角
        this.scrollToBottom();
        return messages[this.id].messages;
      }
      else {
        return [];
      }
    },
    //获取websocket连接对象
    websocket(){
      return store.getters.getWs
    },
    userName(){
      return store.getters.getUserName
    },
    userId(){
      return store.getters.getUserId
    },
    audio(){
      return store.getters.getAudio;
    }
  },
  // watch: {
  //   //当audio发生改变就触发
  //   audio(newaudio){//括号就是新值
  //     if(newaudio){//如果有新消息就触发
  //       this.$refs.notificationSound.play().catch(error => {
  //         console.warn('播放声音失败:', error);
  //       });
  //       //改变状态
  //       store.commit('SET_AUDIO')
  //     }
  //   }
  // },
  methods: {
    //进来先设置当前的聊天对象id
    setCurrentUserId(){
      store.commit('setCurrentUserId',this.id)
    },
    //点击就清除与该好友的未读消息
    cleanNuread(){
      store.commit('CLEAR_UNREAD_COUNT',this.id);
    },
    //图片上传
    handleFileChange(event) {
      const file = event.target.files[0];
      if (file) {
        let message={
          fileName: file.name,
          //这样就类似img标签的src属性，可以展示图片
          text: URL.createObjectURL(file), // 创建临时URL来预览图片或文件
          time: new Date(),
          own: true,
          type: file.type.startsWith('image') ? 'IMAGE' : 'FILE'
        }
        //添加记录
        store.dispatch('ADD_CHAT_MESSAGE',{userId:this.id,type:this.type,message:message})

        this.scrollToBottom();

        //发送图片
        this.sendImage(file);
      }
    },
    //发送图片信息
  sendImage(file) {
    // console.log(file);
    let reader = new FileReader();
    //这个一个触发器，当reader的读取完成之后触发的动作,使用箭头函数保持this能用
    reader.onloadend =  ()=> {
      if (this.websocket.readyState === WebSocket.OPEN) {//如果连接状态==打开

        console.log("准备发送")
        //分块发送
        let MaxSize = 65518;//设置64kB的块大小,注意算上18字节的结束标识符
        let array = [];   //设置存储分块的数组
        let len = reader.result.length;
        for (let i = 0; i < len; i += MaxSize) {//切割
          //切割数据
          array.push(reader.result.slice(i, i + MaxSize));
        }
        //分块发送
        //先读取并发送头部消息
        //获取当前的发送类型
        let send_type='Private';//先写死私发
        //获取当前的目标,//格式：Private:userName:userId:targetName:targetId:message
        let header = send_type + ":" + this.userName + ":" + this.userId + ":" + this.name + ":"+this.id+":";
        console.log(header)
        this.websocket.send(header);//发送头部消息
        let arrayLen = array.length;
        for (let j = 0; j < arrayLen; j++) {//发送
          console.log("发送分片：" + j);
          //发送数据
          this.websocket.send(array[j]);//base64编码之后发送的也是TextWebSocketFrame文本帧
          // if(j===0) console.log(array[j])
        }
        //最后带着标识符发送最后一次
        //发送结束数据
        this.websocket.send(":MESSAGE_DATA_END!");//base64编码之后发送的也是TextWebSocketFrame文本帧

        console.log("发送成功")
      } else {
        console.log('未连接');
      }
    };
    //开始读取将选择的图片以base64的编码读取到reader中，注意触发器要写在前面！！！
    reader.readAsDataURL(file);
  },
    sendMessage() {//发送信息
      if (this.newMessage.trim()) {
        //调用websocket发送信息
        console.log("websocket")
        console.log(this.websocket)
        if(this.websocket && this.websocket.readyState === WebSocket.OPEN){//连接存在就发送

          //格式：Private:userName:userId:targetName:targetId:message
          let send_type='Private';//先写死私发
          let text=send_type + ":" + this.userName + ":" + this.userId + ":" + this.name + ":" +this.id+":"+ this.newMessage + ":MESSAGE_DATA_END!"
          this.websocket.send(text);
          //发送了
          console.log("发送了")
        }
        else {
          alert("与服务器连接丢失，websocket连接断开");
        }

        //添加聊天信息到store中
        let chat={ text: this.newMessage, time: new Date(), own: true, type:'TEXT' };
        store.dispatch('ADD_CHAT_MESSAGE',{userId: this.id,message:chat,type: this.type });

        console.log(this.messages);
        this.newMessage = '';
        this.scrollToBottom();
      }
    },
    scrollToBottom() {
      setTimeout(() => {
        //保存视角在最下
        this.$refs.messagesContainer.scrollTop = this.$refs.messagesContainer.scrollHeight+50;
      }, 10);
    },
    formatTime(dateString) {
      // 尝试将字符串转换为 Date 对象
      const date = new Date(dateString);

      // 检查转换是否成功
      if (isNaN(date.getTime())) {
        console.error('Invalid date string provided:', dateString);
        return 'Invalid Date'; // 或者你可以选择抛出错误或返回其他默认值
      }

      // 格式化时间
      return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    },
    openEmojiPicker(){
      this.showEmojiPicker = !this.showEmojiPicker;
    },
    // 插入表情到输入框
     insertEmoji(emoji){
       console.log(emoji);
      // 更新消息内容，在光标位置插入表情
      this.newMessage= this.newMessage + emoji.i
      // 关闭表情选择器
      this.showEmojiPicker = false;
    },
    //初始化查询历史聊天
    //获取跟用户的聊天记录
    getOldChats(){
      let url=null;
      if(this.type ==='01'){
        url=path+"/messageRecipients/getAllChats"
      }
      else {
        // url=//群组
      }

      //获取用户名跟id
      let userId=store.getters.getUserId;
      //获取当前的目标
      let data={
      userId: userId,
      friendId: this.id,
      type: this.type
    };
    //使用fetch函数发送
      fetch(url,{
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(data)
  })
      .then(res=>res.json())
      .then(response=>{
        if(response.code===200){
          console.log('获取了历史聊天')

          //展示历史聊天
          let chats=response.data.messages
          // console.log(chats)
          if(chats !=null){
            let history=[];//存储与该好友的消息列表
            chats.forEach(chat=>{
              //对方的信息
              if(chat.sendName===this.name){
                history.push({own: false,text: chat.message,type:  chat.messageType,time: chat.sendTime,
                status:chat.readStatus})
              }
              else {//自己的
                history.push({own: true,text: chat.message,type:  chat.messageType,time: chat.sendTime,
                  status:chat.readStatus})
              }
            })
            //调用store里面的方法添加历史聊天记录
            console.log("添加了聊天记录")
            store.commit('SET_CHAT_HISTORY',{userId: this.id,history:history})
          }

        }

      })
      .catch(error=>{
        throw error;
      })
},
    //点击通话
    startVideoCall(){
      if(!this.confirm){
        this.confirm=true;
      }
      else {
        alert("已经发起，请等待！")
      }
    },
    //向对方发送申请通话邀请并跳转通话页面
    sendCallInfo(){
      //关闭弹窗
      this.confirm=false;
      //向服务器发送开始邀请通话消息,这里的群组先写死。注意拼接结束信息,VIDEO:start:userId:username:groupname:targetid列表
      let message = "VIDEO:start:" + this.userId + ":" + this.userName + ":" + "group1" + ":" + this.id + ":MESSAGE_DATA_END!";
      console.log(message)
      //发送
      if(this.websocket && this.websocket.readyState === WebSocket.OPEN){
        this.websocket.send(message);
        console.log("向"+this.name+"发送了邀请信息")
        //改变通话状态为等待接通中
        store.commit('CHANGE_CALLING','waiting')
        //将video设置为true
        store.commit('CHANGE_VIDEO',true);
        //跳转通话组件
        this.$router.push({
          name: 'videoCall',
              query: { id: this.id,name:this.name,type:this.type }
        })
      }
      else {
        alert("与服务器连接丢失，websocket连接断开");
      }

    }
  },
  //挂载阶段加载
  mounted() {
    this.getOldChats();
    this.cleanNuread();
    this.setCurrentUserId();
    this.scrollToBottom();
  },
  components: {
    Header,
    EmojiPicker,
    Confirm,
  }
};
</script>

<template>
  <Header />
  <div class="chat-container">
    <header class="chat-header">
      <h2>{{ name }}</h2>
    </header>
    <main class="chat-messages" ref="messagesContainer">
      <div v-for="(message, index) in messages" :key="index" class="message" :class="{ 'own-message': message.own }">
        <div class="message-content">
          <span v-if="message.type === 'TEXT'" class="message-text">{{ message.text }}</span>
          <img v-else-if="message.type === 'IMAGE'" :src="message.text" alt="Image" class="message-image" />
          <a v-else-if="message.type === 'FILE'" :href="message.text" target="_blank" class="message-file">{{ message.fileName }}</a>
          <span class="message-time">{{ this.formatTime(message.time) }}</span>
        </div>
      </div>
    </main>
    <footer class="chat-input">
      <input type="text" v-model="newMessage" @keyup.enter="sendMessage" placeholder="输入消息..." />
      <button @click="sendMessage">发送</button>
    </footer>
    <footer class="chat-input-footer">
      <input type="file" accept="image/*" ref="fileInput" @change="handleFileChange" style="display: none;" />

      <!-- 文件上传按钮 -->
      <button class="action-button upload-button" @click="$refs.fileInput.click()">
        <img src="/icon/upload.png" alt="上传" class="icon">
      </button>

      <!-- 表情选择按钮 -->
      <button class="action-button emoji-button" @click="openEmojiPicker">
        <img src="/icon/emoji.png" alt="表情" class="icon">
      </button>

      <!-- 视频通话按钮 -->
      <button class="action-button video-call-button" @click="startVideoCall">
        <img src="/icon/telephone.png" alt="视频通话" class="icon">
      </button>
    </footer>

    <!-- 表情包选择器 (可选) -->
    <EmojiPicker v-if="showEmojiPicker" @select="insertEmoji" />
      <!-- 这里放置表情包选择逻辑或使用第三方库vue -->

  </div>
<!--  传递数据进弹窗-->
  <Confirm  :visible="confirm" :message="word"
            @confirm="sendCallInfo"
  @cancel="()=>{this.confirm=false}"/>
</template>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 85vh;
  width: 400px;
  margin: 0 auto;
  border: 1px solid #ccc;
  border-radius: 5px;
  overflow: hidden;
  background-color: #fff;
}

.chat-header {
  background-color: #007bff;
  color: white;
  padding: 10px;
  text-align: center;
}

.chat-messages {
  flex: 1;
  padding: 10px;
  overflow-y: auto;
  background-color: #f9f9f9;
}

.message {
  margin-bottom: 10px;
  padding: 8px 12px;
  border-radius: 10px;
  max-width: 80%;
  position: relative; /* 添加相对定位 */
}

.message-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.message-text {
  font-size: 16px;
  word-wrap: break-word; /* 允许长单词或URL地址换行到下一行 */
  white-space: pre-wrap; /* CSS 2.1 特性, 支持保留空白符并且换行 */
  overflow-wrap: break-word; /* CSS3 特性, 同word-wrap */
  max-width: 100%; /* 确保文本宽度不超过容器宽度 */
}

.message-time {
  font-size: 12px;
  color: #888;
  margin-top: 4px; /* 给时间和消息内容之间增加一点间距 */
}

.own-message {
  background-color: #d1e7dd;
  align-self: flex-end;
}

.own-message .message-text {
  color: #0f5132;
}

.message-image {
  max-width: 100%; /* 图片最大宽度为容器宽度 */
  height: auto; /* 自动调整高度以保持宽高比 */
  border-radius: 5px; /* 可选：给图片添加圆角 */
  margin-top: 8px; /* 根据需要调整图片与文本之间的间距 */
}

/* 新增样式，确保时间戳在图片下方显示 */
.message-content img + .message-time {
  margin-top: 8px; /* 调整时间戳与图片之间的间距 */
}

.chat-input {
  display: flex;
  padding: 10px;
  background-color: #f8f9fa;
  border-top: 1px solid #ddd;
  justify-content: space-between;
}

.chat-input input {
  flex: 1;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 5px;
  margin-right: 10px;
}

.chat-input button {
  padding: 10px 20px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.chat-input button:hover {
  background-color: #0056b3;
}

.chat-input-secondary {
  display: flex;
  padding: 10px;
  background-color: #f8f9fa;
  border-top: 1px solid #ddd;
  justify-content: flex-end;
}

.chat-input-secondary button {
  margin-left: 10px;
}

.button-container {
  display: flex;
  align-items: center;
  gap: 1rem; /* 按钮之间的间距 */
}

.custom-file-upload, .emoji-button {
  background-color: #007bff ; /* 绿色背景 */
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.3s ease;
}

.custom-file-upload:hover, .emoji-button:hover {
  background-color: #0056b3; /* 鼠标悬停时的颜色变化 */
}

.custom-file-upload i, .emoji-button i {
  margin-right: 5px;
}

.chat-input-footer {
  display: flex;
  align-items: center;
  justify-content: space-around; /* 按钮均匀分布 */
  padding: 10px;
  background-color: #f8f9fa; /* 深色背景 */
  border-top: 1px solid #007bff;
}

.action-button {
  background-color: #007bff; /* 按钮背景颜色 */
  color: white;
  border: none;
  border-radius: 50%; /* 圆形按钮 */
  width: 40px;
  height: 40px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.action-button:hover {
  background-color: #0056b3; /* 鼠标悬停时的颜色变化 */
}

.action-button i {
  font-size: 18px;
}

.icon {
  width: 24px; /* 根据需要调整图片大小 */
  height: auto;
}

</style>