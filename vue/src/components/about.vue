<template>

  <header />
  <!-- 头部导航栏 -->

  <Header></Header>
  <div class="resume">
    <!-- 个人信息 -->
    <section class="personal-info">
      <h2>个人信息</h2>
      <ul>
        <li><strong>姓名:</strong> 梁子杰</li>
        <li><strong>性别:</strong> 男</li>
        <li><strong>年龄:</strong> 22岁</li>
        <li><strong>联系方式:</strong> 18977474527</li>
        <li><strong>邮箱:</strong> 2630543395@qq.com</li>
        <li><strong>期望城市:</strong> 南京</li>
      </ul>
    </section>

    <!-- 个人优势 -->
    <section class="personal-advantages">
      <h2>个人优势</h2>
      <ul>
        <li>基础扎实：熟练掌握Java语言，熟悉Spring Boot、MyBatisPlus等框架，熟悉前后端分离开发。</li>
        <li>适应能力强，学习速度快。</li>
      </ul>
    </section>

    <!-- 教育背景 -->
    <section class="education">
      <h2>教育背景</h2>
      <ul>
        <li>
          <strong>河海大学</strong>
          <span>软件工程</span>
          <span>(2021 - 2025)</span>
        </li>
        <li>
          <strong>主修课程:</strong>
          <ul>
            <li>数据结构,计算机组成原理,软件工程,算法设计与分析,Java程序设计</li>
          </ul>
        </li>
      </ul>
    </section>

    <!-- 实习经历 -->
    <section class="internship-experience">
      <h2>实习经历</h2>
      <ul>
        <li>
          <strong>江苏暖心智能科技集团有限公司</strong>
          <span>java开发实习生</span>
          <span>(2024.10 - 2025.01)</span>
          <p>根据任务要求完成功能模块的编写，比如实现一个在线聊天系统，富文本转换，质检系统等。</p>
        </li>
      </ul>
    </section>

    <!-- 项目经历 -->
    <section class="project-experience">
      <h2>项目经历</h2>
      <ul>
        <li>
          <strong>基于spring boot框架的在线web端聊天系统 后端开发</strong>
          <span>(2024.09 - 2024.09)</span>
          <p>
            简介：利用spring boot框架来实现在线的文本/图片聊天，以及一对一，多对多的视频聊天模式。<br />
            技术栈：springboot+mybatisplus+netty+websocket+webrtc+influxdb<br />
            工作内容:实现一对一跟群组的文本聊天，实现一对一的视频聊天，以及多对多的视频会议模式。整合到springboot，使用mybatisplus维护各种信息，完善登录等功能使其成为具体可用的系统，并将其部署到服务器。
          </p>
        </li>
        <li>
          <strong>基于spring boot框架的质检后台系统 后端开发</strong>
          <span>(2024.08 - 2024.08)</span>
          <p>
            项目描述：利用spring boot搭建一个关于停车场的设备的质检后台系统。<br />
            技术栈：springboot+mybatisplus<br />
            工作内容：应用springboot框架完成对设备（地锁等）的质检命令的返回以及验证，并且记录工厂设备的质检记录。以及完成对于工厂，设备，质检人员等的管理功能。
          </p>
        </li>
      </ul>
    </section>

    <!-- 资格证书 -->
    <section class="certificates">
      <h2>资格证书</h2>
      <ul>
        <li>大学英语四级</li>
      </ul>
    </section>

    <!-- 专业技能 -->
    <section class="professional-skills">
      <h2>专业技能</h2>
      <ul>
        <li>Java：熟悉Java语言，各种算法，集合等，有较好的编程能力。</li>
        <li>框架：熟悉spring，Spring Boot, MyBatis，Hibernate, MybatisPlus，Redis等开发框架。</li>
        <li>其他：了解vue，C语言，Python等。</li>
      </ul>
    </section>
  </div>
</template>

<script>
import Header from './header.vue'


export default {
  name: 'Resume',

  components: {
    Header
  }
};
</script>

<style scoped>
.resume {
  font-family: Arial, sans-serif;
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.personal-info,
.personal-advantages,
.education,
.internship-experience,
.project-experience,
.certificates,
.professional-skills {
  margin-bottom: 20px;
}

.personal-info h2,
.personal-advantages h2,
.education h2,
.internship-experience h2,
.project-experience h2,
.certificates h2,
.professional-skills h2 {
  margin-bottom: 10px;
  color: #333;
  font-size: 1.5rem;
}

.personal-info ul,
.personal-advantages ul,
.education ul,
.internship-experience ul,
.project-experience ul,
.certificates ul,
.professional-skills ul {
  list-style-type: none;
  padding: 0;
}

.personal-info ul li,
.personal-advantages ul li,
.education ul li,
.internship-experience ul li,
.project-experience ul li,
.certificates ul li,
.professional-skills ul li {
  margin-bottom: 5px;
}

.personal-info ul li strong,
.personal-advantages ul li,
.education ul li span,
.internship-experience ul li span,
.project-experience ul li span,
.certificates ul li,
.professional-skills ul li {
  display: inline-block;
  margin-right: 10px;
}

.education ul li strong,
.internship-experience ul li strong,
.project-experience ul li strong {
  display: block;
  margin-bottom: 5px;
}

.internship-experience ul li p,
.project-experience ul li p {
  margin-top: 5px;
  color: #555;
}
</style>