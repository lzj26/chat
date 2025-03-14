import './assets/main.css'
//导入创建函数，创建一个新的 Vue 应用实例
import { createApp } from 'vue'
//引入App.vue主组件
import App from './App.vue'
import router from "@/assets/index.js";
//导入创建全局变量的

import store from "@/assets/store.js";

//创建vue App并且挂载（mount）到index.html的div中，使用路由配置创建
createApp(App).use(router,store).mount('#app')
