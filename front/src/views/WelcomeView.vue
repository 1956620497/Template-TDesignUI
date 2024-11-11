<script setup>

</script>

<template>
<div style="width: 100vw;height: 100vh;overflow: hidden;display: flex;">
  <div style="flex: 1;background-color:black ">
    <t-image style="width: 100%;height: 100%" fit="cover"
        src="/images/118282448_p0_scale.png"/>
  </div>
  <div class="welcome-title">
    <div style="font-size: 30px;font-weight: bold">欢迎来到我的个人博客</div>
    <div style="margin-top: 10px;font-size: 20px">欢迎喵，欢迎喵！</div>
    <div style="margin-top: 5px;font-size: 20px">喵喵喵喵喵~</div>
  </div>
  <div class="right-card">
<!--    <router-view />-->

    <router-view v-slot="{ Component }">
      <transition name="fade" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>

  </div>
</div>
</template>

<style scoped>
/* 组件淡入淡出效果 */
/* 进入动画：慢速 */
.fade-enter-active {
  transition: opacity 0.3s ease; /* 进入时 1 秒 */
}
/* 离开动画：快速 */
.fade-leave-active {
  transition: opacity 0.3s ease; /* 离开时 0.3 秒 */
}
/* 进入的起始状态 */
.fade-enter-from {
  opacity: 0;
}
/* 离开的结束状态 */
.fade-leave-to {
  opacity: 0;
}

.welcome-title {
  /* 使用绝对布局 */
  position: absolute;
  bottom: 30px;
  left: 30px;
  color: white;
  text-shadow: 0 0 10px black;
  z-index: 1;

}
.right-card {
  width: 400px;
  z-index: 1;
  background-color: var(--td-bg-color-container);
}
</style>
