<script>
import Header from "@/components/header.vue";
import FriendList from "@/components/friendList.vue";

export default {
  name: "server",
  components: {
    Header,
    FriendList
  }
}
</script>

<template>

<Header></Header>
  <!-- 主要内容区 -->
  <main class="main-content">
    <section class="welcome-section">
      <h1>我知道你很急，但是你先别急</h1>
    </section>
  </main>
</template>

<style global="true">
.welcome-section h1 {
  font-size: 2.5rem;
  margin-bottom: 1rem;
}
</style>