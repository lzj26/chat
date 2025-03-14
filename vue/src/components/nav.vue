<script>
import {defineComponent} from 'vue'

export default defineComponent({
  name: "nav"
})
</script>

<template>
  <!-- 底部导航栏 -->
  <nav class="bottom-nav">
    <router-link to="/friendList" class="nav-item">
      <i class="icon icon-messages"></i>
      <span>消息</span>
    </router-link>
<!--    <router-link to="/short-videos" class="nav-item">-->
<!--      <i class="icon icon-short-videos"></i>-->
<!--      <span>短视频</span>-->
<!--    </router-link>-->
    <router-link to="/contacts" class="nav-item">
      <i class="icon icon-contacts"></i>
      <span>联系人</span>
    </router-link>
<!--    <router-link to="/dynamic" class="nav-item">-->
<!--      <i class="icon icon-dynamic"></i>-->
<!--      <span>动态</span>-->
<!--    </router-link>-->
  </nav>
</template>

<style scoped>
.app-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.friend-list-container {
  flex: 1;
  overflow-y: auto;
  background-color: #fff;
}

.bottom-nav {
  display: flex;
  justify-content: space-around;
  padding: 10px;
  background-color: #f9f9f9;
  border-top: 1px solid #ddd;
}

.nav-item {
  text-align: center;
  color: #6c757d;
}

.nav-item.router-link-active {
  color: #007bff;
}

.icon {
  font-size: 24px;
  margin-bottom: 5px;
}

.icon-messages::before {
  content: "💬";
}

.icon-short-videos::before {
  content: "🎬";
}

.icon-contacts::before {
  content: "👤";
}

.icon-dynamic::before {
  content: "🔄";
}
</style>