<script setup>
import {logout} from "@/net";
import router from "@/router";

function userLogint(){
  logout(() => router.push('/'))
}
</script>

<template>
<div>
  我是主界面
  <t-button @click="userLogint">退出登录</t-button>
</div>
</template>

<style scoped>

</style>
