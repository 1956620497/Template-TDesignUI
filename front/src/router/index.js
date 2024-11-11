import {createRouter, createWebHistory} from "vue-router";
import {unauthorized} from "@/net";

const router = createRouter({
    // 配置路由类型，createWebHashHistory()在路径中会有#号
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path:'/',
            name:'welcome',
            component:() => import('@/views/WelcomeView.vue'),
            children: [
                {
                    path:'',
                    name:'welcome-login',
                    component:() => import('@/views/welcome/LoginPage.vue')
                },
                {
                    path:'register',
                    name:'welcome-register',
                    component:() => import('@/views/welcome/RegisterPage.vue')
                },
                {
                    path:'reset',
                    name:'welcome-reset',
                    component:() => import('@/views/welcome/ResetPage.vue')
                }
            ]
        },
        {
            path:'/index',
            name:'index',
            component:() => import('@/views/Index.vue')
        }
    ]
})

//配置路由守卫
//router.beforeEach是vue router中的一个全局前置守卫，在导航前触发一些操作
//to:即将要进入的目标路由对象，form:当前导航正要离开的路由
// next:resolve钩子，是个方法，方法名为空就是放行，如果是false是中断当前导航，如果为router对象则是重定向
router.beforeEach((to,from,next) => {
    //判断用户是否完成验证
    const isUnauthorized = unauthorized()
    //用户已经登录，并且想进入登录界面，不允许用户访问登录界面，强行跳转到主页面
    if (to.name.startsWith('welcome-') && !isUnauthorized){
        //重定向
        next('/index')
        //用户没有登录的情况下请求访问主页面，也不行
    }else if (to.fullPath.startsWith('/index') && isUnauthorized){
        next('/')
        //正常情况下,就放行用户
    }else{
        next()
    }
})


//将路由暴露出去
export default router
