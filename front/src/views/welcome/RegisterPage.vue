<script setup>
import {computed, reactive, ref} from "vue";
import {LockOnIcon, UserIcon,MailIcon,ChevronRightCircleIcon} from "tdesign-icons-vue-next";
import router from "@/router";
import {get, post} from "@/net";
import {MessagePlugin} from "tdesign-vue-next";




//表单对象
const formRef = ref()

//注册表单数据
const form = reactive({
  username:'',
  password:'',
  password_repeat:'',
  email:'',
  code:''
})

//邮箱备选框：
const emailSuffix = ['@qq.com', '@163.com', '@gmail.com'];
const emailOptions = computed(() => {
  const emailPrefix = form.email.split('@')[0];
  if (!emailPrefix) return [];
  return emailSuffix.map((suffix) => emailPrefix + suffix);
});

//验证用户名
const validateUsername = (value) => {
  if (value === ''){
    return { result: false, message: '用户名不能为空', type: 'error' };
  }else if (!/^[\u4e00-\u9fa5a-zA-Z0-9]+$/.test(value)){
    return { result: false, message: '用户名不能包含特殊字符，只能是中/英文', type: 'warning' };
  }else{
    return { result: true};
  }
}

//验证密码
const validatePassword = (value) => {
  if (value === '')
    return { result: false, message: '请再次输入密码', type: 'error' };
  else if (value !== form.password)
    return { result: false, message: '两次输入的密码不一致', type: 'warning' };
  else
    return { result: true};
}

//发送验证码计时器
const coldTime = ref(0)

//判断email是否通过了校验
const isEmailValid = computed(() => /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(form.email))


//请求验证码
function askCode() {
  //获取email表单的验证状态
  formRef.value.validate({fields:['email']}).then((result) => {
    coldTime.value = 60
    if (result === true){
      get(`/api/auth/ask-code?email=${form.email}&type=register`,() => {
        MessagePlugin.success(`验证码已发送到邮箱: ${form.email} ,请注意查收`)
        //设置定时任务,验证码计时器每秒钟-1
        setInterval(() => coldTime.value-- ,1000)
      },(message) => {
        coldTime.value = 0
        MessagePlugin.warning(message)
      })

    }else {
      MessagePlugin.warning("请输入合法的电子邮件地址")
    }
  })
}

//表单校验规则
const rule = {
  username:[
    {validator: validateUsername,trigger:'blur'},
    { min:2,max:10,message:'用户名的长度必须在2-10个字符之间',type:'warning',trigger:'blur' },
    { min:2,max:10,message:'用户名的长度必须在2-10个字符之间',type:'warning',trigger:'change' }
  ],
  password:[
    {required:true,message:'密码不能为空',trigger:'blur'},
    {min:6,max:20,message:'密码的长度必须在6-16个字符之间',type:'warning',trigger:'blur'},
    {min:6,max:20,message:'密码的长度必须在6-16个字符之间',type:'warning',trigger:'change'}
  ],
  password_repeat:[
    {validator:validatePassword,trigger:'blur'},
    {validator:validatePassword,trigger:'change'}
  ],
  email:[
    {required:true,message:'邮箱不能为空',type:'error'},
    {email:true,message:'请输入合法的电子邮箱地址',trigger:'blur'},
    {email:true,message:'请输入合法的电子邮箱地址',trigger:'change'}
  ],
  code:[
    {required:true,message:'请输入获取的验证码',trigger:'blur'}
  ]
}

//注册操作
// validateResult 表单验证结果 firstError 第一个错误
const register = ({ validateResult,firstError }) => {
  //如果表单验证结果为true，就表示表单验证全部通过，可以发起请求了。
  if (validateResult === true){
    post('/api/auth/register',{...form},() => {
      MessagePlugin.success('注册成功，欢迎加入')
      router.push('/')
    })
  }else{
    console.log('Errors: ',validateResult);
    // MessagePlugin.warning(firstError);
    MessagePlugin.warning('请完整填写表单注册内容');
  }
}

</script>

<!--注册页面-->

<template>
<div style="text-align: center;margin: 0 20px;">
  <div style="margin-top: 100px">
    <div style="font-size: 25px;font-weight: bold;color: #0052d9">注册新用户</div>
    <div style="font-size: 14px;color: grey">欢迎注册我的网站，请在下方填写相关信息</div>
  </div>
  <div style="margin-top: 50px">
    <t-form ref="formRef" :data="form" :rules="rule" :label-width="0" @submit="register">
      <t-form-item name="username" >
        <t-input v-model="form.username" maxlength="15" type="text" placeholder="用户名" >
          <template #prefix-icon>
            <UserIcon />
          </template>
        </t-input>
      </t-form-item>

      <t-form-item name="password">
        <t-input v-model="form.password" maxlength="20" type="password" clearable placeholder="请输入密码">
          <template #prefix-icon>
            <LockOnIcon />
          </template>
        </t-input>
      </t-form-item>

      <t-form-item name="password_repeat">
        <t-input v-model="form.password_repeat" maxlength="20" type="password" clearable placeholder="请重复输入密码">
          <template #prefix-icon>
            <LockOnIcon />
          </template>
        </t-input>
      </t-form-item>

      <t-form-item name="email">
<!--        <t-input v-model="form.email" maxlength="20" type="text" clearable placeholder="请输入电子邮件地址">-->
<!--          <template #prefix-icon>-->
<!--            <MailIcon />-->
<!--          </template>-->
<!--        </t-input>-->
        <t-auto-complete v-model="form.email" :options="emailOptions" placeholder="请输入电子邮件地址" filterable>
          <template #prefix-icon>
            <MailIcon />
          </template>
        </t-auto-complete>
      </t-form-item>


      <t-form-item name="code">
        <t-space direction="vertical">
          <t-row>
            <t-col :flex="4">
                <t-input  v-model="form.code" maxlength="6" type="text" placeholder="请输入验证码">
                  <template #prefix-icon>
                    <ChevronRightCircleIcon />
                  </template>
                </t-input>
            </t-col>
            <t-col :flex="2">
              <t-button theme="success" style="width: 100%;" :disabled="!isEmailValid || coldTime > 0" @click="askCode">
                {{ coldTime > 0 ? `请稍后 ${coldTime} 秒` : '获取验证码' }}
              </t-button>
            </t-col>
          </t-row>
        </t-space>
      </t-form-item>

      <t-form-item>
        <t-button theme="primary" type="submit" block>立即注册</t-button>
      </t-form-item>



    </t-form>
  </div>
  <div>
    <div style="margin-top: 20px">
      <span style="font-size: 12px;color: grey">已有账号？</span>
      <t-link theme="primary" @click="router.push('/')">立即登录</t-link>
    </div>
  </div>
</div>
</template>

<style scoped>
:deep(.t-form__controls-content){
  >:last-child{
    width: 100%;
  }
}
</style>
