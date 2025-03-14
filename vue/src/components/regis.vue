<script>
import header from "@/components/header.vue";
import store from "../assets/store.js"
import {path} from "../assets/store.js"
export default {
  // 注册子组件,使用 components 选项注册组件
  components: {

    header,
  },
props: {
  //声明类型
  url: String,
  url2: String,
},
  data(){
    return {
      userName: '',
      password: '',
      email: '',
      passwordConfirm: '',
      errorMsg: '',
      error:false,
      text:'获取验证码',
      time:60,
      isClick: false
    }
  },
  methods: {
    //获取验证码
    yanzhengma(){
      if(this.isUseing(this.email)){
        //发送验证码
        this.getCode();
        //不能点击
        this.isClick=true;
        this.text=`${this.time} 秒后重试`
        //启动定时
        this.startCountdown();
      }
      else {
        alert('邮箱格式不对!');
      }

    },
    //倒计时
    startCountdown() {
      //开始定时
      this.timer = setInterval(() => {
        this.time--;
        this.text = `${this.time} 秒后重试`;
        if (this.time <= 0) {
          //关闭定时
          clearInterval(this.timer);
          this.time=60;
          this.text='获取验证码'
          //重置点击
          this.isClick=false;
        }
      }, 1000);
    },
    getCode(){
        const body={
          email: this.email
        }
        let url=path+"/getCode"
        console.log("发送的请求为："+url)
        fetch(url,{
          method: 'POST',
          credentials: 'include', // 确保发送凭证,确定标识是同一个用户
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(body)
        })
            .then(res => res.json())
            .then(data => {
              if(data.code === 200){
                console.log(data.message)
              }
              else {
                this.errorMsg=data.message;
                this.error=true
              }
            })
            .catch(err => console.log(err));


    },
    //判断邮箱格式正确与否
    isUseing(email){
      let i=email.indexOf('@')
      if(i!== -1 && i>0 && i<email.length-1){
        return true
      }
      else {
        return false
      }
    },
    //注册
    regis(){
      const body={
        userName: this.userName,
        password: this.password,
        email: this.email,
        code: this.passwordConfirm,
      }
      let url=path+"/register"
      console.log("发送的请求为："+url)
      fetch(url,{
        method: 'POST',
        credentials: 'include', // 确保发送凭证
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(body)
      })
      .then(res => res.json())
      .then(data => {
        if(data.code === 200){
          //注册成功
          alert(data.message)
          //清空状态
          this.userName= ''
          this.password= ''
          this.email=''
          this.passwordConfirm= ''
        }
        else {
          this.errorMsg=data.message;
          this.error=true
        }
      })
      .catch(err => console.log(err));
    }
  }
}
</script>

<template>
  <div class="regis-container">
    <div class="regis-form">
      <h2>注册</h2>
      <input class="input-field" v-model="userName" placeholder="输入用户名">
      <input class="input-field" type="password" placeholder="输入密码" v-model="password">
      <div class="email-verification">
        <input class="input-code" v-model="email" type="email" placeholder="输入邮箱">
        <button class="verify-button" @click="yanzhengma" :disabled="isClick">{{ text }}</button>
      </div>
      <input class="input-field" v-model="passwordConfirm" placeholder="输入验证码">
      <span v-if="error" class="error-message">{{errorMsg}}</span>
      <button class="button" @click="regis">注册</button>

      <router-link to="/login" class="register-link">返回登录</router-link>
    </div>
  </div>
</template>

<style scoped>
.regis-container {
  width: 400px;
  margin: 50px auto;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  background-color: #fff;
}

.regis-form {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.regis-form h2 {
  font-size: 24px;
  margin-bottom: 20px;
  color: #333;
}


.email-verification {
  display: flex;
  align-items: center;
  width: 100%;
  margin-bottom: 14px;
}

.email-verification .input-code {
  flex: 1;
  margin-right: 10px;
}

.verify-button {
  padding: 10px 20px;
  background-color: #28a745;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.verify-button:hover {
  background-color: #218838;
}

.error-message {
  color: red;
  margin-bottom: 10px;
  font-size: 14px;
}
/* 输入框样式 */
.input-code {
  width: 70%;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 5px;
  font-size: 16px;
  transition: border-color 0.3s;
}
.button {
  width: 100%;
  padding: 10px;
  background-color: #28a745;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  margin-top: 20px;
  transition: background-color 0.3s;
}

.button:hover {
  background-color: #218838;
}

.register-link {
  text-align: center;
  margin-top: 20px;
  color: #28a745;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
}

.register-link:hover {
  color: #28a745;
}
</style>