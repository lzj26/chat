// src/router/index.js
//`createRouter` 和 `createWebHistory` 是 Vue Router 提供的函数，用于创建路由器实例和设置历史模式。
import { createRouter, createWebHistory } from 'vue-router';

import Login from '../components/login.vue';
import Regis from '../components/regis.vue';
import Chat from "@/components/chat.vue";
import Index from '../components/index.vue';
import Header from "@/components/header.vue";
import About from "@/components/about.vue";
import Server from "@/components/server.vue";
import FriendList from "@/components/friendList.vue";
import store from "@/assets/store.js";
import AddFriend from "@/components/addFriend.vue"
import Nav from "@/components/nav.vue"
import Contacts from "@/components/contacts.vue"
import VideoCall from "../components/VideoCall.vue"
//1.创建路由规则
const routes = [
    {
        //path是路径
        path: '/login',
        //name是引用时需要的名字
        name: 'Login',
        //转到的vue组件
        component: Login,
    },
    {
        path: '/regis',
        name: 'Regis',
        component: Regis,
    },
    {
        path: '/chat',
        name: 'Chat',
        component: Chat,
        props: (route) => ({name: route.query.name, id: route.query.id, type: route.query.type }),//将路由参数映射到props部分
        meta: { requiresAuth: true } // 登录页面需要认证
    },
    {
        path: '/',
        name: 'index',
        component: Index,
    },
    {
        path: '/header',
        name: 'header',
        component: Header,
    },
    {
        path: '/about',
        name: 'about',
        component: About
        ,
    },
    {
        path: '/server',
        name: 'server',
        component: Server,
    },
    {
        path: '/friendList', // 定义参数 :id
        name: 'friendList',
        component: FriendList,
        meta: { requiresAuth: true } // 标记是否需要认证
    },
    {
        path: '/addFriend',
        name: 'addFriend',
        component: AddFriend
    },
    {
        path: '/nav',
        name: 'nav',
        component: Nav
    },
    {
        path: '/contacts',
        name: 'contacts',
        component: Contacts
    },
    {
        path: '/videoCall',
        name: 'videoCall',
        component: VideoCall,
        props: (route) => ({name: route.query.name, id: route.query.id, type: route.query.type }),
        meta: { requiresAuth: true } // 标记是否需要认证
    }
];


//2.根据指定的路由创建路由实例
const router = createRouter({
    history: createWebHistory(),
    routes,
});

// 全局前置守卫
router.beforeEach((to, from, next) => {
    if (to.matched.some(record => record.meta.requiresAuth)) {
        // 此路由需要授权...检查是否已登录
        if (!isAuthenticated()) {
            // 如果没有登录，则重定向到登录页面
            next({
                path: '/login'
            });
        } else {
            // 否则允许继续
            next();
        }
    } else {
        // 如果不需要认证，则直接允许继续
        next();
    }
});

function isAuthenticated(){
    //判断全局变量的用户名有无
    return store.getters.getUserName !== null;
}

//导出以便使用
export default router;