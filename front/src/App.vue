<script setup>
import {useDark, useToggle} from "@vueuse/core";

// 适配暗色模式
useDark({
  selector:'html',
  attribute:'theme-mode',
  valueDark:'dark',
  valueLight:'light'
})

useDark({
  onChanged(dark){
    if (dark){
      //切换到暗色模式
      document.documentElement.setAttribute('theme-mode', 'dark');
    }else{
      //切换到浅色模式
      document.documentElement.removeAttribute('theme-mode');
    }
  }
})
</script>

<template>
<div>
<!--  路由-->
  <router-view />
</div>
</template>

<style scoped>

</style>
