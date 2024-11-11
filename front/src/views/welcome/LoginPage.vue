<script setup>
import { UserIcon, LockOnIcon } from 'tdesign-icons-vue-next';
import {reactive, ref} from "vue";
import {MessagePlugin} from "tdesign-vue-next";
import {login} from "@/net";
import router from "@/router";

//登录表单数据
const form = reactive({
  username:'',
  password:'',
  remember:false
})

//表单验证是否通过
const formRef = ref()

//规则验证表单
const rule = {
  username:[
    {required:true,message:'请输入用户名',type:'error'}
  ],
  password:[
    {required:true,message:'请输入密码',type:'error'}
  ]
}



//登录请求
//validateResult表单验证是否通过，firstError是未通过的原因
const  userLogin = ({validateResult, firstError}) => {
  //判断表单规则验证是否通过
    //验证通过
    if (validateResult === true){
      login(form.username,form.password,form.remember,() => router.push('/index'))
    }else {
      //表单验证未通过
      console.log('Validate Errors: ', firstError, validateResult);
      MessagePlugin.warning(firstError);
    }

}


</script>

<template>
<div style="text-align: center;margin: 0 20px">
  <div style="margin-top: 150px">
    <div style="font-size: 25px;font-weight: bold;color: #0052d9">登录</div>
    <div style="font-size: 14px;color: grey">在进入系统之前，请先输入用户名和密码进行登录</div>
  </div>
  <div style="margin-top: 50px">
    <t-form :data="form" :label-width="0" :colon="true" :rules="rule" ref="formRef" @submit="userLogin">
      <t-form-item label="用户名" name="username">
        <t-input v-model="form.username" maxlength="20" type="text" placeholder="请输入邮箱" clearable >
          <template #prefix-icon>
            <UserIcon />
          </template>
        </t-input>
      </t-form-item>

      <t-form-item label="密码" name="password">
        <t-input v-model="form.password" maxlength="20" type="password" clearable placeholder="请输入密码">
          <template #prefix-icon>
            <LockOnIcon />
          </template>
        </t-input>
      </t-form-item>

      <t-row justify="center" style="display: flex">
        <t-col :span="12" style="text-align: left">
              <t-checkbox v-model="form.remember" label="记住我" />
        </t-col>
        <t-col :span="12" style="text-align: right;">
            <t-link theme="primary" @click="router.push('/reset')">忘记密码？</t-link>
        </t-col>
      </t-row>

      <t-form-item style="margin-top: 10px">
        <t-button theme="primary" type="submit" block>登录</t-button>
      </t-form-item>
    </t-form>
  </div>
  <t-divider>
    <span style="font-size: 13px;color: grey">没有账号？</span>
  </t-divider>
  <div>
    <t-button variant="dashed" type="button" theme="primary" @click="router.push('/register')" block>立即注册</t-button>
  </div>
</div>
</template>

<style scoped>

:deep(.t-col-12){
  max-width: 50%;
}
</style>
