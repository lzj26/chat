<template>
  <Header />
  <div class="friend-search-container">
    <header class="search-header">
      <h2>搜索好友</h2>
      <input
          type="text"
          v-model="searchQuery"
          placeholder="输入好友名字..."
          @input="debouncedSearchFriends"
          class="search-input"
      />
    </header>
    <main class="friend-list-main">
      <ul v-if="filteredFriends.length > 0" class="friend-list">
        <li
            v-for="(friend, index) in filteredFriends"
            :key="index"
            class="friend-item"
        >
          <img :src="friend.avatar" alt="Avatar" class="avatar" />
          <span class="friend-name">{{ friend.name }}</span>
          <span>{{friend.email}}</span>
          <button @click="addFriend(friend.id)" class="add-button">添加</button>
        </li>
      </ul>
      <p v-else class="no-results">没有找到匹配的好友</p>
    </main>
  </div>
</template>

<script>
//导入使用
import store from "../assets/store.js";
import Header from "@/components/header.vue";
import {path} from "../assets/store.js"
export default {
  name: "FriendSearch",
  data() {
    return {
      searchQuery: '',
      filteredFriends: [

      ]
    };
  },
  components:{
    Header,
  },

  methods: {
    searchFriends() {
      if (this.searchQuery.trim() === '') {
        this.filteredFriends = [];
        return;
      }
      this.filteredFriends = this.friends.filter(friend =>
          friend.name.toLowerCase().includes(this.searchQuery.toLowerCase())
      );
    },
    addFriend(id) {
      console.log('添加好友:', id);
      let url=path+"/userFriendApply/add";
      //读取构建发送参数
      //从全局变量中获取自己的id
      let userId=store.getters.getUserId
      let data={
        userId: userId,
        friendId: id
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
            alert(response.message);
          })
          .catch(error=>{
            throw error;
          })

    },
    //搜索好友
    debouncedSearchFriends() {
      if(this.searchQuery.length>0){
        //查询用户
        let url = path+"/user/findUser"

        //构建发送参数
        let data = {
          keyWord: this.searchQuery
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
              console.log("收到了数据")
              console.log(response)
              if (response.code === 200) {
                //先清空filteredFriends
                this.filteredFriends=[];
                if (response.data.userLink != null) {
                  let users = response.data.userLink;
                  users.forEach((user) =>{
                    this.filteredFriends.push({
                      id :user.id,
                      avatar: user.avatar,
                      name: user.userName,
                      email:user.userEmail
                    })
                  })
                }
              }
            })
            .catch(error => {
              throw error;
            })
      }

    }

},
  created() {
    // 使用 lodash 的 debounce 函数来减少搜索频率
    // this.debouncedSearchFriends = debounce(this.searchFriends, 300);
  }
};
</script>

<style scoped>
.friend-search-container {
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

.search-header {
  background-color: #007bff;
  color: white;
  padding: 10px;
  text-align: center;
  position: relative;
}

.search-input {
  width: calc(100% - 22px);
  padding: 8px;
  margin-top: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.friend-list-main {
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
  padding: 10px;
  margin-bottom: 3px;
  border-radius: 3px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.friend-item:hover {
  background-color: #e9ecef;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin-right: 10px;
}

.friend-name {
  font-size: 16px;
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

.add-button {
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 5px 10px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.add-button:hover {
  background-color: #0056b3;
}

.no-results {
  text-align: center;
  color: #6c757d;
  padding: 20px;
}
</style>