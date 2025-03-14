<template>
  <Header />
  <div class="friend-list-container">

    <header class="friend-header">
      <h2>{{ roomName }}</h2>
      <button @click="toggleAddMenu" class="add-button">+</button>
    </header>
    <main class="friend-main" ref="friendsContainer">
      <ul class="friend-list">
        <li v-for="(value, key, index) in friends" :key="key" @click="selectFriend(key, value.name,value.type)" class="friend-item">
          <img :src="value.avatar" alt="image" class="avatar" />
          <div class="friend-info">
            <span class="friend-name">{{ value.name }}</span>
            <span v-if="value.messages[value.messages.length-1].text.startsWith('data:image')" class="message-preview"> [图片] </span>
            <span v-else-if="value.messages[value.messages.length-1].text.startsWith('data:application')" class="message-preview"> [文件] </span>
            <span v-else class="message-preview">{{ value.messages[value.messages.length-1].text }}</span>
            <span  class="time">{{ this.formatTime(value.messages[value.messages.length-1].time) }}</span>
            <!-- 添加未读消息数展示 -->
            <span v-if="value.unread > 0" class="unread-count">{{ value.unread }}</span>
          </div>
        </li>
      </ul>
    </main>
    <Nav />
    <!-- 添加好友菜单 -->
    <transition name="fade">
      <div v-if="showAddMenu" class="add-menu">
        <ul>
          <li @click="addFriend" class="menu-item">
            <span class="icon">+</span> 添加好友
          </li>
          <!-- 其他功能选项可以在这里添加 -->
        </ul>
      </div>
    </transition>
  </div>


</template>

<script>
import Header from "@/components/header.vue";
import {useStore} from "vuex";
import {computed} from "vue";
import store from "@/assets/store.js";
import Nav from "@/components/nav.vue"
import {path} from "../assets/store.js"

export default {
  name: "FriendList",
  components:{
    Header,
    Nav
  },
  data(){
    return {
      roomName: '信息列表',
      showAddMenu: false, // 控制添加好友菜单的显示状态
    }
  },
  computed:{//计算属性
    friends(){//返回store里面的消息列表
      return store.getters.getMessageMap
    },
    userName(){
      return store.getters.getUserName
    },
    userId(){
      return store.getters.getUserId
    },
    audio(){
      let audio=store.getters.getAudio;
      console.log("audio:"+audio)
      if(audio){
        this.$refs.notificationSound.play().catch(error => {
          console.warn('播放声音失败:', error);
        });
        //改变状态
        store.commit('SET_AUDIO')
      }
    }
  },
  methods: {
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
    //清除当前聊天人id
    cleanCurrentUserId(){
      store.commit('cleanCurrentUserId');
    },
    //跳转聊天页面
    selectFriend(id,name,type) {
      console.log(this.friends)
      //携带好友id跳转
      this.$router.push({name: 'Chat', query: {id: id,name: name,type: type}});

    },
    //获取聊天信息列表
    getListMessage(){
      let url=path+"/messageList/List"
      //从全局变量中获取自己的id
      let userId=store.getters.getUserId
      let data={
        userId: userId
      };
      fetch(url,{
        method: "POST",
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
      })
          .then(res=>res.json())
          .then(response=>{
            if(response.code === 200 && response.data.list !=null) {
              let list = response.data.list;
              let chats = {};
              console.log("信息列表为")
              console.log(list)
              list.forEach((message) => {
                let messages = [];
                //先把最新的信息放入chats，以便聊天列表可以遍历先渲染
                //区分图片还是信息
                if(message.message.startsWith('data:image')){
                  messages.push({text: '[图片]', time: message.time})
                }
                else if(message.message.startsWith('data:application')){//文件
                  messages.push({text: '[文件]', time: message.time})
                }
                else {
                  messages.push({text: message.message, time: message.time})
                }

                //添加该条好友信息记录
                chats[message.listId] = {
                  messages: messages,
                  unread: 0,
                  type: message.type,//区分私法，群聊
                  avatar: message.avatar,
                  name: message.targetName
                }
                // console.log(this.friends)
              })
              //初始化store里面的chats

              store.commit('SET_CHATS',chats);
            }
            })
          .catch(error=>{
            throw error;
          })

    },
    //好友菜单
    toggleAddMenu(){
      this.showAddMenu=!this.showAddMenu;
    },
    //转到搜索添加好友组件
    addFriend(){
      this.$router.push("/addFriend")
    }

  },
  setup() {
    console.log(store); // 打印 store 对象，确保它不是 undefined
    // 使用 computed 来创建一个响应式的 hello 属性
    const hello = computed(() => store.getters.getSharedVariable);

    // 定义方法来更新共享变量
    const updateSharedVariable = () => {
      store.dispatch('updateSharedVariable', '新值');
    };

    return {
      hello,
      updateSharedVariable
    };
  },
  mounted(){
    //挂载阶段加载
    this.getListMessage();
    this.cleanCurrentUserId();
  }
};
</script>

<style scoped>
.friend-list-container {
  display: flex;
  flex-direction: column;
  height: 85vh;
  width: 400px;
  margin: 0 auto;
  border: 1px solid #ccc;
  border-radius: 5px;
  overflow: hidden;
  background-color: #fff;
  position: relative; /* 确保绝对定位的子元素相对于此容器 */
}

.extra-info {
  padding: 10px;
  text-align: center;
  background-color: #f9f9f9;
  border-top: 1px solid #ddd;
}

.selected-friend-info,
.default-info {
  font-size: 16px;
  color: #333;
}
.unread-count {
  background-color: red;
  color: white;
  padding: 2px 6px;
  border-radius: 50%;
  font-size: 0.8em;
  position: absolute; /* 使用绝对定位 */
  right: 0; /* 将元素推到最右边 */
  top: 50%; /* 初始垂直位置为父容器高度的一半 */
  transform: translateY(-50%); /* 微调，使得元素垂直居中 */
}
.selected-friend-info p,
.default-info p {
  margin: 0;
}
.friend-list-container {
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

.friend-header {
  background-color: #007bff;
  color: white;
  padding: 10px;
  text-align: center;
}

.room-id {
  text-align: center;
  font-size: 14px;
  color: #6c757d;
  padding: 5px;
}

.friend-main {
  flex: 1;
  padding: 10px;
  overflow-y: auto;
  background-color: #f9f9f9;
}

.friend-list {
  list-style-type: none;
  padding: 0;
  margin: 0;
}

.friend-item {
  display: flex;
  align-items: center;
  padding: 1px;
  margin-bottom: 3px;
  border-radius: 3px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.friend-item:hover {
  background-color: #e9ecef;
}

.avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  margin-right: 10px;
}

.friend-name {
  font-size: 18px;
  flex: 1;
}

.status {
  display: inline-block;
  width: 15px;
  height: 15px;
  border-radius: 50%;
  background-color: #ccc;
  margin-left: 10px;
}

.status.online {
  background-color: #28a745;
}

.status.offline {
  background-color: #dc3545;
}
 .friend-list-container {
   display: flex;
   flex-direction: column;
   height: 85vh;
   width: 400px;
   margin: 0 auto;
   border: 1px solid #ccc;
   border-radius: 5px;
   overflow: hidden;
   background-color: #fff;
   position: relative; /* 确保绝对定位的子元素相对于此容器 */
 }

.friend-header {
  background-color: #007bff;
  color: white;
  padding: 10px;
  text-align: center;
  position: relative; /* 确保绝对定位的子元素相对于此容器 */
}

.add-button {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 50%;
  width: 36px;
  height: 36px;
  font-size: 24px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.add-button:hover {
  background-color: #0056b3;
}

.add-menu {
  position: absolute;
  top: 60px; /* 调整到合适位置 */
  right: 10px;
  background-color: white;
  border: 1px solid #ddd;
  border-radius: 5px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  width: 200px; /* 设置宽度 */
}

.add-menu ul {
  list-style-type: none;
  padding: 0;
  margin: 0;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: background-color 0.3s, color 0.3s;
  font-size: 14px;
  position: relative;
}

.menu-item:hover {
  background-color: #f0f0f0;
  color: #007bff;
}

.menu-item .icon {
  margin-right: 8px;
  font-size: 16px;
  font-family: 'Arial', sans-serif; /* 确保特殊字符正确显示 */
}

/* 如果有更多菜单项，可以在它们之间添加分隔线 */
.menu-item + .menu-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background-color: #ddd;
}

/* 使最后一个菜单项没有下边框 */
.menu-item:last-child::before {
  display: none;
}
.friend-item {
  display: flex;
  align-items: flex-start; /* 对齐到顶部 */
  padding: 10px;
  margin-bottom: 3px;
  border-radius: 3px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  margin-right: 16px;
}

.friend-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative; /* 添加这一行 */
}

.friend-name {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.message-preview {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.time {
  font-size: 12px;
  color: #999;
}
</style>