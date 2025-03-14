<script>
import Header from "./header.vue"
import store from '../assets/store.js'
import {path} from '../assets/store.js'
export default {
  data() {
    return {
      localStream: null,
      peerConnection: null,
      id_name: {},//存储id_用户名映射
      openVideo: '开启摄像头',
      videoStatu: false,
      // status: 'waiting', // 'calling', 'waiting', 'connecting'
    };
  },
  props: ['id', 'name', 'type'],
  components: {
    Header,
  },
  methods: {
    toggleCall(){//取消拨打
      //发送挂断信号清除房间
      this.hangUp();

      //重置通话状态
      store.commit('CHANGE_CALLING',null)
      //改变video的状态
      store.commit('CHANGE_VIDEO',false);
      this.$router.push({
        name: 'Chat',
        query: { id: this.id,name:this.name,type:this.type }
      })
    },
    getUserInfo(id){
      //先发送请求查询用户的头像，姓名,
      console.log("type:"+this.type)
      let  data={
        type: this.type,
        userId:id
      }
      fetch(path+"/user/getInfo",{
        method: "POST",
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
      })
          .then(response=>response.json())
          .then(res=>{
            if(res.code === 200 && res.data !=null){
              this.id_name[id]=res.data.name;
            }
            else {
              this.id_name[id]='未知';
            }
          })
          .catch(error=>{
            throw error;
          })
    },
    //挂断函数
    end() {
      //清空连接
      store.dispatch('cleanRtcs')

      //发送挂断信息
      this.hangUp();
      //跳转聊天页面
      this.$router.push({
        name: 'Chat',
        query: { id: this.id,name:this.name,type:this.type }
      })
    },
    hangUp() {
      //向服务器发送挂断信号,VIDEO:leave:userId:userName
      let message = "VIDEO:leave:" + this.userId + ":" + this.userName + ":MESSAGE_DATA_END!";
      if(this.websocket && this.websocket.readyState === WebSocket.OPEN){
        this.websocket.send(message);
      }
      else {
        alert("与服务器连接丢失，websocket连接断开");
      }
    },
    //切换开启摄像头
    changeVideo(){
      //改变字体
      this.videoStatu=!this.videoStatu;
      this.openVideo=this.videoStatu?'关闭摄像头':'开启摄像头';

      //调用store里面的更改函数
      store.dispatch('updateAnddConnet',this.videoStatu);
    }

  },
  watch:{
    videoStreams: {
      //处理函数
      handler(newVal, oldVal) {
        console.log("New value:", newVal);
        console.log("Old value:", oldVal);

        // 如果有两个对象了，说明视频流传输完成，可以展示
        const count = Object.keys(newVal).length;
        console.log("视频流对象数量：" + count);

        if (count >= 2) {
          // 改变状态为正在通话
          store.commit('CHANGE_CALLING', 'calling');
          //请求姓名
          for (const key in newVal){
            if(!this.id_name[key]){
              this.getUserInfo(key);
            }
          }
        }
      },
      deep: true, // 添加 deep 选项来监听对象内部的变化
      immediate: false // 根据需要决定是否在初始时立即触发一次监听器
    }
  },
  computed:{
    //返回通话状态
    status(){

      return store.getters.getCalling;
    },
    video(){
      console.log("video为")
      console.log(store.getters.getVideo)
      return store.getters.getVideo
    },
    //返回视频流集合
    videoStreams(){
      console.log("视频流对象")
      console.log(store.getters.getVideoStreams)
      return store.getters.getVideoStreams
    },
    userId(){
      return store.getters.getUserId
    },
    userName(){
      return store.getters.getUserName
    },
    websocket(){
      return store.getters.getWs
    },
  },
  mounted() {
    console.log("type:"+this.type)
  },
  beforeDestroy() {
    this.endCall();
  }
};
</script>

<template>
  <Header />
  <div class="video-call-container">
    <!-- 等待接通界面 -->
    <div v-if="this.status === 'waiting'" class="waiting-screen">
      <div class="waiting-message">等待对方接通...</div>
      <button class="hangup-button" @click="toggleCall">取消呼叫</button>
    </div>
      <!-- 等待连接界面 -->
      <div v-else-if="this.status === 'connecting'" class="waiting-screen">
        <div class="waiting-message">等待连接中...</div>
      </div>

    <!-- 视频通话界面 -->
    <div v-else class="video-content">
      <!-- 视频流容器 -->
      <div
          v-for="(stream, Id) in videoStreams"
          :key="Id"
          :class="[
      'video-container',
      {'local-video': Id == userId},
      {'remote-video': Id != userId}
    ]"
      >
        <div class="video-wrapper">
          <video
              :srcObject.prop="stream"
              autoplay
              controls
              class="video-element"
          ></video>
          <div class="user-info">
            <span class="user-id">{{ id_name[Id] }}</span>
            <div class="status-indicator"></div>
            <button
                v-if="Id == userId"
                @click="changeVideo"
                class="video-toggle-button"
                :class="{'video-active': isVideoActive}"
            >
              <svg-icon icon="camera-video" class="button-icon" />
              <span class="button-text">{{ openVideo }}</span>
            </button>
          </div>
        </div>
      </div>

      <!-- 控制按钮 -->
      <div class="control-bar">
        <button
            class="control-button hangup-button"
            @click="end"
        >
          <svg-icon icon="phone-slash" class="button-icon"/> <!-- 需要图标库支持 -->
          挂断
        </button>
      </div>
    </div>
    </div>
</template>

<style scoped>
.video-call-container {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f0f0f0;
}

/* 等待接通屏幕 */
.waiting-screen {
  text-align: center;
  padding: 20px;
  border-radius: 10px;
  background-color: white;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  /* 使用 flex 布局来垂直居中 */
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 300px; /* 设置最小高度，根据实际情况调整 */
  width: 300px; /* 可以根据需要调整这个值 */
  max-width: 90%; /* 确保在小屏幕上也能正常显示 */
  position: absolute; /* 使用绝对定位 */
  top: 50%; /* 上边距为视窗高度的一半 */
  left: 50%; /* 左边距为视窗宽度的一半 */
  transform: translate(-50%, -50%); /* 使用transform属性进行精确居中 */
}

/* 确保消息和按钮有足够的空间 */
.waiting-message {
  font-size: 1.5em;
  margin-bottom: 20px;
  color: #333; /* 文字颜色调整 */
}

.hangup-button {
  background-color: #ff4d4d;
  color: white;
  border: none;
  padding: 10px 20px;
  font-size: 1em;
  cursor: pointer;
  border-radius: 5px;
  transition: background-color 0.3s ease;
}

.hangup-button:hover {
  background-color: #ff1a1a;
}

.waiting-message {
  font-size: 1.5em;
  margin-bottom: 20px;
  color: #333; /* 文字颜色调整 */
}

/* 基础布局 */
.video-content {
  position: relative;
  min-height: 100vh;
  background: #1a1a1a;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 16px;
  padding: 20px;
}

/* 通用视频容器样式 */
.video-container {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(0,0,0,0.2);
  transition: transform 0.3s ease;
  background: #333;
}

/* 悬停动效 */
.video-container:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0,0,0,0.3);
}

/* 远程视频样式 */
.remote-video {
  flex: 1 1 45%;
  max-width: 800px;
  min-width: 300px;
  aspect-ratio: 16/9;
}

/* 本地视频样式 */
.local-video {
  position: fixed;
  bottom: 20px;
  right: 20px;
  width: 240px;
  height: 135px;
  z-index: 10;
  border: 2px solid rgba(255,255,255,0.1);
}

/* 视频元素 */
.video-element {
  width: 100%;
  height: 100%;
  object-fit: cover;
  background: #000;
}

/* 用户信息栏 */
.user-info {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px;
  background: linear-gradient(transparent, rgba(0,0,0,0.7));
  color: white;
  display: flex;
  align-items: center;
}

.user-id {
  font-size: 14px;
  font-weight: 500;
  margin-right: 8px;
}

.status-indicator {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #4CAF50;
  box-shadow: 0 0 8px #4CAF50;
}

/* 控制栏样式 */
.control-bar {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 20px;
  background: rgba(255,255,255,0.1);
  padding: 12px 24px;
  border-radius: 30px;
  backdrop-filter: blur(8px);
}



.hangup-button {
  background: #ff4444;
  color: white;
}

.hangup-button:hover {
  background: #ff3333;
  transform: scale(1.05);
  box-shadow: 0 4px 16px rgba(255,68,68,0.3);
}



.control-button {
  /* 新增以下属性确保完全居中 */
  display: inline-flex;
  justify-content: center;
  align-items: center;
  /* 修正后的padding */
  padding: 12px 20px;
  /* 确保高度统一 */
  min-height: 48px;
}

/* 如果使用自定义图标 */
.button-icon {
  /* 确保图标与文字对齐 */
  position: relative;
  top: -1px; /* 微调图标位置 */
}

/* 添加文字包装保证对齐 */
.control-button span {
  line-height: 1;
}
/* 基础按钮样式 */
.video-toggle-button {
  position: absolute;
  top: 12px;
  right: 12px;
  display: inline-flex;
  align-items: center;
  padding: 8px 16px;
  border: none;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(4px);
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  z-index: 2;
}

/* 悬停状态 */
.video-toggle-button:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

/* 点击动效 */
.video-toggle-button:active {
  transform: scale(0.95);
}

/* 图标样式 */
.button-icon {
  width: 18px;
  height: 18px;
  margin-right: 8px;
  transition: fill 0.3s ease;
}

/* 文字样式 */
.button-text {
  font-weight: 500;
  letter-spacing: 0.5px;
}

/* 视频激活状态 */
.video-toggle-button.video-active {
  background: rgba(40, 167, 69, 0.9);
  color: #fff;
}

.video-toggle-button.video-active .button-icon {
  fill: #fff;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .video-toggle-button {
    padding: 6px 12px;
    font-size: 12px;
    top: 8px;
    right: 8px;
  }

  .button-icon {
    width: 16px;
    height: 16px;
    margin-right: 6px;
  }
}

/* 加载状态 */
.video-toggle-button.loading {
  pointer-events: none;
  opacity: 0.7;
}

.video-toggle-button.loading .button-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
/* 响应式设计 */
@media (max-width: 768px) {
  .user-info {
    padding: 4px 8px;
    bottom: 4px;
    left: 4px;
  }

  .user-id {
    font-size: 10px;
  }

  .status-indicator {
    width: 6px;
    height: 6px;
  }
}
</style>