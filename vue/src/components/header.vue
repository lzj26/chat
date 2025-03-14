<script>
import store from "../assets/store.js";
export default {
  name: "header",

  computed:{
    userId(){
      return store.getters.getUserId
    },
  },
  methods:{
    //退出登录
    outLogin(){
      //重置状态
      store.commit('setUserName', null);
      store.commit('setUserId', null);
      //断开websocket连接
      store.commit('SET_WEBSOCKET',null);
    },
  }
}
</script>

<template>
  <!-- 头部导航栏 -->
  <header class="header">
    <nav class="navbar">
      <ul class="nav-links">
        <li><router-link to="/">首页</router-link></li>
        <li><router-link to="/about">我的简历</router-link></li>
        <li><router-link to="/friendList">聊天</router-link></li>
        <li v-if="userId ===null"><router-link to="/login">登录</router-link></li>
        <li v-else @click="outLogin"><span class="pointer-cursor">退出登录</span></li>
      </ul>
    </nav>
  </header>
</template>

<style scoped>
.pointer-cursor {
  cursor: pointer;
}
</style>