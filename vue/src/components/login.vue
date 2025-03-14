<script >
import header from "@/components/header.vue";
//导入使用
import {path} from "../assets/store.js";
import store from "../assets/store.js";
  export default {
    // 注册子组件,使用 components 选项注册组件
    components: {
      header,
    },
    data() {
      return {
        //登录页面需要的各种元素
        userName: "",
        password: "",
        error: false,
        errorMsg: "",
      }
    },
    methods: {

      login() {
        let url=path+"/login"
        console.log("url:"+url)
        // //跳转聊天页面
        // this.$router.push({name: 'friendList', params: {id: 1}});
        //登录信息
        const  data={
          userName: this.userName,
          password: this.password,
        }
        // 设置请求头
        const headers = {
          'Content-Type': 'application/json'
        };

        fetch(url, {
          method: 'POST',
          headers: headers,
          //注意要格式化
          body: JSON.stringify(data)
        })
            .then(res => res.json())
            .then(data => {
              if(data.code === 200){
                  //赋值
                console.log("进来了")
                // 使用 Vuex store 的 commit 方法来设置 userName 和 userId
                store.commit('setUserName', data.data.userName);
                store.commit('setUserId', data.data.userId);
                let userId = store.getters.getUserId;
                console.log(userId)
                //跳转信息列表
                this.$router.push({name: 'friendList'});

                //建立websocket连接
                store.dispatch('initWebSocket')
              }
              else {
                this.errorMsg = data.message;
                this.error = true;
              }
            })
            .catch(err => {
              console.log(err)
              this.errorMsg = '出现异常，请稍后重试';
              this.error = true;
            });
      }
    },
    props: {
      //声明类型
      url: String
    }
  }
</script>

<template>

  <div class="login-container">
    <div class="login-form">
      <h2>登录</h2>
      <input class="input-field" v-model="userName" placeholder="输入用户名">
      <input class="input-field" type="password" placeholder="输入密码" v-model="password">
      <span v-if="error" class="error-message">{{errorMsg}}</span>
      <button class="button" @click="login">登录</button>
      <router-link to="/regis" class="register-link">注册</router-link>
    </div>
  </div>
</template>

<style global>

.login-container {
  width: 300px;
  margin: 50px auto;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}

.login-form {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.input-field {
  width: 100%;
  padding: 10px;
  margin-bottom: 15px;
  border: 1px solid #ccc;
  border-radius: 5px;
}

.error-message {
  color: red;
  margin-bottom: 10px;
}

.button {
  width: 100%;
  padding: 10px;
  background-color: #28a745;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  margin-top: 10px;
}

.register-link {
  text-align: center;
  margin-top: 20px;
  color: #28a745;
  text-decoration: none;
}
</style>