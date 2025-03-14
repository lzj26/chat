<template>
  <div class="home-page">
    <!-- 头部导航栏 -->
    <Header></Header>

    <!-- 主要内容区 -->
    <main class="main-content">
      <section class="welcome-section">
        <h1>欢迎来到我的网站</h1>
        <p>这里是我的在线聊天系统应用,请登录以使用。</p>
      </section>

    </main>

    <!-- 底部版权信息 -->
    <footer class="footer">
      <a href="https://beian.miit.gov.cn">备案/许可证编号为：桂ICP备2024048577号 </a>
      <p>© 2025 LuoPoShan.Tech. 保留所有权利。</p>
    </footer>

  </div>
</template>

<script>
import Header from "@/components/header.vue";

export default {
  name: 'HomePage',
  components: {
    Header
  }
};
</script>

<style global="true">
/* 全局重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

/* 页面布局 */
.home-page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  width: 100vw;
}

/* 导航栏 */
.header {
  background-color: #28a745;
  color: white;
  padding: 1rem;
  width: 100vw;
}

.navbar {
  display: flex;
  justify-content: center;
}

.nav-links {
  list-style: none;
  display: flex;
}

.nav-links li {
  margin: 0 1rem;
}

.nav-links a {
  color: white;
  text-decoration: none;
  font-size: 1rem;
  transition: color 0.3s;
}

.nav-links a:hover,
.nav-links a.router-link-exact-active {
  color: #ffcc00;
}

/* 欢迎部分 */
.main-content {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  text-align: center;
  padding: 2rem;
}

.welcome-section h1 {
  font-size: 2.5rem;
  margin-bottom: 1rem;
}

.welcome-section p {
  font-size: 1.2rem;
  color: #555;
}

/* 底部 */
.footer {
  background-color: #f8f9fa;
  padding: 1rem;
  text-align: center;
  color: #777;
}
</style>