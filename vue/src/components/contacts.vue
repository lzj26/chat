<template>
  <Header/>
  <div class="friend-list-container">
    <header class="friend-header">
      <h2>{{ name }}</h2>
    </header>
    <!-- 新朋友区域 -->
    <section class="new-friends-section">
      <h3>新朋友</h3>
      <ul class="friend-list new-friend-list">
        <li v-for="(newFriend, index) in newFriends" :key="index" class="friend-item"
            @click="selectFriend(newFriend.id, newFriend.name)">
          <img :src="newFriend.avatar" alt="Avatar" class="avatar"/>
          <div class="friend-info">
            <span class="name">{{ newFriend.name }}</span>
            <div class="details">
              <span>申请时间:{{ newFriend.time }}</span>
            </div>
          </div>
          <button class="add-button" @click.stop="addFriend(newFriend)">添加</button>
        </li>
      </ul>
    </section>

    <!-- 好友列表 -->
    <section class="friends-section">
      <h3>好友</h3>
      <div class="friends-scrollable">
        <ul class="friend-list">
          <li v-for="(friend, index) in friends" :key="index" class="friend-item"
              @click="selectFriend(friend.id, friend.name)">
            <img :src="friend.avatar" alt="Avatar" class="avatar"/>
            <span class="friend-name">{{ friend.name }}</span>
          </li>
        </ul>
      </div>
    </section>
    <Nav/>
  </div>
</template>

<script>
import Header from "@/components/header.vue";
import Nav from "@/components/nav.vue"
import store from "../assets/store.js"
import {path} from "../assets/store.js"

export default {
  name: "FriendList",
  data() {
    return {
      friends: [],
      newFriends: [],
      name: '联系人'
    };
  },
  methods: {
    selectFriend(id, name) {
      //携带好友id跳转
      this.$router.push({name: 'Chat', query: {id: id,name: name,type: '01'}});
    },
    addFriend(newFriend) {
      let url=path+"/userFriend/add";
      //读取构建发送参数
      //获取自己的id
      let userId = store.getters.getUserId;
      let data={
        userId: userId,
        friendId: newFriend.userId,
        id: newFriend.id
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
              alert(response.message);
              this.friends.push({
                avatar: newFriend.avatar,
                name: newFriend.name,
                id: newFriend.userId
              });
              // 移除新朋友列表中的该条目
              this.newFriends = this.newFriends.filter(f => f !== newFriend);
            }
            else {
              console.log(response.message)
            }
          })
          .catch(error=>{
            throw error;
          })
    },
    //查询是否有待接受的好友申请

    friendApply() {
      let url = path+"/userFriendApply/friendView";
      //读取构建发送参数
      let userId = store.getters.getUserId;
      let data = {
        userId: userId
      };
      //使用fetch函数发送
      fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
      })
          .then(res => res.json())
          .then(response => {
            if (response.code === 200) {
              let applys = response.data.dataList;
              applys.forEach((apply) => {
                this.newFriends.push({
                  avatar: apply.avatar,
                  name: apply.userName,
                  time: apply.cstCreate,
                  userId: apply.userId,
                  id: apply.id
                })
              })
            } else {
              console.log(response.message)
            }
          })
          .catch(error => {
            throw error;
          })
    },

    //查询好友列表
    getAllElement() {
      let url=path+"/userFriend/view"
      let userId = store.getters.getUserId;
      //构建发送参数
      let data={
        userId : userId,
        status : '01'
      };
      console.log("发送的请求体"+JSON.stringify(data))
      //利用fetch发送get请求即可，
      fetch(url,{
        method: 'POST',
        headers: {
          'Content-Type': 'application/json' //发送json的请求头
        },
        //注意用json格式化
        body: JSON.stringify(data)
      })
          //返回json格式数据
          .then(res=>res.json())
          .then(datas=>{
            console.log(datas)
            // 检查响应状态码
            if (datas.code === 200) {
              // 获取好友列表
              const friendList = datas.data.userLink;
              console.log(friendList)
              //遍历填充选择框
              if(friendList!=null){
                friendList.forEach( (friend)=>{
                  this.friends.push({
                    avatar: friend.avatar,
                    name: friend.friendName,
                    id: friend.friendId,
                    email:friend.friendEmail
                  })
                })
              }

            } else {
              console.error(`Request failed with code: ${datas.code}, message: ${datas.message}`);
            }


          })
          .catch(error=>{
            throw error;
          })
      //发送请求获取对应的列表
    }


  },
  components: {
    Header,
    Nav
  },
  mounted() {
    //在挂载时加载
    this.friendApply();
    this.getAllElement();
  },
};
</script>

<style scoped>
.friend-list-container {
  display: flex;
  flex-direction: column;
  width: 400px;
  margin: 0 auto;
  border: 1px solid #ccc;
  border-radius: 8px;
  overflow: hidden;
  background-color: #fff;
  font-family: Arial, sans-serif;
}

section {
  padding: 16px;
  border-bottom: 1px solid #eaeaea;
}

section:last-child {
  border-bottom: none;
}

h3 {
  margin: 0 0 10px;
  font-size: 1.2em;
  color: #333;
}

.friend-list {
  list-style-type: none;
  padding: 0;
  margin: 0;
}

.friend-item {
  display: flex;
  align-items: center;
  padding: 10px;
  margin-bottom: 2px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.friend-item:hover {
  background-color: #f0f0f0;
}

.avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  margin-right: 10px;
}

.friend-name {
  font-size: 1em;
  flex: 1;
}

.status {
  display: inline-block;
  width: 10px;
  height: 10px;
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

.new-friend-list .friend-info {
  flex: 1;
}

.new-friend-list .details {
  font-size: 0.9em;
  color: #6c757d;
}

.add-button {
  margin-left: auto;
  background-color: #007bff;
  color: white;
  border: none;
  padding: 5px 10px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.add-button:hover {
  background-color: #0056b3;
}

/* 新增样式：好友列表滚动 */
.friends-scrollable {
  max-height: 300px; /* 根据需要调整最大高度 */
  overflow-y: auto;
  padding-right: 8px; /* 确保滚动条不影响布局 */
}

/* 隐藏滚动条，但仍然可以滚动 */
.friends-scrollable::-webkit-scrollbar {
  display: none;
}

/* 对于非WebKit浏览器 */
.friends-scrollable {
  -ms-overflow-style: none; /* Internet Explorer 10+ */
  scrollbar-width: none; /* Firefox */
}

.friend-header {
  background-color: #007bff;
  color: white;
  padding: 10px;
  text-align: center;
  position: relative; /* 确保绝对定位的子元素相对于此容器 */
}
</style>
