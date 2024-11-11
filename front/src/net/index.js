import axios  from "axios";
import {MessagePlugin} from "tdesign-vue-next";

//处理内部使用的host和get请求

//统一本地存储的token名字
const authItemName = "account"



//请求出错的处理
const defaultError = (err) => {
    //在出现错误时警告一下用户
    console.error(err)
    MessagePlugin.error('发生了一些错误，请联系管理员')
}

//请求失败的回调函数，传入一个请求失败的原因和失败的错误码，还有一个失败的地址
const defaultFailure = (message,code,url) => {
    //请求失败时警告一下用户
    console.warn(`请求地址: ${url} ,状态码: ${code} ,错误信息: ${message} `)
    MessagePlugin.warning(message)
}

//获取token,携带到请求头中
//有些内容不需要token也能访问，所以部分内容要给未登录的用户查看
function accessHeader() {
    const token = takeAccessToken();
    return token ? {
        'Authorization':`Bearer ${takeAccessToken()}`
    } : {}
}

//保存token的操作
//token，是否勾选了记住我，token过期时间
function storeAccessToken(token,remember,expire){
    //先封装成对象
    const authObj = {token:token,expire:expire}
    //先转换为字符串
    const str = JSON.stringify(authObj)
    //如果勾选了记住我，就往本地存储中存储
    if (remember)
        localStorage.setItem(authItemName,str)
    //如果没勾选记住我，往本次会话中存储
    else
        sessionStorage.setItem(authItemName,str)
}

//取出token
function takeAccessToken(){
    //拿到存储的token，存储的位置只有这两个
    const str = localStorage.getItem(authItemName) || sessionStorage.getItem(authItemName)
    //都没拿到token的话就表示没有登录
    if (!str) return null
    //如果拿到了，重新封回Object
    const authObj = JSON.parse(str)
    //判断一下有没有过期    如果存储的过期时间，小于现在的时间，就说明token过期了
    if (authObj.expire <= new Date()){
        //已经过期了，将token删除
        deleteAccessToken()
        MessagePlugin.warning('登录状态已过期，请重新登录')
        return null
    }
    //到这里就说明token是正常的，返回token
    return authObj.token
}

//删除token
function deleteAccessToken(){
    localStorage.removeItem(authItemName)
    sessionStorage.removeItem(authItemName)
}

//内部使用的post
// 方法，参数：请求地址，请求数据，请求头，成功，失败，出现错误的回调函数
function internalPost(url,data,header,success,failure,error = defaultError){
    axios.post(url,data,{ headers:header })
        //请求数据，请求成功的处理,将data解出来
        .then(({data}) => {
            //解出来判断一下code，看是否请求成功
            if (data.code === 200)
                //操作成功的话，直接将数据给到success回调函数中，将数据传回去
                success(data.data)
             else
                //调用请求失败的方法提醒一下用户
                failure(data.message,data.code,url)
            //可能会出现请求错误，或者500服务器错误
        }).catch(err => error(err))
}

//内部使用的get
// 方法，参数：请求地址，请求头，成功，失败，出现错误的回调函数
//get请求，请求参数直接加在链接里的
function internalGet(url,header,success,failure,error = defaultError){
    axios.get(url,{headers:header})
        //请求数据，请求成功的处理,将data解出来
        .then(({data}) => {
            //解出来判断一下code，看是否请求成功
            if (data.code === 200)
                //操作成功的话，直接将数据给到success回调函数中，将数据传回去
                success(data.data)
             else
                //调用请求失败的方法提醒一下用户
                failure(data.message,data.code,url)
            //可能会出现请求错误，或者500服务器错误
        }).catch(err => error(err))
}

//再次封装post，方便在其他页面使用
function post (url,data,success,failure = defaultFailure){
    internalPost(url,data,accessHeader(),success,failure)
}

//再次封装get，方便其他页面使用
function get (url,success,failure = defaultFailure){
    internalGet(url,accessHeader(),success,failure)
}

//用户登录
function login(username, password, remember, success, failure = defaultFailure){
    internalPost('/api/auth/login',{
        username: username,
        password: password
    },{
        //axios默认以json格式提交数据，但是security只支持表单登录，让axios以表单的形式提交
        'Content-type':'application/x-www-form-urlencoded'
    },(data) => {
        //请求成功
        //存储token
        storeAccessToken(data.token,remember,data.expire)
        //通知一下用户
        MessagePlugin.success(`登录成功，欢迎 ${data.username} 来到我的系统`)
        //调用外部传进来的success
        success(data)
    }, failure)
}

//退出登录
function logout(success,failure = defaultFailure){
    get('/api/auth/logout', () => {
        //退出登录成功后,删除本地token
        deleteAccessToken();
        MessagePlugin.success('退出登录成功，欢迎再次使用！')
        //这个可以不加，但是后面可能有用,这是个传进来的方法，外部传进来后方便该方法的执行
        success()
    },failure)
}

//判断用户是否登录，用于路由守卫
function unauthorized(){
    //如果有token，就说明已经登录了
    return !takeAccessToken();
}


export { unauthorized , login , logout , get , post }
