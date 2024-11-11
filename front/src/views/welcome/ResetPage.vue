<script setup>
import {computed, reactive, ref} from "vue";
import {ChevronRightCircleIcon, LockOnIcon, MailIcon} from "tdesign-icons-vue-next";
import {MessagePlugin} from "tdesign-vue-next";
import router from "@/router";
import {get, post} from "@/net";

//步骤条控制
const current = ref(0);

//表单数据
const form= reactive({
  email:'',
  code:'',
  password:'',
  password_repeat:''
})

//表单对象
const formRef = ref()

//邮箱备选框：
const emailSuffix = ['@qq.com', '@163.com', '@gmail.com'];
const emailOptions = computed(() => {
  const emailPrefix = form.email.split('@')[0];
  if (!emailPrefix) return [];
  return emailSuffix.map((suffix) => emailPrefix + suffix);
});

//验证密码
const validatePassword = (value) => {
  if (value === '')
    return { result: false, message: '请再次输入密码', type: 'error' };
  else if (value !== form.password)
    return { result: false, message: '两次输入的密码不一致', type: 'warning' };
  else
    return { result: true};
}

//表单校验规则
const rule = {
  email:[
    {required:true,message:'邮箱不能为空',type:'error'},
    {email:true,message:'请输入合法的电子邮箱地址',trigger:'blur'},
    {email:true,message:'请输入合法的电子邮箱地址',trigger:'change'}
  ],
  code:[
    {required:true,message:'请输入获取的验证码',trigger:'blur'}
  ]
}

//第二阶段表单校验规则
const rules = {
  password:[
    {required:true,message:'密码不能为空',trigger:'blur'},
    {min:6,max:20,message:'密码的长度必须在6-16个字符之间',type:'warning',trigger:'blur'},
    {min:6,max:20,message:'密码的长度必须在6-16个字符之间',type:'warning',trigger:'change'}
  ],
  password_repeat:[
    {validator: validatePassword,trigger:'blur'},
    {validator: validatePassword,trigger:'change'}
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
      get(`/api/auth/ask-code?email=${form.email}&type=reset`,() => {
        MessagePlugin.success(`验证码已发送到邮箱: ${form.email}  请注意查收`)
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

//验证验证码是否正确
const resetFirm = ({validateResult,firstError}) => {
  if (validateResult === true){
    post('/api/auth/reset-confirm',{
      email:form.email,
      code:form.code
    },() => current.value++)
  }else{
    console.log('Errors: ',validateResult);
    // MessagePlugin.warning(firstError);
    MessagePlugin.warning('请完整填写邮箱和验证码');
  }
}


//修改操作
// validateResult 表单验证结果 firstError 第一个错误
const reset = ({ validateResult,firstError }) => {
  //如果表单验证结果为true，就表示表单验证全部通过，可以发起请求了。
  if (validateResult === true){
    post('/api/auth/reset-password',{...form},() => {
      MessagePlugin.success('密码修改成功，请重新登录')
      router.push('/')
    })
  }else{
    console.log('Errors: ',validateResult);
    // MessagePlugin.warning(firstError);
    MessagePlugin.warning('请完整填写密码重置内容');
  }
}


</script>

<!--找回密码页面-->

<template>
<div style="text-align: center;margin: 0 20px;">
  <div style="margin-top: 100px">
    <t-steps theme="dot" :current="current" readonly>
      <t-step-item title="验证电子邮件" />
      <t-step-item title="重新设定密码" />
    </t-steps>
  </div>
  <div v-if="current === 0">
    <div style="margin-top: 80px">
      <div style="font-size: 25px;font-weight: bold;color: #0052d9">重置密码</div>
      <div style="font-size: 14px;color: grey">请输入需要重置密码的电子邮件地址</div>
    </div>
    <div style="margin-top: 50px">
      <t-form ref="formRef" :data="form" :rules="rule" :label-width="0" @submit="resetFirm">

        <t-form-item name="email">
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
          <t-button theme="primary" type="submit" block>开始重置密码</t-button>
        </t-form-item>

      </t-form>
    </div>
  </div>
  <div v-if="current === 1">
    <div style="margin-top: 80px">
      <div style="font-size: 25px;font-weight: bold;color: #0052d9">重置密码</div>
      <div style="font-size: 14px;color: grey">请填写新的密码</div>
    </div>
    <div style="margin-top: 50px">
      <t-form :data="form" :rules="rules" :label-width="0" @submit="reset">

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

        <t-form-item>
          <t-button theme="primary" type="submit" block>立即重置密码</t-button>
        </t-form-item>

      </t-form>
    </div>
  </div>
  <div style="margin-top: 20px">
    <t-button variant="dashed" theme="primary" type="submit" @click="router.push('/')" block>返回</t-button>
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
