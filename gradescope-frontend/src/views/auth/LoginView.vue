<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElNotification } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: 'Please enter username', trigger: 'blur' }],
  password: [{ required: true, message: 'Please enter password', trigger: 'blur' }]
}

const formRef = ref()
const loading = ref(false)

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  const success = await authStore.login(form.username, form.password)
  loading.value = false

  if (success) {
    ElNotification.success({ title: 'Success', message: 'Login successful' })
    router.push('/dashboard')
  } else {
    // Error is already shown by Axios interceptor
  }
}
</script>

<template>
  <div>
    <h2 style="text-align: center; margin-bottom: 24px; color: #1f2937;">Sign In</h2>
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-position="top"
      @keyup.enter="handleLogin"
    >
      <el-form-item label="Username" prop="username">
        <el-input v-model="form.username" placeholder="Enter your username" size="large" />
      </el-form-item>

      <el-form-item label="Password" prop="password">
        <el-input
          v-model="form.password"
          type="password"
          placeholder="Enter your password"
          size="large"
          show-password
        />
      </el-form-item>

      <el-button
        type="primary"
        size="large"
        style="width: 100%; margin-top: 8px;"
        :loading="loading"
        @click="handleLogin"
      >
        Sign In
      </el-button>
    </el-form>

    <div style="text-align: center; margin-top: 16px;">
      <span style="color: #6b7280;">Don't have an account? </span>
      <router-link to="/register" style="color: #6366f1; text-decoration: none; font-weight: 500;">
        Sign Up
      </router-link>
    </div>
  </div>
</template>
