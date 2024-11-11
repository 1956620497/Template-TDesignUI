import { createApp } from 'vue'
import App from './App.vue'
import router from "@/router";
// 引入腾讯组件库的少量全局样式变量
import 'tdesign-vue-next/es/style/index.css';
import axios from "axios";

const app = createApp(App)

//配置后端服务器请求地址
axios.defaults.baseURL = 'http://127.0.0.1:8080'


//路由相关
app.use(router)

//挂载操作
app.mount('#app')


